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

import fr.liglab.adele.cilia.model.PortType;

public interface Port {

	/**
	 * Get the port Tyoe.
	 * @return
	 */
	public PortType getType();

	/**
	 * Get the port name.
	 * @return
	 */
	public String getName();

	/**
	 * Get the mediator reference which contain this port.
	 * @return
	 */
	public MediatorComponent getMediator();

}