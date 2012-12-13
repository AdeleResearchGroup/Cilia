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
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class ExporterAdapterParser extends DomExtenderParser implements CiliaExtenderParser {

	private ExporterAdapterParser() {
		NAME = "exporter";
	}

	public boolean canHandle(Object elementDescription) {
		Node disp = getNode("adapter-instance", elementDescription);
		if (disp == null) {
			return false;
		}
		return true;
	}

	public Component getComponent(Object componentDescription, Component currentComponent)
			throws CiliaParserException {

		Node node = getNode("adapter-instance", componentDescription);
		if (node != null) {
			String protocol = getAttributeValue(node, "protocol");
			if (protocol != null) {
				currentComponent.setProperty("cilia.exporter.protocol", protocol);
			}
		}
		return currentComponent;
	}
}
