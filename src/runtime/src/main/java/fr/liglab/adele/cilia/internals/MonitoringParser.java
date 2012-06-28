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
import fr.liglab.adele.cilia.ext.MonitoringHandler;
import fr.liglab.adele.cilia.ext.StateVarConfigurationImpl;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.model.impl.MediatorComponentImpl;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;

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
		System.out.println(">>>>>> PARSERUR MONITORING");
	}
	

	protected Node getNode(String parentName, Object componentDescription) {
		if (componentDescription == null || !(componentDescription instanceof Node)) {
			return null;
		}
		Node parent = ((Node) componentDescription).getFirstChild();
		Node child = null;
		while (parent != null) {
			if (parent.getLocalName() != null
					&& parent.getLocalName().equalsIgnoreCase(parentName)) {
				child = parent.cloneNode(true).getFirstChild();
				break;
			}
			parent = parent.getNextSibling();
		}

		while (child != null) {
			if (child.getNamespaceURI() != null
					&& child.getLocalName().equalsIgnoreCase(NAME)) {
				return child;
			}
			child = child.getNextSibling();
		}
		return null;
	}
	public boolean canHandle(Object elementDescription) {
		System.out.println(">>>>>>CAN HANDLE ") ;
		//if ((elementDescription != null) && (elementDescription instanceof Node)) {
		//	Node parent = ((Node) elementDescription);
		//	if (parent != null && parent.getLocalName() != null) {
		//		if (parent.getLocalName().equalsIgnoreCase("adapter-instance"))
		//			return true;
		//		if (parent.getLocalName().equalsIgnoreCase("mediator-instance"))
		//			return true;
		//	}
		//}
		Node disp = getNode(TAG_MONITORING,elementDescription);
		if(disp == null) {
			System.out.println(">>>>>>>>> MONITORING FALSE ") ;

			return false;
		}
		System.out.println(">>>>>>>>> MONITORING TRUE ") ;
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
		Node child = getNode(TAG_STATEVAR,componentDescription) ;
		System.out.println(">>>>>GET MONITORING __> STATE VAR") ;
		if (child != null) {
			MonitoringHandler monitorConfig = new MonitoringHandler(
					(MediatorComponentImpl) currentComponent);

			Node conf = child.getFirstChild();
			while (conf != null) {

				/* Tag STATE VAR attribute enable & id */
				if (conf.getLocalName() != null
						&& conf.getLocalName().equalsIgnoreCase(TAG_STATEVAR)) {
					System.out.println("GET STATE VAR ");
					String id = getAttributeValue(conf, ATTR_ID);
					/* tag mandatory */
					if (id != null) {
						boolean enable = getAttributeBoolean(conf, ATTR_ENABLE);
						StateVarConfigurationImpl statevarConfig = monitorConfig.addId(
								id, enable);
						Node statevar = conf.getFirstChild();
						while (statevar != null) {
							/* TAG setup & TAG threshold */
							if (statevar.getLocalName() != null) {
								String attr;
								if (statevar.getLocalName().equalsIgnoreCase(TAG_SETUP)) {
									attr = getAttributeValue(statevar, ATTR_QUEUE);
									if (attr != null) {
										try {
											int queue = Integer.parseInt(attr);
											statevarConfig.setQueueSize(queue);
										} catch (NumberFormatException e) {
										}
									}
									attr = getAttributeValue(statevar, ATTR_FLOWCONTROL);
									statevarConfig.setControlFlow(attr);
								}
								if (statevar.getLocalName().equalsIgnoreCase(
										TAG_THRESHOLD)) {
									attr = getAttributeValue(statevar, ATTR_VERYLOW);
									if (attr != null) {
										try {
											Double d = Double.parseDouble(attr);
											statevarConfig.setVeryLow(d);
										} catch (NumberFormatException e) {
										}
									}
									attr = getAttributeValue(statevar, ATTR_LOW);
									if (attr != null) {
										try {
											Double d = Double.parseDouble(attr);
											statevarConfig.setLow(d);
										} catch (NumberFormatException e) {
										}
									}
									attr = getAttributeValue(statevar, ATTR_HIGH);
									if (attr != null) {
										try {
											Double d = Double.parseDouble(attr);
											statevarConfig.setHigh(d);
										} catch (NumberFormatException e) {
										}
									}
									attr = getAttributeValue(statevar, ATTR_VERYHIGH);
									if (attr != null) {
										try {
											Double d = Double.parseDouble(attr);
											statevarConfig.setVeryHigh(d);
										} catch (NumberFormatException e) {
										}
									}
								}
							}
						}
						statevar = statevar.getNextSibling();
					}

				}
				conf = conf.getNextSibling();
			}
			monitorConfig.done();
		}
		return currentComponent;
	}
}
