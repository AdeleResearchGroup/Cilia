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

package fr.liglab.adele.cilia.framework.monitor.statevariable;

import java.util.Dictionary;

import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.framework.monitor.IMonitor;

public interface ComponentStateVarService extends IMonitor {

	void setProperty(String key, Object value);

	Object getProperty(String key);

	Dictionary getProperties();

	/**
	 * Define a condition to publish a new value
	 * 
	 * @param stateVarId
	 *            , state var identification
	 * @param ldapCondition
	 *            ldap condition string format, null if no condition associated
	 * @throws InvalidSyntaxException
	 * @throws CiliaInvalidSyntaxException
	 */
	void setCondition(String stateVarId, String ldapCondition)
			throws CiliaInvalidSyntaxException;

	/**
	 * Gives the current condition associated to the state var
	 * 
	 * @param stateVarId
	 * @return String format of ldap condition or null if no condition
	 */
	public String getCondition(String stateVarId);

	/**
	 * Gives the list of state variable id
	 * 
	 * @return null if no state var id
	 */
	String[] getStateVariableId();

	/**
	 * Enable a state var (value are published according the condition )
	 * 
	 * @param stateVarId
	 *            state var id
	 */
	void enableStateVar(String stateVarId);

	/**
	 * disable a state var , nor value are now published
	 * 
	 * @param stateVarId
	 */
	void disableStateVar(String stateVarId);

	/**
	 * Gives the list of state var enabled
	 * 
	 * @return array of state var id , null if no state var
	 */
	String[] getEnabledId();

	/**
	 * Gives the list of categories
	 * 
	 * @return String categories
	 */
	String[] getCategories();

	/**
	 * return a list of state variable given a category
	 * 
	 * @param category
	 * @return list of state var id.
	 */
	String[] getStateVarIdCategory(String category);
	
	/** 
	 * 
	 * @return state of the component Mediator/Adapter
	 */
	boolean isComponentValid();

}
