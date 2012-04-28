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

package fr.liglab.adele.cilia.runtime;

import java.util.Dictionary;

/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings("rawtypes")
public interface CiliaInstance {


	/**
	 * Start the instance
	 */
	public void start();

	/**
	 * Stop the instance
	 */
	public void stop();

	/**
	 * Refresh the instance state
	 */
	public void refresh();

	/**
	 * Get the instance state
	 * 
	 * @return
	 */
	public int getState();

	/**
	 * Get the instance state as a string
	 * 
	 * @return
	 */
	public String getStateAsString();


	/**
	 * Get the name associated to this instance
	 * 
	 * @return
	 */
	public String getName();


	/**
	 * Get the instance's runtime properties
	 * 
	 * @return
	 */
	public Dictionary getInstanceProperties();

	/**
	 * Get instance property
	 * 
	 * @return
	 */
	public Object getInstanceProperty(Object key);
	
	/**
	 * Update the instance's runtime properties
	 * 
	 * @param properties
	 */
	public void updateInstanceProperties(Dictionary properties);

	
	/**
	 * Get the object wrapped by this instance
	 * 
	 * @return
	 */
	public Object getObject();
	
}
