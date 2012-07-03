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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class DomExtenderParser {

	//protected String NAMESPACE = "fr.imag.adele.cilia.dispatcher";
	protected String NAMESPACE="cilia.compendium" ;
	protected String NAME = "extender";

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
					&& child.getNamespaceURI().equalsIgnoreCase(NAMESPACE)
					&& child.getLocalName().equalsIgnoreCase(NAME)) {
				return child;
			}
			child = child.getNextSibling();
		}
		return null;
	}

	protected String getAttributeValue(Node node, String attrName) {
		NamedNodeMap attribs = node.getAttributes();
		if (attribs == null) {
			return null;
		}
		Node nodeAttr = attribs.getNamedItem(attrName);
		String value = null;
		if (nodeAttr != null) {
			value = nodeAttr.getNodeValue();
		}
		return value;
	}

	/**
	 * Retreive a Node (baseName)
	 * 
	 * @param sTag
	 *            localname (base)
	 * @param e
	 *            node to look for
	 * @return null if not found or the node
	 */
	protected static Node getElement(String sTag, Node e) {
		Node node;
		for (Node n = e; n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				node = getTagValue(sTag, (Element) n);
				if (node !=null) return node ;
			}
		}
		return null;
	}

	protected Node nextElementSibling(Node elem) {
		do {
			elem = elem.getNextSibling();
		} while ((elem != null) && (elem.getNodeType() != Node.ELEMENT_NODE));

		return elem;
	}
	
	private static Node getTagValue(String sTag, Element eElement) {
		NodeList nList = eElement.getElementsByTagName(sTag);
		if (nList != null) {
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if ((node != null) && (node.getLocalName() != null)) {
					if (node.getLocalName().equalsIgnoreCase(sTag))
						return node;
				}
			}
		}
		return null;
	}
	
	
}
