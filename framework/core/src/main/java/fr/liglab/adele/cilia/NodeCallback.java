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

/**
 * Callback , events [ node arrival / node departure ]
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface NodeCallback {
    /**
     * @param node , node arrival
     */
    void onArrival(Node node);

    /**
     * @param node , node departure
     */
    void onDeparture(Node node);

    /**
     * @param node node property updated
     */
    void onModified(Node node);

    /**
     * bind between nodes 'from -> to'
     * Event fired only by the applicationSpecification
     *
     * @param from node source
     * @param dest node dest
     */
    void onBind(Node from, Node to);

    /**
     * unbind between nodes 'from-> to'
     * Event fired only by the applicationSpecification
     *
     * @param source
     * @param dest
     */
    void onUnBind(Node from, Node to);

    /**
     * New executing state of a node ,
     *
     * @param isValid true if the new state is valid.<br>
     *                Valid means Scheduler/Processor/Dispatcher are all valid
     */
    void onStateChange(Node node, boolean isValid);

}
