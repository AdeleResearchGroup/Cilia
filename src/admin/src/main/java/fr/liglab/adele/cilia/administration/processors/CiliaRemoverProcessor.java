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
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.ChainImpl;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.runtime.Const;

/**
 * CiliaRemoverProcessor: The processor class. Remove cilia chain instances,
 * chains, mediators, adapters, bindings.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaRemoverProcessor {

	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_ADAPTATION);

	/**
	 * The Cilia context.
	 */
	CiliaContext ccontext;

	/**
	 * The main process method, this method is called by the cilia framework.
	 * 
	 * @param data
	 *            contains the parameters to remove a cilia chain element
	 *            instance.
	 * @return the same unchanged data.
	 */
	protected Data remove(Data data) {
		if ("chain".compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
			removeChain(String.valueOf(data.getProperty("id")));
		} else if ("mediator".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			removeMediator(data);
		} else if ("adapter".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			removeAdapter(data);
		} else if ("binding".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			removeBinding(data);
		}
		return data;
	}

	/**
	 * Removes a mediation chain.
	 * 
	 * @param chainId
	 *            The chain identifier.
	 */
	private void removeChain(String chainId) {
		Builder builder = ccontext.getBuilder();
		try {
			builder.remove(chainId);
			builder.done();
		} catch (CiliaException e) {
			logger.error("Command Error 'remove chain' [{}]", chainId);
			e.printStackTrace();
		}
		logger.info("Command 'remove chain' [{}]", chainId);
	}

	/**
	 * Removes a mediator instance.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void removeMediator(Data data) {
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		if (mediatorId == null) {
			logger.error("MediatorImpl must have an id");
			return;
		}
		Builder builder = ccontext.getBuilder();
		try {
			builder.get(chainId).remove().mediator().id(mediatorId);
			builder.done();
		} catch (CiliaException e) {
			logger.error("Command Error'remove mediator' [{}]",
					ComponentImpl.buildQualifiedId(chainId, mediatorId));
			e.printStackTrace();
			return;
		}
		logger.info("Command 'remove mediator' [{}]",
				ComponentImpl.buildQualifiedId(chainId, mediatorId));
	}

	/**
	 * Removes an adapter instance.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void removeAdapter(Data data) {
		String adapterId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		if (adapterId == null) {
			logger.error("AdapterImpl must have an id");
			return;
		}
		try{
			Builder builder = ccontext.getBuilder();
			builder.get(chainId).remove().adapter().id(adapterId);
			builder.done();
		} catch (CiliaException e) {
			logger.error("Command Error'remove adapter' [{}]",
					ComponentImpl.buildQualifiedId(chainId, adapterId));
			e.printStackTrace();
			return;
		}
		logger.info("Command 'remove adapter' [{}]",
				ComponentImpl.buildQualifiedId(chainId, adapterId));
	}

	/**
	 * Removes a binding between two mediation components (mediator,adapter).
	 * 
	 * @param data
	 *            the given data must contain the binding information (chain,
	 *            from, to). The property "element" in data must be binding.
	 */
	private void removeBinding(Data data) {
		String to = String.valueOf(data.getProperty("to"));
		String from = String.valueOf(data.getProperty("from"));
		String chainId = String.valueOf(data.getProperty("chain"));

		Builder builder = ccontext.getBuilder();
		try {
			builder.get(chainId).unbind().from(from).to(to);
			builder.done();
		} catch (CiliaException e) {
			logger.info("Command Error 'remove binding ' from [{}] to [{}]", to, from);
			e.printStackTrace();
			return;
		} 
		logger.info("Command 'remove binding ' from [{}] to [{}]", to, from);	
	}
}
