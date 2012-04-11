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

import java.io.File;
import java.net.URI;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaFileManager;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.utils.Const;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class CiliaLoadChainProcessor {

	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_ADAPTATION);

	private CiliaFileManager manager;

	public CiliaLoadChainProcessor(BundleContext context) {

	}

	public void start() {
	}

	public void stop() {

	}

	/**
	 * The main process method, this method is called by the cilia framework.
	 * 
	 * @param data
	 *            contains the parameters to start a cilia chain element
	 *            instance.
	 * @return the same unchanged data.
	 */
	protected Data load(Data data) {
		if ("load".compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
			loadChain(String.valueOf(data.getProperty("url")));
		} else if ("unload".compareToIgnoreCase(String.valueOf(data
				.getProperty("element"))) == 0) {
			unloadChain(String.valueOf(data.getProperty("url")));
		}

		return data;
	}

	/**
	 * Start a mediation chain.
	 * 
	 * @param chainId
	 *            The chain identifier to start.
	 */
	private void loadChain(String location) {
		URI uri = new File(location).toURI().normalize();
		String path = uri.getPath();
		File file = new File(path);
		if (!file.canRead()) {
			logger.error(" Unable to read the file [{}]" ,location);
			return;
		}
		manager.loadChain(file);
	}

	/**
	 * Start a mediation chain.
	 * 
	 * @param chainId
	 *            The chain identifier to start.
	 */
	private void unloadChain(String location) {
		URI uri = new File(location).toURI().normalize();
		String path = uri.getPath();
		File file = new File(path);
		if (!file.canRead()) {
			logger.error(" Unable to read the file [{}]" ,location);
			return;
		}
		manager.unloadChain(file);
	}

}
