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

import org.apache.felix.ipojo.InstanceManager;

/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings("rawtypes")
public interface CiliaInstance {

    /**
     * Component Instance State : DISPOSED. The component instance was disposed.
     */
    final static int DISPOSED = -1;
    
    /**
     * Component Instance State : STOPPED. The component instance is not
     * started.
     */
    final static int STOPPED = 0;

    /**
     * Component Instance State : INVALID. The component instance is invalid when it
     * starts or when a component dependency is invalid.
     */
    final static int INVALID = 1;

    /**
     * Component Instance State : VALID. The component instance is resolved when it is
     * running and all its attached handlers are valid.
     */
    final static int VALID = 2;

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
