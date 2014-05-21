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

package fr.liglab.adele.cilia.model.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class is the Scheduler representation model.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class Scheduler extends InternalComponent {


    /**
     * Constructor which will copy the information of the given Scheduler.
     * This constructor will copy only the id, type, classname and
     * properties, it will not copy mediator information.
     * The resulting scheduler will not have any mediator asociated.
     *
     * @param scheduler scheduler wich will be copied.
     */
    public Scheduler(Scheduler scheduler) {
        this(scheduler.getId(), scheduler.getType(), scheduler.getNamespace(), new Hashtable((Map) scheduler.getProperties()));
    }

    /**
     * Create a new scheduler model representation.
     *
     * @param id         identificator of the new scheduler.
     * @param type       type of scheduler.
     * @param classname  class name asociated to the new scheduler.
     * @param properties Properties of the scheduler.
     */
    public Scheduler(String id, String type, String namespace, Dictionary properties) {
        super(id, type, namespace, properties);
    }


}
