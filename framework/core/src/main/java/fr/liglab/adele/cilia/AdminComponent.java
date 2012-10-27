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

import java.util.Map;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public interface AdminComponent {
	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 * @throws CiliaIllegalParameterException 
	 */
	MediatorComponent getComponent(String chainid, String componentId) throws CiliaIllegalParameterException;
	
	
	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 * @throws CiliaIllegalParameterException 
	 */
	Mediator getMediator(String chainid, String componentId) throws CiliaIllegalParameterException;
	
	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	Adapter getAdapter(String chainid, String componentId);
	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentType The mediator type.
	 * @param componentID The id of the mediator.
	 * @param properties The initial properties.
	 * @throws CiliaIllegalParameterException If the chain does not exist or if the mediator with the same ID already exist.
	 */
	void createMediator(String chainId, String componentType, String componentID, Map<String, Object> properties) throws CiliaException ;
	
	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentType The adapter type.
	 * @param componentID The id of the adapter.
	 * @param properties The initial properties.
	 * @throws CiliaIllegalParameterException If the chain does not exist or if the adapter with the same ID already exist.
	 */
	void createAdapter(String chainId, String componentType, String componentID, Map<String, Object> properties) throws CiliaException ;
	
	/**
	 * Update a mediator component instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	void updateMediator(String chainId, String componentId, Map<String, Object> properties) throws CiliaException;
	
	/**
	 * Update an adapter component instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	void updateAdapter(String chainId, String componentId, Map<String, Object> properties) throws CiliaException;
	/**
	 *Delete a mediator component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	boolean deleteMediator(String chainId, String componentId);
	
	/**
	 *Delete an adapter component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	boolean deleteAdapter(String chainId, String componentId);
	
	boolean isMediator(String chainId, String componentId);
	
	boolean isAdapter(String chainId, String componentId);
}
