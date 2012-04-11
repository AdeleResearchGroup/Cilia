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
import fr.liglab.adele.cilia.framework.utils.Const;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.PatternType;

/**
 * CiliaCreatorProcessor: The processor class. Creates cilia chain instances,
 * chains, mediators, adapters, bindings.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaCreatorProcessor {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_ADAPTATION);
	
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
		try {
			ccontext.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try {
			if ("chain".compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
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
			}
		} finally {
			ccontext.getMutex().writeLock().release();
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
		Chain ch = new Chain(chainId, null, null, null);
		ccontext.addChain(ch);
	}

	/**
	 * Creates a mediator instance.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            type, chain). The property "element" in data must be mediator.
	 */
	private void createMediator(Data data) {
		Chain ch = null;
		Mediator med = null;
		String mediatorId = String.valueOf(data.getProperty("id"));
		String mediatorType = String.valueOf(data.getProperty("type"));
		String chainId = String.valueOf(data.getProperty("chain"));
		ch = ccontext.getChain(chainId);
		if (ch == null) {
			logger.error("Chain [{}] not found." + chainId);
			return;
		}
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
		med = new Mediator(mediatorId, type, namespace, null);
		ch.add(med);
		logger.info ("Command 'create mediator ' [{}]",med.getQualifiedId());
	}

	/**
	 * Creates an adapter instance.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id, type,
	 *            chain). The property "element" in data must be adapter.
	 */
	private void createAdapter(Data data) {
		Chain chain = null;
		Adapter adapter = null;
		String adapterId = String.valueOf(data.getProperty("id"));
		String adapterType = String.valueOf(data.getProperty("type"));
		String chainId = String.valueOf(data.getProperty("chain"));
		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("Chain [{}] not found." + chainId);
			return;
		}
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
		adapter = new Adapter(adapterId, type, namespace, null, PatternType.UNASSIGNED);
		chain.add(adapter);
		logger.info ("Command 'create mediator ' [{}]",adapter.getQualifiedId());
	}

	/**
	 * Creates a binding between two mediation components (mediator,adapter).
	 * 
	 * @param data
	 *            the given data must contain the binding information (chain,
	 *            from, to). The property "element" in data must be binding.
	 */
	private void createBinding(Data data) {
		Chain chain = null;
		MediatorComponent mediatorTo = null;

		MediatorComponent mediatorFrom = null;
		String to = String.valueOf(data.getProperty("to"));
		String from = String.valueOf(data.getProperty("from"));
		String chainId = String.valueOf(data.getProperty("chain"));
		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("Chain [{}] not found." + chainId);
			return;
		}
		if (to == null) {
			logger.error("Binding must have receiver component (to)");
			return;
		}
		if (from == null) {
			logger.error("Binding must have sender component (from)");
			return;
		}
		mediatorTo = getMediator(chain, to);
		mediatorFrom = getMediator(chain, from);
		if (mediatorTo == null) {
			logger.error("Component [{}] not found in chain [{}]",mediatorTo,chainId);
			return;
		}
		if (mediatorFrom == null) {
			logger.error("Component [{}] not found in chain [{}]",mediatorFrom,chainId);
			return;
		}
		chain.bind(mediatorFrom.getOutPort(getPortName(from)),
				mediatorTo.getInPort(getPortName(to)));
		logger.info ("Command 'create binding ' from [{}] to [{}]",to,from);

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

}
