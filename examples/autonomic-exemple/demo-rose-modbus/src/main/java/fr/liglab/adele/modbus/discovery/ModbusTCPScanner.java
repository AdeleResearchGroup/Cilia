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
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.ow2.chameleon.rose.RoseMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.util.Const;

/**
 * Periodic scan devices between 2 IP V4:port <br>
 * 
 * @author Denis Morand
 * 
 */
public class ModbusTCPScanner extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

	private RoseMachine roseMachine;
	private InetAddress startAddress, endAddress;
	private int m_delay, m_period, m_timeout, m_port;
	private String m_domainID;
	private Timer m_timer;
	private String m_urlProperties, m_urlfca,m_urlLocalization;
	private Properties m_devicesRankingProps;
	private Map m_fcaAttributes;
	private Set listEndpointsImported ;

	public ModbusTCPScanner(BundleContext bc) {
		logger.debug("Modbus TCP Scanner started");
		m_timer = new Timer();
		m_devicesRankingProps = new Properties();
		m_fcaAttributes = new HashMap();
		listEndpointsImported = new HashSet() ;
	}

	public void setStartAddress(String addr) {
		try {
			startAddress = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			String str = "Properties 'start.address' is not a valid address" + addr;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
	}

	public void setEndAddress(String addr) {
		try {
			endAddress = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			String str = "Properties 'end.address' is not a valid address" + addr;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
	}

	public void runScan() {
		checksParam();
		m_timer.scheduleAtFixedRate(this, m_delay, m_period);

		if (logger.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer("Scanner modbus started [from ");
			sb.append(startAddress.toString()).append(" to ");
			sb.append(endAddress.toString());
			sb.append(", domain :").append(m_domainID).append("]");
			logger.debug(sb.toString());
		}
	}

	public void cancelScan() {
		m_timer.cancel();
	}

	private void setRemoteDevice(Socket socket) {
		/* Generate the unique device ID */
		String deviceID = generateID(socket.getInetAddress().getHostAddress(),
				socket.getPort());

		/* Checks if that ID is already in registry */
		if (!listEndpointsImported.contains(deviceID)){
			Map deviceProperties = setDeviceEndPoint(socket.getInetAddress()
					.getHostAddress(), socket.getPort());
			if (socket != null) {
				Map param = readModbusIdent(socket);
				if (param != null) {
					deviceProperties.put("product.code",param.get("product.code")) ;
					deviceProperties.put("major.minor.revision",param.get("major.minor.revision")) ;
					deviceProperties.put("vendor.name",param.get("vendor.name")) ;	
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Device Present at " + deviceID);
				logger.debug("Device info :" + deviceProperties.toString());
			}
			try {
				roseMachine
						.putRemote(deviceID, new EndpointDescription(deviceProperties));
				listEndpointsImported.add(deviceID) ;
			} catch (Exception e) {
				e.printStackTrace();
				roseMachine.removeRemote(deviceID);
				logger.error("The proxy has not imported the service !");
			}
		} else {
			logger.debug("Device already registered "
					+ socket.getRemoteSocketAddress().toString());
		}
	}

	private void resetRemoteDevice(InetAddress addr, int port) {
		EndpointDescription epd = roseMachine.removeRemote(generateID(addr
				.getHostAddress().toString(), port));
		
		if (epd != null) {
			listEndpointsImported.remove(epd.getId());
			logger.debug("Device removed:" + epd.getProperties().toString());
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
				logger.debug("Device present at :" + address.getHostAddress());
				socket = tcpConnect(address, m_port);
				if (socket != null) {
					setRemoteDevice(socket);
					try {
						socket.close();
					} catch (IOException e) {
					}
				}

			} else {
				logger.debug("Device absent at :" + address.getHostAddress());
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
			pong = InetAddress.getByName(addr.getHostName().toString()).isReachable(to);
		} catch (IOException e) {
			return false ;
		}
		return pong;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map setDeviceEndPoint(String hostAddr, int port) {
		String score;
		String id = generateID(hostAddr, port);
		Map m_props = new HashMap();
		m_props.put(RemoteConstants.ENDPOINT_ID, generateID(hostAddr, port));

		m_props.put(Constants.OBJECTCLASS, new String[] { "none" });
		m_props.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS, "none");
		/*
		 * property 'service.imported" true means imported 
		 */
		m_props.put(RemoteConstants.SERVICE_IMPORTED, "true");
		/*
		 * The endpoint protocol used to select the right proxyimporter
		 */
		m_props.put("endpoint.config", "Modbus/TCP");
		score = getScore(hostAddr);
		if (score != null) {
			/*
			 * property : 'service.ranking' is used by the dynamic importer
			 * service to set the ranking value
			 */
			m_props.put("rank.value", score);
		}

		/* Set FCA attributes */
		Set attributes = (Set) m_fcaAttributes.get(id);
		if (attributes != null) {
			m_props.put("fca.attributes", attributes);
		}

		/*
		 * specifics
		 */
		m_props.put("device.ip.address", hostAddr);
		m_props.put("device.ip.port", port);
		m_props.put("domain.id", m_domainID);
		return m_props;
	}

	public String getScore(String hostAddr) {
		String score = null;
		if (!m_devicesRankingProps.isEmpty()) {
			/* Key = IP address, value = rank */
			String value = m_devicesRankingProps.getProperty(hostAddr);
			if (value != null) {
				try {
					Integer.parseInt(value);
					score = value;
					logger.debug("device=" + hostAddr + " 'service.ranking=" + value);
				} catch (NumberFormatException e) {
					logger.error("Malformed number in device properties file ,value ="
							+ value);
				}
			}

		}
		return score;
	}

	private static String generateID(String addr, int port) {
		StringBuffer sb = new StringBuffer("IP.");
		sb.append(addr);
		sb.append(":").append(Integer.toString(port));
		return sb.toString();
	}

	private void checksParam() {

		if (m_delay <= 0) {
			String str = "Properties 'scan.delay' must not be a negative value" + m_delay;
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
			String str = "Properties 'ping.time.out' must not be less than 0" + m_timeout;
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
		if (m_domainID == null) {
			/* generate a "unique" domain name */
			m_domainID = "domain.ip-" + startAddress.toString() + "-"
					+ endAddress.toString() + "-" + System.currentTimeMillis();
		}
		if (m_urlProperties != null) {
			try {
				m_devicesRankingProps.load(new URL(m_urlProperties).openStream());
				logger.debug("Properties read {}", m_devicesRankingProps.toString());
			} catch (MalformedURLException e) {
				logger.error("Invalid URL");
			} catch (IOException e) {
				logger.error("file {} {}", m_urlProperties, " not existing");
			}
		}
		if (m_urlfca != null) {
			XMLContextDescription xlmfca = new XMLContextDescription();
			try {
				m_fcaAttributes = xlmfca.load(new URL(m_urlfca).openStream());
				logger.debug("FCA attributes read {}", m_fcaAttributes.toString());
			} catch (MalformedURLException e) {
				logger.error("Invalid URL");
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("file {} {}", m_urlfca, " not existing");
			}
			logger.debug("Functional Concept Analysis Attributs read "
					+ m_fcaAttributes.toString());
		}
	}
}
