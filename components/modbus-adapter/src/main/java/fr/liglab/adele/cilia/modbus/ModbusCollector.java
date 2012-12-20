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

package fr.liglab.adele.cilia.modbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractPullCollector;
import fr.liglab.adele.cilia.util.Const;
import fr.liglab.adele.protocol.modbus.ModbusProcotol;

/**
 * Modbus Collector 
 * Read periodic registers
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ModbusCollector extends AbstractPullCollector implements ModbusConst {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

	private int m_unitID;

	private Map m_readRequest;
	private ModbusProcotol[] m_ModbusServices;
	private DataFormater formater;
	private boolean isStarted = false;

	public ModbusCollector(BundleContext bc) {
		m_readRequest = new HashMap(4);
		/* Default values , no request */
		m_readRequest.put(READ_HOLDING_REGISTERS, Collections.EMPTY_MAP);
		m_readRequest.put(READ_DISCRETE_INPUTS, Collections.EMPTY_MAP);
		m_readRequest.put(READ_INPUT_REGISTERS, Collections.EMPTY_MAP);
		m_readRequest.put(READ_COILS, Collections.EMPTY_MAP);
		/* By default , the data formater is plain-text */
		formater = new PlainTextData();

	}

	public void setUnitID(int unitID) {
		if ((unitID >= 0) && (unitID <= 255))
			m_unitID = unitID;
		else
			logger.error("Invalid Modbus unitID :" + unitID);
	}

	public void setReadHoldingRegisters(Map param) {
		Map request;
		if (param == null)
			request = Collections.EMPTY_MAP;
		else
			request = param;
		m_readRequest.put(READ_HOLDING_REGISTERS, request);
	}

	public void setReadDiscreteInputs(Map param) {
		Map request;
		if (param == null)
			request = Collections.EMPTY_MAP;
		else
			request = param;
		m_readRequest.put(READ_DISCRETE_INPUTS, request);
	}

	public void setReadInputRegisters(Map param) {
		if (param != null) {
			m_readRequest.put(READ_INPUT_REGISTERS, param);
		}
	}

	public void setReadCoils(Map param) {
		Map request;
		if (param == null)
			request = Collections.EMPTY_MAP;
		else
			request = param;
		m_readRequest.put(READ_COILS, request);
	}

	public void setDataType(String param) {
		if (param != null) {
			if (param.equals(DATA_TYPE_PLAIN_TEXT))
				formater = new PlainTextData();
			else if (param.equals(DATA_TYPE_XML))
				formater = new XMLData();
		}
	}

	protected List pullData() throws IOException {
		List list = new ArrayList();
		Data data;
		Integer[] mbRegisters = null;
		BitSet mbBits = null;
		Map request;
		Iterator it;
		int ref;
		ModbusProcotol modbus;

		logger.info("#Devices modbus connected=" + m_ModbusServices.length);
		/* iterates over all service reified */
		for (int i = 0; i < m_ModbusServices.length; i++) {
			modbus = m_ModbusServices[i];
			logger.info("Send request to Device :"+modbus.getDebugInfo());

			try {
				/* Read Holding register */
				request = (Map) m_readRequest.get(READ_HOLDING_REGISTERS);
				/* execute all requests holding registers */
				it = request.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					ref = Integer.parseInt((String) pairs.getKey());
					mbRegisters = modbus.getRegisters(m_unitID, ref,
							Integer.parseInt((String) pairs.getValue()));
					if (mbRegisters != null) {
						list.add(formater.data(TAG_HOLDING_REGISTERS, ref, mbRegisters));
						logger.debug(mbRegisters.length + " holding register(s) read");
					} else {
						logger.error("no register read");
					}
				}

				/* Read Discrete Inputs */
				request = (Map) m_readRequest.get(READ_DISCRETE_INPUTS);
				/* execute all requests discrete inputs */
				it = request.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					ref = Integer.parseInt((String) pairs.getKey());
					mbBits = modbus.getDiscreteInput(m_unitID, ref,
							Integer.parseInt((String) pairs.getValue()));
					if (mbBits != null) {
						list.add(formater.data(TAG_DISCRETE_INPUT, ref, mbBits));
						logger.debug(mbBits.size() + " discrete input(s) read");
					} else
						logger.error("no discrete input read");
				}
				/* Read Input Registers */
				request = (Map) m_readRequest.get(READ_INPUT_REGISTERS);
				/* execute all requests input registers */
				it = request.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					ref = Integer.parseInt((String) pairs.getKey());
					mbRegisters = modbus.getInputRegisters(m_unitID, ref,
							Integer.parseInt((String) pairs.getValue()));
					if (mbRegisters != null) {
						list.add(formater.data(TAG_INPUT_REGISTERS, ref, mbRegisters));
						logger.debug(mbRegisters.length + " input register(s) read");
					} else {
						logger.error("no input register read");
					}
				}
				/* Read Coils */
				request = (Map) m_readRequest.get(READ_COILS);
				/* execute all requests input registers */
				it = request.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					ref = Integer.parseInt((String) pairs.getKey());
					mbBits = modbus.getCoils(m_unitID,
							Integer.parseInt((String) pairs.getKey()),
							Integer.parseInt((String) pairs.getValue()));
					if (mbBits != null) {
						list.add(formater.data(TAG_COILS, ref, mbBits));
						logger.debug(mbBits.size() + " coil(s) read");
					} else {
						logger.error("no coil read");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error " + e.getStackTrace().toString());
			}
		}
		return list;
	}

	public void start() {
		logger.info("Adapter Modbus started");
		super.start();
	}

	public void stop() {
		m_ModbusServices = null;
		logger.info("Adapter Modbus stopped");
		super.stop();
	}

	public void bindService() {
		logger.info("New Service appears");
		if ((m_ModbusServices.length > 0) && (!isStarted)) {
			isStarted = true;
			super.start();
		}
	}

	public void unbindService() {
		logger.info("Service disappears");
		if ((m_ModbusServices.length == 0) && (isStarted)) {
			isStarted = false;
			super.stop();
		}
	}


}
