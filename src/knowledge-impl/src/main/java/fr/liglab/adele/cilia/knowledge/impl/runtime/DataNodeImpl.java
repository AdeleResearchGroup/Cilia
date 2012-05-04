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

package fr.liglab.adele.cilia.knowledge.impl.runtime;

import org.osgi.framework.InvalidSyntaxException;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.framework.monitor.statevariable.ComponentStateVarService;
import fr.liglab.adele.cilia.knowledge.Registry;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.registry.RegistryItem;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;

/**
 * Node = [mediator,adapter] at execution time
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class DataNodeImpl extends AbstractDataNode {

	private ComponentStateVarService mediatorHandler;
	
	public DataNodeImpl(String uuid, RuntimeRegistry registry) {
		RegistryItem item = registry.findByUuid(uuid) ;
		super.chainId = item.chainId();
		super.nodeId = item.nodeId();
		super.uuid = uuid ;
		mediatorHandler = item.runtimeReference() ;
	}


	/* return the list of state variables 'category system call' */
	public String[] systemVariable() {
		return mediatorHandler.getStateVarIdCategory("SystemCall");
	}

	/* return the list of state variable category 'dependency' */
	public String[] dependencyVariable() {
		return mediatorHandler.getStateVarIdCategory("DependencyCall");
	}

	/* return the list of state variable category 'eventing' */
	public String[] eventVariable() {
		return mediatorHandler.getStateVarIdCategory("EventingCall");
	}

	public String[] enabledVariable() {
		return mediatorHandler.getEnabledId();
	}

	public void setMonitoring(String variableId, int queueSize, String ldapFilter,
			boolean enable) throws CiliaIllegalParameterException, InvalidSyntaxException {

		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id is null !");

		if (queueSize < 1)
			throw new CiliaIllegalParameterException("queue size must be a positive integer");

		if (!stateVariables.containsKey(variableId)) {
			stateVariables.put(variableId, new Observations(queueSize));

		}
		mediatorHandler.setCondition(variableId, ldapFilter);
		if (enable)
			mediatorHandler.enableStateVar(variableId);
		else
			mediatorHandler.disableStateVar(variableId);
	}

	public void setMonitoring(String variableId, int queueSize)
			throws CiliaIllegalParameterException {
		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id is null !");

		if (queueSize < 1)
			throw new CiliaIllegalParameterException(
					"Queue size must be a positive integer value=" + queueSize);

		if (!stateVariables.containsKey(variableId)) {
			/* default value */
			try {
				/* a variable with defaults configuration is created */
				stateVariables.put(variableId, new Observations(queueSize));
				mediatorHandler.disableStateVar(variableId);
				mediatorHandler.setCondition(variableId, Knowledge.DEFAULT_CONDITION);
			} catch (InvalidSyntaxException e) {
				/* never happens! */
			}
		} else {
			/* The variable has been configured previously */
			Observations observation = (Observations) stateVariables.get(variableId);
			observation.setQueueSize(queueSize);
		}
	}

	public void setMonitoring(String variableId, String ldapFilter)
			throws CiliaIllegalParameterException, InvalidSyntaxException {

		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id is null !");

		if (!stateVariables.containsKey(variableId)) {
			/* a variable with defaults configuration is created */
			stateVariables
					.put(variableId, new Observations(Knowledge.DEFAULT_QUEUE_SIZE));
			mediatorHandler.disableStateVar(variableId);
		}
		mediatorHandler.setCondition(variableId, ldapFilter);
	}

	/*
	 * Enable / Disable the variable if variable is not existing , ldapcondition
	 * is null, Queue size = 10 (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.SetUp#setMonitoring(java
	 * .lang.String, boolean)
	 */
	public void setMonitoring(String variableId, boolean enable)
			throws CiliaIllegalParameterException {
		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id is null !");
		if (!stateVariables.containsKey(variableId)) {
			/* a variable with defaults configuration is created */
			stateVariables
					.put(variableId, new Observations(Knowledge.DEFAULT_QUEUE_SIZE));
			try {
				mediatorHandler.setCondition(variableId, Knowledge.DEFAULT_CONDITION);
			} catch (InvalidSyntaxException e) {
			}
		}
		if (enable)
			mediatorHandler.enableStateVar(variableId);
		else
			mediatorHandler.disableStateVar(variableId);
	}

	/*
	 * Return the number the capability of measures stored for this variable
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.SetUp#queueSize(java.lang
	 * .String)
	 */
	public int queueSize(String variableId) throws CiliaIllegalParameterException {
		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id is null !");

		Observations observations = (Observations) stateVariables.get(variableId);
		if (observations == null)
			throw new CiliaIllegalParameterException(toString() + " missing configuration !");

		return observations.queueSize;
	}

	/*
	 * return the condition to publish data (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.execution.SetUp#flowControl(java
	 * .lang.String)
	 */
	public String flowControl(String variableId) throws CiliaIllegalParameterException {
		if (variableId == null)
			throw new CiliaIllegalParameterException("variable id must not be null !");

		if (!stateVariables.containsKey(variableId))
			throw new RuntimeException(toString() + " missing configuration !");

		return mediatorHandler.getCondition(variableId);
	}

}
