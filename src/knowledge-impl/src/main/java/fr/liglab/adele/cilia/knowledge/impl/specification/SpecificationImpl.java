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

package fr.liglab.adele.cilia.knowledge.impl.specification;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Application;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.event.CiliaEvent;
import fr.liglab.adele.cilia.event.CiliaFrameworkEvent;
import fr.liglab.adele.cilia.event.CiliaFrameworkListener;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.impl.eventbus.Publisher;
import fr.liglab.adele.cilia.model.PatternType;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.util.UnModifiableDictionary;
import fr.liglab.adele.cilia.util.Watch;

/**
 * Application implementation 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SpecificationImpl extends SpecificationListenerSupport implements
		Application, CiliaEvent, CiliaFrameworkEvent {

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);

	private CiliaContext ciliaContext;
	private CiliaFrameworkListener listenerFramework;
	private Publisher publisher;


	public SpecificationImpl(BundleContext bc) {
		super(bc);
	}

	public void start() {
		logger.info("ModelS@RunTime 'Specification components' - started");
		super.start();
		listenerFramework.register(this, ALL_EVENTS);
	}

	public void stop() {
		logger.info("ModelS@RunTime 'Specification component' - stopped");
		super.stop();		
		listenerFramework.unregister(this);
	}

	/**
	 * retreives all chain ID 
	 */
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

	/* Return the list of Adapter for the chain */
	private Set getAdaptersSet(String chain) {
		Set adapter;
		try {
			ciliaContext.getMutex().readLock().acquire();
			adapter = ciliaContext.getChain(chain).getAdapters();
		} catch (InterruptedException e) {
			adapter = Collections.EMPTY_SET;
		} finally {
			ciliaContext.getMutex().readLock().release();
		}
		return adapter;
	}

	/* Return the list of Mediator for the chain */
	private Set getMediatorSet(String chain) {
		Set adapter;
		try {
			ciliaContext.getMutex().readLock().acquire();
			adapter = ciliaContext.getChain(chain).getAdapters();
		} catch (InterruptedException e) {
			adapter = Collections.EMPTY_SET;
		} finally {
			ciliaContext.getMutex().readLock().release();
		}

		return adapter;
	}

	/**
	 * Type = pattern matching
	 * 
	 * @throws CiliaIllegalParameterException
	 */
	private Node[] getEndpoints(String ldapFilter, PatternType type)
			throws CiliaInvalidSyntaxException, CiliaIllegalParameterException {

		Adapter adapter;
		Set adapterResult = new HashSet();
		Filter filter = ConstRuntime.createFilter(ldapFilter);

		Dictionary dico = new Hashtable();
		String chainId[] = getChains();
		for (int i = 0; i < chainId.length; i++) {
			/* retreive all adapters per all chain */
			dico.put(ConstRuntime.CHAIN_ID, chainId[i]);
			/* Iterate over all adapters per chain */
			Iterator it = getAdaptersSet(chainId[i]).iterator();
			while (it.hasNext()) {
				adapter = (Adapter) it.next();
				dico.put(ConstRuntime.NODE_ID, adapter.getId());
				if (filter.match(dico)) {
					/* verify the pattern */
					PatternType pattern = adapter.getPattern();
					if ((pattern.equals(type) || (pattern.equals(PatternType.UNASSIGNED)) || (pattern
							.equals(PatternType.IN_OUT)))) {
						adapterResult.add(adapter);
					}
				}
			}
		}
		return (Node[]) adapterResult.toArray(new Node[adapterResult.size()]);
	}

	public Node[] endpointIn(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {
		return getEndpoints(ldapFilter, PatternType.IN_ONLY);
	}

	public Node[] endpointOut(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {
		return getEndpoints(ldapFilter, PatternType.OUT_ONLY);
	}

	public Node[] connectedTo(Node node) throws CiliaIllegalStateException {

		Binding[] bindings;
		Set nodeSet = new HashSet();
		MediatorComponent mc;
		try {
			mc = getModel(node);
			bindings = mc.getOutBindings();

			if (bindings != null) {
				for (int i = 0; i < bindings.length; i++) {
					nodeSet.add( bindings[i].getTargetMediator());
				}
			}
		} catch (CiliaIllegalParameterException e) {
		}
		return (Node[]) nodeSet.toArray(new Node[nodeSet.size()]);
	}

	public Node[] connectedTo(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {

		Node[] nodes = new Node[0];
		Node[] source = findNodeByFilter(ldapFilter);

		if (source.length > 0) {
			try {
				for (int i = 0; i < nodes.length; i++) {
					nodes = ConstRuntime.concat(nodes, connectedTo(source[i]));
				}
			} catch (CiliaIllegalStateException e) {

			}
		}
		return nodes;
	}

	/* callback event fired by the cilia framework */
	public void event(String chainId, String mediatorId, int evtNumber) {
		if ((evtNumber & EVENT_CHAIN_ADDED) == EVENT_CHAIN_ADDED) {
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					new NodeImpl(chainId, ""), EventProperties.MODEL_CHAIN_CREATE,
					Watch.getCurrentTicks());
			fireEventChain(0, chainId);
		}

		if ((evtNumber & EVENT_CHAIN_REMOVED) == EVENT_CHAIN_REMOVED) {
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					new NodeImpl(chainId, ""), EventProperties.MODEL_CHAIN_DELETE,
					Watch.getCurrentTicks());
			fireEventChain(1, chainId);
		}

		if ((evtNumber & EVENT_CHAIN_STARTED) == EVENT_CHAIN_STARTED) {
			fireEventChain(2, chainId);
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					new NodeImpl(chainId, ""), EventProperties.MODEL_CHAIN_START,
					Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_CHAIN_STOPPED) == EVENT_CHAIN_STOPPED) {
			fireEventChain(3, chainId);
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					new NodeImpl(chainId, ""), EventProperties.MODEL_CHAIN_STOP,
					Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_MEDIATOR_ADDED) == EVENT_MEDIATOR_ADDED) {
			fireEventNode(true, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION, new NodeImpl(chainId,
					mediatorId), EventProperties.MODEL_MEDIATOR_CREATE, Watch
					.getCurrentTicks());
		}

		if ((evtNumber & EVENT_MEDIATOR_REMOVED) == EVENT_MEDIATOR_REMOVED) {
			fireEventNode(false, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION, new NodeImpl(chainId,
					mediatorId), EventProperties.MODEL_MEDIATOR_DELETE, Watch
					.getCurrentTicks());
		}

		if ((evtNumber & EVENT_ADAPTER_ADDED) == EVENT_ADAPTER_ADDED) {
			fireEventNode(true, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION, new NodeImpl(chainId,
					mediatorId), EventProperties.MODEL_ADAPTER_CREATE, Watch
					.getCurrentTicks());
		}

		if ((evtNumber & EVENT_ADAPTER_REMOVED) == EVENT_ADAPTER_REMOVED) {
			fireEventNode(false, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION, new NodeImpl(chainId,
					mediatorId), EventProperties.MODEL_MEDIATOR_DELETE, Watch
					.getCurrentTicks());
		}
	}

	/*
	 * retrieve properties ( Read only access ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.specification.Application#properties
	 * (fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public Dictionary properties(Node node) throws CiliaIllegalStateException,
			CiliaIllegalParameterException {
		MediatorComponent mc = getModel(node);
		return new UnModifiableDictionary(mc.getProperties());
	}

	/*
	 * openess access ! (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.specification.Application#getModel
	 * (fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public MediatorComponent getModel(Node node) throws CiliaIllegalParameterException,
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

	public Node[] findNodeByFilter(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {

		Filter filter = ConstRuntime.createFilter(ldapFilter);

		MediatorComponent component;
		Dictionary dico = new Hashtable();
		Set componentSet = new HashSet();

		String chainId[] = getChains();

		for (int i = 0; i < chainId.length; i++) {
			/* retreive all adapters per all chain */
			dico.put(ConstRuntime.CHAIN_ID, chainId[i]);
			/* Iterate over all adapters */
			Iterator it = getAdaptersSet(chainId[i]).iterator();
			while (it.hasNext()) {
				component = (MediatorComponent) it.next();
				dico.put(ConstRuntime.NODE_ID, component.getId());
				if (filter.match(dico)) {
					componentSet.add(component.getId());
				}
			}
			/* Iterate over all mediators */
			it = getMediatorSet(chainId[i]).iterator();
			while (it.hasNext()) {
				component = (MediatorComponent) it.next();
				dico.put(ConstRuntime.NODE_ID, component.getId());
				if (filter.match(dico)) {
					componentSet.add(component.getId());
				}
			}
		}
		return (Node[]) componentSet.toArray(new Node[componentSet.size()]);
	}

	private class NodeImpl implements Node {

		private final String chain;
		private final String node;

		public NodeImpl(String chain, String node) {
			this.chain = chain;
			this.node = node;
		}

		public String chainId() {
			return chain;
		}

		public String nodeId() {
			return node;
		}

		public String uuid() {
			//throw new UnsupportedOperationException(
			//"uuid is not relevant for the specification");
			return null;
		}
	}
}