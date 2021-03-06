package fr.liglab.adele.cilia.admin.impl;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.*;
import fr.liglab.adele.cilia.framework.data.XmlTools;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.impl.AdapterImpl;
import fr.liglab.adele.cilia.model.impl.MediatorImpl;
import fr.liglab.adele.cilia.model.impl.PatternType;
import fr.liglab.adele.cilia.util.ChainParser;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;
import fr.liglab.adele.cilia.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class CiliaChainInstanceParser implements ChainParser {


    protected String ROOT_FILE = "cilia";
    protected String ROOT_CHAIN = "chain";
    protected String CHAIN_MEDIATORS = "mediators";
    protected String CHAIN_BINDINGS = "bindings";
    protected String CHAIN_ADAPTERS = "adapters";

    protected String MEDIATOR = "mediator-instance";
    protected String MODIFY_MEDIATOR = "mediator";
    protected String ADAPTER = "adapter-instance";
    protected String MODIFY_ADAPTER = "adapter";
    protected String MEDIATOR_TYPE = "type";
    protected String MEDIATOR_Id = "id";

    protected String BINDING = "binding";
    protected String BINDING_from = "from";
    protected String BINDING_to = "to";
    protected String PORT_SEPARATOR = ":";
    protected String BINDING_TYPE = "linker";
    protected String BINDING_TYPE2 = "protocol";

    protected static Logger coreLogger = LoggerFactory.getLogger(Const.LOGGER_CORE);

    private Set<CiliaExtenderParser> customParsers = new HashSet<CiliaExtenderParser>();

    protected volatile static int mediatorNumbers = 0;

    private CiliaContext ccontext;

    /**
     * @param chain
     * @return
     */
    protected Node checkObject(Object chain) {
        if (!(chain instanceof Node)) {
            return null;
        }
        return (Node) chain;
    }


    /**
     * @param xmlFile
     * @return
     */
    public Builder[] obtainChains(URL xmlFile) throws CiliaException, BuilderException, BuilderPerformerException, FileNotFoundException {
        InputStream fis;
        try {
            fis = xmlFile.openStream();
        } catch (IOException e) {
            coreLogger.error("Error when trying to open {} File", xmlFile.getPath(), e);
            throw new CiliaException("Error when trying to open " + xmlFile.getPath()
                    + " File.");
        }
        // First child is the root node.
        Node node = XmlTools.streamToNode(fis).getFirstChild();
        try {
            fis.close();
        } catch (IOException e) {
            coreLogger.error("Error when closing {} File", xmlFile.getPath(), e);
        }
        return parseChains(node);
    }

    public Builder[] parseChains(Object nodeobject) throws CiliaIllegalParameterException, BuilderException, BuilderPerformerException, BuilderConfigurationException {
        List<Builder> listChains = new ArrayList<Builder>();
        Node node = (Node) nodeobject;
        String rootName = node.getNodeName();
        if (rootName.compareTo(ROOT_FILE) != 0) {
            coreLogger.error("Root element must be <"
                    + ROOT_FILE + "> and is <" + rootName + ">");
            throw new CiliaIllegalParameterException("Root element must be <"
                    + ROOT_FILE + "> and is <" + rootName + ">");
        }

        Node possibleChain = node.getFirstChild();
        while (possibleChain != null) {
            String nodeName = possibleChain.getNodeName();
            if (nodeName.compareTo(ROOT_CHAIN) == 0) {
                coreLogger.debug("Found chain in file");
                Builder newChain = parseChain(possibleChain);
                if (newChain == null) {
                    coreLogger.warn("A chain exists in file but is null: ");
                } else {
                    coreLogger.debug("Chain {} has been parsed", newChain.current());
                    listChains.add(newChain);
                }
            }

            possibleChain = possibleChain.getNextSibling();
        }
        if (listChains.isEmpty()) {
            return null;
        }
        Builder[] newArray = new Builder[listChains.size()];
        for (int i = 0; i < listChains.size(); i++)
            newArray[i] = (Builder) listChains.get(i);
        return newArray;
    }

    /**
     * It will convert a Chain in a Node to a chain model.
     *
     * @throws CiliaIllegalParameterException
     * @throws BuilderConfigurationException
     */
    private Builder parseChain(Object objectchain) throws BuilderException, BuilderPerformerException, CiliaIllegalParameterException, BuilderConfigurationException {
        Node nchain = checkObject(objectchain);
        Architecture chain = null;

        if (nchain == null) {
            throw new CiliaIllegalParameterException("Object:" + objectchain
                    + " is not instance of org.w3c.dom.Node");
        }

        if (nchain.getNodeName().compareTo(ROOT_CHAIN) != 0) {
            coreLogger.error(nchain.getNodeName() + "Node is not a chain");
            throw new CiliaIllegalParameterException("Node is not a chain. It must start with <chain>. It is: " + nchain.getNodeName());
        }
        // It must add mediators at first.
        String id = getId(nchain);
        String extentions = getAttributeValue(nchain, "extension");
        //TODO: Having chain type and chain properties
        Builder builder = ccontext.getBuilder();
        if (extentions != null && extentions.compareToIgnoreCase("true") == 0) {
            chain = builder.get(id);
        } else {
            chain = builder.create(id);
        }
        Node node = nchain.getFirstChild();
        while (node != null) {
            if (node.getNodeName().compareToIgnoreCase(CHAIN_MEDIATORS) == 0) {
                getMediators(node, chain);
            }
            node = node.getNextSibling();
        }

        // It must add adapters then.
        Node nodeAdapters = nchain.getFirstChild();
        while (nodeAdapters != null) {
            if (nodeAdapters.getNodeName().compareToIgnoreCase(CHAIN_ADAPTERS) == 0) {
                getAdapters(nodeAdapters, chain);
            }
            nodeAdapters = nodeAdapters.getNextSibling();
        }


        // Add bindings to the chain.
        Node nodeBindings = nchain.getFirstChild();
        while (nodeBindings != null) {
            if (nodeBindings.getNodeName().compareToIgnoreCase(CHAIN_BINDINGS) == 0) {
                setBindings(nodeBindings, chain);
            }
            nodeBindings = nodeBindings.getNextSibling();
        }
        return builder;
    }

    /**
     * @param chain
     * @param node
     * @throws BuilderException
     * @throws BuilderConfigurationException
     */
    protected void setBindings(Node node, Architecture chain) throws BuilderConfigurationException, BuilderException {
        Node binding = node.getFirstChild();
        do {
            if (binding.getNodeName().compareToIgnoreCase(BINDING) == 0) {
                computeBinding(binding, chain);
            }
        } while ((binding = binding.getNextSibling()) != null);

    }

    /**
     * @param chain
     * @param bindingNode
     * @throws BuilderException
     * @throws BuilderConfigurationException
     */
    protected void computeBinding(Node bindingNode, Architecture chain) throws BuilderConfigurationException, BuilderException {

        String sourceMediatorId;
        String sourceMediatorPort;

        String targetMediatorId;
        String targetMediatorPort;

        String bindingType = getAttributeValue(bindingNode, BINDING_TYPE); //Use linker as reserved word.
        if (bindingType == null) {
            bindingType = getAttributeValue(bindingNode, BINDING_TYPE2); //try with protocol (deprecated)
            if (bindingType != null) {
                coreLogger.warn(BINDING_TYPE2 + " is deprecated as reserved word. Use " + BINDING_TYPE + " instead");
            }
        }
        Properties bindingProperties = getProperties(bindingNode);

        String colport = getAttributeValue(bindingNode, BINDING_to);
        String sendport = getAttributeValue(bindingNode, BINDING_from);


        if ((colport == null && sendport != null)
                || (colport != null && sendport == null)) {
            coreLogger.error("Unexpected configuration in binding on the chain {}", chain);
            throw new BuilderConfigurationException("Unexpected configuration in binding");
            //computeHalfBinding(chain, bindingNode, bindingModel);
        } else {
            // get SourceMediator
            String[] targs = colport.split(PORT_SEPARATOR);
            String[] src = sendport.split(PORT_SEPARATOR);
            if (targs.length != 2) {
                targetMediatorPort = "std";

            } else {
                targetMediatorPort = targs[1];
            }
            if (src.length != 2) {
                sourceMediatorPort = "std";

            } else {
                sourceMediatorPort = src[1];
            }

            sourceMediatorId = src[0];
            targetMediatorId = targs[0];

            if (!analizeBindingData(sourceMediatorId, sourceMediatorPort,
                    targetMediatorId, targetMediatorPort)) {
                return;
            }
            chain.bind().using(bindingType).from(sourceMediatorId + ":" + sourceMediatorPort).to(targetMediatorId + ":" + targetMediatorPort).configure(bindingProperties);

        }
    }

    protected boolean analizeBindingData(String sourceMediatorId,
                                         String sourceMediatorPort, String targetMediatorId, String targetMediatorPort) {
        if (sourceMediatorId == null) {
            coreLogger.error("source Mediator is null in binding, unable to bind");
            return false;
        }
        if (sourceMediatorPort == null) {
            coreLogger.error("source Mediator with outport, unable to bind");
            return false;
        }
        if (targetMediatorId == null) {
            coreLogger.error("target Mediator is null in binding, unable to bind");
            return false;
        }
        if (targetMediatorPort == null) {
            coreLogger.error("target Mediator without port, unable to bind");
            return false;
        }
        return true;
    }

    /**
     * Get all mediators.
     *
     * @param node
     * @return
     * @throws BuilderException
     * @throws BuilderConfigurationException
     */
    protected void getMediators(Node node, Architecture chain) throws CiliaIllegalParameterException, BuilderConfigurationException, BuilderException {
        Node mediatorNode = node.getFirstChild();
        int iterator = 0;
        if (mediatorNode == null) {
            throw new CiliaIllegalParameterException("Unable to retrieve first mediator in XML Node");
        }
        do {
            if (mediatorNode.getNodeName().compareTo(MEDIATOR) == 0) {
                getMediator(mediatorNode, chain);
                iterator++;
            } else if (mediatorNode.getNodeName().compareTo(MODIFY_MEDIATOR) == 0) {
                modifyMediator(mediatorNode, chain);
                iterator++;
            }
            mediatorNode = mediatorNode.getNextSibling();
        } while (mediatorNode != null);
        coreLogger.debug("Number of mediators found in chain :" + iterator);

    }

    /**
     * Get all adapters.
     *
     * @param node
     * @return
     * @throws BuilderException
     * @throws BuilderConfigurationException
     */
    protected void getAdapters(Node node, Architecture chain) throws CiliaIllegalParameterException, BuilderConfigurationException, BuilderException {
        Node mediatorNode = node.getFirstChild();
        int iterator = 0;

        if (mediatorNode == null) {
            throw new CiliaIllegalParameterException("Unable to retrieve first adapter in XML Node");
        }
        do {
            if (mediatorNode.getNodeName().compareTo(ADAPTER) == 0) {
                getAdapter(mediatorNode, chain);
                iterator++;
            } else if (mediatorNode.getNodeName().compareTo(MODIFY_ADAPTER) == 0) {
                modifyAdapter(mediatorNode, chain);
                iterator++;
            }
            mediatorNode = mediatorNode.getNextSibling();
        } while (mediatorNode != null);
        coreLogger.debug("Number of adapters found:" + iterator);
    }

    /**
     * Create a mediator from a given Node.
     *
     * @param nmediator Mediator node.
     * @return the new mediator.
     * @throws BuilderException
     * @throws BuilderConfigurationException
     */
    protected void getMediator(Node nmediator, Architecture chain) throws BuilderConfigurationException, BuilderException {
        mediatorNumbers++;
        Mediator mediator = null;
        String mediatorId = getId(nmediator);
        String mediatorType = getType(nmediator);
        String namespace = getAttributeValue(nmediator, "namespace");
        Hashtable mediatorProperties = getMediatorProperties(nmediator);

        if (mediatorId == null) {
            coreLogger.error("Mediator in {} must have an id", chain);
            throw new BuilderConfigurationException("Mediator must have an id");
        }

        if (mediatorType == null) {
            coreLogger.error("Type not found in mediator {} in the chain {}", mediatorId, String.valueOf(chain));
            throw new BuilderConfigurationException("Mediator must have an type");
        }

        if (namespace == null) {
            coreLogger.warn("[{}] does not have a namespace. It will use a default namespace {}", mediatorId, Const.CILIA_NAMESPACE);
            namespace = Const.CILIA_NAMESPACE;
        }

		/*TODO: This code is temporal, Extender parser must be change to use new Builder Pattern*/
        mediator = new MediatorImpl(mediatorId, mediatorType, null, null, null, mediatorProperties, null);
        mediator = (Mediator) extendersParsers(nmediator, mediator);
        coreLogger.debug("[{}] model created with properties:{}", mediatorId, mediator.getProperties());
        chain.create().mediator().type(mediatorType).namespace(namespace).id(mediatorId).configure().set(mediator.getProperties());
    }

    /**
     * Modify a mediator from a given Node.
     *
     * @param nmediator Mediator node.
     * @return the new mediator.
     * @throws BuilderException
     * @throws BuilderConfigurationException
     */
    protected void modifyMediator(Node nmediator, Architecture chain) throws BuilderConfigurationException, BuilderException {
        mediatorNumbers++;
        Mediator mediator = null;
        String mediatorId = getId(nmediator);
        Hashtable mediatorProperties = getMediatorProperties(nmediator);
        if (mediatorId == null) {
            coreLogger.error("ID not found in Mediator XML Node");
            throw new BuilderConfigurationException("Mediator must have an ID");
        }
        /*TODO: This code is temporal, Extender parser must be change to use new Builder Pattern*/
        mediator = new MediatorImpl(mediatorId, "tmp", null, null, null, mediatorProperties, null);
        mediator = (Mediator) extendersParsers(nmediator, mediator);
        chain.configure().mediator().id(mediatorId).set(mediator.getProperties()).set(mediatorProperties);
        coreLogger.debug("[{}] model modified with properties: {}", mediatorId, mediator.getProperties());
    }

    /**
     * Create an adapter from a given Node.
     *
     * @param nadapter adapter node.
     * @return the new adapter.
     * @throws BuilderConfigurationException
     * @throws BuilderException
     */
    protected void getAdapter(Node nadapter, Architecture chain) throws BuilderConfigurationException, BuilderException {
        mediatorNumbers++;
        Adapter adapter = null;
        PatternType pt = getPattern(nadapter);
        String adapterId = getId(nadapter);
        String adapterType = getType(nadapter);
        String namespace = getAttributeValue(nadapter, "namespace");
        Properties adapterProperties = getProperties(nadapter);
        if (adapterType == null) {
            coreLogger.error("Type not found in Adapter XML Node");
            throw new BuilderConfigurationException("Adapter must have an type");
        }
        if (adapterId == null) {
            coreLogger.error("ID not found in Adapter XML Node");
            throw new BuilderConfigurationException("Adapter must have an ID");
        }
        if (namespace == null) {
            coreLogger.warn("[{}] does not have a namespace. It will use a default namespace {}", adapterId, Const.CILIA_NAMESPACE);
            namespace = Const.CILIA_NAMESPACE;
        }
        /*TODO: This code is temporal, Extender parser must be change to use new Builder Pattern*/
        adapter = new AdapterImpl(adapterId, adapterType, null, null, adapterProperties, null, pt);
        adapter = (Adapter) extendersParsers(nadapter, adapter);
        coreLogger.debug("[{}] model created with properties: {}", adapterId, adapter.getProperties());
        chain.create().adapter().type(adapterType).namespace(namespace).id(adapterId).configure().set(adapter.getProperties());
    }

    /**
     * Create an adapter from a given Node.
     *
     * @param nadapter adapter node.
     * @return the new adapter.
     * @throws BuilderConfigurationException
     * @throws BuilderException
     */
    protected void modifyAdapter(Node nadapter, Architecture chain) throws BuilderConfigurationException, BuilderException {
        mediatorNumbers++;
        Adapter adapter = null;
        PatternType pt = getPattern(nadapter);
        String adapterId = getId(nadapter);

        Properties adapterProperties = getProperties(nadapter);

        if (adapterId == null) {
            coreLogger.error("ID not found in Adapter XML Node");
            throw new BuilderConfigurationException("Adapter must have an ID");
        }
		/*TODO: This code is temporal, Extender parser must be change to use new Builder Pattern*/
        adapter = new AdapterImpl(adapterId, "tmp", null, null, adapterProperties, null, pt);
        adapter = (Adapter) extendersParsers(nadapter, adapter);
        coreLogger.debug("[{}] model modified with properties: {}", adapterId, adapter.getProperties());
        chain.configure().adapter().id(adapterId).set(adapter.getProperties()).set(adapterProperties);
    }

    private PatternType getPattern(Node nadapter) {
        PatternType pt = PatternType.UNASSIGNED;
        String pattern = getAttributeValue(nadapter, "pattern");
        if ("in-only".equalsIgnoreCase(pattern)) {
            pt = PatternType.IN_ONLY;
        } else if ("out-only".equalsIgnoreCase(pattern)) {
            pt = PatternType.OUT_ONLY;
        } else if ("in-out".equalsIgnoreCase(pattern)) {
            pt = PatternType.IN_OUT;
        }
        return pt;
    }

    protected Properties getMediatorProperties(Node mediator) {
        Properties props = getProperties(mediator);
        props.putAll(getPropertiesFrom(mediator, "processor"));
        props.putAll(getPropertiesFrom(mediator, "scheduler"));
        props.putAll(getPropertiesFrom(mediator, "dispatcher"));
        props.putAll(getPropertiesFrom(mediator, "monitoring"));
        return props;
    }

    /**
     * Get the properties.
     *
     * @param node node wihi contain properties.
     * @return the properties found.
     */
    protected Properties getProperties(Node node) {
        Properties properties = new Properties();

        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeName().compareTo("property") == 0) {
                if (child.hasAttributes()) {
                    String name = null;
                    String value = null;
                    NamedNodeMap nmp = child.getAttributes();
                    // obtain Property name.
                    Node component = nmp.getNamedItem("name");
                    if (component != null && component.getNodeName() != null) {
                        name = component.getNodeValue();
                    }
                    // obtain Property value.
                    component = nmp.getNamedItem("value");
                    if (component != null && component.getNodeName() != null) {
                        value = component.getNodeValue();
                    }

                    if ((name != null) && (value != null)) {
                        properties.put(name, value);
                    } else if ((name != null) && (value == null)) {
                        Object propEmb = getPropertyValue(child);
                        if (propEmb != null) {
                            properties.put(name, propEmb);
                        }
                    }

                }
            }
            child = child.getNextSibling();
        }
        return properties;
    }

    /**
     * Get the properties.
     *
     * @param componentNode node wihi contain properties.
     * @return the properties found.
     */
    protected Properties getPropertiesFrom(Node componentNode, String preambule) {
        Properties properties = new Properties();
        NodeList nodelist = componentNode.getChildNodes();
        Node child = null;
        Node preambuleNode = null;
        if (preambule != null) {
            for (int i = 0; nodelist != null && i < nodelist.getLength(); i++) { // search
                // preambule
                // Node
                // (<scheduler>,<dispatcher>..)
                child = nodelist.item(i);
                if (child != null
                        && child.getNodeName().compareToIgnoreCase(preambule) == 0) {
                    preambuleNode = child;

                    break;
                }
            }
        } else { // without preambule.
            preambuleNode = componentNode;
        }
        if (preambuleNode == null) {
            return properties;
        }
        Node propertyNode = preambuleNode.getFirstChild();
        while (propertyNode != null) {

            if (propertyNode.getNodeName().compareTo("property") == 0) {
                if (propertyNode.hasAttributes()) {
                    String name = null;
                    String value = null;
                    NamedNodeMap nmp = propertyNode.getAttributes();
                    // obtain Property name.
                    Node component = nmp.getNamedItem("name");
                    if (component != null && component.getNodeName() != null) {
                        name = component.getNodeValue();
                    }
                    // obtain Property value.
                    component = nmp.getNamedItem("value");
                    if (component != null && component.getNodeName() != null) {
                        value = component.getNodeValue();
                    }

                    if (name != null) {
                        // TODO:REMOVE property.
                        if (preambule != null) { // if we have preambule (e.g.
                            // scheduler.<property>)
                            // name = preambule+"." + name;
                        }
                        //
                        if (value != null) {
                            properties.put(name, value);
                        } else { // we search dictionary value.
                            Object propEmb = getPropertyValue(propertyNode);
                            if (propEmb != null) {
                                properties.put(name, propEmb);
                            }
                        }
                    }
                }
            }
            propertyNode = propertyNode.getNextSibling();
        }
        return properties;
    }

    protected Object getPropertyValue(Node propertyNode) {
        Object returnValue = null;
        PropertyType ptype = PropertyType.STRING;
        // This method only acept Map/Array
        Node node = propertyNode.getFirstChild();
        while (node != null) {
            if (node != null && node.getNodeName().compareToIgnoreCase("item") == 0) {
                if (getAttributeValue(node, "key") != null) {
                    ptype = PropertyType.MAP;
                    break;
                } else {
                    ptype = PropertyType.ARRAY;
                    break;
                }
            }
            node = node.getNextSibling();
        }
        if (ptype == PropertyType.MAP) {
            returnValue = getPropertyMap(propertyNode);
        } else if (ptype == PropertyType.ARRAY) {
            returnValue = getPropertyArray(propertyNode);
        } else {
            coreLogger.error("Unsuported property value for " + propertyNode.getNodeName());
        }
        return returnValue;
    }

    private String[] getPropertyArray(Node arrayNode) {
        String[] resultValue = null;
        NodeList nlist = arrayNode.getChildNodes();
        resultValue = new String[nlist.getLength()];
        for (int i = 0; i < nlist.getLength(); i++) {
            Node node = nlist.item(i);
            String value = getAttributeValue(node, "value");
            if (value != null) {
                resultValue[i] = value;
            } else {
                coreLogger.error("Unable to obtain Array property" + arrayNode.getNodeName());
                return null;
            }
        }
        return resultValue;
    }

    private Map getPropertyMap(Node mapProperty) {
        Properties map = new Properties();
        Node node = mapProperty.getFirstChild();

        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String key = getAttributeValue(node, "key");
                String value = getAttributeValue(node, "value");
                if (key != null && value != null) {
                    map.put(key, value);
                } else {
                    coreLogger.error("Unable to obtain Map property" + mapProperty.getNodeName());
                    return null;
                }
            }
            node = node.getNextSibling();
        }
        return map;
    }

    protected String getType(Node node) {
        NamedNodeMap attribs = node.getAttributes();
        if (attribs == null) {
            return null;
        }
        Node nodeType = attribs.getNamedItem("type");
        String type = null;
        if (nodeType != null) {
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
        if (nodeiD != null) {
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
        if (nodeAttr != null) {
            value = nodeAttr.getNodeValue();
        }
        return value;
    }

    protected String getNodeValue(Node node) { // get the value of the Text node
        // in element.
        NodeList childNode = node.getChildNodes();
        String value = null;
        for (int i = 0; childNode != null && i < childNode.getLength(); i++) {
            Node chnode = childNode.item(i);
            if (chnode.getNodeType() == Node.TEXT_NODE) {
                value = chnode.getNodeValue();
                break;
            }
        }
        return value;
    }

    protected void bindExtenderParsers(CiliaExtenderParser parser) {
        synchronized (customParsers) {
            customParsers.add(parser);
        }
    }

    protected void unbindExtenderParser(CiliaExtenderParser parser) {
        synchronized (customParsers) {
            customParsers.remove(parser);
        }
    }

    private Component extendersParsers(Node node, Component component) {
        Set<CiliaExtenderParser> currentParsers = null;
        synchronized (customParsers) {
            currentParsers = new HashSet<CiliaExtenderParser>(customParsers);
        }
        Iterator<CiliaExtenderParser> it = currentParsers.iterator();
        while (it.hasNext()) {
            CiliaExtenderParser cep = it.next();
            if (cep.canHandle(node)) {
                try {
                    component = cep.getComponent(node, component);
                } catch (CiliaParserException e) {
                    e.printStackTrace();
                }
            }
        }
        return component;
    }

    public enum PropertyType {
        STRING, MAP, ARRAY
    }

    ;
}
