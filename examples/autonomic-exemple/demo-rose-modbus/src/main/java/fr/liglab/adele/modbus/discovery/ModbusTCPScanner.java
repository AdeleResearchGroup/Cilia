/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.modbus.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.ow2.chameleon.rose.RoseMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodic scan devices between 2 IP V4:port <br>
 * 
 * @author Denis Morand
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ModbusTCPScanner extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger("rose.modbus");

	private RoseMachine roseMachine;
	private InetAddress startAddress, endAddress;
	private int m_delay, m_period, m_timeout, m_port;

	private Timer m_timer;

	private Set listEndpointsImported;

	public ModbusTCPScanner(BundleContext bc) {
		logger.debug("Modbus TCP Scanner started");
		listEndpointsImported = new HashSet();
	}

	public void setStartAddress(String addr) {
		try {
			startAddress = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			startAddress = null;
			String str = "Properties 'start.address' is not a valid address"
					+ addr;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
	}

	public void setEndAddress(String addr) {
		try {
			endAddress = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			String str = "Properties 'end.address' is not a valid address"
					+ addr;
			endAddress = null;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
	}

	public void runScan() {
		checksParam();
		if ((endAddress != null) && (startAddress != null)) {
			m_timer = new Timer();
			m_timer.scheduleAtFixedRate(this, m_delay, m_period);

			if (logger.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer(
						"Scanner modbus started [from ");
				sb.append(startAddress.toString()).append(" to ");
				sb.append(endAddress.toString()).append("]");
				;
				logger.debug(sb.toString());
			}
		}
	}

	public void cancelScan() {
		if (m_timer != null)
			m_timer.cancel();
	}

	private void setRemoteDevice(Socket socket) {
		/* Generate the unique device ID */
		String deviceID = generateID(socket.getInetAddress().getHostAddress(),
				socket.getPort());

		/* Checks if that ID is already in registry */
		if (!listEndpointsImported.contains(deviceID)) {
			Map deviceProperties = setDeviceEndPoint(socket.getInetAddress()
					.getHostAddress(), socket.getPort());
			if (socket != null) {
				Map param = readModbusIdent(socket);
				if (param != null) {
					deviceProperties.put("product.code",
							param.get("product.code"));
					deviceProperties.put("major.minor.revision",
							param.get("major.minor.revision"));
					deviceProperties.put("vendor.name",
							param.get("vendor.name"));
				}
			}
			try {
				roseMachine.putRemote(deviceID, new EndpointDescription(
						deviceProperties));
				logger.debug("Devices discovered "
						+ roseMachine.getDiscoveredEndpoints());
				listEndpointsImported.add(deviceID);
			} catch (Exception e) {
				e.printStackTrace();
				roseMachine.removeRemote(deviceID);
				listEndpointsImported.remove(deviceID);
				logger.error("The proxy has not imported the service !");
			}
		} 
	}

	private void resetRemoteDevice(InetAddress addr, int port) {
		String id = generateID(addr.getHostAddress().toString(), port);
		if (listEndpointsImported.contains(id)) {
			EndpointDescription epd = roseMachine.removeRemote(id);
			if (epd != null) {
				logger.debug("Device removed:" + epd);
			}
			listEndpointsImported.remove(id);
		}
	}

	private Socket tcpConnect(InetAddress addr, int port) {
		Socket socket = null;
		try {
			socket = new Socket(addr, port);
		} catch (Exception e) {
		}
		return socket;
	}

	private Map readModbusIdent(Socket socket) {
		ReadIdentification modbus;
		Map param = null;
		if (socket != null) {
			try {
				modbus = new ReadIdentification(socket);
				/* Check unitID=0 first , then unitID=255 */
				param = modbus.readModbus4314(0, 0xA1);
				if (param == null)
					param = modbus.readModbus4314(255, 0x2B);
				modbus.close();
			} catch (Exception e) {
				logger.error("Modbus Read identification error");
				e.printStackTrace();
			}
		}
		return param;
	}

	public void reconfiguration(Dictionary conf) {
		logger.debug("Reconfiguration " + conf.toString());
		cancelScan();
		runScan();

	}

	public void reconfiguration() {
		logger.debug("Reconfiguration ");
		cancelScan();
		runScan();
	}

	/**
	 * 
	 * Checks Modbus Device over IP
	 * 
	 */
	public void run() {
		InetAddress address;
		boolean isDone = false;
		Socket socket;

		IPAddressNexter nexter = new IPAddressNexter(startAddress, endAddress);
		address = startAddress;
		do {
			if (ping(address, m_timeout)) {
				socket = tcpConnect(address, m_port);
				if (socket != null) {
					setRemoteDevice(socket);
					try {
						socket.close();
					} catch (IOException e) {
					}
				}

			} else {
				resetRemoteDevice(address, m_port);
			}

			if (nexter.hasMoreElements()) {
				address = (InetAddress) nexter.nextElement();
			} else
				isDone = true;
		} while (isDone == false);
	}

	private static boolean ping(InetAddress addr, int to) {
		boolean pong = false;
		try {
			pong = InetAddress.getByName(addr.getHostName().toString())
					.isReachable(to);
		} catch (IOException e) {
			return false;
		}
		return pong;
	}

	public Map setDeviceEndPoint(String hostAddr, int port) {
		Map m_props = new HashMap();
		m_props.put(RemoteConstants.ENDPOINT_ID, generateID(hostAddr, port));

		m_props.put(Constants.OBJECTCLASS, new String[] { "none" });
		m_props.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS, "Modbus");
		/*
		 * property 'service.imported" true means imported
		 */
		m_props.put(RemoteConstants.SERVICE_IMPORTED, "true");
		/*
		 * The endpoint protocol used to select the right proxyimporter
		 */
		// m_props.put("endpoint.config", "Modbus/TCP");
		/*
		 * specifics
		 */
		m_props.put("device.ip.address", hostAddr);
		m_props.put("device.ip.port", port);
		return m_props;
	}

	private static String generateID(String addr, int port) {
		StringBuffer sb = new StringBuffer("urn:");
		sb.append(addr);
		sb.append(":").append(Integer.toString(port));
		return sb.toString();
	}

	private void checksParam() {

		if (m_delay <= 0) {
			String str = "Properties 'scan.delay' must not be a negative value"
					+ m_delay;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
		if (m_period <= 0) {
			String str = "Properties 'scan.period' must not be a negative value"
					+ m_period;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
		if (m_timeout <= 0) {
			String str = "Properties 'ping.time.out' must not be less than 0"
					+ m_timeout;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
	}
}
