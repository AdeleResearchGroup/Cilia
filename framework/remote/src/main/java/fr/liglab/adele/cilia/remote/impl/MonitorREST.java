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
package fr.liglab.adele.cilia.remote.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.json.JSONService;

import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.CiliaAdminService;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.RawData;
import fr.liglab.adele.cilia.SetUp;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.model.MediatorComponent;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */

@Component(name="remote-monitor-chain")
@Instantiate(name="remote-monitor-chain-0")
@Provides(specifications={MonitorREST.class})
@Path(value="/runtime")
public class MonitorREST {
	@Requires
	CiliaContext ccontext;
	
	@Requires
	CiliaAdminService admin;
	
	@Requires
	private JSONService jsonservice; //JsonService, in order to parse
	
	/**
	 * Retrieve a mediation chain state.
	 * @param id The ID of the chain  to retrieve 
	 * @return The required Chain state, 
	 */
	@GET
	@Path("{chainid}")
	@Produces("application/json")
	public Response chain(@PathParam("chainid") String chainid) {
		int state = 0;
		Date lastcommand = null;
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		Map<String, Object> reponse = new HashMap<String, Object>();
		try {
			state = runtime.getChainState(chainid);
			lastcommand = runtime.lastCommand(chainid);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		switch (state) {
		case ApplicationRuntime.CHAIN_STATE_IDLE:
			reponse.put("state", "idle");
			break;
		case ApplicationRuntime.CHAIN_STATE_STARTED:
			reponse.put("state", "started");
			break;
		case ApplicationRuntime.CHAIN_STATE_STOPPED:
			reponse.put("state", "stopped");
			break;
		default:
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		reponse.put("last command", lastcommand);
		return Response.ok(jsonservice.toJSON(reponse)).build();
	}
	
	/**
	 * Initialize a mediation chain.
	 * @param id The ID of the chain  to initialize 
	 * @return The required Chain state, 
	 */
	@PUT
	@Path("{chainid}/start")
	@Produces("application/json")
	public Response startChain(@PathParam("chainid") String chainid) {
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		try {
			runtime.startChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		return Response.ok().build();
	}
	/**
	 * Initialize a mediation chain.
	 * @param id The ID of the chain  to initialize 
	 * @return The required Chain state, 
	 */
	@PUT
	@Path("{chainid}/stop")
	@Produces("application/json")
	public Response stopChain(@PathParam("chainid") String chainid) {
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		try {
			runtime.stopChain(chainid);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		return Response.ok().build();
	}

	/**
	 * Get the monitor setup of a mediation component
	 * @param chainid The ID of the chain
	 * @param  id The component Id.
	 * @return The required Chain Setup Info
	 */
	@GET
	@Path("{chainid}/component/{id}/setup")
	@Produces("application/json")
	public Response getSetup(@PathParam("chainid") String chainid,@PathParam("id") String id) {
		SetUp setup = null;
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		MediatorComponent component = admin.getComponent(chainid, id);
		try {
			setup = runtime.nodeSetup(component);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		return Response.ok(jsonservice.toJSON(setup.toMap())).build();
	}
	
	/**
	 * Get the setup of a specific variable
	 * @param chainid The ID of the chain
	 * @param  id The component Id.
	 * @param  variable The variable to inspect.
	 * @return The variable setup of the specific mediator 
	 */
	@GET
	@Path("{chainid}/component/{id}/setup/{variable}")
	@Produces("application/json")
	public Response getSetup(@PathParam("chainid") String chainid,@PathParam("id") String id,@PathParam("variable") String variable) {
		SetUp setup = null;
		Map setupMap = null;
		Map variableMap = null;
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		MediatorComponent component = admin.getComponent(chainid, id);
		try {
			setup = runtime.nodeSetup(component);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		setupMap = setup.toMap();
		if (!setupMap.containsKey(variable)){
			return Response.status(404).build();
		}
		variableMap = (Map)setupMap.get(variable);
		return Response.ok(jsonservice.toJSON(variableMap)).build();
	}
	
	/**
	 * Get the monitor setup of a mediation component
	 * @param chainid The ID of the chain
	 * @param  id The component Id.
	 * @return The required Chain Setup Info
	 */
	@GET
	@Path("{chainid}/component/{id}/rawdata")
	@Produces("application/json")
	public Response getRawData(@PathParam("chainid") String chainid,@PathParam("id") String id) {
		RawData rawdata = null;
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		MediatorComponent component = admin.getComponent(chainid, id);
		try {
			rawdata = runtime.nodeRawData(component);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		return Response.ok(jsonservice.toJSON(rawdata.toMap())).build();
	}
	
	/**
	 * Get the setup of a specific variable
	 * @param chainid The ID of the chain
	 * @param  id The component Id.
	 * @param  variable The variable to inspect.
	 * @return The variable setup of the specific mediator 
	 */
	@GET
	@Path("{chainid}/component/{id}/rawdata/{variable}")
	@Produces("application/json")
	public Response getRawData(@PathParam("chainid") String chainid,@PathParam("id") String id,@PathParam("variable") String variable) {
		SetUp setup = null;
		Map setupMap = null;
		Map variableMap = null;
		ApplicationRuntime runtime = ccontext.getApplicationRuntime();
		MediatorComponent component = admin.getComponent(chainid, id);
		try {
			setup = runtime.nodeSetup(component);
		} catch (CiliaIllegalParameterException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (CiliaIllegalStateException e) {
			return Response.status(404).build();
		}
		setupMap = setup.toMap();
		if (!setupMap.containsKey(variable)){
			return Response.status(404).build();
		}
		variableMap = (Map)setupMap.get(variable);
		return Response.ok(jsonservice.toJSON(variableMap)).build();
	}
	
	
}
