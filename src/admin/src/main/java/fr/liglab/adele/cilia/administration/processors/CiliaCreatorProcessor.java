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
package fr.liglab.adele.cilia.administration.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.administration.util.ParserUtils;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.runtime.Const;

/**
 * CiliaCreatorProcessor: The processor class. Creates cilia chain instances,
 * chains, mediators, adapters, bindings.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaCreatorProcessor {
	private static final Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_ADAPTATION);

	/**
	 * The Cilia Context to interact with the framework.
	 */
	CiliaContext ccontext;

	/**
	 * The main process method, this method is called by the cilia framework.
	 * 
	 * @param data
	 *            contains the parameters to create a cilia chain element
	 *            instance.
	 * @return the same unchanged data.
	 */
	protected Data create(Data data) {

		if ("chain".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			createChain(String.valueOf(data.getProperty("id")));
		} else if ("mediator".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			createMediator(data);
		} else if ("adapter".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			createAdapter(data);
		} else if ("binding".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			createBinding(data);
		} else {
			System.out.println("Any available options" + data);
		}
		return data;
	}

	/**
	 * Creates a mediation chain.
	 * 
	 * @param chainId
	 *            The chain identifier.
	 */
	private void createChain(String chainId) {
		Builder builder = ccontext.getBuilder();
		try {
			builder.create(chainId);
			builder.done();
		} catch (BuilderException e) {
			e.printStackTrace();
		} catch (BuilderPerformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a mediator instance.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            type, chain). The property "element" in data must be mediator.
	 */
	private void createMediator(Data data) {
		
		String mediatorId = (String) data.getProperty("id");
		String mediatorType = (String) data.getProperty("type");
		String chainId = (String) data.getProperty("chain");
		
		
		if (mediatorId == null) {
			logger.error("Parameter 'id' must not be null");
			return;
		}
		if (mediatorType == null) {
			logger.error("Parameter 'type' must not be null");
			return;
		}
		String mediatorInfo[] = ParserUtils.split(mediatorType, ":");
		String type = null;
		String namespace = null;
		if (mediatorInfo.length == 2) {
			namespace = mediatorInfo[0];
			type = mediatorInfo[1];
		} else {
			type = mediatorType;
		}

		Builder builder = ccontext.getBuilder();
		try {
			Architecture chain = builder.get(chainId);
			chain.create().mediator().type(type).namespace(namespace).id(mediatorId);
			builder.done();
		} catch (BuilderConfigurationException e) {
			logger.error("Command Error 'create mediator ' [{}]", mediatorId);
			e.printStackTrace();
		} catch (BuilderException e) {
			e.printStackTrace();
		} catch (BuilderPerformerException e) {
			e.printStackTrace();
		}

		logger.info("Command 'create mediator ' [{}]", mediatorId);
	}

	/**
	 * Creates an adapter instance.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id, type,
	 *            chain). The property "element" in data must be adapter.
	 */
	private void createAdapter(Data data) {
		
		String adapterId = (String) data.getProperty("id");
		String adapterType = (String) data.getProperty("type");
		String chainId = (String) data.getProperty("chain");
		
		if (adapterId == null) {
			logger.error("Parameter 'id' must not be null");
			return;
		}
		if (adapterType == null) {
			logger.error("Parameter 'type' must not be null");
			return;
		}
		String mediatorInfo[] = ParserUtils.split(adapterType, ":");
		String type = null;
		String namespace = null;
		if (mediatorInfo.length == 2) {
			namespace = mediatorInfo[0];
			type = mediatorInfo[1];
		} else {
			type = adapterType;
		}
		Builder builder = ccontext.getBuilder();
		
		Architecture chain;
		try {
			chain = builder.get(chainId);
			chain.create().adapter().type(type).namespace(namespace).id(adapterId);
			builder.done();
		} catch (BuilderException e) {
			logger.error("Command Error 'create adapter ' [{}]", adapterId);
			e.printStackTrace();
		} catch (BuilderConfigurationException e) {
			logger.error("Command Error 'create adapter ' [{}]", adapterId);
			e.printStackTrace();
		} catch (BuilderPerformerException e) {
			logger.info("Command Error 'create adapter ' [{}]", adapterId);
			e.printStackTrace();
		}
		logger.info("Command 'create mediator ' [{}]", adapterId);
	}

	/**
	 * Creates a binding between two mediation components (mediator,adapter).
	 * 
	 * @param data
	 *            the given data must contain the binding information (chain,
	 *            from, to). The property "element" in data must be binding.
	 */
	private void createBinding(Data data) {
		
		String to = String.valueOf(data.getProperty("to"));
		String from = String.valueOf(data.getProperty("from"));
		String chainId = String.valueOf(data.getProperty("chain"));
		
		Builder builder = ccontext.getBuilder();
		try {
			Architecture chain = builder.get(chainId);
			chain.bind().from(from).to(to);
			builder.done();
		} catch (CiliaException e) {
			logger.error("Command Erro 'create binding ' from [{}] to [{}]", to, from);
			e.printStackTrace();
		} 
		
		logger.info("Command 'create binding ' from [{}] to [{}]", to, from);

	}
}
