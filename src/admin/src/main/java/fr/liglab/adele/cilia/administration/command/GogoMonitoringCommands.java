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

package fr.liglab.adele.cilia.administration.command;

import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.ApplicationSpecification;
import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.MeasureCallback;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.RawData;
import fr.liglab.adele.cilia.SetUp;
import fr.liglab.adele.cilia.ThresholdsCallback;
import fr.liglab.adele.cilia.Topology;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.MediatorComponent;

/**
 * Simple gogo commands for debug purpose
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class GogoMonitoringCommands {

	private static final String HEADER = "+------------------------------------------------------"
			+ "------------------------------------------------------";
	private CiliaContext ciliaContext;
	private ApplicationRuntime runtime;
	private ApplicationSpecification application;
	private CallbackEventBaseLevel callbacks = new CallbackEventBaseLevel();
	private CallbackEventMetaLevel callbacksMeta = new CallbackEventMetaLevel();

	private void printSuccessor(Node[] nodes) {
		if (nodes.length == 0) {
			System.out.println("| No node matching the filter");
		}
		for (int i = 0; i < nodes.length; i++) {
			System.out.println("| Successor ->" + nodes[i].qualifiedId());
		}
	}

	public void start() {
		runtime = ciliaContext.getApplicationRuntime();
		application = ciliaContext.getApplicationSpecification();
		// app_callback_chain("(!chain=admin-chain)") ;
		// app_callback_node("(&(!chain=admin-chain)(node=*))") ;
		app_callback_chain("(chain=*)");
		app_callback_node("(&(chain=*)(node=*))");
	}

	public void stop() {
	}

	@Descriptor("Dump all successors to the node defined by ldapfiter")
	public void runtime_connected_to(String ldap) {
		try {
			System.out.println(HEADER);
			printSuccessor(runtime.connectedTo(ldap));
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all adapters in to the node defined by ldapfiter")
	public void runtime_endpoints_in(String ldap) {
		try {
			System.out.println(HEADER);
			endpoints(runtime, ldap, true);
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all adapters in to the node defined by ldapfiter")
	public void runtime_endpoints_out(String ldap) {
		try {
			System.out.println(HEADER);
			endpoints(runtime, ldap, false);
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all adapters in to the node defined by ldapfiter")
	public void runtime_find_node(String ldapFilter) {
		try {
			Node[] nodes;
			System.out.println(HEADER);
			nodes = runtime.findNodeByFilter(ldapFilter);
			if (nodes.length == 0) {
				System.out.println("No node matching the filter " + ldapFilter);
			} else {
				for (int i = 0; i < nodes.length; i++) {
					System.out.println("| Node ->" + nodes[i].qualifiedId());
				}
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Configure the setup of the node")
	public void node_setup(String nodeLdap, String variable, int queueSize, String flow,
			boolean enable) {
		try {
			SetUp[] nodes = runtime.nodeSetup(nodeLdap);
			if (nodes.length == 0) {
				System.out.println("no node matching filter :" + nodeLdap);
				return;
			}
			for (int i = 0; i < nodes.length; i++) {
				nodes[i].setMonitoring(variable, queueSize, flow, enable);
			}
			printSetupNode(nodes);

		} catch (Throwable e) {
			System.out
					.println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
			e.printStackTrace();
		}
	}

	@Descriptor("Configure the setup of the node")
	public void node_rawdata(String nodeLdap, String variable) {
		try {
			RawData[] nodes = runtime.nodeRawData(nodeLdap);
			if (nodes.length != 0) {
				System.out.println(HEADER);
				for (int i = 0; i < nodes.length; i++) {
					Measure[] measure = nodes[i].measures(variable);

					System.out.println("| Node #" + ((Node) nodes[i]).qualifiedId());
					System.out.println("| Variable '" + variable + "'");
					for (int j = 0; j < measure.length; j++) {
						System.out
								.println("| Value #" + j + " =" + measure[j].toString());
					}
				}
				System.out.println(HEADER);
			} else {
				System.out.println("No node matching filter :" + nodeLdap);
			}

		} catch (Throwable e) {
			System.out
					.println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
			e.printStackTrace();
		}
	}

	@Descriptor("Configure all Threshold for node matching the filter")
	public void node_monitoring(String nodeLdap, String variable) {
		try {
			System.out.println("Not yet implemented");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all nodes matching the filter")
	public void node_dump(String nodeLdap) {
		try {
			System.out.println("Not yet implemented");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump the state of the chain")
	public void runtime_chain_state(String chainId) {
		try {
			System.out.println(HEADER);
			int state = runtime.getChainState(chainId);
			switch (state) {
			case 0:
				System.out.println(" Chain '" + chainId + "' , state IDLE, last command "
						+ runtime.lastCommand(chainId));
				break;

			case 1:
				System.out.println(" Chain '" + chainId
						+ "' , state RUNNING, last command "
						+ runtime.lastCommand(chainId));
				break;
			case 2:
				System.out.println(" Chain '" + chainId
						+ "' , state STOPPED, last command "
						+ runtime.lastCommand(chainId));
				break;
			default:
				System.out.println("Internal state error ");
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all chainID at runtime")
	public void runtime_chains() {
		String[] chains = runtime.getChainId();
		System.out.println(HEADER);
		for (int i = 0; i < chains.length; i++) {
			System.out.println("ChainID =" + chains[i]);
		}
		System.out.println(HEADER);
	}

	private void printSetupNode(Node[] nodes) {
		String[] variables;
		System.out.println(HEADER);
		for (int i = 0; i < nodes.length; i++) {
			System.out.print("| Node #");
			System.out.println(((Node) nodes[i]).qualifiedId());
			System.out.println("| Valid :" +((SetUp) nodes[i]).isValid());
			System.out.println("| Categories");
			System.out.println("|    SystemCall variables :");
			variables = ((SetUp) nodes[i]).variablesByCategory("SystemCall");
			for (int j = 0; j < variables.length; j++) {
				System.out.println("|      " + variables[j]);

			}
			System.out.println("|    Dependency variables :");
			variables = ((SetUp) nodes[i]).variablesByCategory("DependencyCall");
			for (int j = 0; j < variables.length; j++) {
				System.out.println("|      " + variables[j]);
			}
			System.out.println("|    Eventing variables :");
			variables = ((SetUp) nodes[i]).variablesByCategory("EventingCall");
			for (int j = 0; j < variables.length; j++) {
				System.out.println("|      " + variables[j]);
			}
			System.out.println("|    Functionnal variables :");
			variables = ((SetUp) nodes[i]).variablesByCategory("FunctionnalCall");
			for (int j = 0; j < variables.length; j++) {
				System.out.println("|      " + variables[j]);
			}
		}
		System.out.println(HEADER);
	}

	private void endpoints(Topology source, String ldapFilter, boolean depart) {
		try {
			Node[] array;
			String msg;
			if (depart) {
				array = source.endpointIn(ldapFilter);
				msg = "| Endpoint in #";
			} else {
				array = source.endpointOut(ldapFilter);
				msg = "| Endpoint out #";
			}
			System.out.println(HEADER);
			if (array.length != 0) {
				for (int i = 0; i < array.length; i++) {
					System.out.println(msg + i + " : "
							+ ((MediatorComponent) array[i]).qualifiedId());
				}
			} else {
				System.out.println("No endpoint found for the filter " + ldapFilter);
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			System.out.println("Internal Error !");
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all chainId")
	public void app_chains() {
		try {
			String[] chains = application.getChainId();
			System.out.println(HEADER);
			for (int i = 0; i < chains.length; i++) {
				System.out.println("ChainID =" + chains[i]);
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("List adapters In")
	public void app_endpoints_in(String ldapFilter) {
		endpoints(application, ldapFilter, true);
	}

	@Descriptor("List adapters Out")
	public void app_endpoints_out(String ldapFilter) {
		endpoints(application, ldapFilter, false);
	}

	@Descriptor("Dump all successors to the adapter/mediator defined by ldapfiter")
	public void app_connected_to(String ldap) {
		try {
			System.out.println(HEADER);
			printSuccessor(application.connectedTo(ldap));
			System.out.println(HEADER);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump all adapters in to the node defined by ldapfiter")
	public void app_find_node(String ldapFilter) {
		try {
			Node[] nodes;
			System.out.println(HEADER);
			nodes = application.findNodeByFilter(ldapFilter);
			if (nodes.length == 0) {
				System.out.println("No node matching the filter " + ldapFilter);
			} else {
				for (int i = 0; i < nodes.length; i++) {
					System.out.println("| Node ->" + nodes[i].qualifiedId());
				}
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Regiter/Unregister chain callback")
	public void app_event_chain(String ldapFilter) {
		try {
			if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
				System.out
						.println("Registering events level chain, filter=" + ldapFilter);
				application.addListener(ldapFilter, (ChainCallback) callbacksMeta);
			} else {
				System.out.println("UnRegistering events level chain");
				application.removeListener((ChainCallback) callbacksMeta);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Regiter/Unregister chain callback")
	public void app_callback_chain(String ldapFilter) {
		try {
			if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
				System.out.println("Registering events level node, filter=" + ldapFilter);
				application.addListener(ldapFilter, (ChainCallback) callbacksMeta);
			} else {
				System.out.println("UnRegistering events level node");
				application.removeListener((ChainCallback) callbacksMeta);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Regiter/Unregister chain callback")
	public void app_callback_node(String ldapFilter) {
		try {
			if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
				System.out.println("Registering events level node, filter=" + ldapFilter);
				application.addListener(ldapFilter, (NodeCallback) callbacksMeta);
			} else {
				System.out.println("UnRegistering events level node");
				application.removeListener((NodeCallback) callbacksMeta);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void testApplication() {
		app_callback_chain("(!chain=admin-chain)");
		app_callback_node("(&(!chain=admin-chain)(node=*))");
		String[] array = application.getChainId();
		for (int i = 0; i < array.length; i++) {
			System.out.println("Chain ID :" + array[i]);
		}
		app_endpoints_in("(chain=Chain1)");
		app_endpoints_out("(chain=Chain1)");
		app_connected_to("(&(chain=Chain1)(node=mediator_1))");
	}

	@Descriptor("Entry for test")
	public void my_entry() {
		try {
			testApplication();
			runtime.addListener("(node=mediator_1)", (MeasureCallback) callbacks);
			SetUp[] rt = runtime.nodeSetup("(node=mediator_1)");

			if (rt.length != 0) {
				for (int i = 0; i < rt.length; i++) {
					rt[i].setMonitoring("process.entry.count", 100, "", true);
					System.out.println("isValid "+rt[i].nodeId()+" "+rt[i].isValid());
				}
			}
			node_rawdata("(node=mediator_1)", "process.entry.count");
			rt = runtime.nodeSetup("(node=mediator_2)");
			if (rt.length != 0) {
				for (int i = 0; i < rt.length; i++) {
					rt[i].setMonitoring("process.entry.count", 100, "", true);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private class CallbackEventBaseLevel implements NodeCallback, ThresholdsCallback,
			MeasureCallback {

		public void onUpdate(Node node, String variable, Measure m) {
			System.out.println("GogoCommand--> onUpdate " + node.qualifiedId()
					+ ", variable :" + variable + ", value=" + m.toString());
		}

		public void onThreshold(Node node, String variable, Measure m, int thresholdType) {
			System.out.println("GogoCommand--> onThreshold " + node.qualifiedId()
					+ ", variable :" + variable + ", measure =" + m.toString());
		}

		public void onArrival(Node node) {
			System.out.println("GogoCommand--> onArrival " + node.qualifiedId());
		}

		public void onDeparture(Node node) {
			System.out.println("GogoCommand--> onDeparture " + node.qualifiedId());
		}

		public void onModified(Node node) {
			System.out.println("GogoCommand-->" + " onModified " + node.qualifiedId());
		}

		public void onBind(Node from, Node to) {
			System.out.println("GogoCommand-->" + " onBind from" + from.qualifiedId()
					+ ", to" + to.qualifiedId());
		}

		public void onUnBind(Node from, Node to) {
			System.out.println("GogoCommand-->" + " onUnbind from" + from.qualifiedId()
					+ ", to" + to.qualifiedId());
		}
	}

	private class CallbackEventMetaLevel implements ChainCallback, NodeCallback {

		public void onAdded(String chainId) {
			System.out.println("GogoCommand-->" + " onAdded " + chainId);
		}

		public void onRemoved(String chainId) {
			System.out.println("GogoCommand-->" + " onRemoved " + chainId);
		}

		public void onStarted(String chainId) {
			System.out.println("GogoCommand-->" + " onStarted " + chainId);
		}

		public void onStopped(String chainId) {
			System.out.println("GogoCommand-->" + " onStopped " + chainId);
		}

		public void onArrival(Node node) {
			System.out.println("GogoCommand-->" + " onArrival " + node.qualifiedId());
		}

		public void onDeparture(Node node) {
			System.out.println("GogoCommand-->" + " onDeparture " + node.qualifiedId());
		}

		public void onModified(Node node) {
			System.out.println("GogoCommand-->" + " onModify " + node.qualifiedId());
		}

		public void onBind(Node from, Node to) {
			System.out.println("GogoCommand-->" + " onBind ( " + from.qualifiedId() + ","
					+ to.qualifiedId() + ")");
		}

		public void onUnBind(Node from, Node to) {
			System.out.println("GogoCommand-->" + " onUnBind ( " + from.qualifiedId() + ","
					+ to.qualifiedId() + ")");
		}

	}
}
