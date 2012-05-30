package fr.liglab.adele.cilia.administration.jmx;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.dynamic.ApplicationRuntime;
import fr.liglab.adele.cilia.dynamic.Measure;
import fr.liglab.adele.cilia.dynamic.MeasureCallback;
import fr.liglab.adele.cilia.dynamic.RawData;
import fr.liglab.adele.cilia.dynamic.SetUp;
import fr.liglab.adele.cilia.dynamic.ThresholdsCallback;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

public class ApplRuntime implements NodeCallback, MeasureCallback, ThresholdsCallback {
	private CiliaContext ciliaContext ;

	private volatile String nodes_arrival;
	private volatile String nodes_departure;
	private volatile String variable_updated;
	private volatile String variable_outOfBounds;
	private volatile String nodes_modified;

	private static synchronized String convertNodeToString(Node nodes) {
		return "{" + nodes.getQualifiedId() + "}";
	}

	private static synchronized String[] convertNodeToString(Node[] nodes) {
		String[] results = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			results[i] = convertNodeToString(nodes[i]);
		}
		return results;
	}

	private String variableToString(Node node, String variable, Object value) {
		StringBuffer sb = new StringBuffer(convertNodeToString(node));
		sb.append(variable).append("=").append(value);
		return sb.toString();
	}

	public String[] getChainIDs() {
		return ciliaContext.getApplicationRuntime().getChains();
	}

	public String[] getEndpointsIn(String ldapFilter) {
		try {
			return convertNodeToString(ciliaContext.getApplicationRuntime().endpointIn(ldapFilter));
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
	}

	public String[] getEndpointsOut(String ldapFilter) {
		try {
			return convertNodeToString(ciliaContext.getApplicationRuntime().endpointOut(ldapFilter));
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
	}

	public String[] getConnectedTo(String ldapFilter) {
		try {
			return convertNodeToString(ciliaContext.getApplicationRuntime().connectedTo(ldapFilter));
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
	}

	public String[] getNodeByFilter(String ldapfilter) {
		try {
			return convertNodeToString(ciliaContext.getApplicationRuntime().findNodeByFilter(ldapfilter));
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
	}

	public String[] nodeSetUp(String nodeLdap, String variable, int queueSize,
			String flow, boolean enable) {
		SetUp[] nodes;
		String[] result;
		try {
			nodes = ciliaContext.getApplicationRuntime().nodeSetup(nodeLdap);
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		}
		if (nodes.length == 0) {
			return new String[] { "no node matching ldap filter " + nodeLdap };
		}
		result = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			try {
				nodes[i].setMonitoring(variable, queueSize, flow, enable);
				result[i] = variableToString(nodes[i], variable, "configured successfully");
			} catch (CiliaIllegalParameterException e) {
				result[i] = variableToString(nodes[i], variable,
						"CiliaIllegalParameterException " + e.getMessage());
			} catch (CiliaInvalidSyntaxException e) {
				result[i] = variableToString(nodes[i], variable,
						"CiliaInvalidSyntaxException " + e.getMessage());
			}
		}
		return result;
	}

	public String[] getQueueSize(String ldapFilter, String variable) {
		SetUp[] nodes;
		try {
			nodes = ciliaContext.getApplicationRuntime().nodeSetup(ldapFilter);
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
		String[] queueSize;
		Object value;
		if (nodes.length == 0) {
			return new String[] { "no node matching filter " + ldapFilter };
		}
		queueSize = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			try {
				value = nodes[i].queueSize(variable);
				queueSize[i] = variableToString(nodes[i], variable, value);
			} catch (CiliaIllegalParameterException e) {
				queueSize[i] = variableToString(nodes[i], variable,
						"CiliaIllegalParameterException " + e.getMessage());
			}
		}
		return queueSize;
	}

	public String[] getFlowControl(String ldapFilter, String variable) {
		SetUp[] nodes;
		try {
			nodes = ciliaContext.getApplicationRuntime().nodeSetup(ldapFilter);
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };
		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
		String[] queueSize;

		if (nodes.length == 0) {
			return new String[] { "no node matching filter " + ldapFilter };
		}
		queueSize = new String[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			try {
				queueSize[i] = variableToString(nodes[i], variable,
						nodes[i].flowControl(variable));
			} catch (CiliaIllegalParameterException e) {
				queueSize[i] = variableToString(nodes[i], variable,
						"CiliaIllegalParameterException " + e.getMessage());
			}
		}
		return queueSize;
	}

	public String[] nodeRawData(String nodeLdap, String variable) {
		RawData[] nodes;
		try {
			nodes = ciliaContext.getApplicationRuntime().nodeRawData(nodeLdap);
		} catch (CiliaIllegalParameterException e) {
			return new String[] { "CiliaIllegalParameterException " + e.getMessage() };

		} catch (CiliaInvalidSyntaxException e) {
			return new String[] { "CiliaInvalidSyntaxException " + e.getMessage() };
		}
		String[] mesures = new String[nodes.length]  ;
		for (int i = 0; i < nodes.length; i++) {
			try {	        
				Measure[] m= nodes[i].measures(variable);
				mesures[i] = variableToString(nodes[i], variable,m[0].toString());	
				for (int j=1 ; j< m.length; j++) {
					mesures[i] += ";"+m[j].toString();
				}
			} catch (CiliaIllegalParameterException e) {
				mesures[i] += variableToString(nodes[i], variable,
						"CiliaIllegalParameterException " + e.getMessage());
			}
		}
		return mesures;
	}

	public void onUpdate(Node node, String varName, Measure m) {
		variable_updated = variableToString(node, varName, m.toString());
	}

	public void onArrival(Node node) {
		nodes_arrival = node.getQualifiedId();
	}

	public void onDeparture(Node node) {
		nodes_departure = node.getQualifiedId();
	}

	public void onThreshold(Node node, String variable, Measure measure, int thresholdType) {
		variable_outOfBounds = variableToString(node, variable, measure.toString())
				+ " threshold " + thresholdType;
	}

	public void onModified(Node node) {
		nodes_modified = node.getQualifiedId();
	}
}
