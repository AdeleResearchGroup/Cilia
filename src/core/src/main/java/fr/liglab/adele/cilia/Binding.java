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


public interface Binding extends Component {

	/**
	 * Get the parent chain.
	 * @return the parent chain.
	 */
	Chain getChain();

	/**
	 * Get the source mediator port in this binding.  
	 * @return the source mediator port.
	 */
	Port getSourcePort();

	/**
	 * Get the target mediator port asociated to this binding. 
	 * @return the target mediator port.
	 */
	Port getTargetPort();

	/**
	 * Get the source mediator model.  
	 * @return the source mediator model.
	 */
	MediatorComponent getSourceMediator();

	/**
	 * Get the target mediator model.
	 * @return the target mediator model.
	 */
	MediatorComponent getTargetMediator();

	/**
	 * @param sourcePort
	 */
	void setSourcePort(Port sourcePort);

	/**
	 * @param targetPort
	 */
	void setTargetPort(Port targetPort);

	/**
	 * @return
	 */
	Component getCollector();
	
	/**
	 * @return
	 */
	Component getSender();

	/**
	 * @param senderm
	 */
	boolean addSender(Component senderm);
	
	/**
	 * @param collectorm
	 */
	boolean addCollector(Component collectorm);
	

}