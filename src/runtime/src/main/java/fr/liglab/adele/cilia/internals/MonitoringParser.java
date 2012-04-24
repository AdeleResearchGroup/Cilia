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

import java.util.HashMap;

import org.w3c.dom.Node;

import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;
import fr.liglab.adele.cilia.Component;

public class MonitoringParser extends DomExtenderParser implements CiliaExtenderParser {
	private static final String TAG_MONITORING = "monitoring";
	private static final String ATTR_ENABLE = "enable";
	private static final String TAG_STATEVAR = "state-variable";
	private static final String ATTR_ID = "id";
	private static final String ATTR_CONDITION = "condition";

	public MonitoringParser() {

	}

	public boolean canHandle(Object elementDescription) {
		if ((elementDescription != null) && (elementDescription instanceof Node)) {
			Node parent = ((Node) elementDescription);
			if (parent != null && parent.getLocalName() != null) {
				if (parent.getLocalName().equalsIgnoreCase("adapter-instance"))
					return true;
				if (parent.getLocalName().equalsIgnoreCase("mediator-instance"))
					return true;
			}
		}
		return false;
	}

	public Component getComponent(Object componentDescription,
			Component currentComponent) throws CiliaParserException {
		HashMap stateVarList = null;
		Node child = getElement(TAG_MONITORING,(Node) componentDescription);
		if (child != null) {
			String enable = getAttributeValue(child, ATTR_ENABLE);
			if (enable != null) {
				if (enable.equalsIgnoreCase("true")) {
					currentComponent.setProperty("state.variable.status", "true");
				} else currentComponent.setProperty("state.variable.status", "false");
			} else currentComponent.setProperty("state.variable.status", "true");
			
			Node conf = child.getFirstChild();
			while (conf != null) {
				if (conf.getLocalName() != null
						&& conf.getLocalName().equalsIgnoreCase(TAG_STATEVAR)) {
					String id = getAttributeValue(conf, ATTR_ID);
					String condition = getAttributeValue(conf, ATTR_CONDITION);
					if (id != null) {
						if (stateVarList == null)
							stateVarList = new HashMap();
						if (condition == null)
							condition = "";
						stateVarList.put(id, condition);
					}
				}
				conf = conf.getNextSibling();
			}
			if (stateVarList != null)
				currentComponent
						.setProperty("state.variable.configuration", stateVarList);
		}
		return currentComponent;
	}
}
