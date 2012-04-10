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

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public interface IComponent {

	/**
	 * Set new properties as String separated as Strings.
	 * @param propertiesAsString
	 */
	void setProperties(String propertiesAsString) ;
	
	/**
	 * get the model representation identificator.
	 * @return the model representation identificator.
	 */
	String getId() ;

	/**
	 * Get this model representation type.
	 * @return the model type.
	 */
	String getType() ;
	/**
	 *Get a copy of the properties.
	 *@return properties. 
	 */
	Dictionary getProperties() ;
	/**
	 * Get the specified property.
	 * @param key property name.
	 * @return the property asociated to the given key.
	 */
	Object getProperty(Object key) ;
	/**
	 * Set new properties.
	 * @param newProps the new properties.
	 */
	void setProperties(Dictionary newProps) ;
	/**
	 * Set a new property.
	 * @param key property key.
	 * @param value property value.
	 */
	void setProperty(Object key, Object value) ;
	/**
	 * This method returns the identificator of the model representation.
	 * @return the model representation identificator. 
	 */
	String toString() ;
	/**
	 * @param namespace the namespace to set
	 */
	void setNamespace(String namespace) ;
	/**
	 * @return the namespace
	 */
	String getNamespace() ;

;

}
