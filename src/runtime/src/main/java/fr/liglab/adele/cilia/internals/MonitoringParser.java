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
	private static final String TAG_MONITORING = "monitoring";
	private static final String ATTR_ENABLE = "enable";
	private static final String TAG_STATEVAR = "state-variable";
	private static final String TAG_SETUP = "setup";
	private static final String TAG_THRESHOLD = "threshold";
	private static final String ATTR_ID = "id";
	private static final String ATTR_FLOWCONTROL = "flow-control";
	private static final String ATTR_VERYLOW = "very-low";
	private static final String ATTR_LOW = "low";
	private static final String ATTR_HIGH = "high";
	private static final String ATTR_VERYHIGH = "very-high";
	private static final String ATTR_QUEUE = "queue";

	public MonitoringParser() {
		NAMESPACE = "fr.liglab.adele.cilia";
		NAME = "state-variable";
	}

	public boolean canHandle(Object elementDescription) {
		Node disp = getNode(TAG_MONITORING, elementDescription);
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

	public Component getComponent(Object componentDescription, Component current)
			throws CiliaParserException {

		ComponentImpl currentComponent = (ComponentImpl) current;

		ParserConfiguration monitoringConfig = new ParserConfiguration(
				(MediatorComponent) current);
		Node node = getNode(TAG_STATEVAR, componentDescription);

		while (node != null) {
			Node conf;

			String variableId = getAttributeValue(node, ATTR_ID);
			boolean enable = getAttributeBoolean(node, ATTR_ENABLE);

			if (monitoringConfig.addVariable(variableId, enable)) {
				conf = node.getFirstChild();
			} else
				conf = null;

			while (conf != null) {
				if (conf.getLocalName() != null
						&& conf.getLocalName().equalsIgnoreCase(TAG_SETUP)) {
					String queue = getAttributeValue(conf, ATTR_QUEUE);
					String dataflow = getAttributeValue(conf, ATTR_FLOWCONTROL);
					monitoringConfig.addSetUp(variableId, queue, dataflow);
				}
				if (conf.getLocalName() != null
						&& conf.getLocalName().equalsIgnoreCase(TAG_THRESHOLD)) {
					String low = getAttributeValue(conf, ATTR_LOW);
					String veryLow = getAttributeValue(conf, ATTR_VERYLOW);
					String high = getAttributeValue(conf, ATTR_HIGH);
					String veryhigh = getAttributeValue(conf, ATTR_VERYHIGH);
					monitoringConfig.addThreshold(variableId, low, veryLow, high,
							veryhigh);
				}
			}
			monitoringConfig.configure();
			node = node.getNextSibling();
		}
		return currentComponent;
	}
}
