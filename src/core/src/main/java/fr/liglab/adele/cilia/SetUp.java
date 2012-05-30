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

package fr.liglab.adele.cilia;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

/**
 * Monitoring Configuration
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface SetUp extends Node {

	/**
	 * 
	 * @return Categories of variables
	 */
	String[] getCategories();

	/**
	 * 
	 * @param category , if null return all variables name
	 * @return array of variables name
	 */
	String[] variablesByCategory(String category);

	/**
	 * Configure the monitoring on this object
	 * 
	 * @param variableId
	 *            , name of the variable to configure
	 * @param queueSize
	 *            , number of monitored value stored ( circular queue)
	 * @param LdapFilter
	 *            , data control flow management
	 * @param enable
	 *            , true values are published
	 * @return true if action done , false otherwhise
	 * @throws CiliaInvalidSyntaxException
	 */
	void setMonitoring(String variableId, int queueSize, String LdapFilter, boolean enable)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException;

	/**
	 * Configure the monitoring on this object ( others parameters are not
	 * modified )
	 * 
	 * @param variableId
	 *            , name of the variable to configure
	 * @param queueSize
	 *            , number of monitored values stored ( circular queue)
	 * @return true if action done , false otherwhise
	 */
	void setMonitoring(String variableId, int queueSize)
			throws CiliaIllegalParameterException;

	/**
	 * Configure the monitoring on this object
	 * 
	 * @param variableId
	 *            , name of the variable to configure
	 * @param LdapFilter
	 *            , data control flow management
	 * @return true if action done , false otherwhise
	 * @throws CiliaInvalidSyntaxException
	 */
	void setMonitoring(String variableId, String LdapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException;

	/**
	 * Configure the monitoring on this object
	 * 
	 * @param variableId
	 *            , name of the variable to configure
	 * @param enable
	 *            , true values are published
	 * @return true if action done , false otherwhise
	 */
	void setMonitoring(String variableId, boolean enable)
			throws CiliaIllegalParameterException;

	/**
	 * 
	 * @return list of state variable enabled
	 */
	String[] enabledVariable();

	/**
	 * 
	 * @param variableId
	 * @return number of objects stored
	 */
	int queueSize(String variableId) throws CiliaIllegalParameterException;

	/**
	 * @param variableId
	 * @return ldap filter for the flow control
	 * @throws CiliaIllegalParameterException
	 */
	String flowControl(String variableId) throws CiliaIllegalParameterException;
}
