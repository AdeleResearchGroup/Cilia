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

package fr.liglab.adele.cilia.framework.monitor.statevariable;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.measurement.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.AbstractMonitor;
import fr.liglab.adele.cilia.model.ComponentImpl;
import fr.liglab.adele.cilia.model.ConstModel;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.util.UUID;
import fr.liglab.adele.cilia.util.Watch;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;

public abstract class AbstractStateVariable extends AbstractMonitor implements
		ComponentStateVarService, ComponentStateVarProperties {

	private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_STATE_VARIABLE);

	private BundleContext m_bundleContext;

	protected Map m_listStateVariable = new HashMap();
	protected Set m_listStateVarEnable = new HashSet();
	private Map m_lastPublich = new HashMap();
	private ReadWriteLock m_lock = new WriterPreferenceReadWriteLock();
	private Hashtable m_properties = new Hashtable();
	protected String m_qualifiedId;
	private String topic;

	/**
	 * Primitive Handler implementation
	 */
	public void configure(Element metadata, Dictionary configuration)
			throws ConfigurationException {
		String chainId, componentId;
		chainId = (String) configuration.get(ConstModel.PROPERTY_CHAIN_ID);
		componentId = (String) configuration.get(ConstModel.PROPERTY_COMPONENT_ID);
		m_properties.put(MONITOR_CHAIN_ID, chainId);
		m_properties.put(MONITOR_NODE_ID, componentId);
		m_properties.put(MONITOR_UUID, UUID.generate().toString());
		topic = TOPIC_HEADER + chainId;
		m_qualifiedId = ComponentImpl.buildQualifiedId(chainId, componentId);
	}

	public void validate() {
		m_bundleContext = getFactory().getBundleContext();
		retreiveEventAdmin();
	}

	public void unvalidate() {
		/* Disable all state var */
		enable(false);
		/* Clear state var */
		m_lastPublich.clear();
		m_listStateVarEnable.clear();
	}

	/*
	 * Enable all state var (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.framework.monitor.statevariable.
	 * ComponentStateVarService#enable(boolean)
	 */
	public void enable(boolean enable) {
		if (enable) {
			if ((m_listStateVariable != null) && (!m_listStateVariable.isEmpty())) {
				Iterator it = m_listStateVariable.keySet().iterator();
				if (it != null) {
					while (it.hasNext()) {
						enableStateVar((String) it.next());
					}
				}
			}
		} else {

			if ((m_listStateVariable != null) && (!m_listStateVariable.isEmpty())) {
				Iterator it = m_listStateVariable.keySet().iterator();
				if (it != null) {
					while (it.hasNext()) {
						disableStateVar((String) it.next());
					}
				}
			}
		}
	}

	/* retreive EventAdmin reference */
	private ServiceReference retreiveEventAdmin() {
		ServiceReference[] refs = null;
		ServiceReference refEventAdmin;
		try {
			refs = m_bundleContext.getServiceReferences(EventAdmin.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			logger.error("Event Admin  service lookup unrecoverable error");
			throw new RuntimeException("Event Adminservice lookup unrecoverable error");
		}
		if (refs != null)
			refEventAdmin = refs[0];
		else
			refEventAdmin = null;
		return refEventAdmin;
	}

	/*
	 * 
	 */
	private Data dataToPublish(Object o, String stateVarId, String type) {
		Data d = new Data(o, stateVarId, null);
		d.setId((String) m_properties.get(MONITOR_UUID));
		d.setType("measure");
		return d;
	}

	private void firer(String stateVarId, long ticksCount, Data d) {
		EventAdmin m_eventAdmin;
		ServiceReference refEventAdmin = retreiveEventAdmin();
		if (refEventAdmin == null) {
			logger.error("Unable to retrieve Event Admin");
		} else {
			m_lastPublich.put(stateVarId, new Long(ticksCount));
			m_eventAdmin = (EventAdmin) m_bundleContext.getService(refEventAdmin);
			m_eventAdmin.postEvent(new Event(topic, d.getAllData()));
			m_bundleContext.ungetService(refEventAdmin);
			if (logger.isInfoEnabled()) {
				logger.info("Component [" + m_qualifiedId + "] state-var [" + stateVarId
						+ "] =" + d.getContent());
			}
		}
	}

	/**
	 * Value is published in a asynchronous way only if the condition matches
	 * 
	 * @param stateVarId
	 *            sate var unique Id
	 * @param value
	 *            double value to publish
	 * @param ticksCount
	 *            current ticks count ( timestamp at source level )
	 */
	protected void publish(String stateVarId, double value, long ticksCount) {
		Condition cond;

		long last_tickscount;
		boolean fire;

		if (stateVarId == null) {
			throw new NullPointerException();
		}

		try {
			m_lock.readLock().acquire();
			try {
				if (m_listStateVarEnable.contains(stateVarId)) {
					cond = (Condition) m_listStateVariable.get(stateVarId);
					Measurement m = new Measurement(value, 0, null, ticksCount);
					Data d = dataToPublish(m, stateVarId, "measure");
					if (cond != null) {
						last_tickscount = ((Long) m_lastPublich.get(stateVarId))
								.longValue();
						fire = cond.match(
								m,
								Watch.fromTicksToMs(ticksCount)
										- Watch.fromTicksToMs(last_tickscount));
					} else {
						fire = true;
					}
					if (fire) {
						firer(stateVarId, ticksCount, d);
					}
				}
			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

	}

	/**
	 * Value is published in a asynchronous way only if the condition matches
	 * 
	 * @param stateVarId
	 *            sate var unique Id
	 * @param data
	 *            object to publish
	 * @param ticksCount
	 *            current tick count ( timestamp at source level )
	 */
	protected void publish(String stateVarId, Object data, long ticksCount) {
		long last_ticksCount;
		Condition cond;
		boolean fire;

		if (stateVarId == null)
			throw new NullPointerException();

		try {
			m_lock.readLock().acquire();
			try {
				if (m_listStateVarEnable.contains(stateVarId)) {
					cond = (Condition) m_listStateVariable.get(stateVarId);
					Data m = new Data(data);
					Data d = dataToPublish(m, stateVarId, "data");

					if (cond != null) {
						last_ticksCount = ((Long) m_lastPublich.get(stateVarId))
								.longValue();
						fire = cond.match(ticksCount, Watch.fromTicksToMs(ticksCount)
								- Watch.fromTicksToMs(last_ticksCount));
					} else {
						fire = true;
						m_lastPublich.put(stateVarId, new Long(ticksCount));
					}

					if (fire)
						firer(stateVarId, ticksCount, d);
				}
			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * return the condition (ldap filter) associated to the stateVar return null
	 * if no condition
	 */
	public String getCondition(String stateVarId) {
		if (stateVarId == null)
			throw new NullPointerException();

		try {
			m_lock.readLock().acquire();
			try {
				return (String) m_listStateVariable.get(stateVarId);

			} finally {
				m_lock.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

	}

	/*
	 * Return list of state variable (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.autonomic.mediator.MediatorMonitorService#
	 * getStateVariable()
	 */
	public String[] getStateVariableId() {
		String[] listId = null;
		try {
			m_lock.readLock().acquire();

			try {
				final Set keys = m_listStateVariable.keySet();
				return (String[]) keys.toArray(new String[keys.size()]);

			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Insert a new state variable whithout condition
	 * 
	 * @param stateVarId
	 *            unique state var identificator
	 */
	protected void addStateVarId(String stateVarId) {
		addStateVarId(stateVarId, null);
	}

	/**
	 * Insert a new state var
	 * 
	 * @param stateVarId
	 *            ,unique state varaible identificator
	 * @param filter
	 *            , ldap filter , condition to publish a new value
	 */
	protected void addStateVarId(String stateVarId, String ldapFilter) {
		if (stateVarId == null)
			throw new NullPointerException();
		ComponentImpl.checkID(stateVarId);
		try {
			m_lock.writeLock().acquire();
			try {
				Condition cond = null;
				if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
					try {
						cond = new Condition(getInstanceManager().getContext(),
								ldapFilter);
						logger.debug("Component [" + m_qualifiedId + "], state-var ["
								+ stateVarId + "], filter [" + cond.getCondition() + "]");

					} catch (Exception ex) {
						logger.error("Invalid LDAP syntax '" + ldapFilter
								+ "' ,state variable '" + stateVarId + "'");
					}
				} else {
					logger.debug("Component [" + m_qualifiedId + "], state-var ["
							+ stateVarId + "], filter [none]");
				}
				m_listStateVariable.put(stateVarId, cond);
				m_lastPublich.put(stateVarId, new Long(Watch.getCurrentTicks()));
				m_listStateVarEnable.remove(stateVarId);
			} finally {
				m_lock.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * Set a condition to the state variable the filter ldap may be null
	 */
	public void setCondition(String stateVarId, String ldapFilter)
			throws InvalidSyntaxException {
		if (stateVarId == null)
			throw new NullPointerException();
		try {
			m_lock.writeLock().acquire();
			try {
				if (m_listStateVariable.containsKey(stateVarId)) {
					Condition cond = null;
					if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
						cond = new Condition(getInstanceManager().getContext(),
								ldapFilter);
						logger.debug("Component [" + m_qualifiedId + "], state-var ["
								+ stateVarId + "], filter updated ["
								+ cond.getCondition() + "]");
					} else {
						logger.debug(
								"State variable configured [{}], filter updated [none]",
								stateVarId);
					}
					m_listStateVariable.put(stateVarId, cond);
					m_lastPublich.put(stateVarId, new Long(Watch.getCurrentTicks()));
				}
			} finally {
				m_lock.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * Set property (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.autonomic.mediator.MediatorMonitorService#setProperty
	 * (java.lang.String, java.lang.Object)
	 */
	public void setProperty(String propertyName, Object propertyValue) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.autonomic.mediator.MediatorMonitorService#getProperty
	 * (java.lang.String)
	 */
	public Object getProperty(String propertyName) {
		if (propertyName == null)
			throw new NullPointerException();
		Object value = null;
		try {
			m_lock.readLock().acquire();
			try {
				return m_properties.get(propertyName);

			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

	}

	/*
	 * Return all properties (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.autonomic.mediator.MediatorMonitorService#getProperties
	 * ()
	 */
	public Dictionary getProperties() {
		Dictionary rp = null;
		try {
			m_lock.readLock().acquire();
			try {
				return new Hashtable(m_properties);
			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

	}

	public void enableStateVar(String stateVarId) {
		if (stateVarId == null)
			throw new NullPointerException();
		try {
			m_lock.writeLock().acquire();
			try {
				if (m_listStateVariable.containsKey(stateVarId)) {
					m_listStateVarEnable.add(stateVarId);
					m_lastPublich.put(stateVarId, new Long(0));
					logger.debug("Component [{}], state-var [{}], enabled",
							m_qualifiedId, stateVarId);
				}
			} finally {
				m_lock.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void disableStateVar(String stateVarId) {
		if (stateVarId == null)
			throw new NullPointerException();
		try {
			m_lock.writeLock().acquire();
			try {
				m_listStateVarEnable.remove(stateVarId);
				m_lastPublich.remove(stateVarId);
				logger.debug("Component [{}], state-var [{}], disabled", stateVarId);
			} finally {
				m_lock.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String[] getEnabledId() {
		String[] listId = null;
		try {
			m_lock.readLock().acquire();
			try {
				return (String[]) m_listStateVarEnable
						.toArray(new String[m_listStateVarEnable.size()]);

			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public boolean isEnabled(String stateVarId) {
		boolean isEnabled;
		if (stateVarId == null)
			throw new NullPointerException();
		try {
			m_lock.readLock().acquire();
			try {
				return m_listStateVarEnable.contains(stateVarId);
			} finally {
				m_lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String getId() {
		return (String) getProperty(MONITOR_NODE_ID);
	}

}
