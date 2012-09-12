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

package fr.liglab.adele.cilia.framework.data;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fr.liglab.adele.cilia.exceptions.CiliaException;

/**
 * This class intends to be used when working with XML data.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class XmlTools {
    /**
     * Convert a Node in XML String.
     * @param node to be converted.
     * @return the XML string. 
     */
    public static String nodeToString(Node node) throws CiliaException{
        Source source = new DOMSource(node);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, result);
            String strResult = stringWriter.getBuffer().toString(); 
            strResult = strResult.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            return strResult;
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
            throw new CiliaException (e.getMessage());
        }
    }

    /**
     * Create an org.w3c.dom.Node object using a given xml in a string object.
     * @param xml the xml in a string object
     * @return the create Node
     * @throws CiliaException when there is a problem to build the Node.
     */
    public static Node stringToNode(String xml) throws CiliaException {
    	Node node;
        ByteArrayInputStream is  = new ByteArrayInputStream (xml.getBytes());
        try {
        node = streamToNode(is);
        is.close();
        } catch(Exception ex) {
            throw new CiliaException("Unable to parse: " + xml);
        }
        return node;
    }
    /**
     * Create a node from a given file.
     * @param urlxml URL where the file is.
     * @return the builded node.
     * @throws CiliaException when there is a problem to build the Node.
     */
    public static Node urlToNode(String urlxml) throws CiliaException {
        InputStream is = null;
        URL url = null;
        Node node = null;
        try {
            url = new URL(urlxml);
            is = url.openStream();
            node =  streamToNode(is);
            is.close();
        } catch (FileNotFoundException e) {
            throw new CiliaException("File not found: " + e.getMessage());
        } catch (MalformedURLException e) {
            throw new CiliaException("Unable to load malformed URL");
        } catch (IOException e) {
        	throw new CiliaException("I/O Exception when open file");
        }
        return node;
    }

    public static Node fileToNode(String urlfile) throws CiliaException {
        InputStream is = null;
        Node node = null;
        try {
            is = new FileInputStream(urlfile);
            node = streamToNode(is);
            is.close();
        } catch (FileNotFoundException e) {
            throw new CiliaException("File not found: " + e.getMessage());
        } catch (IOException e) {
        	throw new CiliaException("I/O Exception when open file");
		}

        return node;
    }
    
    public static Node streamToNode(InputStream is) throws CiliaException{
        DocumentBuilderFactory domFactory = 
            DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); 
        DocumentBuilder builder = null;
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new CiliaException("Unable to create DocumentBuilder in XmlTools: " + e.getMessage());
        }
        Document doc = null;
        try {
            doc = builder.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new CiliaException("Unable to create org.w3c.dom.Node from URL in XmlTools: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CiliaException(e.getMessage());
        }
        return doc;
    }

}
