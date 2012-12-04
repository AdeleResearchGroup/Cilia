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
package fr.liglab.adele.cilia.admin.impl;

import java.util.Map;

import org.w3c.dom.Node;

import fr.liglab.adele.cilia.AdminBinding;
import fr.liglab.adele.cilia.AdminChain;
import fr.liglab.adele.cilia.AdminComponent;
import fr.liglab.adele.cilia.CiliaAdminService;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.framework.data.XmlTools;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.util.ChainParser;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class CiliaAdminServiceImpl implements CiliaAdminService, AdminChain, AdminComponent, AdminBinding  {

	CiliaContext ccontext;

	ChainParser parser;

	/**
	 * Retrieve a mediation chain.
	 * @param id The ID of the chain  to retrieve 
	 * @return The required Chain, 
	 * return <code>null<code> if chain does not exist.
	 * @throws CiliaIllegalParameterException 
	 */
	public Chain getChain(String chainId) throws CiliaIllegalParameterException {
		Chain chain = null;
		chain = ccontext.getApplicationRuntime().getChain(chainId);
		return chain;
	}

	/**
	 * Create a new initial empty chain chain/
	 * @param id The ID of the new mediation chain.
	 * @throws BuilderException 
	 * @throws BuilderPerformerException 
	 * @throws CiliaException if the given chain id already exist.
	 */
	public void createEmptyChain(String id) throws BuilderException, BuilderPerformerException  { 
		Builder builder = ccontext.getBuilder();
		builder.create(id);
		builder.done();
	}

	/**
	 * Create a new mediation chain contained 
	 * @param chain
	 * @throws CiliaException
	 */
	public void createChain(String chain) throws CiliaException  {
		Builder builders[] = null;
		Node node = XmlTools.stringToNode(chain);
		builders = parser.parseChains(node);
		for (Builder b: builders){
			b.done();
		}
	}
	/**
	 * Load a mediation chain specified on the given URI string. The resource must be in XML format.
	 * @param URI The URI where the chain is located
	 * @throws CiliaException If Chain id exist.
	 * @throws CiliaParserException If the XML containing the mediation chain is not well formed.
	 */
	public void loadChain(String url) throws CiliaException, CiliaParserException {
		Builder builders[] = null;
		Node node = XmlTools.urlToNode(url).getFirstChild();
		builders = parser.parseChains(node);
		for (Builder b: builders){
			b.done();
		}
	}

	/**
	 * It modifies the given mediation chain by using a set of reconfiguration instructions.
	 * @param id The ID of the chain to modify
	 * @param instructions The set of instructions to which will modify the mediation chain.
	 * @throws CiliaException if there is any error on the instructions.
	 * @return true if the reconfiguration is successful, false if the chain does not exist. 
	 */
	public boolean updateChain(String id, String...instructions) throws CiliaException {
		throw new CiliaException("Unsupported operation");
	}

	/**
	 * Delete a mediation chain. 
	 * @param id The ID of the chain to be deleted
	 * @return true if chain is successful deleted, false if it does not exist.
	 * @throws CiliaIllegalStateException 
	 * @throws CiliaIllegalParameterException 
	 * @throws BuilderException 
	 * @throws BuilderPerformerException 
	 */
	public boolean deleteChain(String id) throws CiliaIllegalParameterException, CiliaIllegalStateException, BuilderException, BuilderPerformerException {
		Builder b = null;
		ccontext.getApplicationRuntime().stopChain(id);
		b = ccontext.getBuilder();
		b.remove(id);
		b.done();

		return true;
	}

	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 * @throws CiliaIllegalParameterException 
	 */
	public MediatorComponent getComponent(String chainid, String componentId) throws CiliaIllegalParameterException{
		Chain chain = null;
		MediatorComponent component = null;
		chain = ccontext.getApplicationRuntime().getChain(chainid);
		if (chain == null) {
			return null;
		}
		component = chain.getMediator(componentId);
		if (component == null) {
			component = chain.getAdapter(componentId);
		}
		return component;
	}


	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 * @throws CiliaIllegalParameterException 
	 */
	public Mediator getMediator(String chainid, String componentId) throws CiliaIllegalParameterException{
		Chain chain = null;
		Mediator component = null;
		chain = ccontext.getApplicationRuntime().getChain(chainid);
		if (chain == null) {
			return null;
		}
		component = chain.getMediator(componentId);
		return component;
	}

	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	public Adapter getAdapter(String chainid, String componentId){
		Chain chain = null;
		Adapter component = null;
		try {
			chain = ccontext.getApplicationRuntime().getChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
		}
		if (chain == null) {
			return null;
		}
		component = chain.getAdapter(componentId);
		return component;
	}
	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentID The id of the mediator.
	 * @param componentType The component type in the form <namespace>:<type>
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist. 
	 */
	public void createMediator(String chainId, String componentType, String componentID, Map<String, Object> properties) throws CiliaException {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;
		chain = builder.get(chainId);
		chain.create().mediator().type(componentType).id(componentID).configure().set(properties);
		builder.done();
	}

	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentID The id of the mediator.
	 * @param componentType The component type in the form <namespace>:<type>
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist. 
	 */
	public void createMediator(String chainId, String componentType,String componentID) throws CiliaException {
		createMediator(chainId, componentType, componentID, null) ;
	}
	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentID The id of the adapter.
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist.
	 */

	public void createAdapter(String chainId, String componentType, String componentID, Map<String, Object> properties) throws CiliaException {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;
		chain = builder.get(chainId);
		chain.create().adapter().type(componentType).id(componentID).configure().set(properties);
		builder.done();
	}

	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentID The id of the adapter.
	 * @param properties The initial properties.
	 * @throws CiliaException If the chain does not exist or if the adapter with the same ID already exist.
	 */
	public void createAdapter(String chainId, String componentType, String componentID) throws CiliaException {
		createAdapter(chainId, componentType, componentID, null);
	}	
	/**
	 * Update a mediatorcomponent instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	public void updateMediator(String chainId, String componentId,Map properties) throws CiliaException {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;
		chain = builder.get(chainId);
		chain.configure().mediator().id(componentId).set(properties);
		builder.done();
	}
	/**
	 * Update an adapter instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	public void updateAdapter(String chainId, String componentId, Map properties) throws CiliaException {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;
		chain = builder.get(chainId);
		chain.configure().adapter().id(componentId).set(properties);
		builder.done();
	}



	private boolean deleteComponent(String chainId, String componentId, boolean isMediator) {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;
		try {
			chain = builder.get(chainId);
			if (isMediator){
				chain.remove().mediator().id(componentId);
			} else {
				chain.remove().adapter().id(componentId);
			}
			builder.done();
		} catch (CiliaException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}



	/**
	 *Delete a component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	public boolean deleteMediator(String chainId, String componentId) {
		return deleteComponent(chainId, componentId,true);
	}



	/**
	 *Delete a component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	public boolean deleteAdapter(String chainId, String componentId) {
		return deleteComponent(chainId, componentId,false);
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
	public void createBinding(String chainId, String from, String to, String linker,
			Map<String, Object> properties)
					throws CiliaIllegalParameterException, CiliaException {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;

		try {
			chain = builder.get(chainId);
		} catch (BuilderException e) {
			throw new CiliaIllegalParameterException("Unable to locate chain:" + chainId);
		}
		try {
			chain.bind().using(linker).from(from).to(to).configure(properties);
		} catch (CiliaException e) {
			throw new CiliaIllegalParameterException("Ilegal Parameters exception:" + e.getMessage());
		}

		try {
			builder.done();
		} catch (CiliaException e) {
			throw e;
		}

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
	public void deleteBinding(String chainId, String from,String to)
			throws CiliaIllegalParameterException, CiliaException {
		Builder builder = ccontext.getBuilder();
		Architecture  chain = null;

		try {
			chain = builder.get(chainId);
		} catch (BuilderException e) {
			throw new CiliaIllegalParameterException("Unable to locate chain:" + chainId);
		}
		try {
			chain.unbind().from(from).to(to);
		} catch (CiliaException e) {
			throw new CiliaIllegalParameterException("Ilegal Parameters exception:" + e.getMessage());
		}

		try {
			builder.done();
		} catch (CiliaException e) {
			throw e;
		}
	}



	/**
	 * Initialize a mediation chain.
	 * @param chainId
	 * @return
	 */
	public void startChain(String chainId) throws CiliaIllegalParameterException {
		if (ccontext.getApplicationRuntime().getChain(chainId) == null) {
			throw new CiliaIllegalParameterException ("Chain null or not exist:" + chainId); 
		}
		try {
			ccontext.getApplicationRuntime().startChain(chainId);
		} catch (CiliaIllegalParameterException e) {
			throw e;
		} catch (CiliaIllegalStateException e) {
			throw new CiliaIllegalParameterException(e.getMessage());
		}
	}

	/**
	 * Stop a mediation chain
	 * @param chainId
	 * @return
	 */
	public void stopChain(String chainId) throws CiliaIllegalParameterException{
		if (ccontext.getApplicationRuntime().getChain(chainId) == null) {
			throw new CiliaIllegalParameterException ("Chain null or not exist:" + chainId); 
		}
		try {
			ccontext.getApplicationRuntime().stopChain(chainId);
		} catch (CiliaIllegalParameterException e) {
			throw e;
		} catch (CiliaIllegalStateException e) {
			throw new CiliaIllegalParameterException(e.getMessage());
		}
	}



	/**
	 * Copy the information of an existing component to another one.
	 * @param chainId The chain Identification.
	 * @param source The id of the component source.
	 * @param destination The id of the component destination.
	 */
	public void copyComponent(String chainId, String source, String destination) throws CiliaException {
		Architecture chain = null;
		Builder builder = ccontext.getBuilder();

		chain = builder.get(chainId);

		chain.copy().id(source).to(destination);

		builder.done();

	}

	/**
	 * Replace one component for another and copy his data.
	 * @param chainId The chain to modify.
	 * @param from the original component.
	 * @param to The destination component
	 * @throws CiliaException
	 */
	public void replaceComponent(String chainId, String from, String to)
			throws CiliaException {
		Architecture chain = null;
		Builder builder = ccontext.getBuilder();

		chain = builder.get(chainId);

		chain.replace().id(from).to(to);

		builder.done();

	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.AdminComponent#isMediator(java.lang.String, java.lang.String)
	 */
	public boolean isMediator(String chainId, String componentId) {
		Chain chain = null;
		try {
			chain = ccontext.getApplicationRuntime().getChain(chainId);
		} catch (CiliaIllegalParameterException e) {
			return false;
		}
		if (chain.getMediator(componentId) != null){
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.AdminComponent#isAdapter(java.lang.String, java.lang.String)
	 */
	public boolean isAdapter(String chainId, String componentId) {
		Chain chain = null;
		try {
			chain = ccontext.getApplicationRuntime().getChain(chainId);
		} catch (CiliaIllegalParameterException e) {
			return false;
		}
		if (chain.getAdapter(componentId) != null){
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}
}
