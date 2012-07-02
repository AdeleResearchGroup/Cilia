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
import org.w3c.dom.NodeList;

import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.knowledge.configuration.ParserConfiguration;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class MonitoringParser extends DomExtenderParser implements CiliaExtenderParser {

	public MonitoringParser() {
		NAME = "state-variable";
	}

	public boolean canHandle(Object elementDescription) {
		Node disp = getNode("monitoring", elementDescription);
		if (disp == null) {
			return false;
		}
		return true;
	}

	private boolean getAttributeBoolean(Node node, String attribute) {
		String enableStr = getAttributeValue(node, attribute);
		boolean enable = false;
		if ((enableStr != null) && (enableStr.equalsIgnoreCase("true")))
			enable = true;
		else
			enable = false;
		return enable;
	}

	public Component getComponent(Object componentDescription, Component currentComponent)
			throws CiliaParserException {
		ParserConfiguration monitoringConfig = null;
		Node node = getNode("monitoring", componentDescription);
		if (node != null) {
			monitoringConfig = new ParserConfiguration((MediatorComponent) currentComponent);
			/* Tag state-variable */
			while (node != null) {		
				String variableId = getAttributeValue(node, "id");
				boolean enable = getAttributeBoolean(node, "enable");
				
				if (monitoringConfig.addVariable(variableId, enable)) {
					Node child = node.getFirstChild();
					/* tag setup and tag Threshold */
					while (child != null) {
						if (child.getLocalName() != null
								&& child.getLocalName().equalsIgnoreCase("setup")) {
							String queue = getAttributeValue(child, "queue");
							String dataflow = getAttributeValue(child, "flow-control");
							System.out.println(">>>Variable =" + variableId
									+ " enable = " + enable + " queue=" + queue
									+ "dataflow =" + dataflow);
							monitoringConfig.addSetUp(variableId, queue, dataflow);
						} else {
							if (child.getLocalName() != null
									&& child.getLocalName().equalsIgnoreCase(
											"threshold")) {
								String low = getAttributeValue(child, "low");
								String veryLow = getAttributeValue(child, "very-low");
								String high = getAttributeValue(child, "high");
								String veryhigh = getAttributeValue(child, "very-high");
								monitoringConfig.addThreshold(variableId, low, veryLow,
										high, veryhigh);
								System.out.println("Thresolhld low =" + low);
							}
						}
						child = child.getNextSibling();
					}
				}
				node = node.getNextSibling();
			}
			monitoringConfig.configure();
		}
		return currentComponent;
	}
}
