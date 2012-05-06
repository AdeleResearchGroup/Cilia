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

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

/**
 * Retreival of a node, topology access
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface Topology {

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters keywords (chain,node,uuid) uuid is relevant only
	 *            for dynamic components ( SetUp,RawData,Threshold)
	 * @return list of adapter In ,
	 * @throws CiliaInvalidSyntaxException
	 *             LDAP syntax error
	 * @throws CiliaIllegalParameterException
	 *             null parameter
	 */
	Node[] endpointIn(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters keywords (chain,node,uuid) uuid is relevant only
	 *            for dynamic components ( SetUp,RawData,Threshold)
	 * @return list of adapter Out
	 * @throws CiliaInvalidSyntaxException
	 *             LDAP syntax error
	 * @throws CiliaIllegalParameterException
	 *             null parameter
	 */
	Node[] endpointOut(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException;

	/**
	 * 
	 * @param node
	 *            node reference
	 * @return array of successors , size = 0 if no successor
	 * @throws CiliaIllegalStateException
	 *             the node doesn't exist
	 * @throws CiliaIllegalParameterException
	 *             null parameter
	 */
	Node[] connectedTo(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters keywords (chain,node,uuid) uuid is relevant only
	 *            for dynamic components ( SetUp,RawData,Threshold)
	 * @return array of successors, size=0 if no node matching the filter
	 * @throws CiliaIllegalParameterException
	 *             null parameter
	 * @throws CiliaInvalidSyntaxException
	 *             LDAP syntax error
	 */
	Node[] connectedTo(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException;

}
