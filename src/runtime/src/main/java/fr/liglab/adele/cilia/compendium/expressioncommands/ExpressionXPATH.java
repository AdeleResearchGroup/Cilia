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

package fr.liglab.adele.cilia.compendium.expressioncommands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.compendium.data.manipulation.XmlTools;
/**
 * XPATH Cilia expression parser to be used in data
 * @author torito
 *
 */
public class ExpressionXPATH  implements CiliaExpression {
    
   /**
     * Evaluate the given expression and see if it match with the given data.
     * @param expression ldap expression.
     * @param data Data to analize
     * @return true if the expression match with the Data. False if not.
     */
    public boolean evaluateBooleanExpression(String expr, Data data) {
        List res =  (List) evaluateExpression (expr, data);
        if (res.size() > 0)
            return true;
        return false;
    }

    
    
    /**
     * 
     * @param expression Expression string to use.
     * @param data which contains content to parse.
     * @return
     */
    public List evaluateExpression(String expression, Data data) {

        String content = (String) data.getContent();
        ByteArrayInputStream is  = new ByteArrayInputStream (content.getBytes());
        DocumentBuilderFactory domFactory = 
            DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); 
        DocumentBuilder builder = null;
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = builder.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = null;
        try {
            expr = xpath.compile(expression);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        Object result = null;
        try {
            result = expr.evaluate(doc, XPathConstants.NODESET);
            
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        NodeList nodes = (NodeList) result;
        List resultData = new ArrayList();
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                resultData.add(XmlTools.nodeToString(nodes.item(i)));
            } catch (CiliaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        return resultData;
    }


    /**
     * TODO: move the next code to another class.
     */
    public String resolveVariables(String expr, Data data) {
        return expr;
    }
    /**
     * 
     */
    public void start() { }
    /**
     * 
     */
    public void stop() {}

}
