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

package fr.liglab.adele.cilia.runtime.knowledge;

import java.util.Map;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.util.WeakValueHashMap;

/**
 * 
 * for a fast retreival existing node
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListNodes {

	private final Map registry;
	private TopologyImpl topology;

	public ListNodes(TopologyImpl topo) {
		registry = new WeakValueHashMap();
		topology = topo;
	}

	/**
	 * Retreive the model , in the cache
	 * 
	 * @param uuid
	 * @return
	 * @throws CiliaIllegalStateException
	 */
	public MediatorMonitoring getAndStore(String uuid) throws CiliaIllegalStateException {
		MediatorMonitoring monitoring = null;
		MediatorComponent mc;
		/* containsKey remove gargabed value and associated key */
		monitoring = retreive(uuid);
		if (monitoring == null) {
			/* Retrieve the node if existing */
			Node[] nodes;
			try {
				nodes = topology.findNodeByFilter("(uuid=" + uuid + ")", false);
				if (nodes.length == 1) {
					mc = (MediatorComponent) nodes[0];
					synchronized (registry) {
						monitoring = (MediatorMonitoring) mc
								.getModel(MediatorMonitoring.NAME);
						registry.put(uuid, mc);
					}
				} else
					throw new CiliaIllegalStateException("no node found with uuid="
							+ uuid);
			} catch (CiliaInvalidSyntaxException e) {
			} catch (CiliaIllegalParameterException e) {
			}
		}
		return monitoring;
	}

	private MediatorMonitoring retreive(String uuid) {
		MediatorMonitoring mc = null;
		synchronized (registry) {
			/* containsKey remove garbaged value and associated key */
			if (registry.containsKey(uuid)) {
				mc = (MediatorMonitoring) ((MediatorComponent) registry.get(uuid))
						.getModel(MediatorMonitoring.NAME);
			}
		}
		return mc;
	}

	/* Get item in the registry or fire Exception if no more existing */
	public MediatorMonitoring get(String uuid) throws CiliaIllegalStateException {
		synchronized (registry) {
			if (registry.containsKey(uuid)) {
				MediatorMonitoring mc = (MediatorMonitoring) ((MediatorComponent) registry
						.get(uuid)).getModel(MediatorMonitoring.NAME);
				return mc;
			} else {
				throw new CiliaIllegalStateException("uuid " + uuid + " no longer exist");
			}
		}
	}

	public void clearCache() {
		registry.clear();
	}
}
