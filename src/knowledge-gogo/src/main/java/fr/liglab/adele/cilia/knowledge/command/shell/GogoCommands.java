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

package fr.liglab.adele.cilia.knowledge.command.shell;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.util.measurement.Measurement;

import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.eventbus.Cache;
import fr.liglab.adele.cilia.knowledge.eventbus.CachedEvent;
import fr.liglab.adele.cilia.knowledge.runtime.DynamicProperties;
import fr.liglab.adele.cilia.knowledge.runtime.RawData;
import fr.liglab.adele.cilia.knowledge.runtime.SetUp;
import fr.liglab.adele.cilia.knowledge.specification.Application;
import fr.liglab.adele.cilia.management.Watch;

/**
 * Simple gogo commands for debug purpose
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class GogoCommands {

	private static final String HEADER = "+------------------------------------------------------"
			+ "------------------------------------------------------";
	private DynamicProperties exec;
	private Cache eventbus;
	private Application specification;
	private RemoteServiceAdmin adminService;

	/* -- Registry access -- */

	@Descriptor("Find an UUID in the Cilia@Runtime registry using ldap")
	public void reg_find(String filter) {
		try {
			Node[] item = exec.findByFilter(filter);
			System.out.println(HEADER);
			if (item.length == 0) {
				System.out.println("The registry is empty");
			}
			for (int i = 0; i < item.length; i++) {
				System.out.println("| " + item[i].toString());
			}
			System.out.println(HEADER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* -- Dynamic properties access -- */

	@Descriptor("Dump all successors to the node defined by ldapfiter")
	public void node_connected_to(String ldap) {
		try {
			Node[] nodes = exec.connectedTo(ldap);
			if (nodes.length == 0) {
				System.out.println("no node matching filter :" + ldap);
			}
			for (int i = 0; i < nodes.length; i++) {
				System.out.println("Successor ->" + nodes[i].toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Configure the setup of the node")
	public void node_setup(String nodeLdap, String variable, int queueSize, String flow,
			boolean enable) {
		try {
			SetUp[] nodes = exec.nodeSetup(nodeLdap);
			if (nodes.length == 0) {
				System.out.println("no node matching filter :" + nodeLdap);
			}
			for (int i = 0; i < nodes.length; i++) {
				nodes[i].setMonitoring(variable, queueSize, flow, enable);
			}

		} catch (Exception e) {
			System.out
					.println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
			e.printStackTrace();
		}
	}

	private static String measureToString(Measurement m) {
		StringBuffer sb = new StringBuffer(Double.toString(m.getValue())).append(" :");
		sb.append(Watch.formatDateIso8601(m.getTime()));
		return sb.toString();
	}

	@Descriptor("Configure the setup of the node")
	public void node_rawdata(String nodeLdap, String variable) {
		try {
			RawData[] nodes = exec.nodeRawData(nodeLdap);
			if (nodes.length != 0) {
				System.out.println(HEADER);
				for (int i = 0; i < nodes.length; i++) {
					Iterator it = nodes[i].measures(variable).listIterator();
					int j = 0;
					System.out.println("| Node #" + ((Node) nodes[i]).toString());
					System.out.println("| Variable '" + variable + "'");
					while (it.hasNext()) {
						Object value = it.next();
						if (value instanceof Measurement) {
							System.out.println("| Value #" + j + " ="
									+ measureToString((Measurement) value));
						} else
							System.out.println("| Value #" + j + " ="
									+ (it.next()).toString());
						j++;
					}
				}
				System.out.println(HEADER);
			} else {
				System.out.println("no node matching filter :" + nodeLdap);
			}

		} catch (Exception e) {
			System.out
					.println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
			e.printStackTrace();
		}
	}

	@Descriptor("Dump a node")
	public void node_dump(String ldapFilter) {

		try {
			String[] variables;
			SetUp[] nodes = exec.nodeSetup(ldapFilter);
			if (nodes.length == 0) {
				System.out.println("no node matching filter :" + ldapFilter);
				return;
			}
			System.out.println(HEADER);
			for (int i = 0; i < nodes.length; i++) {
				System.out.print("| Node #");
				System.out.println(((Node) nodes[i]).toString());
				System.out.println("| Categories");
				System.out.println("|    SystemCall variables :");
				variables = nodes[i].systemVariable();
				for (int j = 0; j < variables.length; j++) {
					System.out.println("|      " + variables[j]);

				}
				System.out.println("|    Dependency variables :");
				variables = nodes[i].dependencyVariable();
				for (int j = 0; j < variables.length; j++) {
					System.out.println("|      " + variables[j]);
				}
				System.out.println("|    Eventing variables :");
				variables = nodes[i].eventVariable();
				for (int j = 0; j < variables.length; j++) {
					System.out.println("|      " + variables[j]);
				}
			}
			System.out.println(HEADER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* -- Event -- */

	@Descriptor("Set the size of the bus event cache")
	public void cache_size(int sizeToSet) {
		try {
			int s = eventbus.size();
			eventbus.size(sizeToSet);
			System.out.println("Cache size set=" + eventbus.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump the bus event cache")
	public void cache_dump() {
		try {
			CachedEvent[] c = eventbus.cachedEvent();

			System.out.println(HEADER);
			for (int i = 0; i < c.length; i++) {
				System.out.println("| Event #" + i + " : " + c[i].toString());
				i++;
			}
			System.out.println(HEADER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Clear the bus event cache")
	public void cache_clear() {
		try {
			eventbus.clearCache();
			System.out.println("Cache cleared");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Dump the list of chain Id")
	public void chain_list() {
		try {
			String[] array = specification.getChains();
			System.out.println(HEADER);
			if (array.length != 0) {
				for (int i = 0; i < array.length; i++) {
					System.out.println("| Chain name -> " + array[i]);
				}
			} else {
				System.out.println("No chain id founded");
			}
			System.out.println(HEADER);
		} catch (Exception e) {
			System.out.println("Internal Error !");
		}
	}

	private void endpoints(String ldapFilter, boolean depart) {
		try {
			Node[] array;
			String msg;
			if (depart) {
				array = specification.endpointIn(ldapFilter);
				msg = "| Endpoint in #";
			} else {
				array = specification.endpointOut(ldapFilter);
				msg = "| Endpoint out #";
			}
			System.out.println(HEADER);
			if (array.length != 0) {
				for (int i = 0; i < array.length; i++) {
					System.out.println(msg + i + " : " + array[i].toString());
				}
			} else {
				System.out.println("No endpoint found for the filter " + ldapFilter);
			}
			System.out.println(HEADER);
		} catch (Exception e) {
			System.out.println("Internal Error !");
			e.printStackTrace();
		}
	}

	@Descriptor("List adapters In")
	public void app_endpoints_in(String ldapFilter) {
		endpoints(ldapFilter, true);
	}

	@Descriptor("List adapters Out")
	public void app_endpoints_out(String ldapFilter) {
		endpoints(ldapFilter, false);
	}

	@Descriptor("Dump all successors to the adapter/mediator defined by ldapfiter")
	public void app_connected_to(String ldap) {
		try {
			System.out.println("Not yet implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Entry for test")
	public void my_entry() {
		try {
			SetUp[] nodes = exec.nodeSetup("(node=mediator_1)");
			if (nodes.length != 0) {
				for (int i = 0; i < nodes.length; i++) {
					nodes[i].setMonitoring("process.entry.count", 100, "", true);
				}
			}
			node_rawdata("(node=mediator_1)", "process.entry.count");
		} catch (Exception e) {
			System.out
					.println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public EndpointService[] importedEndpoints() {
		Set set = new HashSet();
		Collection registry = adminService.getImportedEndpoints();
		if (registry != null) {
			ImportReference reference;
			Iterator it = registry.iterator();
			while (it.hasNext()) {
				reference = ((ImportReference) it.next());
				set.add(new EndpointService(reference.getImportedEndpoint().getId(),
						reference.getImportedEndpoint().getProperties()));
			}
		}
		return (EndpointService[]) set.toArray(new EndpointService[set.size()]);
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
