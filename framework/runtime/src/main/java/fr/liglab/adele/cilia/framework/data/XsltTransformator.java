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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.Data;

/**
 * This class transform an XML in another XMl using XSLT.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class XsltTransformator {
    /**
     * Transforms the given xml Node, using XSL in a form of a Node.
     * @param nsource XML to transform.
     * @param xsltNode XSLT to use to transform.
     * @return The transformation result.
     * @throws CiliaException When there is not possible to transform XMl.
     */
    public static Node nodeTransformFromNode (Node nsource, Node xsltNode) throws CiliaException{
        Node resultNode = null;
        
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            
            Templates template = factory.newTemplates(new DOMSource(xsltNode)); //xslt to use
            Transformer xformer = template.newTransformer();
            Source source = new DOMSource(nsource);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            resultNode = builder.newDocument();
            Result result = new DOMResult(resultNode);
            xformer.transform(source, result);
            
        } catch (TransformerConfigurationException e1) {
            e1.printStackTrace();
            throw new CiliaException (e1.getMessage());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new CiliaException (e.getMessage());
        } catch (TransformerException e) {
            e.printStackTrace();
            throw new CiliaException (e.getMessage());
        }
        
        return resultNode;
    }
    /**
     * Transforms the content of Data using XSLT.
     * The content of the given data must be an string which could be transformed in a xml node.
     * @param data data to transform.
     * @param xsltUrl URL where the xslt is located.
     * @return return a new data which content is the new xml.
     * @throws CiliaException when there is a problem transformating the xml.
     */
    public static Data dataTransformFromURL(Data data, String xsltUrl) throws CiliaException{
        String content = (String) data.getContent();
        
        Node xmlNode = XmlTools.stringToNode(content);
        Node xsltNode = XmlTools.urlToNode(xsltUrl);
        Node noderesult = nodeTransformFromNode(xmlNode, xsltNode);
        
        Data returnData = (Data)data.clone();
        returnData.setContent(XmlTools.nodeToString(noderesult));
        return returnData;
    }
    
    /**
     * Transforms the content of Data using XSLT.
     * The content of the given data must be an string which could be transformed in a xml node.
     * @param data data to transform.
     * @param xsltPathFile Path File where the xslt is located.
     * @return return a new data which content is the new xml.
     * @throws CiliaException when there is a problem transformating the xml.
     */
    public static Data dataTransformFromPathFile(Data data, String xsltPathFile) throws CiliaException{
        String content = (String) data.getContent();
        
        Node xmlNode = XmlTools.stringToNode(content);
        Node xsltNode = XmlTools.fileToNode(xsltPathFile);
        Node noderesult = nodeTransformFromNode(xmlNode, xsltNode);
        
        Data returnData = (Data)data.clone();
        returnData.setContent(XmlTools.nodeToString(noderesult));
        return returnData;
    }
    
    /**
     * Transforms the given node using the xslt located in the given URL.
     * @param node Node to transofm.
     * @param xslturl location of xslt.
     * @return the new Node.
     * @throws CiliaException when there is a problem transformating the xml.
     */
    public static Node nodeTransformFromURL (Node node, String xslturl) throws CiliaException{
        Node xsltNode = XmlTools.urlToNode(xslturl);
        Node noderesult = nodeTransformFromNode(node, xsltNode);
        return noderesult;
    }
    /**
     * Transforms the given xml in the given string, using the xslt located in the given URL.
     * @param source xml in a string object.
     * @param xslturl location of xslt.
     * @return the New xml in a string format.
     * @throws CiliaException when there is a problem transformating the xml.
     */
    public static String stringTransformFromURL (String source, String xsltUrl) throws CiliaException {
        Node xmlNode = XmlTools.stringToNode(source);
        Node xsltNode = XmlTools.urlToNode(xsltUrl);
        Node noderesult = nodeTransformFromNode(xmlNode, xsltNode);
        return XmlTools.nodeToString(noderesult);
    }
    
    public static String stringTransformFromFilePath(String xml, String filepath) throws CiliaException {
        Node xmlNode = XmlTools.stringToNode(xml);
        Node xsltNode = XmlTools.fileToNode(filepath);
        Node noderesult = nodeTransformFromNode(xmlNode, xsltNode);
        return XmlTools.nodeToString(noderesult);
    }
}
