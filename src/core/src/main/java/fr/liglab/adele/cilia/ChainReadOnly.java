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

package fr.liglab.adele.cilia;

import java.util.Set;

public interface ChainReadOnly {
	/**
	 * get the model representation identificator.
	 * @return the model representation identificator.
	 */
	String getId()  ;
	
	/**
	 * Get this model representation type.
	 * @return the model type.
	 */
	String getType() ;
	
	/**
	 * @return the namespace
	 */
	String getNamespace()  ;
	
	/**
	 * Obtain the mediator model which has the given identificator.
	 * @param mediatorId Mediator identificator.
	 * @return the mediator which has the given identificator.
	 */
	MediatorReadOnly getMediator(String mediatorId);

	/**
	 * Get all the mediators models added to the chain model.
	 * @return
	 */
	Set getMediators();

	/**
	 * Obtain the adaptor model which has the given identificator.
	 * @param mediatorId Mediator identificator.
	 * @return the mediator which has the given identificator.
	 */
	AdapterReadOnly getAdapter(String adapterId);

	/**
	 * Get all the mediators models added to the chain model.
	 * @return
	 */
	Set getAdapters();

	/**
	 * Get all the bindings added to the chain model.
	 * @return the added bindings.
	 */
	Set getBindings();

	/**
	 * Obtain an array of all the bindings asociated to the given mediators.
	 * @param source source mediator which contains the searched bindings.
	 * @param target target mediator which contains the searched bindings.
	 * @return the array of bindings.
	 */
	BindingReadOnly[] getBindings(MediatorReadOnly source, MediatorReadOnly target);

}