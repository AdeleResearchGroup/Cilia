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

public interface MediatorReadOnly {

	/**
	 * get the model representation identificator.
	 * 
	 * @return the model representation identificator.
	 */
	String getId();
	
	/**
	 * get the qualified id 'chainId/mediatorId'
	 * @return
	 */
	String getQualifiedId() ;

	/**
	 * Get this model representation type.
	 * 
	 * @return the model type.
	 */
	String getType();

	/**
	 * @return the namespace
	 */
	String getNamespace();

	/**
	 * @param name
	 *            Port Name to create.
	 * @return the Port with the given port Name.
	 */
	PortReadOnly getInPort(String name);

	PortReadOnly getOutPort(String name);

	/**
	 * @return the category
	 */
	String getCategory();

	/**
	 * Get an array of all the bindings added to the mediator.
	 * 
	 * @return
	 */
	BindingReadOnly[] getInBindings();

	BindingReadOnly[] getOutBindings();

}