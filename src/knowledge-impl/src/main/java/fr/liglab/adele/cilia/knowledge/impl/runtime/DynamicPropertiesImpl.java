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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.knowledge.Constants;
import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.eventbus.OnEvent;
import fr.liglab.adele.cilia.knowledge.eventbus.SubscriberRegistration;
import fr.liglab.adele.cilia.knowledge.exception.IllegalParameterException;
import fr.liglab.adele.cilia.knowledge.exception.IllegalStateException;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.impl.eventbus.Publisher;
import fr.liglab.adele.cilia.knowledge.impl.registry.RegistryItemImpl;
import fr.liglab.adele.cilia.knowledge.registry.RegistryItem;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;
import fr.liglab.adele.cilia.knowledge.runtime.DynamicProperties;
import fr.liglab.adele.cilia.knowledge.runtime.RawData;
import fr.liglab.adele.cilia.knowledge.runtime.SetUp;
import fr.liglab.adele.cilia.knowledge.runtime.Thresholds;
import fr.liglab.adele.cilia.management.UUID;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.PatternType;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;

/**
 * React on [arrival & departure] components <br>
 * hold state variables published by cilia framework (monitoring handler)
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class DynamicPropertiesImpl extends NodeListenerSupport implements
		DynamicProperties, OnEvent {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	/* injected by ipojo , registry access */
	private RuntimeRegistry registry;
	/* Bus event , to subscribe to event from discovery */
	private SubscriberRegistration subscriber;
	/* Bus Event , publisher */
	private Publisher publisher;
	/* Cilia components discovery (adapters, mediators) */
	private NodeDiscoveryImpl discovery;
	/* Cilia application model */
	private CiliaContext ciliaContext;
	/* holds values fired by mediators/adapters */
	private StateVariablesListener chainRt;
	private ReadWriteLock mutex;

	public DynamicPropertiesImpl(BundleContext bc) {
		discovery = new NodeDiscoveryImpl(bc);
		chainRt = new StateVariablesListener(bc, this);
		mutex = new WriterPreferenceReadWriteLock();
	}

	/*
	 * Start the service
	 */
	public void start() {
		discovery.setPublisher(publisher);
		discovery.setRegistry(registry);
		chainRt.setPublisher(publisher);
		chainRt.setRegistry(registry);
		/* Subscribes to Events published by the discovery */
		subscriber.subscribe(EventProperties.TOPIC_DYN_PROPERTIES, this);
		/* Register state variables from the Cilia's machine */
		frameworkRegister();
		/* Start listening state variables */
		chainRt.start();
		/* Start runtime discovery */
		discovery.start();
		logger.info("ModelS@RunTime 'Dynamic properties' - started");
	}

	/*
	 * Stop the service
	 */
	public void stop() {
		chainRt.stop();
		discovery.stop();
		subscriber.unSubscribe(this);
		logger.info("ModelS@RunTime 'Dynamic properties' - stopped");
	}

	private void addNode(String uuid) {
		try {
			mutex.writeLock().acquire();
			try {
				chainRt.addNode(uuid);
			} finally {
				mutex.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	private void removeNode(String uuid) {
		try {
			mutex.writeLock().acquire();
			try {
				chainRt.removeNode(uuid);
				registry.unregister(uuid);
			} finally {
				mutex.writeLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/* Event fired by the discovery */
	public void onEvent(int evt, String uuid, long timeStamp, Dictionary param) {
		switch (evt) {
		case EventProperties.REGISTER:
			addNode(uuid);
			break;
		case EventProperties.UNREGISTER:
			removeNode(uuid);
			break;
		}

	}

	/*
	 * Return a proxy for configuring the node (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.dynproperties.DynamicProperties#
	 * nodeSetup(java.lang.String)
	 */
	public SetUp[] nodeSetup(String ldapFilter) throws InvalidSyntaxException {
		RegistryItem[] item = registry.findByFilter(ldapFilter);
		Set set = new HashSet();
		try {
			mutex.readLock().acquire();
			try {
				String uuid;
				for (int i = 0; i < item.length; i++) {
					uuid = (String) item[i].uuid();
					try {
						registry.lock_uuid(uuid);
						SetUp proxy = chainRt.proxySetUp(uuid);
						if (proxy != null)
							set.add(proxy);
					} finally {
						registry.unlock_uuid(uuid);
					}
				}
				return (SetUp[]) set.toArray(new SetUp[set.size()]);
			} finally {
				mutex.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * return a proxy for gathering raw data sent by the node at runtime
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.dynproperties.DynamicProperties#
	 * nodeMonitoring(java.lang.String)
	 */
	public RawData[] nodeRawData(String ldapFilter) throws InvalidSyntaxException {
		RegistryItem[] item = registry.findByFilter(ldapFilter);
		Set set = new HashSet();
		try {
			mutex.readLock().acquire();
			try {
				String uuid;
				for (int i = 0; i < item.length; i++) {
					uuid = (String) item[i].uuid();
					try {
						registry.lock_uuid(uuid);
						RawData proxy = chainRt.proxyRawData(uuid);
						if (proxy != null)
							set.add(proxy);
					} finally {
						registry.unlock_uuid(uuid);
					}
				}
				return (RawData[]) set.toArray(new RawData[set.size()]);
			} finally {
				mutex.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public Thresholds[] nodeMonitoring(String ldapFilter) throws InvalidSyntaxException {
		RegistryItem[] item = registry.findByFilter(ldapFilter);
		Set set = new HashSet();
		try {
			mutex.readLock().acquire();
			try {
				String uuid;
				for (int i = 0; i < item.length; i++) {
					uuid = (String) item[i].uuid();
					try {
						registry.lock_uuid(uuid);
						Thresholds proxy = chainRt.proxyMonitoring(uuid);
						if (proxy != null)
							set.add(proxy);
					} finally {
						registry.unlock_uuid(uuid);
					}
				}
				return (Thresholds[]) set.toArray(new Thresholds[set.size()]);
			} finally {
				mutex.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/* register the framework */
	private void frameworkRegister() {

		String uuid = UUID.generate().toString();
		RegistryItem item;

		item = new RegistryItemImpl(uuid, "cilia.framework.engine", null, null);
		/* insert in the registry the uuid and the object */
		registry.register(item);

	}

	/* retreives adapters in or out */
	private Node[] getEndpoints(String ldapFilter, PatternType type)
			throws InvalidSyntaxException {
		Chain chain;
		Adapter adapter;
		Set adapterSet = new HashSet();
		RegistryItem[] item = registry.findByFilter(ldapFilter);
		for (int i = 0; i < item.length; i++) {
			chain = ciliaContext.getChain(item[i].chainId());
			if (chain != null) {
				adapter = chain.getAdapter(item[i].nodeId());
				if (adapter != null) {
					PatternType pattern = adapter.getPattern();
					/* Checks the pattern if possible ! */
					if ((pattern.equals(type)) || (pattern.equals(PatternType.IN_OUT))
							|| (pattern.equals((PatternType.UNASSIGNED)))) {
						adapterSet.add(item[i]);
					}
				}
			}
		}
		return (Node[]) adapterSet.toArray(new Node[adapterSet.size()]);
	}

	/*
	 * retreives all adapter in ( entries of mediation chain ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#endpointIn
	 * (java.lang.String)
	 */
	public Node[] endpointIn(String ldapFilter) throws InvalidSyntaxException {
		return getEndpoints(ldapFilter, PatternType.IN_ONLY);
	}

	/*
	 * retreives all adapter out ( exit of mediation chain ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#endpointOut
	 * (java.lang.String)
	 */
	public Node[] endpointOut(String ldapFilter) throws InvalidSyntaxException {
		return getEndpoints(ldapFilter, PatternType.OUT_ONLY);

	}

	/*
	 * build the ldap filter for retreiving a specific nodes
	 */
	private static String makefilter(String chain, String node) {
		StringBuffer sb = new StringBuffer("(&(");
		sb.append(Constants.CHAIN_ID).append("=").append(chain);
		sb.append(")(").append(Constants.NODE_ID).append("=").append(node);
		sb.append("))");
		return sb.toString();
	}

	/*
	 * using the binding retreives nodes connected to
	 */
	private Node[] getNextNodes(Binding[] bindings, Node node) {
		if (bindings == null)
			return new Node[0];

		Set nodeSet = new HashSet();
		Set set = new HashSet();
		/* Retreive the mediators name in the cilia context (model) */
		for (int i = 0; i < bindings.length; i++) {
			set.add(bindings[i].getTargetMediator().getId());
		}
		Iterator it = set.iterator();
		/* Retreives real mediators connected */
		while (it.hasNext()) {
			String name = (String) it.next();
			/* construct the ldap filter */
			String filter = makefilter(node.chainId(), name);
			RegistryItem item[];
			try {
				item = registry.findByFilter(filter);
				for (int i = 0; i < item.length; i++) {
					nodeSet.add(item[i]);
				}
			} catch (InvalidSyntaxException e) {
				logger.error("Internal ldap syntax error !, should never happens");
			}

		}
		return (Node[]) nodeSet.toArray(new Node[nodeSet.size()]);
	}

	/*
	 * Return the list of nodes connected at runtime (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.dynproperties.DynamicProperties#
	 * connectedTo(fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public Node[] connectedTo(Node node) throws IllegalStateException {
		Chain chain;
		Mediator mediator;
		Adapter adapter;
		Node[] nodes;

		if (node == null)
			return new Node[0];
		if (registry.findByUuid(node.uuid()) == null)
			throw new IllegalStateException("node disappears");
		/* retreive the chain hosting the mediator/component */
		chain = ciliaContext.getChain(node.chainId());
		if (chain != null) {
			/* checks if the node is an adapter */
			adapter = chain.getAdapter(node.nodeId());
			if (adapter != null) {
				nodes = getNextNodes(adapter.getOutBindings(), node);

			} else {
				/* Mediators */
				mediator = chain.getMediator(node.nodeId());
				if (mediator == null) {
					nodes = new Node[0];
				} else {
					nodes = getNextNodes(mediator.getOutBindings(), node);
				}
			}
		} else
			/* chain no found */
			nodes = new Node[0];
		return nodes;
	}

	/*
	 * retreive the list of nodes connected ( at runtime ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#connectedTo
	 * (java.lang.String)
	 */

	public Node[] connectedTo(String ldapFilter) throws InvalidSyntaxException {
		RegistryItem[] item = registry.findByFilter(ldapFilter);
		Node[] nodes = new Node[0];

		if (item.length > 0) {
			try {
				for (int i = 0; i < item.length; i++) {
					nodes = Knowledge.concat(nodes, connectedTo(item[i]));
				}
			} catch (IllegalStateException e) {

			}
		}
		return nodes;
	}

	/*
	 * return a proxy to the node Setup (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#nodeSetup
	 * (fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public SetUp nodeSetup(Node node) throws IllegalStateException,
			IllegalParameterException {
		if (node == null)
			throw new IllegalParameterException("node is null !");
		/* uuid is locked for unregister during the call */
		try {
			registry.lock_uuid(node.uuid());
			return chainRt.proxySetUp(node.uuid());
		} finally {
			registry.unlock_uuid(node.uuid());
		}
	}

	/*
	 * return a proxy to the node Raw Data (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#nodeRawData
	 * (fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public RawData nodeRawData(Node node) throws IllegalStateException,
			IllegalParameterException {
		if (node == null)
			throw new IllegalParameterException("node is null !");
		/* uuid is locked for unregister during the call */
		try {
			registry.lock_uuid(node.uuid());
			return chainRt.proxyRawData(node.uuid());
		} finally {
			registry.unlock_uuid(node.uuid());
		}
	}

	/*
	 * return a proxy to tthe node monitoring ( thresholds ) (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#
	 * nodeMonitoring(fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public Thresholds nodeMonitoring(Node node) throws IllegalStateException,
			IllegalParameterException {
		if (node == null)
			throw new IllegalParameterException("node is null !");
		/* uuid is locked for unregister during the call */
		try {
			registry.lock_uuid(node.uuid());
			return chainRt.proxyMonitoring(node.uuid());
		} finally {
			registry.unlock_uuid(node.uuid());
		}
	}

	public Node[] findByFilter(String ldapFilter) throws InvalidSyntaxException {
		return registry.findByFilter(ldapFilter);
	}

	public Node findByUuid(String uuid) {
		return registry.findByUuid(uuid);
	}
}
