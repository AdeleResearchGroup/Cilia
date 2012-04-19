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

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.Port;
import fr.liglab.adele.cilia.administration.util.ParserUtils;
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
		try {
			ccontext.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try {
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
		} finally {
			ccontext.getMutex().writeLock().release();
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
		Chain ch = null;
		String chainId = String.valueOf(data.getProperty("id"));
		ch = ccontext.getChain(chainId);
		Hashtable prop = (Hashtable) getProperties(data);
		if (ch == null) {
			logger.error("ChainImpl [{}] not found." + chainId);
			return;
		}
		if (prop == null) {
			logger.error("Properties not found.");
			return;
		}
		ch.setProperties(prop);
	}

	/**
	 * Modifies mediator properties.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void modifyMediator(Data data) {
		Chain ch = null;
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		Mediator mediator;
		Hashtable props = getProperties(data);
		ch = ccontext.getChain(chainId);
		if (props == null) {
			logger.error("Properties not found");
			return;
		}
		if (ch == null) {
			logger.error("ChainImpl [{}] not found." + chainId);
			return;
		}
		if (mediatorId == null) {
			logger.error("Parameter 'id' must not be null");
			return;
		}
		mediator = ch.getMediator(mediatorId);
		if (mediator == null) {
			logger.error("ComponentImpl [{}] not found in chain [{}]",mediatorId,chainId);
			return;
		}
		logger.info ("Command 'modify property mediator' [{}]",mediator.getQualifiedId());
		mediator.setProperties(props);
	}

	/**
	 * Modifies adapter properties.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id,
	 *            chain). The property "element" in data must be adapter.
	 */
	private void modifyAdapter(Data data) {
		Chain ch = null;
		String adapterId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		Adapter adapter;
		Hashtable props = getProperties(data);
		ch = ccontext.getChain(chainId);
		if (props == null) {
			logger.error("Properties not found");
			return;
		}
		if (ch == null) {
			logger.error("ChainImpl [{}] not found." + chainId);
			return;
		}
		if (adapterId == null) {
			logger.error("Parameter id must not be null");
			return;
		}
		adapter = ch.getAdapter(adapterId);
		if (adapter == null) {
			logger.error("ComponentImpl [{}] not found in chain [{}]",adapterId,chainId);
			return;
		}
		logger.info ("Command 'modify property adapter' [{}]",adapter.getQualifiedId());
		adapter.setProperties(props);
	}

	/**
	 * Modifies binding properties.
	 * 
	 * @param data
	 *            the given data must contain the binding information (chain,
	 *            from, to). The property "element" in data must be binding.
	 */
	private void modifyBinding(Data data) {
		Chain chain = null;
		MediatorComponent mediatorTo = null;
		MediatorComponent mediatorFrom = null;
		String to = String.valueOf(data.getProperty("to"));
		String from = String.valueOf(data.getProperty("from"));
		String chainId = String.valueOf(data.getProperty("chain"));
		Hashtable props = getProperties(data);
		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("ChainImpl [{}] not found" + chainId);
			return;
		}
		if (to == null) {
			logger.error("BindingImpl must have receiver component (to)");
			return;
		}
		if (from == null) {
			logger.error("BindingImpl must have sender component (from)");
			return;
		}

		mediatorTo = getMediator(chain, to);
		mediatorFrom = getMediator(chain, from);
		if (mediatorTo == null) {
			logger.error("ComponentImpl " + to + " not found in " + chainId);
			return;
		}
		if (mediatorFrom == null) {
			logger.error("MediatorImpl [{}] not found.",mediatorFrom );
			return;
		}
		Binding bindings[] = mediatorFrom.getBinding(mediatorFrom
				.getOutPort(getPortName(from)));

		Port inport = mediatorFrom.getInPort(getPortName(to));

		for (int i = 0; bindings != null && i < bindings.length; i++) {
			Binding binding = bindings[i];
			if (binding.getTargetMediator().getId().compareTo(mediatorTo.getId()) == 0
					&& binding.getTargetPort().getName().compareTo(getPortName(to)) == 0) {
				chain.unbind(binding);
				binding.setProperties(props);
			}
		}
	}

	private MediatorComponent getMediator(Chain ch, String info) {
		MediatorComponent mediator;
		String sinfo[] = ParserUtils.split(info, ":");
		String fromMediatorId = null;
		if (sinfo.length == 2) {
			fromMediatorId = sinfo[0];
		} else {
			fromMediatorId = info;
		}
		mediator = ch.getMediator(fromMediatorId);
		if (mediator == null) { // see if there is an adapter with the same id.
			mediator = ch.getAdapter(fromMediatorId);
		}
		return mediator;
	}

	private String getPortName(String info) {
		String sinfo[] = ParserUtils.split(info, ":");
		String port = null;
		if (sinfo.length == 2) {
			port = sinfo[1];
		} else {
			port = "std";
		}
		return port;
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
