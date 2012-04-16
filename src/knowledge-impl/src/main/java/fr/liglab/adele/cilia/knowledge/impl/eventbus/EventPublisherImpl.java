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

package fr.liglab.adele.cilia.knowledge.impl.eventbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.util.concurrent.Mutex;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class EventPublisherImpl implements Publisher {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private final BundleContext bcontext;
	private final Mutex mutex;

	public EventPublisherImpl(BundleContext bc) {
		mutex = new Mutex();
		bcontext = bc;
	}

	public void start() {
		logger.info("ModelS@RunTime 'Event bus publisher' - started");
	}

	/* retreive EventAdmin reference */
	private ServiceReference retreiveEventAdmin() {
		ServiceReference[] refs = null;
		ServiceReference refEventAdmin;
		try {
			refs = bcontext.getServiceReferences(EventAdmin.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			logger.error("Missing reference to EventAdmin");
			throw new RuntimeException("Missing reference to EventAdmin");
		}
		if (refs != null)
			refEventAdmin = refs[0];
		else
			refEventAdmin = null;
		return refEventAdmin;
	}

	public void stop() {
		logger.info("ModelS@RunTime 'Event bus publisher' - stopped");
	}

	public void publish(String topic, Map param) {
		Map eventData;
		EventAdmin eventAdmin;
		if ((topic == null) || (topic.length() == 0))
			throw new RuntimeException("topic is null !");
		if (param == null)
			param = Collections.EMPTY_MAP;

		if (!param.containsKey(EventProperties.EVENT_NUMBER))
			throw new RuntimeException("Missing mandatory key {"
					+ EventProperties.EVENT_NUMBER + "}");
		if (!param.containsKey(EventProperties.EVENT_SOURCE_ID))
			throw new RuntimeException("Missing mandatory key {"
					+ EventProperties.EVENT_SOURCE_ID + "}");

		try {
			mutex.acquire();
			try {
				eventData = new HashMap(param);
				ServiceReference refEventAdmin = retreiveEventAdmin();
				if (refEventAdmin == null) {
					logger.error("Unable to retrieve Event Admin");
				} else {
					eventAdmin = (EventAdmin) bcontext.getService(refEventAdmin);
					if (eventAdmin != null) {
						eventAdmin.postEvent(new Event(topic, eventData));
						bcontext.ungetService(refEventAdmin);
						logger.debug("Event fired [{}]", eventData.toString());
					} else {
						logger.error("Event Admin service not found");
					}
				}
			} finally {
				mutex.release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void publish(String topic, int evt, String uuid, long timestamp) {
		Map param = new HashMap(3);
		param.put(EventProperties.EVENT_NUMBER, new Integer(evt));
		param.put(EventProperties.EVENT_SOURCE_ID, uuid);
		param.put(EventProperties.EVENT_TICK_NUMBER, new Long(timestamp));
		publish(topic, param);
	}

	public void publish(String topic, int evt, String uuid, long timestamp,
			boolean isEventCached) {
		Map param = new HashMap(4);
		param.put(EventProperties.EVENT_NUMBER, new Integer(evt));
		param.put(EventProperties.EVENT_SOURCE_ID, uuid);
		param.put(EventProperties.EVENT_TICK_NUMBER, new Long(timestamp));
		param.put(EventProperties.EVENT_CACHED, new Boolean(isEventCached));
		publish(topic, param);
	}

}
