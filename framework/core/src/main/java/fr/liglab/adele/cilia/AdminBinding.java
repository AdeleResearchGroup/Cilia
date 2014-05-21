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

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

import java.util.Map;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface AdminBinding {

    /**
     * Create a new binding between two components.
     *
     * @param chainId    The chain Id where the binding will be created.
     * @param from       The component which will deliver data. Parameter format must be
     *                   <componentID>:<portName>
     * @param to         The component which will obtain the data. Parameter format must be
     *                   <componentID>:<portName>
     * @param linker     To specifies the linker protocol to use to communicate two components.
     * @param properties The properties if needed to create the binding.
     * @throws CiliaInvalidSyntaxException    if from or to parameters are not well formed.
     * @throws CiliaIllegalParameterException If chain or any of the components does not exist.
     */

    void createBinding(String chainId, String from, String to, String linker, Map<String, Object> properties) throws CiliaIllegalParameterException, CiliaException;

    /**
     * Delete a binding from two mediators.
     *
     * @param chainID The chain where mediators are.
     * @param from    The component which deliver data. Parameter format must be
     *                <componentID>:<portName>
     * @param to      The component which receives data. Parameter format must be
     *                <componentID>:<portName>
     * @throws CiliaIllegalParameterException If any of the components does not exist.
     */
    void deleteBinding(String chainID, String from, String to) throws CiliaIllegalParameterException, CiliaException;

}
