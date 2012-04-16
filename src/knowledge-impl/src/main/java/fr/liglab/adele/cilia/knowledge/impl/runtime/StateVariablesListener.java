/* Copyright Adele Team LIG
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

package fr.liglab.adele.cilia.knowledge.impl.runtime;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.measurement.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.statevariable.ComponentStateVarProperties;
import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.UniformResourceName;
import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.impl.eventbus.Publisher;
import fr.liglab.adele.cilia.knowledge.impl.registry.RegistryItemImpl;
import fr.liglab.adele.cilia.knowledge.registry.RegistryItem;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;
import fr.liglab.adele.cilia.knowledge.runtime.RawData;
import fr.liglab.adele.cilia.knowledge.runtime.SetUp;
import fr.liglab.adele.cilia.knowledge.runtime.Thresholds;
import fr.liglab.adele.cilia.management.Watch;

/**
 * This class is in charge to store data sent by <br>
 * mediator/adapter monitoring handler
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class StateVariablesListener implements ComponentStateVarProperties, EventHandler {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);
	
	private final BundleContext bundleContext;
	/* topic to subscribe -> state variables published by mediator/adapter */
	private ServiceRegistration serviceEventAdmin;
	private RuntimeRegistry registry;
	private Publisher publisher;
	private NodeProxy weakProxy;

	private final NodeListenerSupport evtSupport;

	public StateVariablesListener(BundleContext bc, NodeListenerSupport evtSupport) {
		this.bundleContext = bc;
		this.evtSupport = evtSupport;
		this.weakProxy = new NodeProxy();
	}

	public void setRegistry(RuntimeRegistry r) {
		registry = r;
	}

	public void setPublisher(Publisher p) {
		publisher = p;
	}

	/* Start receiving state variable for this chain */
	public void start() {
		registerEventAdmin();
		logger.info("ModelS@RunTime 'Listening on topic [{}] started", TOPIC_HEADER + "*");
	}

	/* Stop receiving state variable value */
	public void stop() {
		unregisterEventAdmin();
		logger.info("ModelS@RunTime 'Listening on topic [{}] started", TOPIC_HEADER + "*");
	}

	/* Register event admin */
	private void registerEventAdmin() {
		Dictionary dico = new Hashtable();
		dico.put(EventConstants.EVENT_TOPIC, TOPIC_HEADER + "*");

		if (serviceEventAdmin != null) {
			serviceEventAdmin.unregister();
		}
		serviceEventAdmin = bundleContext.registerService(
				EventHandler.class.getName(), this, dico);
	}

	private void unregisterEventAdmin() {
		if (serviceEventAdmin != null) {
			serviceEventAdmin.unregister();
		}
	}

	/* handle data published by the mediator handler */
	public void handleEvent(Event event) {

		Dictionary dico = new Hashtable();

		String[] keys = event.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				if (!keys[i].equalsIgnoreCase("event.topics")) {
					dico.put(keys[i], event.getProperty(keys[i]));
				}
			}
			handleEventData(dico);
		}
	}

	private void handleEventData(Dictionary dico) {
		String uuid;
		String stateVariable;

		/* state variable name */
		stateVariable = (String) dico.get(Data.DATA_NAME);
		/* uuid : mediator / adapter source */
		uuid = (String) dico.get(Data.DATA_ID);
		/* value published */
		Object value = dico.get(Data.DATA_CONTENT);
		// String type = (String) dico.get(Data.DATA_TYPE);
		if ((uuid == null) || (stateVariable == null) || (value == null)) {
			String str = "uuid, data.name , data.content, source.mediator must not be null";
			logger.error(str);
			throw new RuntimeException(str);

		} else {
			if (!((value instanceof Data) || (value instanceof Measurement))) {
				String str = "source.mediator must be instance of [Data or Measurement]";
				logger.error(str);
				throw new RuntimeException(str);
			}
		}

		/* Retrieve the node and insert a new measure */
		RegistryItem item = registry.findByUuid(uuid);
		if (item != null) {
			NodeImpl node = (NodeImpl) item.nodeReference();
			if (node != null) {
				int evt = node.addMeasure(stateVariable, value);
				if (evt > 0) {
					String urn = UniformResourceName.makeURN(uuid, stateVariable);
					/* Do not call callback upon new update */
					if (evt != EventProperties.DATA_UPDATE) {
						evtSupport.fireThresholdEvent(evt, urn);
						/* publish all value */
						publisher.publish(EventProperties.TOPIC_DATA_UPDATE, evt, urn,
								Watch.getCurrentTicks());
					}
				}
			}
		}
	}

	public void addNode(String uuid) {

		/* retreive the uuid in the registry */
		RegistryItem item = registry.findByUuid(uuid);
		NodeImpl c;
		/* construct a node -> hold data fired by the mediator-adapter */
		c = new NodeImpl(uuid, item.chainId(), item.nodeId());
		/* necessary the node generate a proxy to the real object */
		c.setReference(item.objectRef());
		/* Store in the registry the node reference, for fast access  */
		((RegistryItemImpl) item).setNodeReference(c);
		/* informs all listeners 'node arrival' */
		evtSupport.fireNodeEvent(true, c);
		logger.debug("Listen data published by [{}]", c.toString());
	}

	public void removeNode(String uuid) {
		Node item = registry.findByUuid(uuid);
		if (item != null) {
			/* informs all listeners 'node departure' */
			evtSupport.fireNodeEvent(false, item);
			logger.debug("Remove listening data from [{}]", item.toString());
		}
	}

	/* Return a proxy type Setup to this object */
	public SetUp proxySetUp(String uuid) {
		SetUp proxy = null;
		RegistryItem item = registry.findByUuid(uuid);
		if (item != null) {
			NodeImpl node = (NodeImpl) item.nodeReference();
			proxy = (SetUp) weakProxy.make(registry, uuid, node, SetUp.class);
		} else {
			proxy = null;
			logger.error("should never happens !");
		}
		return proxy;
	}

	public RawData proxyRawData(String uuid) {
		RawData proxy;
		RegistryItem item = registry.findByUuid(uuid);
		if (item != null) {
			NodeImpl node = (NodeImpl) item.nodeReference();
			proxy = (RawData) weakProxy.make(registry, uuid, node, RawData.class);
		} else {
			proxy = null;
			logger.error("should never happens !");
		}
		return proxy;
	}

	public Thresholds proxyMonitoring(String uuid) {
		Thresholds proxy;
		RegistryItem item = registry.findByUuid(uuid);
		if (item != null) {
			NodeImpl node = (NodeImpl) item.nodeReference();
			proxy = (Thresholds) weakProxy.make(registry, uuid, node, Thresholds.class);
		} else {
			proxy = null;
			logger.error("should never happens !");
		}
		return proxy;
	}

}
