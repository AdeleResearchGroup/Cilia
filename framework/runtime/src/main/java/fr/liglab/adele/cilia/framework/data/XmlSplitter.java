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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class XmlSplitter {
    /**
     * Split a given org.ow2.chameleon.cilia.Data using a given XPath expression.
     * The given data must have as content an Xml in a form of string.
     *
     * @param data       The Data to split.
     * @param expression The expression to use to split Node.
     * @return a List of org.w3c.dom.Node objects
     * @throws CiliaException When there is an error building the Xml parser or when there is a problem parsing the Node
     */
    public static List split(Data data, String expression) throws CiliaException {
        return split(data, expression, true);
    }

    public static List split(Data data, String expression, boolean addSplitInfo) throws CiliaException {
        List returnedDataSet = new ArrayList();
        String content = (String) data.getContent();
        Node docNode = XmlTools.stringToNode(content);
        List listOfNodes = split(docNode, expression);

        for (int i = 0; i < listOfNodes.size(); i++) {
            Data ndata = (Data) data.clone();
            String dataString = XmlTools.nodeToString((Node) listOfNodes.get(i));
            ndata.setContent(dataString);
            ndata = DataEnrichment.addCorrelationInfo(ndata, listOfNodes.size(), i, String.valueOf(data.hashCode()));
            returnedDataSet.add(ndata);
        }
        return returnedDataSet;
    }


    /**
     * Split a given org.w3c.dom.Node using a given XPath expression.
     *
     * @param node       The node to split.
     * @param expression The expression to use to split Node.
     * @return a List of org.w3c.dom.Node objects
     * @throws CiliaException When there is an error building the Xml parser or when there is a problem parsing the Node
     */
    public static List split(Node node, String expression) throws CiliaException {
        List returnedDataSet = new ArrayList();

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = null;
        try {
            expr = xpath.compile(expression);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new CiliaException("Expression : " + expression + " is not valid in XmlSplitter" + e.getMessage());
        }
        Object result = null;
        try {
            result = expr.evaluate(node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new CiliaException("Expression : " + expression + " throws an exception" + e.getMessage());
        }
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nn = nodes.item(i);
            returnedDataSet.add(nn);
        }

        if (nodes.getLength() == 0) {
            returnedDataSet.add(node);
        }
        return returnedDataSet;
    }


}
