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


package fr.liglab.adele.cilia.components.senders;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.components.collectors.IDirectCollector;
import fr.liglab.adele.cilia.framework.ISender;

/**
 * This class is the implementation of the DirectSender component
 * for the Cilia compendium.
 * It need the reference of the Collector where it will delivery the information.  
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class DirectSender implements ISender {
    /**
     * The sender must have a collector reference to give the data.
     */
    IDirectCollector collector;
    /**
     * It will send the given data to the collector
     * associated to this sender.
     * @param data Data to send.
     */
    public boolean send(Data data) {
        collector.receive(data);
        return true;
    }

}
