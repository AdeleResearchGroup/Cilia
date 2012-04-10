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

package fr.liglab.adele.cilia.model.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.liglab.adele.cilia.CiliaException;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;

/**
 * This class is used to convert chain model to
 * Xml Node and vice versa.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class XmlChainParser implements ChainParser {

    private static final Logger logger = LoggerFactory.getLogger("cilia.core");

    private static int mediatorNumbers = 0;

    public XmlChainParser() {
    }
    /**
     * 
     * @param chain
     * @return
     */
    protected Node checkObject(Object chain) {
        if ( !(chain instanceof Node) ) {
            return null;
        }
        return (Node)chain;
    }
    /**
     * It will convert a Chain model to
     * a Chain in a Node.
     */
    public Object convertChain(Chain chain) {
        logger.error("public Object convertChain(Chain chain){..} method is not implemented");
        throw new UnsupportedOperationException(" This method is not implemented");
    }
    /**
     * 
     * @param xmlFile
     * @return
     */
    public Chain[] obtainChains(URL xmlFile) throws CiliaException, FileNotFoundException{
        List listChains = new ArrayList();
        InputStream fis;
        try {
            fis = xmlFile.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CiliaException("Error when trying to open " + xmlFile.getPath() + " File.");
        }
        //First child is the root node.
        Node node = XmlTools.streamToNode(fis).getFirstChild();
        String rootName = node.getNodeName();
        if (rootName.compareTo("cilia") != 0) {
            throw new CiliaException(xmlFile.getPath() + " Root element must be <cilia> and is <" + rootName + ">");
        }
        logger.debug("Found cilia tag");
        Node possibleChain = node.getFirstChild();
        while (possibleChain!= null) {
            String nodeName = possibleChain.getNodeName(); 
            if (nodeName.compareTo("chain") == 0) {
                logger.debug("Found chain in file: ");
                Chain newChain = parseChain(possibleChain);
                if (newChain == null) {
                    logger.warn("Found chain in file but is null: ");
                } else {
                    listChains.add(newChain);
                }
            }

            possibleChain = possibleChain.getNextSibling();
        }
        if (listChains.isEmpty()) {
            return null;
        }
        Chain[] newArray = new Chain[listChains.size()];
        for (int i = 0; i < listChains.size(); i++)
            newArray[i] = (Chain)listChains.get(i);
        return newArray;
    }
    /**
     * It will convert a Chain in a Node to
     * a chain model.
     */
    public Chain parseChain(Object chain) throws CiliaException{
        Node nchain = checkObject(chain);
        Chain newChain = null;
        Mediator[] mediators = null;
        if (nchain == null) {
            throw new CiliaException ("Object:" + chain + " is not instance of org.w3c.dom.Node");
        }


        if (nchain == null || nchain.getNodeName().compareTo("chain")!=0) {
            logger.debug(nchain.getNodeName() + "Node is not a chain");
            return null;
        }
        //It must add mediators at first.
        String id = getId(nchain);
        String type = getType(nchain);
        Properties props = getProperties(nchain);
        newChain = new Chain(id, type, null, props);
        Node node = nchain.getFirstChild();
        while (node != null) {
            if (node.getNodeName().compareToIgnoreCase("mediators") == 0) {
                mediators = getMediators(node);
            }
            node = node.getNextSibling();
        }
        //Add mediators to the new chain.
        for (int i = 0 ; mediators !=null && i < mediators.length; i++) {
            newChain.add(mediators[i]);
        }
        //Add bindings to the chain.
        Node nodeBindings = nchain.getFirstChild();
        while (nodeBindings != null) {
            if (nodeBindings.getNodeName().compareToIgnoreCase("bindings") == 0) {
                setBindings(newChain, nodeBindings);
            }
            nodeBindings = nodeBindings.getNextSibling();
        }


        return newChain;
    }
    /**
     * 
     * @param chain
     * @param node
     */
    protected void setBindings(Chain chain, Node node) {
        Node binding = node.getFirstChild();
        do {
            if (binding.getNodeName().compareToIgnoreCase("bind") == 0) {
                computeBinding(chain, binding);
            }
        }while((binding = binding.getNextSibling()) != null);

    }
    /**
     * 
     * @param chain
     * @param bindingNode
     */
    protected void computeBinding(Chain chain, Node bindingNode) {
        Binding bindingModel = null;
        Mediator sourceMediator;
        Mediator targetMediator;
        String sourceMediatorId;
        String sourceMediatorPort;

        String targetMediatorId;
        String targetMediatorPort;


        String bindingId = getId(bindingNode);
        String bindingType = getType(bindingNode);
        Properties bindingProperties = getProperties(bindingNode);
        bindingModel = new Binding(bindingId, bindingType, null, bindingProperties);
        //
        String colport = getAttributeValue(bindingNode, "to");
        String sendport = getAttributeValue(bindingNode, "from");
        if ((colport == null && sendport != null ) || (colport != null && sendport == null )) {
        	logger.error("Half binding is not supported, from and to is need.");
        	//computeHalfBinding(chain, bindingNode, bindingModel);
        } else {
            //get SourceMediator
            String[] targs = colport.split("->");
            String[] src = sendport.split("->");
            if (targs.length != 2 ) {
                logger.warn("Mediators in bindings must have ports. Found" + colport + ". std port will be used");
                targetMediatorPort = "std";                

            } else {
                targetMediatorPort = targs [1];                
            }
            if (src.length != 2) {
                logger.warn("Mediators in bindings must have ports. Found" + sendport + ". std port will be used");
                sourceMediatorPort = "std";                

            } else {
                sourceMediatorPort = src[1];
            }


            sourceMediatorId = src[0];
            targetMediatorId = targs[0];


            if (!analizeBindingData(sourceMediatorId, sourceMediatorPort, targetMediatorId, targetMediatorPort)) {
                return ;
            }
            sourceMediator = chain.getMediator(sourceMediatorId);
            targetMediator = chain.getMediator(targetMediatorId);
            if (sourceMediator == null) {
                logger.error("Binding not added. Mediator " + sourceMediatorId + "does not exist in chain" + chain.getId());
                return;
            }
            if (targetMediator == null) {
                logger.error("Binding not added. Mediator " + targetMediatorId + "does not exist in chain" + chain.getId());
                return;
            }
            chain.bind(sourceMediator.getOutPort(sourceMediatorPort), targetMediator.getInPort(targetMediatorPort));
        }
    }

    protected boolean analizeBindingData(String sourceMediatorId,
            String sourceMediatorPort, String targetMediatorId,
            String targetMediatorPort) {
        if (sourceMediatorId == null) {
        	logger.error("source Mediator is null in binding, unable to bind");
            return false;
        }
        if (sourceMediatorPort == null) {
        	logger.error("source Mediator with outport, unable to bind");
            return false;
        }
        if (targetMediatorId == null) {
        	logger.error("target Mediator is null in binding, unable to bind");
            return false;
        }
        if (targetMediatorPort == null) {
        	logger.error("target Mediator without port, unable to bind");
            return false;
        }
        return true;
    }
    /**
     * Get all mediators.
     * @param node
     * @return
     */
    protected Mediator[] getMediators(Node node) {
        Node mediatorNode = node.getFirstChild();
        List mediatorsList = new ArrayList();
        if (mediatorNode == null) {
            return null;
        }
        do {
            if (mediatorNode.getNodeName().compareTo("mediator")==0) {
                Mediator mediator = getMediator(mediatorNode);
                if (mediator == null) {
                	logger.error("Unable to Mediator :" );
                } else {
                    mediatorsList.add(mediator);
                    logger.debug("Adding Mediator:" + mediator.getId());
                }
            }
            mediatorNode = mediatorNode.getNextSibling();
        }while(mediatorNode != null);
        if (mediatorsList.isEmpty()) {
            return null;
        }
        logger.debug("Number of mediators found:" + mediatorsList.size());
        Mediator[] mediators = new Mediator[mediatorsList.size()];
        for (int i = 0; i < mediatorsList.size(); i++)
            mediators[i] = (Mediator)mediatorsList.get(i);
        return mediators;
    }

    /**
     * Create a mediator from a given Node.
     * @param nmediator Mediator node.
     * @return the new mediator.
     */
    protected Mediator getMediator(Node nmediator) {
        mediatorNumbers++;
        Mediator mediator = null;
        String mediatorId = getId(nmediator);
        String mediatorType = getType(nmediator);;
        Properties mediatorProperties = getProperties(nmediator);

        if (mediatorType == null) {
        	logger.error("Type not found in Mediator Node");
            return null;
        }
        if (mediatorId == null) {
            mediatorId = mediatorType + mediatorNumbers;
        }

        mediator = new Mediator(mediatorId, mediatorType, mediatorProperties);
        return mediator;
    }

    /**
     * Get the properties.
     * @param node node wihi contain properties.
     * @return the properties found.
     */
    protected Properties getProperties(Node node) {
        Properties  properties = new Properties();

        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeName().compareTo("property") == 0) {
                if (child.hasAttributes()) {
                    String name = null;
                    String value = null;
                    NamedNodeMap nmp = child.getAttributes();
                    //obtain Property name.
                    Node component = nmp.getNamedItem("name");
                    if (component != null && component.getNodeName() != null) {
                        name = component.getNodeValue();
                    }
                    // obtain Property value.
                    component = nmp.getNamedItem("value");
                    if (component != null && component.getNodeName() != null) {
                        value = component.getNodeValue();
                    }
                    if ( (name != null) && (value != null) ) {
                        properties.put(name, value);
                    } else if ((name != null) && (value == null)) {
                        Properties propEmb = getProperties(child);
                        if ( !propEmb.isEmpty()) {
                            properties.put(name, propEmb);
                        }
                    }
                }
            }
            child = child.getNextSibling();
        }
        return properties;
    }

    protected String getType(Node node) {
        NamedNodeMap attribs = node.getAttributes();
        if (attribs == null) {
            return null;
        }
        Node nodeType = attribs.getNamedItem("type");
        String type = null;
        if (nodeType!=null){
            type = nodeType.getNodeValue();
        }
        return type;
    }

    protected String getId(Node node) {
        NamedNodeMap attribs = node.getAttributes();
        if (attribs == null) {
            return null;
        }
        Node nodeiD = attribs.getNamedItem("id");
        String id = null;
        if (nodeiD!=null){
            id = nodeiD.getNodeValue();
        }
        return id;
    }

    protected String getAttributeValue(Node node, String attrName) {
        NamedNodeMap attribs = node.getAttributes();
        if (attribs == null) {
            return null;
        }
        Node nodeAttr = attribs.getNamedItem(attrName);
        String value = null;
        if (nodeAttr!=null){
            value = nodeAttr.getNodeValue();
        }
        return value;
    }
    public String getChainType() {
        return "cilia";
    }
}
