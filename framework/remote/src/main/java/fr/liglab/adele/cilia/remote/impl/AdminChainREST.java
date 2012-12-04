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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.json.JSONService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.AdminBinding;
import fr.liglab.adele.cilia.AdminChain;
import fr.liglab.adele.cilia.AdminComponent;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.util.Const;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@Component(name="remote-admin-chain")
@Instantiate(name="remote-admin-chain-0")
@Provides(specifications={AdminChainREST.class})
@Path(value="/")
public class AdminChainREST {

	@Requires
	AdminChain admin;

	@Requires
	CiliaContext ccontext;

	@Requires
	AdminComponent acomponent;

	@Requires
	AdminBinding abinding;

	@Requires
	private JSONService jsonservice; //JsonService, in order to parse

	protected static Logger coreLogger = LoggerFactory.getLogger(Const.LOGGER_CORE);

	/*****************************************/
	/**          GET METHODS                **/
	/*****************************************/

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getChainNames() {
		Map result = new Hashtable();
		boolean atLeastOne = false;
		StringBuffer chainsIds = new StringBuffer("{ \nchains: [");
		for (String id : ccontext.getApplicationRuntime().getChainId()) {
			chainsIds.append(id.trim());
			chainsIds.append(", ");
			atLeastOne = true;
		}
		if (atLeastOne) {
			chainsIds.delete(chainsIds.length()-2,chainsIds.length());
		}
		chainsIds.append("]\n}");
		return chainsIds.toString();
	}

	/**
	 * Retrieve a mediation chain.
	 * @param id The ID of the chain  to retrieve 
	 * @return The required Chain, 
	 * return <code>null<code> if chain does not exist.
	 * @throws ParseException 
	 */
	@GET
	@Path("{chainid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response chain(@PathParam("chainid") String chainid) {
		if (chainid == null || chainid.length()<1 || chainid.compareToIgnoreCase("cilia")==0){
			return Response.ok(getChainNames()).build();
		}
		Chain chain;
		try {
			chain = admin.getChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			chain = null;
		}
		if (chain == null) {
			return Response.status(404).build();
		} else {
			return Response.ok(jsonservice.toJSON(chain.toMap())).build();
		}

	}


	/**
	 * Retrieve the list of mediation components
	 * @param id The ID of the chain  to retrieve 
	 * @return The list of mediation components, 
	 * return <code>null<code> if chain does not exist.
	 * @throws ParseException 
	 */
	@GET
	@Path("{chainid}/components")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllComponents(@PathParam("chainid") String chainid) {
		Chain chain;
		try {
			chain = admin.getChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			chain = null;
		}
		if (chain == null) {
			return Response.status(404).build();
		}
		Map result = new Hashtable();
		result.put("Adapters", getSetId(chain.getAdapters()));
		result.put("Mediators", getSetId(chain.getMediators()));
		return Response.ok(String.valueOf(jsonservice.toJSON(result))).build();

	}

	/**
	 * Retrieve the list of mediators
	 * @param id The ID of the chain  to retrieve 
	 * @return The list of mediation components, 
	 * return <code>null<code> if chain does not exist.
	 * @throws ParseException 
	 */
	@GET
	@Path("{chainid}/mediators")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMediators(@PathParam("chainid") String chainid) {
		Chain chain;
		try {
			chain = admin.getChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			chain = null;
		}
		if (chain == null) {
			return Response.status(404).build();
		}
		Map result = new Hashtable();
		result.put("Mediators", getSetId(chain.getMediators()));
		return Response.ok(String.valueOf(jsonservice.toJSON(result))).build();
	}

	/**
	 * Retrieve the list of mediators
	 * @param id The ID of the chain  to retrieve 
	 * @return The list of mediation components, 
	 * return <code>null<code> if chain does not exist.
	 * @throws ParseException 
	 */
	@GET
	@Path("{chainid}/adapters")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAdapters(@PathParam("chainid") String chainid) {
		Chain chain;
		try {
			chain = admin.getChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			chain = null;
		}
		if (chain == null) {
			return Response.status(404).build();
		}
		Map result = new Hashtable();
		result.put("Adapters", getSetId(chain.getAdapters()));
		return Response.ok(String.valueOf(jsonservice.toJSON(result))).build();
	}

	/**
	 * Retrieve the list of mediators
	 * @param id The ID of the chain  to retrieve 
	 * @return The list of mediation components, 
	 * return <code>null<code> if chain does not exist.
	 * @throws ParseException 
	 */
	@GET
	@Path("{chainid}/bindings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllBindings(@PathParam("chainid") String chainid) {
		Chain chain;
		StringBuilder sb = new StringBuilder("{");
		try {
			chain = admin.getChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			chain = null;
		}
		if (chain == null) {
			return Response.status(404).build();
		}
		sb.append("\"Bindings\" : ").append(chain.getBindings()).append("}");

		return Response.ok(String.valueOf(sb)).build();
	}

	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	@GET
	@Path("{chainid}/components/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComponent(@PathParam("chainid")String chainid, @PathParam("componentId")String componentId) {
		Response reponse;
		MediatorComponent component;
		try {
			component = acomponent.getComponent(chainid, componentId);
		} catch (CiliaIllegalParameterException e1) {
			component = null;
		}
		if (component != null) {
			Map map = component.toMap();
			String tojson = jsonservice.toJSON(map);
			System.out.println(component);
			System.out.println(map);
			System.out.println(tojson);
			reponse =  Response.ok(tojson).build();
		} else { // Maybe it is an uuid
			try {
				Node []nodes = ccontext.getApplicationRuntime().findNodeByFilter("(uuid=" + componentId+")");
				if (nodes.length > 0) {
					reponse =  Response.ok(jsonservice.toJSON(((MediatorComponent)(nodes[0])).toMap())).build();
				} else {
					reponse = Response.status(404).build();
				}
			} catch (CiliaIllegalParameterException e) {
				reponse = Response.status(404).build();
			} catch (CiliaInvalidSyntaxException e) {
				reponse = Response.status(Status.BAD_REQUEST).build();
			}
		}
		return reponse;
	}

	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	@GET
	@Path("{chainid}/mediators/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMediator(@PathParam("chainid")String chainid, @PathParam("componentId")String componentId) {
		Response reponse;
		Mediator component;
		try {
			component = admin.getChain(chainid).getMediator(componentId);
		} catch (CiliaIllegalParameterException e1) {
			component = null;
		}
		if (component != null) {
			reponse =  Response.ok(jsonservice.toJSON(component.toMap())).build();
		} else { // Maybe it is an uuid
			try {
				Node []nodes = ccontext.getApplicationRuntime().findNodeByFilter("(uuid=" + componentId+")");
				if (nodes.length > 0) {
					if (admin.getChain(chainid).getMediator(nodes[0].nodeId()) != null) { //See if the uuid node is an adapter
						reponse =  Response.ok(jsonservice.toJSON(((MediatorComponent)(nodes[0])).toMap())).build();
					} else {
						reponse = Response.status(404).build();	
					}
				} else {
					reponse = Response.status(404).build();
				}
			} catch (CiliaIllegalParameterException e) {
				reponse = Response.status(404).build();
			} catch (CiliaInvalidSyntaxException e) {
				reponse = Response.status(Status.BAD_REQUEST).build();
			}
		}
		return reponse;
	}

	/**
	 * Retrieve a mediation component.
	 * @param chainid The chain where the component is.
	 * @param componentId The id of the component
	 * @return The required component, null if it does not exist.
	 */
	@GET
	@Path("{chainid}/adapters/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAdapter(@PathParam("chainid")String chainid, @PathParam("componentId")String componentId) {
		Response reponse;
		Adapter component;
		try {
			component = admin.getChain(chainid).getAdapter(componentId);
		} catch (CiliaIllegalParameterException e1) {
			component = null;
		}
		if (component != null) {
			reponse =  Response.ok(jsonservice.toJSON(component.toMap())).build();
		} else { // Maybe it is an uuid
			try {
				Node []nodes = ccontext.getApplicationRuntime().findNodeByFilter("(uuid=" + componentId+")");
				if (nodes.length > 0) {
					if (admin.getChain(chainid).getAdapter(nodes[0].nodeId()) != null) { //See if the uuid node is an adapter
						reponse =  Response.ok(jsonservice.toJSON(((MediatorComponent)(nodes[0])).toMap())).build();
					} else {
						reponse = Response.status(404).build();	
					}
				} else {
					reponse = Response.status(404).build();
				}
			} catch (CiliaIllegalParameterException e) {
				reponse = Response.status(404).build();
			} catch (CiliaInvalidSyntaxException e) {
				reponse = Response.status(Status.BAD_REQUEST).build();
			}
		}
		return reponse;
	}

	/*****************************************/
	/**          PUT METHODS                **/
	/*****************************************/

	
	/**
	 * Copy the information of an existing component to another one.
	 * @param chainId The chain Identification.
	 * @param source The id of the component source.
	 * @param destination The id of the component destination.
	 */
	@PUT
	@Path("{chainid}/copy")
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public String copyComponentPUT(@PathParam("chainid")String chainId, @FormParam("from")String source, @FormParam("to")String destination) {
		coreLogger.warn("Using a deprecated RESOURCE");
		coreLogger.warn("use URL/cilia/<Chain>/components/ with command=copy, from=<mediatorId> and to=<mediatorId> as parameters instead");
		try {
			admin.copyComponent(chainId, source, destination);
		} catch (CiliaException e) {
			e.printStackTrace();
			return "{ return : false, " +
			"exception : " + e.getMessage() +
			"}";
		}
		return "{ return : true}";
	}

	/**
	 * Replace one component for another and copy his data.
	 * @param chainId The chain to modify.
	 * @param from the original component.
	 * @param to The destination component
	 * @throws CiliaException
	 */
	@PUT
	@Path("{chainid}/replace")
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public String replaceComponentPUT(@PathParam("chainid")String chainId, @FormParam("from")String from, @FormParam("to")String to) {
		coreLogger.warn("Using a deprecated RESOURCE");
		coreLogger.warn("use URL/cilia/<Chain>/components/ with command=replace, from=<mediatorId> and to=<mediatorId> as parameters instead");
		try {
			admin.replaceComponent(chainId, from, to);
		} catch (CiliaException e) {
			e.printStackTrace();
			return "{ return : false, " +
			"exception : " + e.getMessage() +
			"}";
		}
		return "{ return : true }";	
	}

	@PUT
	@Path("{chainid}/components")
	@Produces(MediaType.APPLICATION_JSON)
	public Response PUTCommand(@PathParam("chainid")String chainId, @FormParam("command")String command, @FormParam("from")String from, @FormParam("to")String to) {

		if (command != null && command.compareToIgnoreCase("copy") == 0) {
			try {
				admin.copyComponent(chainId, from, to);
			} catch (CiliaException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} else if (command != null && command.compareToIgnoreCase("replace") == 0) {
			try {
				admin.replaceComponent(chainId, from, to);
			} catch (CiliaException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			return Response.status(Status.FORBIDDEN).build();
		}
		return Response.ok().build();
	}

	/**
	 * Update a mediator component instance.
	 * @param chainId The chain id where the component is located.
	 * @param componentId The ID of the component to reconfigure.
	 * @param properties The new properties
	 */
	@PUT
	@Path("{chainid}/components/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateComponentPUT(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId, @FormParam("properties")String properties) {
		System.out.println("Will update component " + componentId);
		System.out.println("Properties " + properties);
		if (properties == null || properties.length()<1){
			return Response.status(Status.BAD_REQUEST).build();
		}
		Map props = null;
		try {
			props = jsonservice.fromJSON(properties);
		} catch (ParseException e1) {
			e1.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}

		try {
			if (acomponent.isMediator(chainId, componentId) == true) {
				acomponent.updateMediator(chainId, componentId, props);
			} else if (acomponent.isAdapter(chainId, componentId) == true){
				acomponent.updateAdapter(chainId, componentId, props);
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		} catch (CiliaException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok().build();	
	}
	
	/*****************************************/
	/**          POST METHODS               **/
	/*****************************************/

	
	/**
	 * Create a new initial empty chain chain/
	 * @param id The ID of the new mediation chain.
	 * @throws CiliaException if the given chain id already exist.
	 */
	@POST
	@Path("{chainid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response emptyChain(@PathParam("chainid")String id) {
		Chain chain = null;
		try {
			chain = admin.getChain(id);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(400).build();
		}
		if (chain != null) {
			return Response.status(Status.CONFLICT).entity("{result : \"chain with the same id exist w\"}").build();
		} 
		try {
			admin.createEmptyChain(id);
		} catch (CiliaException e) {
			return Response.status(Status.BAD_REQUEST).entity("{result : \"Unable to create chain \"}").build();
		}
		try {
			chain = admin.getChain(id);
		} catch (CiliaIllegalParameterException e) {
		}
		return Response.ok(String.valueOf(chain)).build();
	}




	/**
	 * Create a new mediator component
	 * @param chainId The chain where the mediator will be created.
	 * @param componentType The mediator type.
	 * @param componentID The id of the mediator.
	 * @param properties The initial properties.
	 * @throws CiliaIllegalParameterException If the chain does not exist or if the mediator with the same ID already exist.
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("{chainid}/mediators/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMediator(@PathParam("chainid")String chainId, @FormParam("type")String componentType, @PathParam("componentId")String componentID, @FormParam("properties")String  properties) {
		Map<String, Object> prop = null;
		System.out.println("Creating mediator");
		System.out.println(properties);
		StringBuilder result = new StringBuilder();
		Response response;
		if (properties != null && properties.length()>2){
			try {
				prop = jsonservice.fromJSON(properties);
			} catch (Exception e) {
				e.printStackTrace();
				result.append(createMessage(chainId, componentID, "Unable to create mediator"));
				result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
				response = Response.status(Status.BAD_REQUEST).entity(result.toString()).build();
				return response;
			}
		} 
		try {
			acomponent.createMediator(chainId, componentType, componentID, prop);
		} catch (CiliaException e) {
			e.printStackTrace();
			result.append(createMessage(chainId, componentID, "Unable to create mediator"));
			result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
			response = Response.status(Status.BAD_REQUEST).entity(result.toString()).build();
			return response;
		}
		result.append("{ \"result\" : \"Mediator created successfully\"}");
		response = Response.ok(result.toString()).build();
		return response;
	}

	/**
	 * Create a new adapter component
	 * @param chainId The chain where the adapter will be created.
	 * @param componentType The adapter type.
	 * @param componentID The id of the adaper.
	 * @param properties The initial properties.
	 * @throws CiliaIllegalParameterException If the chain does not exist or if the adapter with the same ID already exist.
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("{chainid}/adapters/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAdapter(@PathParam("chainid")String chainId, @FormParam("type")String componentType, @PathParam("componentId")String componentID, @FormParam("properties")String  properties) {
		Map<String, Object> prop = null;
		StringBuilder result = new StringBuilder();
		Response response;
		
		if (properties != null && properties.length()>2){
			try {
				prop = jsonservice.fromJSON(properties);
			}catch(Exception e){
				e.printStackTrace();
				result.append(createMessage(chainId, componentID, "Unable to create adapter"));
				result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
				response = Response.status(Status.BAD_REQUEST).entity(result.toString()).build();
				return response;
			}

		}
		try {
			acomponent.createAdapter(chainId, componentType, componentID, prop);
		} catch (CiliaException e) {
			e.printStackTrace();
			result.append(createMessage(chainId, componentID, "Unable to create adapter"));
			result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
			response = Response.status(Status.BAD_REQUEST).entity(result.toString()).build();
			return response;
		}
		result.append("{ \"result\" : \"Adapter created successfully\"}");
		response = Response.ok(result.toString()).build();
		return response;
	}





	/**
	 * Create a new binding between two components.
	 * @param chainId The chain Id where the binding will be created.
	 * @param from The component which will deliver data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @param to The component which will obtain the data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @param properties The properties if needed to create the binding.
	 */
	@POST
	@Path("{chainid}/bindings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBinding(@PathParam("chainid")String chainId, @FormParam("from")String from, @FormParam("to")String to, @FormParam("linker")String linker, @FormParam("properties")String properties) {
		System.out.println("Will create binding");
		Map<String, Object> prop = null;
		Response response;
		StringBuilder result = new StringBuilder("{");

		if (properties != null && properties.length()>2){
			try {
				prop = jsonservice.fromJSON(properties);
			} catch (ParseException e) {
				e.printStackTrace();
				result.append(createMessage(chainId, from, "Unable to parse properties"));
				result.append("exception :").append(e.getMessage());
				result.append("}");
				response = Response.status(Status.BAD_REQUEST).entity(result.toString()).build();
				return response;
			}
		}
		try {
			abinding.createBinding(chainId, from, to, linker, prop);
		} catch (CiliaException e) {
			e.printStackTrace();
			result.append(createMessage(chainId, from, "Unable to bind components"));
			result.append("exception :").append(e.getMessage());
			result.append("}");
			response = Response.status(Status.BAD_REQUEST).entity(result.toString()).build();
			return response;
		}
		result.append(createMessage(chainId, from, "Binding successful")).append("}");
		
		response = Response.ok(result.toString()).build();
		return response;
	}

	/*****************************************/
	/**          DELETE METHODS             **/
	/*****************************************/

	/**
	 * Delete a mediation chain. 
	 * @param id The ID of the chain to be deleted
	 * @return true if chain is successful deleted, false if it does not exist.
	 */
	@DELETE
	@Path("{chainid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteChain(@PathParam("chainid")String id) {
		boolean res = false;
		try {
			if (admin.getChain(id) == null) {
				return Response.status(Status.NOT_FOUND).build();
			}
			res = admin.deleteChain(id);
		} catch (CiliaException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build(); 
		}
		if (res){
			return Response.ok().build();
		}
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}



	/**
	 *Delete a mediator component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	@DELETE
	@Path("{chainid}/components/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteComponent(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId){
		Response response = null;
		if (acomponent.isMediator(chainId, componentId) == true) {
			acomponent.deleteMediator(chainId, componentId);
			response = Response.ok().build();
		} else if (acomponent.isAdapter(chainId, componentId) == true) {
			acomponent.deleteAdapter(chainId, componentId);
			response = Response.ok().build();
		} else {
			response = Response.status(Status.NOT_FOUND).build();
		}
		return response;
	}



	/**
	 *Delete a mediator component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	@DELETE
	@Path("{chainid}/mediators/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteMediator(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId){
		Response response = null;
		if (acomponent.isMediator(chainId, componentId) == true) {
			acomponent.deleteMediator(chainId, componentId);
			response = Response.ok().build();
		} else {
			response = Response.status(Status.NOT_FOUND).build();
		}
		return response;
	}
	
	/**
	 *Delete a mediator component. 
	 * @param chainId The chain where the component is.
	 * @param componentId The id to the component to delete.
	 * @return true if component is deleted, false if not.
	 */
	@DELETE
	@Path("{chainid}/adapters/{componentId}")
	public Response deleteAdapter(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId){
		Response response = null;
		if (acomponent.isAdapter(chainId, componentId) == true) {
			acomponent.deleteAdapter(chainId, componentId);
			response = Response.ok().build();
		} else {
			response = Response.status(Status.NOT_FOUND).build();
		}
		return response;
	}
	/**
	 * Delete a binding from two mediators.
	 * @param chainID The chain where mediators are.
	 * @param from The component which deliver data. Parameter format must be 
	 * 			<componentID>:<portName>
	 * @param to The component which receives data. Parameter format must be 
	 * 			<componentID>:<portName>
	 */
	@DELETE
	@Path("{chainid}/bindings")
	public Response deleteBinding(@PathParam("chainid")String chainId, @QueryParam("from")String from, @QueryParam("to")String to) {
		System.out.println("Remove binding" + from);
		try {
			abinding.deleteBinding(chainId, from, to);
		} catch (CiliaException e) {
			e.printStackTrace();
			return  Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok().build();
	}

	public String createMessage(String chainId, String componentId, String result){
		StringBuilder resultString = new StringBuilder();
		resultString.append("\"chain\" : \"").append(chainId).append("\",\n");
		resultString.append("\"ID\" : \"").append(componentId).append("\",\n");
		resultString.append("\"result\" : \"").append(result).append("\",\n");
		return resultString.toString();
	}


	private List getSetId(Set set ){
		List<String> ids = new ArrayList<String>();
		Iterator<MediatorComponent> it = set.iterator();
		while(it.hasNext()){
			ids.add(it.next().getId());
		}
		return ids;
	}
}
