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

import java.util.Map;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public interface MediatorComponent extends Node, Component {

    public enum State {
        /**
         * Component Instance State : DISPOSED. The component instance was disposed.
         */
        DISPOSED,
        /**
         * Component Instance State : STOPPED. The component instance is not
         * started.
         */
        STOPPED,
        /**
         * Component Instance State : VALID. The component instance is resolved when it is
         * running and all its attached handlers are valid.
         */
        INVALID,
        /**
         * Component Instance State : VALID. The component instance is resolved when it is
         * running and all its attached handlers are valid.
         */
        VALID
    }

    /**
     * @return
     */
    Chain getChain();

    /**
     * @param name PortImpl Name to create.
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
     * @return list of extended model
     */
    String[] extendedModelName();

    /**
     * @param modelName
     * @return Model extended or null if modelName doesn't exist
     */
    ModelExtension getModel(String modelName);

    void addModel(String modelName, ModelExtension modelExtension);

    void removeModel(String modelName);


    State getState();

    String getVersion();

    Map toMap();

    boolean isRunning();
}
