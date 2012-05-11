package fr.liglab.adele.cilia.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContainer;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.Topology;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

import fr.liglab.adele.cilia.model.PatternType;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractTopology implements Topology {

	private CiliaContainer ciliaContext;

	public AbstractTopology() {
	}

	protected void setContext(CiliaContainer cc) {
		ciliaContext = cc;
	}

	/**
	 * Retreives all nodes matching the filter
	 * 
	 * @param ldapFilter
	 *            , keywords = chain, node
	 * 
	 * @return array of node matching the filter, array size 0 if no node
	 *         matching the filter�
	 * @throws CiliaInvalidSyntaxException
	 *             , ldap syntax error
	 */
	public abstract Node[] findNodeByFilter(String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException;

	/*
	 * Return the list of endpoints matching the filter
	 */
	protected abstract Node[] getEndpoints(String ldapFilter, PatternType type)
			throws CiliaInvalidSyntaxException, CiliaIllegalParameterException;

	/*
	 * build the ldap filter for retreiving a specific nodes
	 */
	protected static String makefilter(String chain, String node) {
		StringBuffer sb = new StringBuffer("(&(");
		sb.append(ConstRuntime.CHAIN_ID).append("=").append(chain);
		sb.append(")(").append(ConstRuntime.NODE_ID).append("=").append(node);
		sb.append("))");
		return sb.toString();
	}

	public Node[] connectedTo(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {
		Node[] nodes = new Node[0];
		Node[] source = findNodeByFilter(ldapFilter);
		try {
			for (int i = 0; i < source.length; i++) {
				nodes = ConstRuntime.concat(nodes, connectedTo(source[i]));
			}
		} catch (CiliaIllegalStateException e) {
		}
		return nodes;
	}

	protected Node[] getNextNodes(Binding[] bindings, Node node) {
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
			Node item[];
			try {
				item = findNodeByFilter(filter);
				for (int i = 0; i < item.length; i++) {
					nodeSet.add(item[i]);
				}
			} catch (CiliaInvalidSyntaxException e) {
			} catch (CiliaIllegalParameterException e) {
			}
		}
		return (Node[]) nodeSet.toArray(new Node[nodeSet.size()]);
	}

	protected MediatorComponent getModel(String chainId, String component)
			throws CiliaIllegalStateException {
		Chain chain;
		MediatorComponent mc;
		try {
			ciliaContext.getMutex().readLock().acquire();
			try {
				chain = ciliaContext.getChain(chainId);
				if (chain == null) /* chain not found */
					throw new CiliaIllegalStateException(chainId + " not existing !");
				mc = chain.getAdapter(component);
				/* checks Adapter or mediator */
				if (mc == null)
					mc = chain.getMediator(component);
				if (mc == null)
					throw new CiliaIllegalStateException(chainId + "/" + component
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
		return getModel(node.chainId(), node.nodeId());
	}

	/*
	 * retreives all adapter in ( entries of mediation chain ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.DynamicProperties#endpointIn
	 * (java.lang.String)
	 */
	public Node[] endpointIn(String ldapFilter) throws CiliaInvalidSyntaxException,
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
	public Node[] endpointOut(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {
		return getEndpoints(ldapFilter, PatternType.OUT_ONLY);

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

}
