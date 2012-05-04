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

package fr.liglab.adele.cilia.knowledge;

import org.osgi.framework.InvalidSyntaxException;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;

/**
 * Node retreival of a node, topology access
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface Topology {

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, 
	 * @return list of adapter In
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 */
	Node[] endpointIn(String ldapFilter) throws CiliaIllegalParameterException,
			InvalidSyntaxException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return list of adapter In
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 */
	Node[] endpointOut(String ldapFilter) throws CiliaIllegalParameterException,
			InvalidSyntaxException;

	/**
	 * 
	 * @param node
	 *            node reference
	 * @return array of successors , size = 0 if no node successor founded
	 * @throws IllegalStateException
	 * @throws CiliaIllegalStateException
	 */
	Node[] connectedTo(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param ldapFilter
	 *            define an unique node , ldap filters, keywords
	 *            {uuid,chain,node}
	 * @return array of successors, size=0 if no node matching the filter
	 * @throws InvalidSyntaxException
	 *             if syntax error ldapfilter
	 */
	Node[] connectedTo(String ldapFilter) throws CiliaIllegalParameterException,
			InvalidSyntaxException;

}
