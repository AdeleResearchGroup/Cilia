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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.runtime.Const;
/**
 * CiliaStarterProcessor: The processor class. Start cilia chain instances. 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaStopProcessor {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_ADAPTATION);
	/**
	 * The Cilia Context service, injected by iPOJO
	 */
	CiliaContainer ccontext;
	/**
	 * The main process method, this method is called by the cilia framework.
	 * @param data contains the parameters to start a cilia chain element instance.
	 * @return the same unchanged data.
	 */
	protected Data stop(Data data) {
		try {
			ccontext.getMutex().readLock().acquire();
		}
		catch (InterruptedException e) {}
		try {
		if ("chain".compareToIgnoreCase(String.valueOf(data.getProperty("element")))== 0){
			stopChain(String.valueOf(data.getProperty("id")));
		} else {
			logger.error("unknown parameters");
		}
		}
		finally {
			ccontext.getMutex().readLock().release();
		}
		return data;
	}
	/**
	 * Start a mediation chain.
	 * @param chainId The chain identifier to start.
	 */
	private void stopChain(String chainId) {
		Chain chain = ccontext.getChain(chainId);
		if (chain == null) {
			System.out.println("ChainImpl " + chainId + " Not found.");
			return;
		}
		logger.info("Command 'stop chain' [{}]",chainId);
		ccontext.stopChain(chainId);

	}
}

