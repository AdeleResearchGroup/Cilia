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


package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;

/**
 * This is the implementation of direct collector service.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class DirectCollector extends AbstractCollector implements IDirectCollector {
    /**
     * identificator used to identify the mediator.
     */
    private String asociatedMediator;

    /**
     * This method is called by the direct sender.
     *
     * @param data data received.
     */
    public void receive(Data data) {
        super.notifyDataArrival(data);
    }
}
