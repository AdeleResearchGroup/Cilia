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

import fr.liglab.adele.cilia.Node;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public interface MediatorComponent extends Node,Component { 
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
     * Component instance state: SEMIVALID. the component instance is resolved and is running but
     * there are at least one binding that is not working.
     */
    final static int SEMIVALID = 3;
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
	 * @param outPort
	 * @return
	 */
	Binding[] getBinding(Port outPort);


	/**
	 * 
	 * @return list of extended model 
	 */
	String[] extendedModelName() ;
	
	/**
	 * 
	 * @param modelName
	 * @return Model extended or null if modelName doesn't exist
	 */
	ModelExtension getModel(String modelName) ;
	
	void addModel(String modelName,ModelExtension modelExtension) ;
	
	void removeModel(String modelName) ;
	
	
	int getState();
	
	boolean isRunning();
}
