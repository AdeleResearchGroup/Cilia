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
package fr.liglab.adele.cilia.remote.impl;

import java.util.Hashtable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.json.JSONService;

import fr.liglab.adele.cilia.AdminComponent;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.model.MediatorComponent;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
//@Component(name="remote-admin-component")
//@Instantiate(name="remote-admin-component-0")
//@Provides(specifications={AdminComponentREST.class})
@Path(value="{chainid}")
public class AdminComponentREST {

	//@Requires
	AdminComponent admin;
	
	//@Requires
	private JSONService jsonservice; //JsonService, in order to parse
	
	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	@GET
	@Path("{componentId}")
	@Produces("application/json")
	public String getComponent(@PathParam("chainid")String chainid, @PathParam("componentId")String componentId) {
		System.out.println("");
		MediatorComponent component = admin.getComponent(chainid, componentId);
		StringBuffer result = new StringBuffer();
		if (component != null) {
			result.append(component);
		} else {
			result.append("{ chain : ").append(chainid).append(",\n");
			result.append(componentId).append(" : Does not exist}");
		}
		return result.toString();
	}
	
	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentType The mediator type.
	 * @param componentID The id of the mediator.
	 * @param properties The initial properties.
	 * @throws CiliaIllegalParameterException If the chain does not exist or if the mediator with the same ID already exist.
	 */
	public String createMediator(String chainId, String componentType, String componentID, Hashtable<String, Object> properties) throws CiliaException {
		return null;
	}
	
	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentType The adapter type.
	 * @param componentID The id of the adapter.
	 * @param properties The initial properties.
	 * @throws CiliaIllegalParameterException If the chain does not exist or if the adapter with the same ID already exist.
	 */
	public String createAdapter(String chainId, String componentType, String componentID, Hashtable<String, Object> properties) throws CiliaException {
		return null;
	}
	
	/**
	 * Update a mediator component instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	public String updateMediator(String chainId, String componentId, Hashtable<String, Object> properties) throws CiliaException {
		return null;
	}
	
	/**
	 * Update an adapter component instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 * @throws CiliaException. If the chain or the component does not exist.
	 */
	public String updateAdapter(String chainId, String componentId, Hashtable<String, Object> properties) throws CiliaException {
		return null;
	}
	/**
	 *Delete a mediator component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	public String deleteMediator(String chainId, String componentId) {
		return null;
	}
	
	/**
	 *Delete an adapter component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	public String deleteAdapter(String chainId, String componentId) {
		return null;
	}
}
