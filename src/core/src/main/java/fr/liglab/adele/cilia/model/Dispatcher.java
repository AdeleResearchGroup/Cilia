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
import java.util.Map;
/**
 * This class is the Dispatcher representation model.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class Dispatcher extends MediatorElement {



	/**
	 * Constructor which will copy the information of the given dispatcher.
	 * This constructor will copy only the id, type, classname and
	 * properties, it will not copy mediator information.
	 * The resulting dispatcher will not have any mediator asociated.
	 * @param disp Dispatcher wich will be copied.
	 */
	public Dispatcher(Dispatcher disp) {
		this(disp.getId(), disp.getType(), disp.getNamespace(), new Hashtable((Map)disp.getProperties()));
	}
	/**
	 * Create a new dispatcher model representation.
	 * @param id identificator of the new dispatcher.
	 * @param type type of dispatcher.
	 * @param classname class name asociated to the new dispatcher.
	 * @param properties Properties of the dispatcher.  
	 */
	public Dispatcher(String id, String type, String namespace, Dictionary properties) {
		super(id, type, namespace, properties);
	}


}
