package fr.liglab.adele.cilia.administration.jmx;

import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

public class ApplSpecification implements ChainCallback, NodeCallback {
	private CiliaContext ciliaContext ;

	private String nodes_arrival;
	private String nodes_departure;
	private String nodes_modified;
	private String chain_arrival;
	private String chain_departure;
	private String chain_started;
	private String chain_stopped;

	private static synchronized String[] convertNodeToString(Node[] nodes) {
		String[] results = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			results[i] = "{" + nodes[i].getQualifiedId() + "}";
		}
		return results;
	}

	public String[] getChainIDs() {
		return ciliaContext.getApplicationSpecification().getChainId();
	}

	public String[] getEndpointsIn(String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		return convertNodeToString(ciliaContext.getApplicationSpecification().endpointIn(ldapFilter));
	}

	public String[] getEndpointsOut(String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		return convertNodeToString(ciliaContext.getApplicationSpecification().endpointOut(ldapFilter));
	}

	public String[] getConnectedTo(String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		return convertNodeToString(ciliaContext.getApplicationSpecification().connectedTo(ldapFilter));
	}

	public String[] getNodeByFilter(String ldapfilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		return convertNodeToString(ciliaContext.getApplicationSpecification().findNodeByFilter(ldapfilter));
	}

	public void onArrival(Node node) {
		nodes_arrival = node.toString();
	}

	public void onDeparture(Node node) {
		nodes_departure = node.toString();
	}

	public void onModified(Node node) {
		nodes_modified = node.toString();
	}

	public void onAdded(String chainId) {
		chain_arrival = chainId;
	}

	public void onRemoved(String chainId) {
		chain_departure = chainId;
	}

	public void onStarted(String chainId) {
		chain_started = chainId;
	}

	public void onStopped(String chainId) {
		chain_stopped = chainId;
	}

}
