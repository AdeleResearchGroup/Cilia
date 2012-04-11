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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.utils.Const;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.ConstModel;
import fr.liglab.adele.cilia.model.Mediator;

public class CiliaCopierProcessor {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_ADAPTATION);
	/**
	 * The Cilia context.
	 */
	CiliaContext ccontext;

	BundleContext m_bundleContext;

	public CiliaCopierProcessor(BundleContext bc) {
		m_bundleContext = bc;
	}

	/**
	 * The main process method, this method is called by the cilia framework.
	 * 
	 * @param data
	 *            contains the parameters to remove a cilia chain element
	 *            instance.
	 * @return the same unchanged data.
	 */
	protected Data copy(Data data) {
		try {
			ccontext.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try {
			if ("mediator"
					.compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
				copyMediator(data);

			} else if ("adapter".compareToIgnoreCase(String.valueOf(data
					.getProperty("element"))) == 0) {
				copyAdapter(data);
			}
		} finally {
			ccontext.getMutex().writeLock().release();
		}
		return data;
	}

	private void copyMediator(Data data) {
		Chain chain = null;
		Mediator mediatorSource, mediatorDest;
		String chainId = String.valueOf(data.getProperty("chain"));
		String mediatorIdSource = String.valueOf(data.getProperty("from"));
		String mediatorIdDest = String.valueOf(data.getProperty("to"));
		chain = ccontext.getChain(chainId);

		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("Chain [{}] not found" + chainId);
			return;
		}
		mediatorSource = ccontext.getChain(chainId).getMediator(mediatorIdSource);
		if (mediatorSource == null) {
			logger.error("Component [{}] not found in chain [{}]",mediatorSource,chainId);
			return;
		}
		mediatorDest = ccontext.getChain(chainId).getMediator(mediatorIdDest);

		if (mediatorDest != null) {
			logger.error("Component [{}] in chain [{}]is already existing",mediatorSource,chainId);		
			return;
		}


		Dictionary properties = mediatorSource.getProperties();
		if (properties != null) {
			properties.remove(ConstModel.PROPERTY_COMPONENT_ID);
			properties.remove(ConstModel.PROPERTY_CHAIN_ID);
			properties.remove(ConstModel.PROPERTY_LOCK_UNLOCK);
		}
		mediatorDest = new Mediator(mediatorIdDest, mediatorSource.getType(),
				mediatorSource.getNamespace(), properties);
		
		if (mediatorDest != null) {
			chain.add(mediatorDest);
			logger.info("Command 'copy mediator' [{}] to [{}] ",mediatorSource.getQualifiedId(),mediatorDest.getQualifiedId());

		} else
			logger.error("Component [{}] not created",mediatorDest );
	}

	private void copyAdapter(Data data) {
		Chain chain = null;
		Adapter adapterSource, adapterDest;
		String chainId = String.valueOf(data.getProperty("chain"));
		String adapterIdSource = String.valueOf(data.getProperty("from"));
		String adapterIdDest = String.valueOf(data.getProperty("to"));
		chain = ccontext.getChain(chainId);

		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("Chain [{}] not found" + chainId);
			return;
		}
		adapterSource = ccontext.getChain(chainId).getAdapter(adapterIdSource);
		if (adapterSource == null) {
			logger.error("Component [{}] not found in chain [{}]",adapterIdSource,chainId);
			return;
		}
		adapterDest = ccontext.getChain(chainId).getAdapter(adapterIdDest);

		if (adapterDest != null) {
			logger.error("Component [{}] in chain [{}]is already existing",adapterDest,chainId);	
			return;
		}

		/* Convertion Dictionary to Properties */
		Dictionary dico = adapterSource.getProperties();
		Properties properties = new Properties();

		if (dico != null) {
			dico.remove(ConstModel.PROPERTY_COMPONENT_ID);
			dico.remove(ConstModel.PROPERTY_CHAIN_ID);
			dico.remove(ConstModel.PROPERTY_LOCK_UNLOCK);
			Enumeration e = dico.keys();
			while (e.hasMoreElements()) {
				Object key = (String) e.nextElement();
				Object value = dico.get(key);
				properties.put(key,value);
			}
		}
		adapterDest = new Adapter(adapterIdDest, adapterSource.getType(),
				adapterSource.getNamespace(), properties, adapterSource.getPattern());
		
		if (adapterDest != null) {
			chain.add(adapterDest);
			logger.info("Command 'copy adapter' [{}] to [{}] ",adapterSource.getQualifiedId(),adapterDest.getQualifiedId());

		} else
			logger.error("Component [{}] not created",adapterDest );
	}
}
