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

import static fr.liglab.adele.cilia.dependency.DependencyHandler.DEFAULT_FILTER_NAME;

import java.util.Properties;

import org.w3c.dom.Node;

import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class DependencyParser extends DomExtenderParser implements CiliaExtenderParser {
	private static final String TAG_DEPENDENCY = "dependency";
	private static final String ATTR_ID = "filter.name";
	private static final String ATTR_FILTER = "selection";
	private static final String ATTR_CARDINALITY = "cardinality";
	private static final String ATTR_RANKING = "ranking";
	private static final String ATTR_IMMEDIATE="immediate" ;

	private DependencyParser() {
		NAMESPACE = "fr.liglab.adele.cilia";
		NAME = TAG_DEPENDENCY;
	}

	public boolean canHandle(Object elementDescription) {
		Node disp = getNode(TAG_DEPENDENCY,elementDescription);
		if(disp == null) {
			return false;
		}
		return true;
	}

	public Component getComponent(Object componentDescription,
			Component component) throws CiliaParserException {
		ComponentImpl currentComponent = (ComponentImpl)component ;
		if ((componentDescription != null) && (componentDescription instanceof Node)) {
			Node node = getNode(TAG_DEPENDENCY, ((Node) (componentDescription)));
			if (node != null) {
				String id = getAttributeValue(node, ATTR_ID);
				if (id == null) {
					id = DEFAULT_FILTER_NAME;
				}
				String filter = getAttributeValue(node, ATTR_FILTER);
				if (filter != null) {
					Properties props = new Properties();
					props.put(id, filter);
					currentComponent.setProperty("requires.filters", props);
				}
				String cardinality = getAttributeValue(node, ATTR_CARDINALITY);
				if (cardinality != null) {
					currentComponent.setProperty("cardinality", cardinality);
				}
				String ranking = getAttributeValue(node, ATTR_RANKING);
				if (ranking != null) {
					if (ranking.equalsIgnoreCase("true")) {
						currentComponent.setProperty("policy", "dynamic-priority");
					} else {
						if (ranking.equalsIgnoreCase("false")) {
							currentComponent.setProperty("policy", "dynamic");
						}
					}
				}
				String immediate = getAttributeValue(node, ATTR_IMMEDIATE);
				if (immediate != null) {
					if (immediate.equalsIgnoreCase("true")) {
						currentComponent.setProperty("immediate", "true");
					} else {
						if (immediate.equalsIgnoreCase("false")) {
							currentComponent.setProperty("immediate", "false");
						}
					}
				}
			}
		}
		return currentComponent;
	}
}
