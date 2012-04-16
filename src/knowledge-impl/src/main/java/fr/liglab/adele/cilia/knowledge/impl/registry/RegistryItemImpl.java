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

package fr.liglab.adele.cilia.knowledge.impl.registry;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import fr.liglab.adele.cilia.knowledge.Constants;
import fr.liglab.adele.cilia.knowledge.UniformResourceName;
import fr.liglab.adele.cilia.knowledge.registry.RegistryItem;

/**
 * Object stored in the registry
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class RegistryItemImpl implements RegistryItem {

	private final Map props;

	public RegistryItemImpl(String uuid, String appid, String componentId, Map p) {

		if (p != null) {
			this.props = new HashMap(p);
		} else
			this.props = new HashMap(5);

		props.put(Constants.UUID, uuid);

		if (appid != null)
			props.put(Constants.CHAIN_ID, appid);
		if (componentId != null)
			props.put(Constants.NODE_ID, componentId);

	}
	
	public RegistryItemImpl(String uuid, String appid, String componentId) {
		this(uuid,appid,componentId,null);
	}
	

	public String uuid() {
		return (String) props.get(Constants.UUID);
	}

	public String chainId() {
		return (String) props.get(Constants.CHAIN_ID);
	}

	public String nodeId() {
		return (String) props.get(Constants.NODE_ID);
	}

	public Object objectRef() {
		return (Object) props.get("registry.object.reference");
	}

	public Object nodeReference() {
		return (Object) props.get("registry.node.reference");
	}

	public void setObjectReference(Object o) {
		props.put("registry.object.reference", o);
	}

	public void setNodeReference(Object o) {
		props.put("registry.node.reference", o);
	}

	public void clear() {
		props.clear();
	}

	public Dictionary getProperties() {
		return new Hashtable(props);
	}

	public void setProperty(String key, Object value) {
		props.put(key, value);
	}

	public Object getProperty(String key) {
		return props.get(key);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("uuid [").append(uuid()).append("] ");
		sb.append("qualified name [").append(chainId()).append("/").append(nodeId())
				.append("]");
		return sb.toString();
	}

}
