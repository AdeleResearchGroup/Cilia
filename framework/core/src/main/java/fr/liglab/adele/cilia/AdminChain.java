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
/**
 *
 */
package fr.liglab.adele.cilia;

import fr.liglab.adele.cilia.exceptions.*;
import fr.liglab.adele.cilia.model.Chain;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface AdminChain {

    /**
     * Retrieve a mediation chain.
     *
     * @param id The ID of the chain  to retrieve
     * @return The required Chain,
     * return <code>null<code> if chain does not exist.
     * @throws CiliaIllegalParameterException
     */
    Chain getChain(String id) throws CiliaIllegalParameterException;

    /**
     * Create a new initial empty chain chain/
     *
     * @param id The ID of the new mediation chain.
     * @throws CiliaException if the given chain id already exist.
     */
    void createEmptyChain(String id) throws CiliaException;

    /**
     * Create a new mediation chain contained
     *
     * @param chain
     * @throws CiliaException
     */
    void createChain(String chain) throws CiliaException;

    /**
     * Load a mediation chain specified on the given URI string. The resource must be in XML format.
     *
     * @param URI The URI where the chain is located
     * @throws CiliaException       If Chain id exist.
     * @throws CiliaParserException If the XML containing the mediation chain is not well formed.
     */
    void loadChain(String URI) throws CiliaException, CiliaParserException;

    /**
     * It modifies the given mediation chain by using a set of reconfiguration instructions.
     *
     * @param id           The ID of the chain to modify
     * @param instructions The set of instructions to which will modify the mediation chain.
     * @return true if the reconfiguration is successful, false if the chain does not exist.
     * @throws CiliaException if there is any error on the instructions.
     */
    boolean updateChain(String id, String... instructions) throws CiliaException;

    /**
     * Copy the information of an existing component to another one.
     *
     * @param chainId     The chain Identification.
     * @param source      The id of the component source.
     * @param destination The id of the component destination.
     */
    void copyComponent(String chainId, String source, String destination) throws CiliaException;

    /**
     * Replace one component for another and copy his data.
     *
     * @param chainId The chain to modify.
     * @param from    the original component.
     * @param to      The destination component
     * @throws CiliaException
     */
    void replaceComponent(String chainId, String from, String to) throws CiliaException;

    /**
     * Delete a mediation chain.
     *
     * @param id The ID of the chain to be deleted
     * @return true if chain is successful deleted, false if it does not exist.
     * @throws CiliaIllegalStateException
     * @throws CiliaIllegalParameterException
     * @throws BuilderException
     * @throws BuilderPerformerException
     */
    boolean deleteChain(String id) throws CiliaIllegalParameterException, CiliaIllegalStateException, BuilderException, BuilderPerformerException;
}
