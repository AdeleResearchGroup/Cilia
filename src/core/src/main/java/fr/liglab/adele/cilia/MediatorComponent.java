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

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public interface MediatorComponent extends Component { 
	
	/**
	 * get the qualified id 'chainId/mediatorId'
	 * @return
	 */
	String getQualifiedId() ;

	/**
	 * 
	 * @return
	 */
	Chain getChain();

	/**
	 * @param name
	 *            PortImpl Name to create.
	 * @return the PortImpl with the given port Name.
	 */
	Port getInPort(String name);

	Port getOutPort(String name);

	/**
	 * @return the category
	 */
	String getCategory();

	/**
	 * Get an array of all the bindings added to the mediator.
	 * 
	 * @return
	 */
	Binding[] getInBindings();

	Binding[] getOutBindings();

	/**
	 * @param bindingImpl
	 */
	//void addOutBinding(Binding binding);

	/**
	 * @param bindingImpl
	 */
	//void addInBinding(Binding bindingImpl); 
	/**
	 * 
	 * @param chain
	 */
	//void setChain(Chain chain);

	/**
	 * @param binding
	 * @return 
	 */
	//boolean removeInBinding(Binding binding);

	/**
	 * @param binding
	 */
	//boolean removeOutBinding(Binding binding);

	/**
	 * @param outPort
	 * @return
	 */
	Binding[] getBinding(Port outPort);

}
