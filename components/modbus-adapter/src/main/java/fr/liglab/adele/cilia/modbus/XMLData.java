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

package fr.liglab.adele.cilia.modbus;

import java.io.StringWriter;
import java.util.BitSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.util.Const;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class XMLData implements DataFormater {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

	public XMLData() {
	}

	public Data data(String key, int ref, Integer[] values) {
		Document doc = createDocument();
		Element root = doc.createElement(TAG_ROOT_RESPONSE);
		doc.appendChild(root);
		Element request = doc.createElement(key);
		root.appendChild(request);
		int offset = ref;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				Element e = doc.createElement(TAG_VALUE);
				e.setAttribute(ATTR_REGISTER, Integer.toString(offset++));
				e.setTextContent(values[i].toString());
				request.appendChild(e);
			}
		}
		dumpXml(doc);
		return new Data(doc, "xml");
	}

	public Data data(String key, int ref, BitSet values) {
		Document doc = createDocument();
		Element root = doc.createElement(TAG_ROOT_RESPONSE);
		doc.appendChild(root);
		Element request = doc.createElement(key);
		root.appendChild(request);
		int offset = ref;
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				Element e = doc.createElement(TAG_VALUE);
				e.setAttribute(ATTR_BIT, Integer.toString(offset++));
				e.setTextContent(Boolean.toString(values.get(i)));
				request.appendChild(e);
			}
		}
		dumpXml(doc);
		return new Data(doc, "xml");
	}

	public Document createDocument() {
		Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			doc = parser.newDocument();
			doc.setXmlVersion("1.0");
			doc.setXmlStandalone(true);
		} catch (Exception e) {
			logger.error("Internal xml error");
		}
		return doc;
	}

	public void dumpXml(Document doc) {
		if (logger.isDebugEnabled()) {
			try {
				TransformerFactory tranFactory = TransformerFactory.newInstance();
				Transformer aTransformer = tranFactory.newTransformer();
				Source src = new DOMSource(doc);
				StringWriter writer = new StringWriter();
				Result dest = new StreamResult(writer);
				aTransformer.transform(src, dest);
				logger.debug(writer.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
