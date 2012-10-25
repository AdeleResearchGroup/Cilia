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

package fr.liglab.adele.cilia.knowledge.configuration;

import java.util.HashMap;
import java.util.Map;

import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.RawData;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.knowledge.ListNodes;
import fr.liglab.adele.cilia.knowledge.MediatorMonitoring;
import fr.liglab.adele.cilia.knowledge.NodeImpl;

/**
 * Access to data stored in the Monitor Model
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class RawDataImpl extends NodeImpl implements RawData {

	private final ListNodes registry;

	public RawDataImpl(ListNodes registry, Node node) throws CiliaIllegalStateException {
		super(node);
		this.registry = registry;
	}

	private MediatorMonitoring getModel() throws CiliaIllegalStateException {
		MediatorMonitoring model = registry.get(uuid);
		if (model == null) {
			throw new CiliaIllegalStateException("Node " + super.toString()
					+ "no longer exist");
		}
		return model;
	}

	public boolean isValid() throws CiliaIllegalStateException {
		return getModel().getState();
	}

	public Measure[] measures(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		return getModel().measures(variableId);
	}

	public String[] getEnabledVariable() throws CiliaIllegalStateException {
		return getModel().getEnabledVariable();
	}

	public Map toMap(){
		Map map = new HashMap();
		String variables[] = ConfigurationHelper.variablesByCategory(null);
		for (String variable : variables) {
			Map variableMap = new HashMap();
			variableMap.put("Enabled", "unknown");
			try {
				Measure measures[] = measures(variable);
				StringBuffer buffer = new StringBuffer("[");
				for (Measure measure : measures) {
					buffer.append(measure.toString()).append(",");
				}
				if (measures.length>0){
					buffer.delete(buffer.length()-2, buffer.length()-1);
				}
				buffer.append("]");
				variableMap.put("Measures", buffer.toString());
			} catch (CiliaIllegalParameterException e) {
			} catch (CiliaIllegalStateException e) {
				return new HashMap();
			}
			map.put(variable, variableMap);
		}
		return map;
	}

}
