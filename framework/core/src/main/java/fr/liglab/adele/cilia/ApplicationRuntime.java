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
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface ApplicationRuntime extends Topology, EventsConfiguration, ModelComponents {

    static final int CHAIN_STATE_IDLE = 0;
    static final int CHAIN_STATE_STARTED = 1;
    static final int CHAIN_STATE_STOPPED = 2;

    /**
     * @return list of chain Id
     */
    String[] getChainId();

    /**
     * gives the current chain state
     *
     * @param chainId
     * @return CHAIN_STATE_IDLE, CHAIN_STATE_STARTED, CHAIN_STATE_STOPPED
     * @throws CiliaIllegalParameterException ,CiliaIllegalStateException
     */
    int getChainState(String chainId) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * Return date and time last command [start,stop] level chain
     *
     * @param chainId
     * @return
     * @throws CiliaIllegalParameterException
     * @throws CiliaIllegalStateException
     */
    Date lastCommand(String chainId) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * start a chain
     *
     * @param chainId the chain ID to initialize
     * @return true if success, false if not.
     * @throws CiliaIllegalParameterException when the chain ID does not exist.
     */
    void startChain(String chainId) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * stop a chain
     *
     * @param chainId The chain id to stop.
     * @return true if success, false if not.
     * @throws CiliaIllegalParameterException when the chain ID does not exist.
     */
    void stopChain(String chainId) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * @param ldapFilter ldap filters, keywords {uuid,chain,node}
     * @return array of Setup , size is 0 if no node found matching the filter
     * @throws CiliaInvalidSyntaxException
     * @throws CiliaIllegalStateException
     */
    SetUp[] nodeSetup(String ldapFilter) throws CiliaIllegalParameterException,
            CiliaInvalidSyntaxException;

    /**
     * fast access using the node reference
     *
     * @param node
     * @return object Setup
     * @throws IllegalStateException          if the node is no more existing
     * @throws CiliaIllegalParameterException if the node is null
     * @throws CiliaIllegalStateException
     */
    SetUp nodeSetup(Node node) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * @param ldapFilter ldap filters, keywords {uuid,chain,node}
     * @return array of object raw data matching the ldap filter
     * @throws InvalidSyntaxException         if ldap syntax is not valid
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
     * @throws InvalidSyntaxException         if ldap syntax is not valid
     * @throws CiliaIllegalParameterException if the node is null
     * @throws CiliaIllegalStateException
     */
    RawData nodeRawData(Node node) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * @param ldapFilter ldap filters, keywords {uuid,chain,node}
     * @return array of object type Diagnostic matching the ldap filter
     * @throws InvalidSyntaxException         if ldap syntax is not valid
     * @throws CiliaIllegalParameterException
     * @throws CiliaInvalidSyntaxException
     */
    Thresholds[] nodeMonitoring(String ldapFilter)
            throws CiliaIllegalParameterException, CiliaInvalidSyntaxException;

    /**
     * fast access using the node reference
     *
     * @param node
     * @return object Diagnostics
     * @throws CiliaIllegalParameterException if the node is null
     * @throws CiliaIllegalStateException
     */
    Thresholds nodeMonitoring(Node node) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * Get a copy of the map buffered for the fiven mediator
     *
     * @param node the node to retrieve the data
     * @return the copy of the stocked data
     * @throws CiliaIllegalParameterException
     */
    Map getBufferedData(Node node) throws CiliaIllegalParameterException;


    /**
     * copy messages ( regular and stored)
     *
     * @param from The node from the buffered data will be copied.
     * @param to   The node to the buffered data will be copied.
     * @return true if succeed, false if not.
     */
    public boolean copyData(Node from, Node to) throws CiliaIllegalParameterException;

}
