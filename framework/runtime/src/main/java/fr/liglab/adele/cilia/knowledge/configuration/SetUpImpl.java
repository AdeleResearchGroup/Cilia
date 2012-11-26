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
import java.util.Set;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.SetUp;
import fr.liglab.adele.cilia.Thresholds;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.knowledge.ListNodes;
import fr.liglab.adele.cilia.knowledge.MediatorMonitoring;
import fr.liglab.adele.cilia.knowledge.NodeImpl;

/**
 * Configure the Monitor Model ( Meta level ) or Base Monitor 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SetUpImpl extends NodeImpl implements SetUp, Thresholds {

	private final ListNodes registry;

	public SetUpImpl(final ListNodes registry, Node node) {
		super(node);
		this.registry = registry;
	}

	public String[] getCategories() {
		return ConfigurationHelper.getCategories();
	}

	public String[] variablesByCategory(String category) {
		return ConfigurationHelper.variablesByCategory(category);
	}

	public void setMonitoring(String variableId, int queueSize, String ldapfilter,
			boolean enable) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		ConfigurationHelper.checkQueueSize(queueSize);
		ConfigurationHelper.checkDataFlowFilter(ldapfilter);
		m.setQueueSize(variableId, queueSize);
		Map config = ConfigurationHelper.getRootConfig(m.getModel()) ;
		ConfigurationHelper.storeDataFlowControl(config, variableId, ldapfilter);
		ConfigurationHelper.storeEnable(config, variableId, enable);
		ConfigurationHelper.storeRootConfig(m.getModel(), config);

	}

	public void setMonitoring(String variableId, int queueSize)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring mo = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		ConfigurationHelper.checkQueueSize(queueSize);
		mo.setQueueSize(variableId, queueSize);
	}

	public void setMonitoring(String variableId, String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkDataFlowFilter(ldapFilter);
		Map config = ConfigurationHelper.getRootConfig(m.getModel());
		ConfigurationHelper.storeDataFlowControl(config, variableId, ldapFilter);
		ConfigurationHelper.storeRootConfig(m.getModel(), config) ;
	}

	public void setMonitoring(String variableId, boolean enable)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		Map configBase = ConfigurationHelper.getRootConfig(m.getModel());
		ConfigurationHelper.storeEnable(configBase, variableId, enable) ;
		ConfigurationHelper.storeRootConfig(m.getModel(), configBase) ;
	}

	public String[] getEnabledVariable() throws CiliaIllegalStateException {
		Set listEnabled ;
		MediatorMonitoring m = registry.getAndStore(uuid);
		Map config = ConfigurationHelper.getRootConfig(m.getModel()) ;
		listEnabled = ConfigurationHelper.getEnabledVariable(config) ;
		return (String[]) listEnabled.toArray(new String[listEnabled.size()]);
	}

	public int getQueueSize(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		return m.getQueueSize(variableId);
	}


	public String getFlowControl(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		Map config = ConfigurationHelper.getRootConfig(m.getModel()) ;
		return ConfigurationHelper.getFlowControl(config, variableId);
	}

	public void setLow(String variableId, double low)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		m.setLow(variableId, low);

	}

	public void setVeryLow(String variableId, double veryLow)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		m.setVeryLow(variableId, veryLow);
	}

	public void setHigh(String variableId, double high)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		m.setHigh(variableId, high);
	}

	public void setVeryHigh(String variableId, double veryHigh)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		m.setVeryHigh(variableId, veryHigh);
	}

	public double getLow(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		return m.getLow(variableId);
	}

	public double getVeryLow(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		return m.getVeryLow(variableId);
	}

	public double getHigh(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		return m.getHigh(variableId);
	}

	public double getVeryHigh(String variableId) throws CiliaIllegalParameterException,
	CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		ConfigurationHelper.checkStateVarId(variableId);
		return m.getVeryHigh(variableId);
	}


	public Map toMap(){
		Map map = new HashMap();
		String [] variables = variablesByCategory(null);
		for (String variable : variables) {
			Map varInfo = getVariableInfo(variable);
			map.put(variable, varInfo);
		}
		System.out.println(map);
		return map;
	}

	private Map getVariableInfo(String variable){
		Map map = new HashMap();
		try {
			map.put("QueueSize", String.valueOf(getQueueSize(variable)));
			map.put("FlowControl", String.valueOf(getFlowControl(variable)));
			map.put("Enabled", "unknown");
			map.put("Low", String.valueOf(getLow(variable)));
			map.put("Very Low", String.valueOf(getVeryLow(variable)));
			map.put("High", String.valueOf(getHigh(variable)));
			map.put("Very High", String.valueOf(getVeryHigh(variable)));
		}catch(Exception e){
			e.printStackTrace();
		}
		return map;
	}


}
