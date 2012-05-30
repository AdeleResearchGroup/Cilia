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

package fr.liglab.adele.cilia.model.impl;

import java.util.Dictionary;

import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;

/**
 * This class is a mediator representation model.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class MediatorImpl extends MediatorComponentImpl implements Mediator {
	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param nspace
	 *            classname of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            ChainImpl where this mediator will be.
	 */

	public MediatorImpl(String id, String type, String nspace, String catego, String version,
			Dictionary properties, Chain chain) {
		super(id, type, nspace, catego, version, properties, chain);
		setChain(chain);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 */
	public MediatorImpl(String id, String type) {
		super(id, type, null, null, null, null, null);
	}

	/**
	 * Set the chain representation model which will contain this mediator.
	 * 
	 * @param chain
	 *            chain which will contain this mediator.
	 */
	public void setChain(Chain chain) {
		synchronized (lockObject) {
			super.chain = chain;
		}
	}

}
