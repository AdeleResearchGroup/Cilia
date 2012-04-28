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

import org.w3c.dom.Node;

import fr.liglab.adele.cilia.Component;
import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.ext.ContentBasedRouting;
import fr.liglab.adele.cilia.model.MediatorComponentImpl;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class ContentBasedParser extends DomExtenderParser implements CiliaExtenderParser{

	private static final String CONDITION = "condition";
	private static final String CONFIGURATION = "when";
	private static final String TO = "sendTo";
	private static final String LANGUAGE = "language";

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.model.parser.CiliaExtenderParser#getComponent(java.lang.Object, fr.liglab.adele.cilia.model.IComponent)
	 */
	public ContentBasedParser(){
		NAMESPACE = "fr.imag.adele.cilia.dispatcher";
		NAME = "content-based";
	}
	public Component getComponent(Object componentDescription,
			Component currentComponent) throws CiliaParserException {
		Node child = getNode("dispatcher",componentDescription);
		if (child != null) {
			ContentBasedRouting cbr = new ContentBasedRouting((MediatorComponentImpl)currentComponent);
			String language = getAttributeValue(child, LANGUAGE);
			if (language != null) {
				cbr.evaluator(language);
			}
			Node conf = child.getFirstChild();
			while (conf != null) {
				if (conf.getLocalName() != null && conf.getLocalName().equalsIgnoreCase(CONFIGURATION)) {
					String condition = getAttributeValue(conf, CONDITION);
					String sendTo = getAttributeValue(conf, TO);
					if (condition != null && sendTo != null) {
						cbr.condition(condition).to(sendTo);
					}
				}
				conf = conf.getNextSibling();
			}
		}
		return currentComponent;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.model.parser.CiliaExtenderParser#canHandle(java.lang.Object)
	 */
	public boolean canHandle(Object mediatorDescription) {
		Node disp = getNode("dispatcher",mediatorDescription);
		if(disp == null) {
			return false;
		}
		return true;
	}
}
