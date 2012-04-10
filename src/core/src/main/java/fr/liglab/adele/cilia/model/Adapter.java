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
package fr.liglab.adele.cilia.model;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This class represent the adapter in the model at execution.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class Adapter extends MediatorComponent {
	/**
	 * The pattern this adapter has.
	 */
	private PatternType adapterPattern = PatternType.UNASSIGNED;
	/**
	 * 
	 * @param adapterId
	 * @param adapterType
	 * @param adapterProperties
	 * @param unassigned
	 */
	public Adapter(String adapterId, String adapterType, String adapterNamespace,
			Dictionary adapterProperties, PatternType unassigned) {
		super(adapterId, adapterType, adapterNamespace,adapterProperties);
		adapterPattern = unassigned;
	}
	
	public Adapter(String id, String type, String nspace,
			Dictionary properties, Chain chain) {
		super(id, type, nspace, null, properties, chain);
		
	}

	/**
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param classname
	 *            classname of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 */
	public Adapter(String id, String type, String namespace, Dictionary properties) {
		super(id, type, namespace, null, properties, null);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 */
	public Adapter(String id, String type, Dictionary properties) {
		super(id, type, null, null, properties, null);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            Chain where this mediator will be.
	 */
	public Adapter(String id, String type, Dictionary properties, Chain chain) {
		super(id, type, null, null, properties, chain);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            Chain where this mediator will be.
	 */
	public Adapter(String id, String type, Chain chain) {
		super(id, type, new Hashtable(), chain);
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
	public Adapter(String id, String type) {
		super(id, type, new Hashtable(), null);
	}
	
	/**
	 * Get the pattern associated to the Adapter.
	 * @return The pattern.
	 */
	public PatternType getPattern() {return adapterPattern;}
	
	
	/**
	 * Set the chain representation model which will contain this mediator.
	 * 
	 * @param chain
	 *            chain which will contain this mediator.
	 */
	public void setChain(Chain chain) {
		synchronized (lockObject) {
			if (chain != null) {
				//chain.add(this);
				setQualifiedId(Component.buildQualifiedId(getId(),chain.getId()));
			}
			this.chain = chain;
		}
	}
	
}
