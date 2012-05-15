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

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.event.CiliaEvent;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.runtime.CiliaFrameworkEventProperties;
import fr.liglab.adele.cilia.util.concurrent.Mutex;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CiliaFrameworkEventPublisher implements CiliaEvent,
		CiliaFrameworkEventProperties {

	private static final Logger logger = LoggerFactory.getLogger("cilia.ipojo.runtime");

	private final BundleContext bcontext;
	private ServiceReference m_refEventAdmin;
	private final Mutex mutex;

	public CiliaFrameworkEventPublisher(BundleContext bc) {
		bcontext = bc;
		mutex = new Mutex();
		m_refEventAdmin = bcontext.getServiceReference(EventAdmin.class.getName());
	}

	private String buildTopic(String chain, String mediator) {
		StringBuffer sb = new StringBuffer(ROOT_TOPIC).append(chain);
		if (mediator != null) {
			sb.append("/").append(mediator);
		}
		return sb.toString();
	}

	public boolean publish(String chain, String mediator, int operation) {
		boolean status = false;
		Map eventData;
		EventAdmin eventAdmin;
		if ((chain == null) || (chain.length() == 0)) {
			logger.error("Parameter String 'chain' must not be null");
			return false;
		}
		if (m_refEventAdmin != null) {
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				logger.error("event '" + operation + "' cannot be published");
				return false ;
			}
			try {
				eventData = new HashMap();
				eventData.put(PROPERTY_CHAIN, chain);
				if ((mediator != null) && (mediator.length() > 0)) {
					eventData.put(PROPERTY_MEDIATOR, mediator);
				}
				eventData.put(EVENT_ID, Integer.toString(operation));
				eventAdmin = (EventAdmin) bcontext.getService(m_refEventAdmin);
				if (eventAdmin != null) {
					eventAdmin
							.postEvent(new Event(buildTopic(chain, mediator), eventData));
					bcontext.ungetService(m_refEventAdmin);
					if (logger.isDebugEnabled())
						logger.debug("Event fired " + eventData.toString());
					status = true;
				} else
					logger.debug("EventAdmin service not found ");
			} finally {
				mutex.release();
			}

		} else {
			logger.debug("reference eventAdmin not retreived (null)");
		}
		return status;
	}

	public boolean publish(String chain, int operation) {
		return publish(chain, null, operation);
	}

	public boolean publish(MediatorComponent m, int operation) {

		if (m == null) {
			logger.error("parameter 'mediator' must not be null");
			return false;
		}
		return publish(m.getChain().getId(), m.getId(), operation);
	}

	public boolean publish(Chain c, int operation) {

		if (c == null) {
			logger.error("parameter 'chain' must not be null");
			return false;
		}
		return publish(c.getId(), null, operation);
	}
}
