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
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
//@SuppressWarnings("rawtypes")
public interface Chain extends Component{
	/**
	 * Obtain the mediator model which has the given identificator.
	 * @param mediatorId MediatorImpl identificator.
	 * @return the mediator which has the given identificator.
	 */
	Mediator getMediator(String mediatorId);

	/**
	 * Get all the mediators models added to the chain model.
	 * @return
	 */
	Set getMediators();

	/**
	 * Obtain the adaptor model which has the given identificator.
	 * @param mediatorId MediatorImpl identificator.
	 * @return the mediator which has the given identificator.
	 */
	Adapter getAdapter(String adapterId);

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
	Binding[] getBindings(MediatorComponent source, MediatorComponent target);

	/**
	 * @param a1
	 */
	//boolean add(Adapter a1);

	/**
	 * @param m2
	 */
	//boolean add(Mediator m2);

	/**
	 * @param outPort
	 * @param inPort
	 * @return
	 */
	//Binding bind(Port outPort, Port inPort);

	/**
	 * @param outPort
	 * @param inPort
	 * @param bindingModel
	 */
	//Binding bind(Port outPort, Port inPort, Binding bindingModel);

	/**
	 * @param port
	 * @param bindingModel
	 * @return 
	 */
	//Binding bind(Port port, Binding bindingModel);

	/**
	 * @param binding
	 */
	//boolean unbind(Binding binding);

	/**
	 * @param id
	 */
	//boolean removeAdapter(String id);

	/**
	 * @param mediatorId
	 * @return
	 */
	//boolean removeMediator(String mediatorId);

}