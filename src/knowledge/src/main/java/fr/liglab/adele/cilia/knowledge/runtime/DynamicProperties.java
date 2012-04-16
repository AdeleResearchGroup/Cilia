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

package fr.liglab.adele.cilia.knowledge.runtime;

import org.osgi.framework.InvalidSyntaxException;

import fr.liglab.adele.cilia.exceptions.IllegalParameterException;
import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.NodeRegistration;
import fr.liglab.adele.cilia.knowledge.Registry;
import fr.liglab.adele.cilia.knowledge.Topology;

/**
 * Class Runtime properties
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface DynamicProperties extends Topology, NodeRegistration,
		ThresholdsRegistration ,Registry {

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return array of Setup , size is 0 if no node found matching the filter
	 * @throws InvalidSyntaxException
	 *             if syntax error for the filter
	 * 
	 */
	SetUp[] nodeSetup(String ldapFilter) throws InvalidSyntaxException;

	/**
	 * fast access using the node reference
	 * 
	 * @param node
	 * @return object Setup
	 * @throws IllegalStateException
	 *             if the node is no more existing
	 * @throws IllegalParameterException
	 *             if the node is null
	 */
	SetUp nodeSetup(Node node) throws IllegalStateException, IllegalParameterException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return array of object raw data matching the ldap filter
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 */
	RawData[] nodeRawData(String ldapFilter) throws InvalidSyntaxException;

	/**
	 * fast access using the node reference
	 * 
	 * @param node
	 * @return object providing raw data
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 * @throws IllegalStateException
	 *             if the node is no more existing
	 * @throws IllegalParameterException
	 *             if the node is null
	 */
	RawData nodeRawData(Node node) throws IllegalStateException,
			IllegalParameterException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return array of object type Diagnostic matching the ldap filter
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 */
	Thresholds[] nodeMonitoring(String ldapFilter) throws InvalidSyntaxException;

	/**
	 * fast access using the node reference
	 * 
	 * @param node
	 * @return object Diagnostics
	 * @throws IllegalStateException
	 *             if the node is no more existing
	 * @throws IllegalParameterException
	 *             if the node is null
	 */
	Thresholds nodeMonitoring(Node node) throws IllegalStateException,
			IllegalParameterException;

}
