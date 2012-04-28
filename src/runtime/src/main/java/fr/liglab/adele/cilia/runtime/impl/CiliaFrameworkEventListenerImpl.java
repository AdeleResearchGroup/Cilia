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

package fr.liglab.adele.cilia.runtime.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.event.CiliaEvent;
import fr.liglab.adele.cilia.event.CiliaFrameworkEvent;
import fr.liglab.adele.cilia.event.CiliaFrameworkListener;
import fr.liglab.adele.cilia.runtime.CiliaFrameworkEventProperties;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CiliaFrameworkEventListenerImpl implements CiliaFrameworkListener,
		CiliaEvent, CiliaFrameworkEventProperties {

	private static final Logger logger = LoggerFactory.getLogger("cilia.ipojo.runtime");

	private final ArrayList m_listeners;
	private final BundleContext m_bundleContext;

	public CiliaFrameworkEventListenerImpl(BundleContext bc) {
		this.m_bundleContext = bc;
		this.m_listeners = new ArrayList();
	}

	public String buildTopic(String chain, String mediator) {
		StringBuffer sb = new StringBuffer(ROOT_TOPIC);
		if (chain != null) {
			sb.append(chain).append("/");
			if (mediator != null) {
				sb.append(mediator).append("/");
			}
		}
		sb.append("*");
		return sb.toString();
	}

	public boolean register(String chain, String mediator, CiliaFrameworkEvent listener,
			int evts) {
		boolean isActionDone = false;
		if (listener != null) {
			synchronized (m_listeners) {
				if (retreiveSubscriber(listener) == null) {
					StateSubscriber subs = new StateSubscriber(m_bundleContext, listener,
							buildTopic(chain, mediator));
					if (subs.setListEvents(evts) == true) {
						m_listeners.add(subs);
						subs.start();
						isActionDone = true;
					}
					else subs=null ;
				}
			}
		}
		return isActionDone;
	}

	public boolean register(String chain, CiliaFrameworkEvent listener, int evts) {
		return register(chain, null, listener, evts);
	}

	public boolean register(CiliaFrameworkEvent listener, int evts) {
		return register(null, null, listener, evts);
	}

	public boolean unregister(CiliaFrameworkEvent listener) {
		boolean isActionDone = false;
		StateSubscriber subscriber;
		if (listener != null) {
			synchronized (m_listeners) {
				subscriber = retreiveSubscriber(listener);
				if (subscriber != null) {
					m_listeners.remove(subscriber);
					subscriber.stop();
					subscriber = null;
					isActionDone = true;
				}
			}
		}
		return isActionDone;
	}

	public void start() {
	}

	public void stop() {
		StateSubscriber subscriber;
		synchronized (m_listeners) {
			for (int i = 0; i < m_listeners.size(); i++) {
				subscriber = (StateSubscriber) m_listeners.get(i);
				m_listeners.remove(i);
				subscriber.stop();
				subscriber = null;
			}
		}
	}

	private StateSubscriber retreiveSubscriber(CiliaFrameworkEvent listener) {
		StateSubscriber subscriber;
		if (listener != null) {
			for (int i = 0; i < m_listeners.size(); i++) {
				subscriber = (StateSubscriber) m_listeners.get(i);
				if (subscriber.m_event == listener)
					return subscriber;
			}
		}
		return null;
	}

	public boolean setListEvent(CiliaFrameworkEvent listener, int evts) {
		StateSubscriber subscriber;
		boolean rtn;
		synchronized (m_listeners) {
			subscriber = retreiveSubscriber(listener);
			if (subscriber != null) {
				rtn = subscriber.setListEvents(evts);
			} else
				rtn = false;
		}
		return rtn;
	}

	public int getListEvent(CiliaFrameworkEvent listener) {
		StateSubscriber subscriber;
		int listEvts = -1;
		synchronized (m_listeners) {
			subscriber = retreiveSubscriber(listener);
			if (subscriber != null)
				listEvts = subscriber.getListEvents();
		}
		return listEvts;
	}

	/**
	 * Class handling events received from EventAdmin
	 * 
	 * @author denismorand
	 * 
	 */
	private class StateSubscriber implements EventHandler, CiliaEvent,
			CiliaFrameworkEventProperties {

		private ServiceRegistration m_serviceEventAdmin;
		private final BundleContext m_bundleContext;
		private final CiliaFrameworkEvent m_event;
		private final String topic;
		private int listEvents;

		public StateSubscriber(BundleContext bc, CiliaFrameworkEvent cb, String topic) {
			this.m_bundleContext = bc;
			this.m_event = cb;
			this.topic = topic;
			this.listEvents = 0;
		}

		/* Event Admin */
		private void registerEventAdmin() {
			Dictionary dico = new Hashtable();
			dico.put(EventConstants.EVENT_TOPIC, topic);

			if (m_serviceEventAdmin != null) {
				m_serviceEventAdmin.unregister();
			}
			m_serviceEventAdmin = m_bundleContext.registerService(
					EventHandler.class.getName(), this, dico);

		}

		private void unregisterEventAdmin() {
			if (m_serviceEventAdmin != null) {
				m_serviceEventAdmin.unregister();
			}
		}

		public void start() {
			registerEventAdmin();
		}

		public void stop() {
			unregisterEventAdmin();
		}

		public boolean setListEvents(int evts) {
			boolean rtn;
			if ((evts | ALL_EVENTS) == ALL_EVENTS) {
				listEvents = evts;
				rtn = true;
			}
			else
				rtn = false;
			return rtn;
		}

		public int getListEvents() {
			return listEvents;
		}

		public void handleEvent(Event event) {

			Dictionary dico = new Hashtable();
			String chainId;
			String mediatorId;
			String eventStrId;
			int eventNumber;

			String[] keys = event.getPropertyNames();

			if (keys != null) {
				for (int i = 0; i < keys.length; i++) {
					if (!keys[i].equalsIgnoreCase("event.topics")) {
						dico.put(keys[i], event.getProperty(keys[i]));
					}
				}
				eventStrId = (String) dico.get(EVENT_ID);
				/* Fire the event if configured */
				if (eventStrId != null) {
					eventNumber = (Integer.parseInt(eventStrId) & listEvents);
					if ((eventNumber) != 0) {
						chainId = (String) dico.get(PROPERTY_CHAIN);
						mediatorId = (String) dico.get(PROPERTY_MEDIATOR);
						/* Fire event ... */
						m_event.event(chainId, mediatorId, eventNumber);

						if (CiliaFrameworkEventListenerImpl.logger.isDebugEnabled()) {
							CiliaFrameworkEventListenerImpl.logger
									.debug("Event received chain=" + chainId
											+ " mediator=" + mediatorId + " event number"
											+ evtNumberToString(eventNumber));
						}

					}
				}
			}
		}

	}

	public final String evtNumberToString(int listEvts) {
		StringBuffer sb = new StringBuffer();
		if ((listEvts & ALL_EVENTS) != 0) {
			sb = new StringBuffer();
			if ((listEvts & EVENT_CHAIN_ADDED) == EVENT_CHAIN_ADDED) {
				sb.append(STR_EVENT_CHAIN_ADDED);
			}
			if ((listEvts & EVENT_CHAIN_STARTED) == EVENT_CHAIN_STARTED) {
				sb.append(STR_EVENT_CHAIN_STARTED);
			}
			if ((listEvts & EVENT_CHAIN_STOPPED) == EVENT_CHAIN_STOPPED) {
				sb.append(STR_EVENT_CHAIN_STOPPED);
			}
			if ((listEvts & EVENT_CHAIN_REMOVED) == EVENT_CHAIN_REMOVED) {
				sb.append(STR_EVENT_CHAIN_REMOVED);
			}
			if ((listEvts & EVENT_MEDIATOR_ADDED) == EVENT_MEDIATOR_ADDED) {
				sb.append(STR_EVENT_MEDIATOR_ADDED);
			}
			if ((listEvts & EVENT_MEDIATOR_REMOVED) == EVENT_MEDIATOR_REMOVED) {
				sb.append(STR_EVENT_MEDIATOR_REMOVED);
			}
			if ((listEvts & EVENT_MEDIATOR_PROPERTIES_UPDATED) == EVENT_MEDIATOR_PROPERTIES_UPDATED) {
				sb.append(STR_EVENT_MEDIATOR_PROPERTIES_UPDATED);
			}
			if ((listEvts & EVENT_ADAPTER_ADDED) == EVENT_ADAPTER_ADDED) {
				sb.append(STR_EVENT_ADAPTER_ADDED);
			}
			if ((listEvts & EVENT_ADAPTER_REMOVED) == EVENT_ADAPTER_REMOVED) {
				sb.append(STR_EVENT_ADAPTER_REMOVED);
			}
		}
		return sb.toString();
	}
}
