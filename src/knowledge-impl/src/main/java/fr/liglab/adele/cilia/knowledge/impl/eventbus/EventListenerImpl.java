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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.Registry;
import fr.liglab.adele.cilia.knowledge.eventbus.Cache;
import fr.liglab.adele.cilia.knowledge.eventbus.CachedEvent;
import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.eventbus.OnEvent;
import fr.liglab.adele.cilia.knowledge.eventbus.OnVeto;
import fr.liglab.adele.cilia.knowledge.eventbus.SubscriberRegistration;
import fr.liglab.adele.cilia.knowledge.eventbus.VetoSubscriberRegistration;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;
import fr.liglab.adele.cilia.util.Watch;
import fr.liglab.adele.cilia.util.concurrent.CopyOnWriteArrayList;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.SyncList;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class EventListenerImpl extends CacheListenerSupport implements
		SubscriberRegistration, VetoSubscriberRegistration, Cache {
	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private SyncList cachedEvent;
	private int capacity;

	private CopyOnWriteArrayList listeners;
	private Map vetoByTopic;
	private final BundleContext bundleContext;
	private RuntimeRegistry registry ;
	private boolean enable ;
	
	public EventListenerImpl(BundleContext bc) {
		this.bundleContext = bc;
		this.listeners = new CopyOnWriteArrayList();
		this.cachedEvent = new SyncList(new ArrayList(),
				new ReentrantWriterPreferenceReadWriteLock());
		this.vetoByTopic = new ConcurrentHashMap();
		this.enable=Knowledge.CACHED_AUTORUN ;
	}

	/*
	 * Register a listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.eventbus.SubscriberRegistration#
	 * subscribe(java.lang.String,
	 * fr.liglab.adele.cilia.knowledge.core.eventbus.OnEvent)
	 */
	public void subscribe(String topic, OnEvent listener) {
		if ((topic != null) && (listener != null)) {
			if (retreiveSubscriber(listener) == null) {
				Subscriber subs = new Subscriber(bundleContext, listener, topic);
				if (listeners.addIfAbsent(subs))
					subs.start();
			}
		}
	}

	/*
	 * Unregister the listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.eventbus.SubscriberRegistration#
	 * unSubscribe(java.lang.String,
	 * fr.liglab.adele.cilia.knowledge.core.eventbus.OnEvent)
	 */
	public void unSubscribe(OnEvent listener) {
		if (listener != null) {
			Subscriber subscriber = retreiveSubscriber(listener);
			if (subscriber != null) {
				subscriber.stop();
				listeners.remove(subscriber);
			}
		}
	}

	private Subscriber retreiveSubscriber(OnEvent listener) {
		Subscriber subscriber;
		Iterator it = listeners.listIterator();
		while (it.hasNext()) {
			subscriber = (Subscriber) it.next();
			if (subscriber.callbackEvent == listener)
				return subscriber;
		}
		return null;
	}

	public void start() {
		logger.info("ModelS@RunTime 'Event bus listener' - started");
	}

	public void stop() {
		cachedEvent.clear();
		logger.info("ModelS@RunTime 'Event bus listener' - stopped");
	}

	/**
	 * Class handling events received
	 */
	private class Subscriber implements EventHandler {
		private ServiceRegistration refServiceEvent;
		private final BundleContext bc;
		private final OnEvent callbackEvent;
		private final String topic;

		public Subscriber(BundleContext bc, OnEvent cb, String topic) {
			this.bc = bc;
			this.callbackEvent = cb;
			this.topic = topic;
		}

		private void registerEventAdmin() {
			Dictionary dico = new Hashtable();
			dico.put(EventConstants.EVENT_TOPIC, topic);

			if (refServiceEvent != null) {
				refServiceEvent.unregister();
			}
			refServiceEvent = bc
					.registerService(EventHandler.class.getName(), this, dico);
		}

		private void unregisterEventAdmin() {
			if (refServiceEvent != null) {
				refServiceEvent.unregister();
			}
		}

		public void start() {
			registerEventAdmin();
		}

		public void stop() {
			unregisterEventAdmin();
		}

		public void handleEvent(Event event) {
			Dictionary dico = new Hashtable();
			String[] keys = event.getPropertyNames();
			if (keys != null) {
				for (int i = 0; i < keys.length; i++) {
					if (!keys[i].equalsIgnoreCase("event.topics")) {
						dico.put(keys[i], event.getProperty(keys[i]));
					}
				}
				try {
					/* source.id and event.number are mandatories */
					String uuid = (String) dico.get(EventProperties.EVENT_SOURCE_UUID);
					String chainId = (String) dico.get(EventProperties.EVENT_SOURCE_CHAIN_ID) ;
					String nodeId = (String) dico.get(EventProperties.EVENT_SOURCE_NODE_ID) ;				
					int i = ((Integer) dico.get(EventProperties.EVENT_NUMBER)).intValue();
					/* optional 'tick.number' */
					Long timestamp = ((Long) dico.get(EventProperties.EVENT_TICK_NUMBER));
					if (timestamp == null)
						timestamp = new Long(0);

					/* optional 'event.cached' */
					Boolean b = (Boolean) dico.get(EventProperties.EVENT_CACHED);

					if (b == null) {
						/* by default all events are cached */
						addEventToCache(i,timestamp.longValue(),uuid,chainId,nodeId);
					} else {
						if (b.booleanValue())
							addEventToCache(i,timestamp.longValue(),uuid,chainId,nodeId);
					}
					/* Event will be notified if there is no veto */
					if (!isVetoable(topic, i, uuid, dico))
						callbackEvent.onEvent(i, uuid, timestamp.longValue(), dico);

				} catch (Exception e) {
					logger.error("Error while calling callback registered");
				}

			}
		}
	}

	/*
	 * Set the size of the cache (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.knowledge.core.eventbus.Cache#size(int, int)
	 */
	public void size(int newSize) {
		if (newSize < 0)
			throw new IllegalArgumentException("Cache capacity must be greater than 0 !");
		try {
			try {
				cachedEvent.writerSync().acquire();
				int reduction = capacity - newSize;
				/* remove events */
				for (int i = 0; i < reduction; i++) {
					cachedEvent.remove(cachedEvent.size() - 1);
				}
			} finally {
				cachedEvent.writerSync().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

		/* Traiter le cas ou la taille diminue */
	}

	/*
	 * Return the size of the cache (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.knowledge.core.eventbus.Cache#size(int)
	 */
	public int size() {
		return capacity;
	}

	/*
	 * Number of events cached per cache type (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.knowledge.core.eventbus.Cache#eventCached(int)
	 */
	public int cachedEventCount() {
		return cachedEvent.size();
	}

	/*
	 * Collection of events cached (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.knowledge.core.eventbus.Cache#cachedEvent(int)
	 */
	public CachedEvent[] cachedEvent() {
		try {
			cachedEvent.readerSync().acquire();
			try {
				ArrayList list= new ArrayList(cachedEvent);
				return (CachedEvent[])list.toArray(new CachedEvent[list.size()]);
			} finally {
				cachedEvent.readerSync().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

	}

	public void clearCache() {
		try {
			try {
				cachedEvent.writerSync().acquire();
				cachedEvent.clear();
			} finally {
				cachedEvent.writerSync().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/* insert a new event in the cache */
	private void addEventToCache(int event, long timestamp,String uuid,String chainId,String nodeId) {
		if (!enable) return ;
		try {
			try {
				cachedEvent.writerSync().acquire();
				cachedEvent.add(0, new CachedEventImpl(event, timestamp,uuid,chainId,nodeId));
				if (cachedEvent.size() > capacity) {
					cachedEvent.remove(capacity - 1);
					fireOverRun(Watch.getCurrentTicks());
				}
			} finally {
				cachedEvent.writerSync().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void subscribeVeto(String topic, OnVeto listener) {
		CopyOnWriteArrayList listveto;
		if ((topic == null) || (listener == null))
			return;

		if (!vetoByTopic.containsKey(topic)) {
			listveto = new CopyOnWriteArrayList();
		} else
			listveto = (CopyOnWriteArrayList) vetoByTopic.get(topic);
		listveto.addIfAbsent(listener);
	}

	public void unSubscribeVeto(String topic, OnVeto listener) {
		List listveto;

		if ((topic == null) || (listener == null))
			return;

		if (vetoByTopic.containsKey(topic)) {
			listveto = (List) vetoByTopic.get(topic);
			listveto.remove(listener);
		}
	}

	/**
	 * If veto, the event will not be registered and published
	 * 
	 * @param topic
	 * 
	 * @param evt
	 * @param uuid
	 *            or urn
	 * @param param
	 * @return true , the event will not be notified
	 */
	private boolean isVetoable(String topic, int evt, String uuid, Dictionary param) {
		List listener = (List) vetoByTopic.get(topic);
		if (listener == null)
			return false;
		Iterator it = listener.iterator();
		while (it.hasNext()) {
			if (((OnVeto) it.next()).shouldVeto(evt, uuid, param))
				return true;
		}
		return false;
	}


	/**
	 * enable / disable cache service 
	 */
	public void enable(boolean e) {
		enable=e ;
	}


	/**
	 * state of the cache service 
	 */
	public boolean enable() {
		return enable ;
	}

}
