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

package fr.liglab.adele.cilia.dynamic;

import java.util.Date;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeRegistration;
import fr.liglab.adele.cilia.Topology;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

/**
 * Class Runtime properties
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ApplicationRuntime extends Topology, NodeRegistration,
		MeasuresRegistration {
	/**
	 * @return list of chain Id
	 */
	String[] getChains();

	/**
	 * 
	 * @param chainId
	 * @return 0=IDLE, 1 = STARTED , 2 = STOPPED
	 * @throws CiliaIllegalParameterException
	 *             ,CiliaIllegalStateException
	 */
	int getChainState(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	
	/**
	 * Return last Command start or stop level chain
	 * 
	 * @param chainId
	 * @return
	 * @throws CiliaIllegalParameterException
	 * @throws CiliaIllegalStateException
	 */
	Date lastCommand(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;


	/**
	 * @throws CiliaInvalidSyntaxException
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return array of Setup , size is 0 if no node found matching the filter
	 */
	SetUp[] nodeSetup(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException;

	/**
	 * fast access using the node reference
	 * 
	 * @param node
	 * @return object Setup
	 * @throws IllegalStateException
	 *             if the node is no more existing
	 * @throws CiliaIllegalParameterException
	 *             if the node is null
	 * @throws CiliaIllegalStateException
	 */
	SetUp nodeSetup(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return array of object raw data matching the ldap filter
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 * @throws CiliaIllegalParameterException
	 * @throws CiliaInvalidSyntaxException
	 */
	RawData[] nodeRawData(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException;

	/**
	 * fast access using the node reference
	 * 
	 * @param node
	 * @return object providing raw data
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 * @throws CiliaIllegalParameterException
	 *             if the node is null
	 * @throws CiliaIllegalStateException
	 */
	RawData nodeRawData(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param ldapFilter
	 *            ldap filters, keywords {uuid,chain,node}
	 * @return array of object type Diagnostic matching the ldap filter
	 * @throws InvalidSyntaxException
	 *             if ldap syntax is not valid
	 * @throws CiliaIllegalParameterException
	 * @throws CiliaInvalidSyntaxException
	 */
	Thresholds[] nodeMonitoring(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException;

	/**
	 * fast access using the node reference
	 * 
	 * @param node
	 * @return object Diagnostics
	 * @throws CiliaIllegalParameterException
	 *             if the node is null
	 * @throws CiliaIllegalStateException
	 */
	Thresholds nodeMonitoring(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;
	
	/**
	 * initializes a chain.
	 * @param chainId the chain ID to initialize
	 * @return true if success, false if not.
	 * @throws CiliaIllegalParameterException when the chain ID does not exist.
	 */
	boolean start(String chainId) throws CiliaIllegalParameterException;
	
	/**
	 * Stops a mediation chain.
	 * @param chainId The chain id to stop.
	 * @return true if success, false if not.
	 * @throws CiliaIllegalParameterException when the chain ID does not exist.
	 */
	boolean stop(String chainId) throws CiliaIllegalParameterException;

}
