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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.NodeCallback;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.impl.eventbus.Publisher;
import fr.liglab.adele.cilia.knowledge.impl.registry.RegistryItemImpl;
import fr.liglab.adele.cilia.knowledge.registry.RegistryItem;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;
import fr.liglab.adele.cilia.knowledge.runtime.DynamicProperties;
import fr.liglab.adele.cilia.knowledge.runtime.RawData;
import fr.liglab.adele.cilia.knowledge.runtime.SetUp;
import fr.liglab.adele.cilia.knowledge.runtime.Thresholds;
import fr.liglab.adele.cilia.model.ChainRuntime;
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
		DynamicProperties, NodeCallback {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	/* injected by ipojo , registry access */
	private RuntimeRegistry registry;
	/* Bus Event , publisher */
	private Publisher publisher;
	/* Cilia components discovery (adapters, mediators) */
	private NodeDiscoveryImpl discovery;
	/* Cilia application model */
	private CiliaContext ciliaContext;
	/* holds values fired by mediators/adapters */
	private StateVariablesListener chainRt;
	private ReadWriteLock mutex;
	private BundleContext bundleContext ;

	public DynamicPropertiesImpl(BundleContext bc) {
		super(bc);
		bundleContext = bc ;
		discovery = new NodeDiscoveryImpl(bc, this);
		chainRt = new StateVariablesListener(bc, this);
		mutex = new WriterPreferenceReadWriteLock();
	}

	/*
	 * Start the service
	 */
	public void start() {
		super.start() ;
		discovery.setPublisher(publisher);
		discovery.setRegistry(registry);
		chainRt.setPublisher(publisher);
		chainRt.setRegistry(registry);
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
		super.stop();
		chainRt.stop();
		discovery.stop();
		logger.info("ModelS@RunTime 'Dynamic properties' - stopped");
	}

	private void addNode(String uuid) {
		try {
			mutex.writeLock().acquire();
			try {
				RegistryItem item = registry.findByUuid(uuid);
				/* Store in the registry the specification reference */
				((RegistryItemImpl)item).setSpecificationReference(getModel(item));
				chainRt.addNode(uuid);
			} catch (Exception e) {
				logger.error("Internal error, cannot retrieve mediatorComponent reference");
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

	public void arrival(Node node) {
		addNode(node.uuid());
	}

	public void departure(Node node) {
		removeNode(node.uuid());
	}

	/*
	 * Return a proxy for configuring the node (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.dynproperties.DynamicProperties#
	 * nodeSetup(java.lang.String)
	 */
	public SetUp[] nodeSetup(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
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
	public RawData[] nodeRawData(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
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

	public Thresholds[] nodeMonitoring(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
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

	/* retreives adapters in or out */
	private Node[] getEndpoints(String ldapFilter, PatternType type)
			throws InvalidSyntaxException, CiliaIllegalParameterException {
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
	public Node[] endpointIn(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
		return getEndpoints(ldapFilter, PatternType.IN_ONLY);
	}

	/*
	 * retreives all adapter out ( exit of mediation chain ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#endpointOut
	 * (java.lang.String)
	 */
	public Node[] endpointOut(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
		return getEndpoints(ldapFilter, PatternType.OUT_ONLY);

	}

	/*
	 * build the ldap filter for retreiving a specific nodes
	 */
	private static String makefilter(String chain, String node) {
		StringBuffer sb = new StringBuffer("(&(");
		sb.append(Node.CHAIN_ID).append("=").append(chain);
		sb.append(")(").append(Node.NODE_ID).append("=").append(node);
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
			} catch (CiliaIllegalParameterException e) {
				logger.error("Internal parameter ! null");
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
	public Node[] connectedTo(Node node) throws CiliaIllegalStateException {
		Chain chain;
		Mediator mediator;
		Adapter adapter;
		Node[] nodes;

		if (node == null)
			return new Node[0];
		if (registry.findByUuid(node.uuid()) == null)
			throw new CiliaIllegalStateException("node disappears");
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

	public Node[] connectedTo(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
		RegistryItem[] item = registry.findByFilter(ldapFilter);
		Node[] nodes = new Node[0];

		if (item.length > 0) {
			try {
				for (int i = 0; i < item.length; i++) {
					nodes = Knowledge.concat(nodes, connectedTo(item[i]));
				}
			} catch (CiliaIllegalStateException e) {

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
	public SetUp nodeSetup(Node node) throws CiliaIllegalStateException,
			CiliaIllegalParameterException {
		if (node == null)
			throw new CiliaIllegalParameterException("node is null !");
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
	public RawData nodeRawData(Node node) throws CiliaIllegalStateException,
			CiliaIllegalParameterException {
		if (node == null)
			throw new CiliaIllegalParameterException("node is null !");
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
	public Thresholds nodeMonitoring(Node node) throws CiliaIllegalStateException,
			CiliaIllegalParameterException {
		if (node == null)
			throw new CiliaIllegalParameterException("node is null !");
		/* uuid is locked for unregister during the call */
		try {
			registry.lock_uuid(node.uuid());
			return chainRt.proxyMonitoring(node.uuid());
		} finally {
			registry.unlock_uuid(node.uuid());
		}
	}

	public Node[] findByFilter(String ldapFilter) throws InvalidSyntaxException,
			CiliaIllegalParameterException {
		return registry.findByFilter(ldapFilter);
	}

	public Node findByUuid(String uuid) throws CiliaIllegalParameterException {
		if (uuid == null)
			throw new CiliaIllegalParameterException("uuid is null !");
		return registry.findByUuid(uuid);
	}

	private MediatorComponent getModel(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (node == null)
			throw new CiliaIllegalParameterException("node is null !");
		Chain chain;
		MediatorComponent mc;
		try {
			ciliaContext.getMutex().readLock().acquire();
			try {
				chain = ciliaContext.getChain(node.chainId());
				if (chain == null) /* chain not found */
					throw new IllegalStateException(node.chainId() + " not existing !");
				mc = chain.getAdapter(node.nodeId());
				/* checks Adapter or mediator */
				if (mc == null)
					mc = chain.getMediator(node.nodeId());
				if (mc == null)
					throw new IllegalStateException(node.chainId() + "/" + node.nodeId()
							+ " not existing !");
			} finally {
				ciliaContext.getMutex().readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
		return mc;
	}

	public String[] getChains() {
		Set chainSet;
		try {
			ciliaContext.getMutex().readLock().acquire();
			chainSet = ciliaContext.getAllChains();
		} catch (InterruptedException e) {
			chainSet = Collections.EMPTY_SET;
		} finally {
			ciliaContext.getMutex().readLock().release();
		}
		Set setName = new HashSet();
		if (chainSet == null) {
			chainSet = Collections.EMPTY_SET;
		}
		Iterator it = chainSet.iterator();
		while (it.hasNext()) {
			Chain c = (Chain) it.next();
			setName.add(c.getId());
		}
		return (String[]) setName.toArray(new String[setName.size()]);

	}

	public int getChainState(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null)
			throw new CiliaIllegalParameterException("chain id is null");
		ChainRuntime chain = ciliaContext.getChainRuntime(chainId);
		if (chain == null)
			throw new CiliaIllegalStateException("'" + chainId + "' not found");
		return chain.getState();
	}

	public Date lastCommand(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null)
			throw new CiliaIllegalParameterException("chain id is null");
		ChainRuntime chain = ciliaContext.getChainRuntime(chainId);
		if (chain == null)
			throw new CiliaIllegalStateException("'" + chainId + "' not found");
		return chain.lastCommand();
	}

}
