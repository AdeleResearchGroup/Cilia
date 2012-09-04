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
package fr.liglab.adele.cilia.admin.impl;

import java.util.Hashtable;
//import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.admin.CiliaAdminService;
import fr.liglab.adele.cilia.admin.util.ParserUtils;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class CiliaAdminServiceGogo {

	CiliaAdminService admin;
	
	CiliaContext ccontext;
	
	/**
	 * Retrieve a mediation chain.
	 * @param id The ID of the chain  to retrieve 
	 * @return The required Chain, 
	 * return <code>null<code> if chain does not exist.
	 */
	//@Descriptor("Shows the information of a chain.")
	public String chain(/*@Descriptor("The chain ID")*/String chainId) {
		Chain chain = admin.getChain(chainId);
		return String.valueOf(chain);
	}

	/**
	 * Retrieve a mediation chain.
	 * @param id The ID of the chain  to retrieve 
	 * @return The required Chain, 
	 * return <code>null<code> if chain does not exist.
	 */
	//@Descriptor("Shows the chains.")
	public String[] chains() {
		return ccontext.getApplicationRuntime().getChainId();
	}
	
	/**
	 * Create a new initial empty chain chain/
	 * @param id The ID of the new mediation chain.
	 * @throws CiliaException if the given chain id already exist.
	 */
	//@Descriptor("Creates an empty mediation chain.")
	public void emptyChain(/*@Descriptor("The chain ID")*/ String id) throws CiliaException { 
		admin.createEmptyChain(id);
	}

	/**
	 * Create a new mediation chain contained 
	 * @param chain
	 * @throws CiliaException
	 */
	//@Descriptor("Creates a chain using XML format")
	public void createChain(/*@Descriptor("The chain in XML format")*/String chain) throws CiliaException {
		admin.createChain(chain);
	}
	/**
	 * Load a mediation chain specified on the given URI string. The resource must be in XML format.
	 * @param URI The URI where the chain is located
	 * @throws CiliaException If Chain id exist.
	 * @throws CiliaParserException If the XML containing the mediation chain is not well formed.
	 */
	//@Descriptor("Loads a chain")
	public void loadChain(/*@Descriptor("The URL where the chain is located")*/String url) throws CiliaException, CiliaParserException {
		admin.loadChain(url);
	}



	/**
	 * Delete a mediation chain. 
	 * @param id The ID of the chain to be deleted
	 * @return true if chain is successful deleted, false if it does not exist.
	 */
	//@Descriptor("Detele a mediation chain")
	public boolean deleteChain(/*@Descriptor("The chain ID")*/ String id) {
		return admin.deleteChain(id);
	}


	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	//@Descriptor("Get the mediator information")
	public String mediator(/*@Descriptor("The chain ID")*/String chainid, /*@Descriptor("The mediator ID")*/String componentId){
		Mediator mediator = admin.getMediator(chainid, componentId);
		return String.valueOf(mediator);
	}

	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	//@Descriptor("Get the adapter information")
	public String adapter(/*@Descriptor("The chain ID")*/String chainid,/*@Descriptor("The adapter ID")*/String componentId){
		Adapter component = admin.getAdapter(chainid, componentId);
		return String.valueOf(component);
	}
	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentID The id of the mediator.
	 * @param componentType The component type in the form <namespace>:<type>
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist. 
	 */
	//@Descriptor("Create a new mediator")
	public void createMediator(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The component type in the form <namespace>:<type>")*/String componentType, /*@Descriptor("The component ID")*/String componentID, String properties) throws CiliaException {
		Hashtable<String, Object> props = null;
		if (properties != null && properties.length()>2) {
			props = ParserUtils.getProperties(properties);
		}
		admin.createMediator(chainId, componentType, componentID, props);
	}
	
	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentID The id of the mediator.
	 * @param componentType The component type in the form <namespace>:<type>
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist. 
	 */
	//@Descriptor("Create a new mediator")
	public void createMediator(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The component type in the form <namespace>:<type>")*/String componentType, /*@Descriptor("The component ID")*/String componentID) throws CiliaException {
		try{
		createMediator(chainId, componentType, componentID, null) ;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentID The id of the adapter.
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist.
	 */
	//@Descriptor("Create a new Adapter")
	public void createAdapter(/*@Descriptor("The chain Id")*/String chainId,/* @Descriptor("The adapter type in the form <namespace>:<type>")*/String componentType, /*@Descriptor("The ID of the new adapter")*/String componentID, /*@Descriptor("The properties of the new adaper")*/String properties) throws CiliaException {
		Hashtable<String, Object> props = null;
		if (properties != null && properties.length()>2) {
			props = ParserUtils.getProperties(properties);
		}
		admin.createAdapter(chainId, componentType, componentID, props);
	}

	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentID The id of the adapter.
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist.
	 */
	//@Descriptor("Create a new Adapter")
	public void createAdapter(/*@Descriptor("The chain Id")*/String chainId, /*@Descriptor("The adapter type in the form <namespace>:<type>")*/String componentType, /*@Descriptor("The ID of the new adapter")*/String componentID) throws CiliaException {
		createAdapter(chainId, componentType, componentID, null);
	}	
	/**
	 * Update a mediatorcomponent instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	//@Descriptor("Update mediator properties")
	public void updateMediator(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The mediator ID")*/String componentId, /*@Descriptor("The new properties")*/String  properties) throws CiliaException {
		admin.updateMediator(chainId, componentId, ParserUtils.getProperties(properties));
	}
	/**
	 * Update an adapter instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	//@Descriptor("Update mediator properties")
	public void updateAdapter(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The adapter ID")*/String componentId, /*@Descriptor("The new properties")*/ String properties) throws CiliaException {
		admin.updateAdapter(chainId, componentId, ParserUtils.getProperties(properties));
	}





	/**
	 *Delete a component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	//@Descriptor("Delete a mediator component")
	public boolean deleteMediator(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The mediator ID")*/String componentId) {
		return admin.deleteMediator(chainId, componentId);
	}



	/**
	 *Delete a component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	//@Descriptor("Delete an adapter component")
	public boolean deleteAdapter(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The component ID")*/String componentId) {
		return admin.deleteAdapter(chainId, componentId);
	}



	/**
	 * Create a new binding between two components.
	 * @param chainId The chain Id where the binding will be created.
	 * @param from The component which will deliver data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @param to The component which will obtain the data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @param properties The properties if needed to create the binding.
	 * @throws CiliaInvalidSyntaxException if from or to parameters are not well formed.
	 * @throws CiliaIllegalParameterException If chain or any of the components does not exist.
	 */
	//@Descriptor("Create a new Binding between two components")
	public void createBinding(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The source mediator and its port in the form <mediatorID>:<port>")*/String from, /*@Descriptor("The target mediator and its port in the form <mediatorID>:<port>")*/String to,
			/*@Descriptor("The new properties")*/String properties)
					throws CiliaIllegalParameterException, CiliaException {
		admin.createBinding(chainId, from, to, ParserUtils.getProperties(properties));

	}


	//@Descriptor("Create a new Binding between two components")
	public void createBinding(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The source mediator and its port in the form <mediatorID>:<port>")*/String from, /*@Descriptor("The target mediator and its port in the form <mediatorID>:<port>")*/String to)
					throws CiliaIllegalParameterException, CiliaException {
		admin.createBinding(chainId, from, to, null);

	}

	/**
	 * Delete a binding from two mediators.
	 * @param chainID The chain where mediators are.
	 * @param from The component which deliver data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @param to The component which receives data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @throws CiliaIllegalParameterException If any of the components does not exist.
	 */
	//@Descriptor("Deletes a binding between two components")
	public void deleteBinding(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The sender mediator in the form <mediatorId>:<port>")*/String from, /*@Descriptor("The receiver mediator in the form <mediatorId>:<port>")*/String to)
			throws CiliaIllegalParameterException, CiliaException {
		admin.deleteBinding(chainId, from, to);
	}



	/**
	 * Initialize a mediation chain.
	 * @param chainId
	 * @return
	 */
	//@Descriptor("Start a mediation chain")
	public void startChain(/*@Descriptor("The chain ID")*/String chainId) throws CiliaIllegalParameterException {
		admin.startChain(chainId);
	}

	/**
	 * Stop a mediation chain
	 * @param chainId
	 * @return
	 */
	//@Descriptor("Stop a mediation chain ")
	public void stopChain(/*@Descriptor("The chain ID")*/String chainId) throws CiliaIllegalParameterException{
		admin.stopChain(chainId);
	}



	/**
	 * Copy the information of an existing component to another one.
	 * @param chainId The chain Identification.
	 * @param source The id of the component source.
	 * @param destination The id of the component destination.
	 */
	//@Descriptor("Copy the information of one mediator to another")
	public void copyComponent(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The mediator source")*/String source, /*@Descriptor("The mediator target")*/String destination) throws CiliaException {
		admin.copyComponent(chainId, source, destination);
	}

	/**
	 * Replace one component for another and copy his data.
	 * @param chainId The chain to modify.
	 * @param from the original component.
	 * @param to The destination component
	 * @throws CiliaException
	 */
	//@Descriptor("Replaces a mediator component for another")
	public void replaceComponent(/*@Descriptor("The chain ID")*/String chainId, /*@Descriptor("The mediator source")*/String from, /*@Descriptor("The mediator target")*/String to)
			throws CiliaException {
		admin.replaceComponent(chainId, from, to);
		
	}
}
