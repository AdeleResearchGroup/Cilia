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
/**
 * This class is the Element representation model.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public abstract class InternalComponent extends ComponentImpl {
	/**
	 * Reference to the mediator which contain this element. 
	 */
	private volatile MediatorComponentImpl mediator;
	
	
	
	private final Object lockObject = new Object(); 
	
	public InternalComponent(String id, String type, String nspace,
			Dictionary properties)  {
		super(id, type, nspace, properties);
	}
	/**
	 * Get the mediator wich contains this dispatcher.
	 * @return the mediator.
	 */
	public MediatorComponentImpl getMediator() {
		synchronized (lockObject) {
			return mediator;	
		}
		
	}
	/**
	 * Set the mediator reference which contains this dispatcher. 
	 * @param mediator the mediator representation model reference.
	 */
	public void setMediator(MediatorComponentImpl mediator) {
		synchronized (lockObject) {
			this.mediator = mediator;	
		}
	}
}
