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
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.impl.AdapterImpl;
import fr.liglab.adele.cilia.model.impl.ConstModel;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.util.FrameworkUtils;

public class CiliaCopierProcessor {
	private static final Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_ADAPTATION);
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

			copyMediator(data);

		return data;
	}

	private void copyMediator(Data data) {
		Architecture chain = null;
		String chainId = String.valueOf(data.getProperty("chain"));
		String mediatorIdSource = String.valueOf(data.getProperty("from"));
		String mediatorIdDest = String.valueOf(data.getProperty("to"));

		Builder builder = ccontext.getBuilder();

		try {
			chain = builder.get(chainId);

			chain.copy().id(mediatorIdSource).to(mediatorIdDest);

			builder.done();

		} catch (CiliaException e) {
			e.printStackTrace();
		}

	}

	
}
