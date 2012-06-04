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

import java.util.Dictionary;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;


/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes" })
public interface ApplicationSpecification extends Topology, NodeRegistration, ChainRegistration {

	/**
	 * @return list of chain Id
	 */
	String[] getChainId();
	
	/**
	 * Unmodifiable properties
	 * 
	 * @param node
	 *            retreive the properties
	 * @return unmodiable dictionary, or empty dictionary 
	 * @throws CiliaIllegalParameterException
	 *             , wrong parameter
	 * @throws CiliaIllegalStateException
	 *             , the node object doesn't exsit           
	 */
	Dictionary properties(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param node
	 * @return Mediator component model
	 * @throws CiliaIllegalParameterException
	 *             , wrong parameter
	 * @throws CiliaIllegalStateException
	 *             , the node object doesn't
	 */
	MediatorComponent getModel(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;
	
	/**
	 * 
	 * @param chainId
	 * @return Chain model
	 * @throws CiliaIllegalParameterException
	 */
	Chain getChain(String chainId) throws CiliaIllegalParameterException;
	
}
