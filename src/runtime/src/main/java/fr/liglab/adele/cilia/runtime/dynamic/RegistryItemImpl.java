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

package fr.liglab.adele.cilia.runtime.dynamic;


import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import fr.liglab.adele.cilia.framework.monitor.statevariable.ComponentStateVarService;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import fr.liglab.adele.cilia.util.Watch;

/**
 * Object stored in the registry
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RegistryItemImpl implements RegistryItem {

	private final Map props;
	
	private static final long timeStamp = System.currentTimeMillis();
	
	public RegistryItemImpl(String uuid, String appid, String componentId, Map p) {

		if (p != null) {
			this.props = new HashMap(p);
		} else
			this.props = new HashMap(6);

		props.put(ConstRuntime.UUID, uuid);

		if (appid != null)
			props.put(ConstRuntime.CHAIN_ID, appid);
		if (componentId != null)
			props.put(ConstRuntime.NODE_ID, componentId);
	}
	
	public RegistryItemImpl(String uuid, String appid, String componentId) {
		this(uuid,appid,componentId,null);
	}
	
	public String uuid() {
		return (String) props.get(ConstRuntime.UUID);
	}

	public String chainId() {
		return (String) props.get(ConstRuntime.CHAIN_ID);
	}

	public String nodeId() {
		return (String) props.get(ConstRuntime.NODE_ID);
	}

	public ComponentStateVarService runtimeReference() {
		return (ComponentStateVarService) props.get("registry.object.runtime.reference");
	}

	public DynamicNode dataRuntimeReference() {
		return  (DynamicNode) props.get("registry.node.data.reference");
	}

	public void setRuntimeReference(ComponentStateVarService o) {
		props.put("registry.object.runtime.reference", o);
	}

	public void setDataRuntimeReference(DynamicNode o) {
		props.put("registry.node.data.reference", o);
	}
	
	public MediatorComponent specificationReference() {
		return (MediatorComponent)props.get("registry.mediator.component") ;
	}
	
	public void setSpecificationReference(MediatorComponent mc) {
		props.put("registry.mediator.component", mc) ;
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

	public String qualifiedId() {
		return FrameworkUtils.makeQualifiedId(chainId(), nodeId(), uuid());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(qualifiedId()) ;
		sb.append("creation date :"+Watch.formatDateIso8601(timeStamp));
		sb.append(", properties:").append(props);
		return sb.toString();
	}

	public long timeStamp() {
		return timeStamp;
	}
}
