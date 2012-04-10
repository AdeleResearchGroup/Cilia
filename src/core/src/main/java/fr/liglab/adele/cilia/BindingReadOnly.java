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


public interface BindingReadOnly {
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
	 * Get the parent chain.
	 * @return the parent chain.
	 */
	ChainReadOnly getChain();

	/**
	 * Get the source mediator port in this binding.  
	 * @return the source mediator port.
	 */
	PortReadOnly getSourcePort();

	/**
	 * Get the target mediator port asociated to this binding. 
	 * @return the target mediator port.
	 */
	PortReadOnly getTargetPort();

	/**
	 * Get the source mediator model.
	 * @return the source mediator model.
	 */
	MediatorReadOnly getSourceMediator();

	/**
	 * Get the target mediator model.
	 * @return the target mediator model.
	 */
	MediatorReadOnly getTargetMediator();
	
	String getSenderId() ;

	String getSenderType();

	String getSenderNameSpace();
	
}