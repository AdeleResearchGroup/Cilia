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

package fr.liglab.adele.cilia.knowledge.specification;

import java.util.Date;
import java.util.Dictionary;

import org.osgi.framework.InvalidSyntaxException;

import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.exceptions.IllegalParameterException;
import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.NodeRegistration;
import fr.liglab.adele.cilia.knowledge.Topology;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public interface Application extends Topology, NodeRegistration, ChainRegistration {


	/**
	 * @return list of chain Id
	 */
	String[] getChains();

	/**
	 * 
	 * @param chainId
	 * @return 0=IDLE, 1 = STARTED , 2 = STOPPED
	 */
	int getChainState(String chainId);
	
	/**
	 * Return last start 
	 * @param chainId
	 * @return
	 */
	Date lastStart(String chainId) ;

	/**
	 * 
	 * @param ldapFilter
	 *            define a node , ldap filters, keywords
	 * @return array of node matching the filter, array size 0 if no node
	 *         matching the filterá
	 */
	Node[] findByFilter(String ldapFilter) throws InvalidSyntaxException;

	/**
	 * 
	 * @param node
	 * @return Readonly properties or empty if no property found
	 * @throws IllegalParameterException
	 *             if null
	 *             
	 */
	Dictionary properties(Node node) throws IllegalParameterException,
			IllegalStateException;

	/**
	 * 
	 * @param node
	 * @return
	 * @throws IllegalParameterException
	 * @throws IllegalStateException
	 */
	MediatorComponent getModel(Node node) throws IllegalParameterException,
			IllegalStateException;
}
