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

import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.Port;
import fr.liglab.adele.cilia.administration.util.ParserUtils;
import fr.liglab.adele.cilia.model.ComponentImpl;
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
		try {
			ccontext.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try {
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
		} finally {
			ccontext.getMutex().writeLock().release();
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
		Chain ch = ccontext.getChain(chainId);
		if (ch == null) {
			logger.error("ChainImpl [{}] not found.", chainId);
			return;
		}
		logger.info("Command 'remove chain' [{}]", chainId);
		ccontext.removeChain(chainId);
	}

	/**
	 * Removes a mediator instance.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void removeMediator(Data data) {
		Chain ch = null;
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		ch = ccontext.getChain(chainId);
		if (ch == null) {
			logger.error("ChainImpl [{}] not found.", chainId);
			return;
		}
		if (mediatorId == null) {
			logger.error("MediatorImpl must have an id");
			return;
		}
		if (ch.removeMediator(mediatorId)) {
			logger.info("Command 'remove mediator' [{}]",
					ComponentImpl.buildQualifiedId(chainId, mediatorId));
		} else {
			logger.error("Command 'remove mediator' [{}], mediator not removed",
					ComponentImpl.buildQualifiedId(chainId, mediatorId));
		}
	}

	/**
	 * Removes an adapter instance.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void removeAdapter(Data data) {
		Chain ch = null;
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		ch = ccontext.getChain(chainId);
		if (ch == null) {
			logger.error("ChainImpl [{}] not found.", chainId);
			return;
		}
		if (mediatorId == null) {
			logger.error("AdapterImpl must have an id");
			return;
		}
		if (ch.removeAdapter(mediatorId)) {
			logger.info("Command 'remove adapter' [{}]",
					ComponentImpl.buildQualifiedId(chainId, mediatorId));
		} else {
			logger.error("Command 'remove adapter' [{}], adapter not removed",
					ComponentImpl.buildQualifiedId(chainId, mediatorId));
		}
	}

	/**
	 * Removes a binding between two mediation components (mediator,adapter).
	 * 
	 * @param data
	 *            the given data must contain the binding information (chain,
	 *            from, to). The property "element" in data must be binding.
	 */
	private void removeBinding(Data data) {
		Chain chain = null;
		MediatorComponent mediatorTo = null;
		MediatorComponent mediatorFrom = null;
		String to = String.valueOf(data.getProperty("to"));
		String from = String.valueOf(data.getProperty("from"));
		String chainId = String.valueOf(data.getProperty("chain"));
		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("ChainImpl [{}] not found.", chainId);
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
		logger.info("Command 'remove binding' from [{}] to [{}]",
				mediatorFrom.getQualifiedId(), mediatorTo.getQualifiedId());

		if (mediatorTo == null) {
			logger.error("ComponentImpl [{}] not found in chain [{}]",to,chainId);
			return;
		}
		if (mediatorFrom == null) {
			logger.error("ComponentImpl [{}] not found in chain [{}]",from,chainId);
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
		if (mediator == null) {
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

}
