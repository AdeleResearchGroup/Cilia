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
package fr.liglab.adele.cilia.helper;

import fr.liglab.adele.cilia.Data;

import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public interface MediatorTestHelper {
    /**
     * Get the last port name where the mediator send data.
     */
    String lastExitPort();

    /**
     * See if mediator has finish to process data.
     */
    boolean hasFinishToProcess();

    /**
     * Inject a single Data to the mediator.
     *
     * @param the data to inject.
     * @return true if is injected, false if not.
     */
    boolean injectData(Data data);

    /**
     * Inject an array of data to the mediator.
     *
     * @param data, the array of data to inject.
     * @return true if the array is injected, false if some data is not well injected.
     */
    boolean notifyData(Data[] data);

    /**
     * Get the last received Data.
     */
    Data getLastData();

    /**
     * Get the amount of data treated and delivered by the mediator.
     */
    int getAmountData();

    List<Data> getData();

}
