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
package fr.liglab.adele.cilia.jms;

import java.util.Dictionary;
import java.util.Properties;

import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.framework.GenericBindingService;
import fr.liglab.adele.cilia.model.Binding;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class JMSLinker extends GenericBindingService implements CiliaBindingService{
	protected static final String TOPIC = "jms.topic";

    public Dictionary getProperties(Dictionary collectorProperties,
            Dictionary senderProperties, Binding b) {
        Dictionary properties = new Properties();

        String topic = getTopic(b);
        topic = topic.replace(" ", "_");
        if (collectorProperties != null) {
            collectorProperties.put(TOPIC, topic);
            properties.put(CILIA_COLLECTOR_PROPERTIES, collectorProperties);
        }
        if (senderProperties != null) {
            senderProperties.put(TOPIC, topic);
            properties.put(CILIA_SENDER_PROPERTIES, senderProperties);
        }
        return properties;
    }

    private String getTopic(Binding b) {
        if (b.getProperty(TOPIC) != null) {
            String topic = (String)b.getProperty(TOPIC);
            return topic;
        }
        
        StringBuffer topic = new StringBuffer();
        topic.append(b.getChain().getId());
        if (b.getSourceMediator() != null) {
            topic.append("$");
            topic.append(b.getSourceMediator().getId());
        }
        if (b.getTargetMediator() != null) {
            topic.append("$");
            topic.append(b.getTargetMediator().getId());
        }
        return topic.toString();
    }
}
