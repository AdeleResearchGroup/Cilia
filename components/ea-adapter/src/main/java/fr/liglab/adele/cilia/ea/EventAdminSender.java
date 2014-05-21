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

package fr.liglab.adele.cilia.ea;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ISender;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * EventAdmin Sender. Send Data objects using Event-based protocol (OSGi
 * EventAdmin).
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class EventAdminSender implements ISender {

    /**
     * Event Admin. Its reference will be injected by iPOJO.
     */
    private EventAdmin m_eventAdmin;

    private static final Logger log = LoggerFactory.getLogger("cilia.component.eventadmin");

    /**
     * Configurable property that holds the exported topic. If it is null, the
     * topic will be: mediator_name/data_name
     */
    private String m_topic;


    /**
     * Send the Data object
     *
     * @param data
     */
    public boolean send(Data data) {
        if (data != null) {
            String topic = "";
            if (m_topic != null && m_topic.length() != 0) {
                topic = m_topic;
            } else {
                log.error("topic is null");
                return false;
            }
            if (m_eventAdmin != null) {

                m_eventAdmin.postEvent(new Event(topic, data.getAllData()));
                return true;
            } else {
                log.error("unable to send, m_eventAdmin is NULL");
            }
        }
        return false;
    }


}
