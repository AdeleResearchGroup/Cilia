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
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.event.CiliaEvent;
import fr.liglab.adele.cilia.event.CiliaFrameworkEvent;
import fr.liglab.adele.cilia.event.CiliaFrameworkListener;
import fr.liglab.adele.cilia.exceptions.IllegalParameterException;
import fr.liglab.adele.cilia.knowledge.Constants;
import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.impl.eventbus.Publisher;
import fr.liglab.adele.cilia.knowledge.specification.Application;
import fr.liglab.adele.cilia.knowledge.util.UnModifiableDictionary;
import fr.liglab.adele.cilia.model.ChainRuntime;
import fr.liglab.adele.cilia.model.PatternType;
import fr.liglab.adele.cilia.util.Watch;

/**
 * Specification
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class SpecificationImpl extends SpecificationListenerSupport implements
		Application, CiliaEvent, CiliaFrameworkEvent {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private CiliaContext ciliaContext;
	private CiliaFrameworkListener listenerFramework;
	private Publisher publisher;

	public SpecificationImpl(BundleContext bc) {

	}

	public void start() {
		logger.info("ModelS@RunTime 'Specification components' - started");
		listenerFramework.register(this, ALL_EVENTS);
	}

	public void stop() {
		logger.info("ModelS@RunTime 'Specification component' - stopped");
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
	 */
	private Node[] getEndpoints(String ldapFilter, PatternType type)
			throws InvalidSyntaxException {

		Adapter adapter;
		Set adapterResult = new HashSet();
		Filter filter = Knowledge.createFilter(ldapFilter);

		Dictionary dico = new Hashtable();
		String chainId[] = getChains();
		for (int i = 0; i < chainId.length; i++) {
			/* retreive all adapters per all chain */
			dico.put(Constants.CHAIN_ID, chainId[i]);
			/* Iterate over all adapters per chain */
			Iterator it = getAdaptersSet(chainId[i]).iterator();
			while (it.hasNext()) {
				adapter = (Adapter) it.next();
				dico.put(Constants.NODE_ID, adapter.getId());
				if (filter.match(dico)) {
					/* verify the pattern */
					PatternType pattern = adapter.getPattern();
					if ((pattern.equals(type) || (pattern.equals(PatternType.UNASSIGNED)) || (pattern
							.equals(PatternType.IN_OUT)))) {
						adapterResult.add(new NodeImpl(chainId[i], adapter.getId()));
					}
				}
			}
		}
		return (Node[]) adapterResult.toArray(new Node[adapterResult.size()]);
	}

	public Node[] endpointIn(String ldapFilter) throws InvalidSyntaxException {
		return getEndpoints(ldapFilter, PatternType.IN_ONLY);
	}

	public Node[] endpointOut(String ldapFilter) throws InvalidSyntaxException {
		return getEndpoints(ldapFilter, PatternType.OUT_ONLY);
	}

	public Node[] connectedTo(Node node) throws IllegalStateException {

		Binding[] bindings;
		Set nodeSet = new HashSet();
		MediatorComponent mc;
		try {
			mc = getModel(node);
			bindings = mc.getOutBindings();

			if (bindings != null) {
				for (int i = 0; i < bindings.length; i++) {
					nodeSet.add(new NodeImpl(node.chainId(), bindings[i]
							.getTargetMediator().getId()));
				}
			}
		} catch (IllegalParameterException e) {
		}
		return (Node[]) nodeSet.toArray(new Node[nodeSet.size()]);
	}

	
	public Node[] connectedTo(String ldapFilter) throws InvalidSyntaxException {

		Node[] nodes = new Node[0];
		Node[] source = findByFilter(ldapFilter);

		if (source.length > 0) {
			try {
				for (int i = 0; i < nodes.length; i++) {
					nodes = Knowledge.concat(nodes, connectedTo(source[i]));
				}
			} catch (IllegalStateException e) {

			}
		}
		return nodes;
	}

	public int getChainState(String chainId) {
		if (chainId == null)
			throw new RuntimeException("chain id is null");
		ChainRuntime chain = ciliaContext.getChainRuntime(chainId);
		if (chain == null)
			throw new RuntimeException("'" + chainId + "' not found");
		return chain.getState();
	}

	/* callback event fired by the cilia framework */
	public void event(String chainId, String mediatorId, int evtNumber) {

		if ((evtNumber & EVENT_CHAIN_ADDED) == EVENT_CHAIN_ADDED) {
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_CHAIN_CREATE, chainId, Watch.getCurrentTicks());
			fireEventChain(0, chainId);
		}

		if ((evtNumber & EVENT_CHAIN_REMOVED) == EVENT_CHAIN_REMOVED) {
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_CHAIN_DELETE, chainId, Watch.getCurrentTicks());
			fireEventChain(1, chainId);
		}

		if ((evtNumber & EVENT_CHAIN_STARTED) == EVENT_CHAIN_STARTED) {
			fireEventChain(2, chainId);
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_CHAIN_START, chainId, Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_CHAIN_STOPPED) == EVENT_CHAIN_STOPPED) {
			fireEventChain(3, chainId);
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_CHAIN_STOP, chainId, Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_MEDIATOR_ADDED) == EVENT_MEDIATOR_ADDED) {
			fireEventNode(true, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_MEDIATOR_CREATE, chainId + "/" + mediatorId,
					Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_MEDIATOR_REMOVED) == EVENT_MEDIATOR_REMOVED) {
			fireEventNode(false, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_MEDIATOR_DELETE, chainId + "/" + mediatorId,
					Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_ADAPTER_ADDED) == EVENT_ADAPTER_ADDED) {
			fireEventNode(true, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_ADAPTER_CREATE, chainId + "/" + mediatorId,
					Watch.getCurrentTicks());
		}

		if ((evtNumber & EVENT_ADAPTER_REMOVED) == EVENT_ADAPTER_REMOVED) {
			fireEventNode(false, new NodeImpl(chainId, mediatorId));
			publisher.publish(EventProperties.TOPIC_APPLICATION,
					EventProperties.MODEL_MEDIATOR_DELETE, chainId + "/" + mediatorId,
					Watch.getCurrentTicks());
		}
	}

	/*
	 * retrieve properties ( Read only access ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.specification.Application#properties
	 * (fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public Dictionary properties(Node node) throws IllegalStateException,
			IllegalParameterException, IllegalStateException {
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
	public MediatorComponent getModel(Node node) throws IllegalParameterException,
			IllegalStateException {
		if (node == null)
			throw new IllegalParameterException("node is null !");
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

	public Node[] findByFilter(String ldapFilter) throws InvalidSyntaxException {

		Filter filter = Knowledge.createFilter(ldapFilter);

		MediatorComponent component;
		Dictionary dico = new Hashtable();
		Set componentSet = new HashSet();

		String chainId[] = getChains();

		for (int i = 0; i < chainId.length; i++) {
			/* retreive all adapters per all chain */
			dico.put(Constants.CHAIN_ID, chainId[i]);
			/* Iterate over all adapters */
			Iterator it = getAdaptersSet(chainId[i]).iterator();
			while (it.hasNext()) {
				component = (MediatorComponent) it.next();
				dico.put(Constants.NODE_ID, component.getId());
				if (filter.match(dico)) {
					componentSet.add(new NodeImpl(chainId[i], component.getId()));
				}
			}
			/* Iterate over all mediators */
			it = getMediatorSet(chainId[i]).iterator();
			while (it.hasNext()) {
				component = (MediatorComponent) it.next();
				dico.put(Constants.NODE_ID, component.getId());
				if (filter.match(dico)) {
					componentSet.add(new NodeImpl(chainId[i], component.getId()));
				}
			}
		}
		return (Node[]) componentSet.toArray(new Node[componentSet.size()]);
	}

	public Date lastStart(String chainId) {
		throw new UnsupportedOperationException("a mettre dans ChainRuntime l'heure du Start");
	}

}