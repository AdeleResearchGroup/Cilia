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
package fr.liglab.adele.cilia.internals;

import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.ext.SimpleEnricher;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class EnricherParser extends DomExtenderParser implements CiliaExtenderParser {

    private static final String CONTENT = "name";
    private static final String VALUE = "value";

    /* (non-Javadoc)
     * @see fr.liglab.adele.cilia.model.impl.parser.CiliaExtenderParser#getComponent(java.lang.Object, fr.liglab.adele.cilia.model.impl.IComponent)
     */
    public EnricherParser() {
        //NAMESPACE = "fr.imag.adele.cilia.processor.enricher";
        NAME = "enricher";
    }

    public Component getComponent(Object componentDescription,
                                  Component currentComponent) throws CiliaParserException {
        SimpleEnricher enricher = new SimpleEnricher((Mediator) currentComponent);
        Node child = getNode("processor", componentDescription);
        try {
            while (child != null) {
                if (child.getLocalName() != null && child.getLocalName().equalsIgnoreCase(NAME)) {
                    String content = getAttributeValue(child, CONTENT);
                    String value = getAttributeValue(child, VALUE);
                    enricher.key(content).value(value);
                }
                child = child.getNextSibling();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return currentComponent;
    }

    /* (non-Javadoc)
     * @see fr.liglab.adele.cilia.model.impl.parser.CiliaExtenderParser#canHandle(java.lang.Object)
     */
    public boolean canHandle(Object mediatorDescription) {
        Node disp = getNode("processor", mediatorDescription);
        if (disp == null) {
            return false;
        }
        return true;
    }

}
