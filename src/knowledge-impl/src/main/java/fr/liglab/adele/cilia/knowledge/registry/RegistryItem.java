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

package fr.liglab.adele.cilia.knowledge.registry;

import java.util.Dictionary;

import fr.liglab.adele.cilia.knowledge.Node;

/**
 * Objects stored in the registry
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface RegistryItem extends Node {

	/**
	 * 
	 * @param key
	 *            String key
	 * @param value
	 *            Value to set , must not be null
	 */
	void setProperty(String key, Object value);

	/**
	 * 
	 * @param key
	 *            String property to retreive
	 * @return value
	 */
	Object getProperty(String key);

	/**
	 * 
	 * @return all properties
	 */
	Dictionary getProperties();

	/**
	 * 
	 * @return object ( mediator handler , adapter handler ,etc... )
	 */
	Object objectRef();

	/**
	 * 
	 * @return node reference ( mediator , adapter at runtime )
	 */
	Object nodeReference();

}
