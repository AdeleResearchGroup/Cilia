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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
//import org.osgi.service.remoteserviceadmin.ImportReference;
//import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

import fr.liglab.adele.cilia.ApplicationSpecification;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.Topology;
import fr.liglab.adele.cilia.dynamic.ApplicationRuntime;
import fr.liglab.adele.cilia.dynamic.Measure;
import fr.liglab.adele.cilia.dynamic.MeasureCallback;
import fr.liglab.adele.cilia.dynamic.RawData;
import fr.liglab.adele.cilia.dynamic.SetUp;
import fr.liglab.adele.cilia.dynamic.ThresholdsCallback;

/**
 * Simple gogo commands for debug purpose
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class GogoMonitoringCommands {

	private static final String HEADER = "+------------------------------------------------------"
			+ "------------------------------------------------------";
	private ApplicationRuntime runtime;
	private ApplicationSpecification application;
//	private RemoteServiceAdmin adminService;
	private CallbackSystems callbacks = new CallbackSystems();

	private void printSuccessor(Node[] nodes) {
		if (nodes.length == 0) {
			System.out.println("| No node matching the filter");
		}
		for (int i = 0; i < nodes.length; i++) {
			System.out.println("| Successor ->" + nodes[i].getQualifiedId());
		}
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
					System.out.println("| Node ->" + nodes[i].getQualifiedId());
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

					System.out.println("| Node #" + ((Node) nodes[i]).getQualifiedId());
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
		String[] chains = runtime.getChains();
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
			System.out.println(((Node) nodes[i]).getQualifiedId());
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
							+ ((MediatorComponent) array[i]).getQualifiedId());
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
			String[] chains = application.getChains();
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
					System.out.println("| Node ->" + nodes[i].getQualifiedId());
				}
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void testApplication() {
		String[] array = application.getChains();
		for (int i = 0; i < array.length; i++) {
			System.out.println("Chain ID :" + array[i]);
		}
		app_endpoints_in("(chain=Chain1)");
		app_endpoints_out("(chain=Chain1)");
		/* tester connected to */
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

	@Descriptor("Dump service imported")
	public void imported_services() {
		try {
			EndpointService[] exServices = importedEndpoints();
			System.out.println(HEADER);
			if (exServices.length != 0) {
				for (int i = 0; i < exServices.length; i++) {
					System.out.println("| Service imported #id "
							+ exServices[i].endpointId());
					System.out.println("| \tProperties " + exServices[i].properties());
				}
			} else {
				System.out.println("No service imported ");
			}
			System.out.println(HEADER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public EndpointService[] importedEndpoints() {
		Set set = new HashSet();
//		if (adminService != null) {
//			Collection registry = adminService.getImportedEndpoints();
//			if (registry != null) {
//				ImportReference reference;
//				Iterator it = registry.iterator();
//				while (it.hasNext()) {
//					reference = ((ImportReference) it.next());
//					set.add(new EndpointService(reference.getImportedEndpoint().getId(),
//							reference.getImportedEndpoint().getProperties()));
//				}
//			}
//		}
		return (EndpointService[]) set.toArray(new EndpointService[set.size()]);
	}

	private class CallbackSystems implements NodeCallback, ThresholdsCallback,
			MeasureCallback {

		public void onUpdate(Node node, String variable, Measure m) {
			System.out.println("GogoCommand--> onUpdate " + node.getQualifiedId()
					+ ", variable :" + variable + ", value=" + m.toString());
		}

		public void onThreshold(Node node, String variable, Measure m, int thresholdType) {
			System.out.println("GogoCommand--> onThreshold " + node.getQualifiedId()
					+ ", variable :" + variable + ", measure =" + m.toString());
		}

		public void onArrival(Node node) {
			System.out.println("GogoCommand--> onArrival " + node.getQualifiedId());
		}

		public void onDeparture(Node node) {
			System.out.println("GogoCommand--> onDeparture " + node.getQualifiedId());
		}

		public void onModified(Node node) {
			System.out.println("GogoCommand-->" + " onModified " + node.getQualifiedId());
		}
	}

	private class EndpointService {
		private final String id;
		private final Map props;

		public EndpointService(String id, Map props) {
			this.id = id;
			if (props == null)
				this.props = Collections.EMPTY_MAP;
			else
				this.props = Collections.unmodifiableMap(props);
		}

		public String endpointId() {
			return id;
		}

		public Map properties() {
			return props;
		}
	}
}
