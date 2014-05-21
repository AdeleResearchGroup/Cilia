/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package fr.liglab.adele.cilia.jms2;

import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.framework.GenericBindingService;
import fr.liglab.adele.cilia.model.Binding;

import java.util.Dictionary;
import java.util.Properties;

/**
 * JMS/Joram linker based on new JMS 2.0 specification.
 * <p/>
 * This JMS adapter works either in Point-To-Point (PTP) or Publish/Subscribe (P/S)
 * mode messages depending of the destination type (Queue or Topic) used to exchange
 * messages.
 * <p/>
 * The destination type could be specified in the name of the destination using
 * the "jms.dest" property:
 * - "queue:q1" to specify a queue with the name q1,
 * - "topic:t1" to specify a topic with the name t1.
 * If the destination type is omitted by default it is a topic.
 *
 * @author ScalAgent Team
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class JMSLinker extends GenericBindingService implements CiliaBindingService {
    /**
     * Property name used to specify the name and type of the destination used to
     * exchange messages.
     */
    protected static final String DEST = "jms.dest";

    public Dictionary getProperties(Dictionary collectorProperties,
                                    Dictionary senderProperties,
                                    Binding b) {
        Dictionary properties = new Properties();

        String dest = getDestination(b);
        dest = dest.replace(" ", "_");
        if (collectorProperties != null) {
            collectorProperties.put(DEST, dest);
            properties.put(CILIA_COLLECTOR_PROPERTIES, collectorProperties);
        }
        if (senderProperties != null) {
            senderProperties.put(DEST, dest);
            properties.put(CILIA_SENDER_PROPERTIES, senderProperties);
        }
        return properties;
    }

    private String getDestination(Binding b) {
        if (b.getProperty(DEST) != null) {
            String dest = (String) b.getProperty(DEST);
            return dest;
        }

        StringBuffer dest = new StringBuffer();
        dest.append(b.getChain().getId());
        if (b.getSourceMediator() != null) {
            dest.append("$");
            dest.append(b.getSourceMediator().getId());
        }
        if (b.getTargetMediator() != null) {
            dest.append("$");
            dest.append(b.getTargetMediator().getId());
        }
        return dest.toString();
    }
}
