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

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.administration.util.ParserUtils;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.ChainImpl;
import fr.liglab.adele.cilia.runtime.Const;

/**
 * CiliaRemoverProcessor: The processor class. Remove cilia chain instances,
 * chains, mediators, adapters, bindings.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaModifierProcessor {
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
	protected Data modify(Data data) {

		if ("chain".compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
			modifyChain(data);
		} else if ("mediator".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			modifyMediator(data);
		} else if ("adapter".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			modifyAdapter(data);
		} else if ("binding".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			modifyBinding(data);
		}

		return data;
	}

	/**
	 * Modifies a mediation chain.
	 * 
	 * @param data
	 *            the given data must contain the chain information (id). The
	 *            property "element" in data must be chain.
	 */
	private void modifyChain(Data data) {
		logger.error("Unsupported operation: Modify chain");
	}

	/**
	 * Modifies mediator properties.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void modifyMediator(Data data) {
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		Hashtable props = getProperties(data);
		if (props == null) {
			logger.error("Properties not found");
			return;
		}
		Builder builder = ccontext.getBuilder();
		Architecture chain;
		try {
			chain = builder.get(chainId);
			chain.configure().mediator().id(mediatorId).set(props);
			builder.done();
		} catch (CiliaException e) {
			logger.error("Command Error 'modify property mediator' [{}]",mediatorId);
			e.printStackTrace();
		}
		logger.info("Command 'modify property mediator' [{}]",mediatorId);
	}

	/**
	 * Modifies adapter properties.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id,
	 *            chain). The property "element" in data must be adapter.
	 */
	private void modifyAdapter(Data data) {
		Builder builder = ccontext.getBuilder();
		Architecture chain;
		String adapterId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		Hashtable props = getProperties(data);
		if (props == null) {
			logger.error("Properties not found");
			return;
		}
		try {
			chain = builder.get(chainId);
			chain.configure().adapter().id(adapterId).set(props);
			builder.done();
		} catch (CiliaException e) {
			logger.error("Command Error 'modify property mediator' [{}]",adapterId);
			e.printStackTrace();
		}
		logger.info ("Command 'modify property adapter' [{}]",adapterId);
	}

	/**
	 * Modifies binding properties.
	 * 
	 * @param data
	 *            the given data must contain the binding information (chain,
	 *            from, to). The property "element" in data must be binding.
	 */
	private void modifyBinding(Data data) {
		String to = String.valueOf(data.getProperty("to"));
		String from = String.valueOf(data.getProperty("from"));
		String chainId = String.valueOf(data.getProperty("chain"));
		Hashtable props = getProperties(data);
		logger.error("Unsupported operation: Modify bind");

	}


	private Hashtable getProperties(Data data) {
		Hashtable props = new Hashtable();
		String key = (String) data.getProperty("name");
		Object value = data.getProperty("value");
		if (key == null || value == null) {
			return null;
		}
		props.put(key, value);
		return props;
	}

}
