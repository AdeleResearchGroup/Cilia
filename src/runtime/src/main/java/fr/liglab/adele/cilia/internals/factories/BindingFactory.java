package fr.liglab.adele.cilia.internals.factories;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.FactoryStateListener;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.IPojoContext;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.framework.AbstractBindingService;
import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.framework.GenericBindingService;
/**
 * This class defines the binding factory.
 * @author torito
 *
 */
public class BindingFactory extends CiliaComponentFactory implements FactoryStateListener {


    public static final String CLASSNAME = "classname";

    private static final String GENERIC_CLASSNAME = GenericBindingService.class.getName();
    /**
     * Binding service instance.
     */
    protected ComponentInstance bindingComponentInstance = null;
    /**
     * Binding classname.
     */
    protected String className = null;
    /**
     * True when Binding doesn't specifies a classname. It will use a generic classname. 
     */
    protected boolean isGeneric = false; 

    protected short nature = CiliaBindingService.NATURE_UNASSIGNED;

    private String collectorName;

    private String senderName;

    private String senderNS;

    private String collectorNS;

    private Dictionary configuration = new Hashtable();


    /**
     * Binding Factory constructor.
     * @param context OSGi BundleContext.
     * @param element Element used to configure factory (i.e. Component metadata)
     * @throws ConfigurationException When binding doesn't have a specified name or protocol
     */
    public BindingFactory(BundleContext context, Element element)
    throws ConfigurationException {
        super(context, element);

        className = element.getAttribute(CLASSNAME);
        if (className == null) {
            isGeneric = true;
            className = GENERIC_CLASSNAME;
            m_componentMetadata.addAttribute(new Attribute("classname", className));
            updateGenericManipulationInfo();
        } else {
            isGeneric = false;
        }
        m_componentMetadata.addAttribute(new Attribute("public", "false"));
        configureConstituentsDescriptions();
        addProvidedSpecification();
        super.check(m_componentMetadata);
        this.addFactoryStateListener(this);
    }

    public void check(Element element) throws ConfigurationException {}

    public String getComponentType() {
        return "binding";
    }
    /**
     * Add provides handler.
     */
    public List getRequiredHandlerList() {

        List returnedList = new ArrayList();
        List handlerList = super.getRequiredHandlerList();
        Iterator it = handlerList.iterator();
        // Delete required handlers (processor, scheduler, dispatcher)
        while (it.hasNext()) {
            RequiredHandler req = (RequiredHandler) it.next();
            if (!(req.equals(new RequiredHandler("collector", null))) && !(req.equals(new RequiredHandler("collector", DEFAULT_NAMESPACE)))
                    &&  !(req.equals(new RequiredHandler("sender", null))) && !(req.equals(new RequiredHandler("sender", DEFAULT_NAMESPACE)))
            ) {
                if (!returnedList.contains(req)) {
                    returnedList.add(req);
                }
            }
        }
        //Add provides handler, to provide the Binding Service. ;-)
        RequiredHandler reqd = new RequiredHandler("provides", null);
        if (!returnedList.contains(reqd)) {
            returnedList.add(reqd);
        }
        //Add configuration handler. to handle sender/collector types
        RequiredHandler proph = new RequiredHandler("Properties", null);
        if (!returnedList.contains(proph)) {
            returnedList.add(proph);
        }

        return returnedList;
    }


    private void configureConstituentsDescriptions() throws ConfigurationException{
        Element[] colls = m_componentMetadata.getElements("collector");
        Element[] sendss = m_componentMetadata.getElements("sender");
        Element collector = null;

        Element sender = null;

        //Only one sender and collector is valid.
        if (colls != null && colls.length > 1) {
            log.error("Incorrect Binding specification, only one collector must be defined in" + getComponentName());
            throw new ConfigurationException("Incorrect Binding specification, only one collector must be defined in" + getComponentName());
        }
        if (sendss != null && sendss.length > 1) {
            log.error("Incorrect Binding specification, only one sender must be defined in" + getComponentName());
            throw new ConfigurationException("Incorrect Binding specification, only one sender must be defined in" + getComponentName());            
        }
        //get collector name.
        if (colls != null) {
            collector = colls[0];
            collectorName = collector.getAttribute("name");
            if (collectorName == null) {
            	collectorName = collector.getAttribute("type");	
            }
            collectorNS = collector.getAttribute("namespace");
        }
        //get sender name.
        if (sendss != null) {
            sender = sendss[0];
            senderName = sender.getAttribute("name");
            if (senderName == null) {
            	senderName = sender.getAttribute("type");	
            }
            senderNS = sender.getAttribute("namespace");
        }
        //Test if in-out nature.
        if (collectorName != null && senderName != null) {
            nature = CiliaBindingService.NATURE_INOUT;
        }
        //test if IN Only.
        else if (collectorName != null && senderName == null) {
            nature = CiliaBindingService.NATURE_IN;
        }
        //test if Out only.
        else if (senderName != null && collectorName == null) {
            nature = CiliaBindingService.NATURE_OUT;
        }
        addDefaultProperties();
    }

    private void addDefaultProperties() {
        Element properties = null;
        Element[] props = m_componentMetadata.getElements("Properties");
        if (props == null) {
            properties = new Element("Properties", null); 
        } else {
            properties = props[0];
        }
        //add setSenderType
        if (senderName != null) {
            Element setSender = new Element("Property", null);
            setSender.addAttribute(new Attribute("name", "sender.type"));
            setSender.addAttribute(new Attribute("method", "setSenderType"));
            setSender.addAttribute(new Attribute("type", "String"));
            setSender.addAttribute(new Attribute("value", senderName));
            properties.addElement(setSender);
        }
        //Add sender Namespace
        if (senderNS != null) {
            Element setSender = new Element("Property", null);
            setSender.addAttribute(new Attribute("name", "sender.namespace"));
            setSender.addAttribute(new Attribute("method", "setSenderNS"));
            setSender.addAttribute(new Attribute("type", "String"));
            setSender.addAttribute(new Attribute("value", senderNS));
            properties.addElement(setSender);
        }

        //Add setCollectorType
        if (collectorName != null) {
            Element setCollector = new Element("Property", null);
            setCollector.addAttribute(new Attribute("name", "collector.type"));
            setCollector.addAttribute(new Attribute("method", "setCollectorType"));
            setCollector.addAttribute(new Attribute("type", "String"));
            setCollector.addAttribute(new Attribute("value", collectorName));
            properties.addElement(setCollector);
        }
        //Add setCollectorNS
        if (collectorNS != null) {
            Element setCollector = new Element("Property", null);
            setCollector.addAttribute(new Attribute("name", "collector.namespace"));
            setCollector.addAttribute(new Attribute("method", "setCollectorNS"));
            setCollector.addAttribute(new Attribute("type", "String"));
            setCollector.addAttribute(new Attribute("value", collectorNS));
            properties.addElement(setCollector);
        }
        //Add nature.
        Element setNature = new Element("Property", null);
        setNature.addAttribute(new Attribute("name", "binding.nature"));
        setNature.addAttribute(new Attribute("method", "setBindingNature"));
        setNature.addAttribute(new Attribute("type", "String"));
        setNature.addAttribute(new Attribute("value", String.valueOf(nature)));
        properties.addElement(setNature);
        //If there are any properties, we add the created one.
        if (props == null) {
            m_componentMetadata.addElement(properties);
        }
    }
    /**
     * There is possible to create only one instance, and is done when starting the factory.
     */
    public ComponentInstance createInstance(Dictionary config,
            IPojoContext context, HandlerManager[] handlers)
    throws org.apache.felix.ipojo.ConfigurationException {
        return super.createInstance(config, context, handlers);
    }
    /**
     * Method called when initializing the factory service.
     * This method will create a single instance
     * @throws MissingHandlerException 
     * @throws UnacceptableConfiguration 
     */
    public void startingSpecification() throws UnacceptableConfiguration, MissingHandlerException {

        try {
            if (isGeneric) {
                createGenericInstance();
            } else {
                createSingleInstance();
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
    /**
     * Stoping the service factory.
     * This method is called when the factory is disappearing, the binding service must be removed
     */

    public void removingSpecification() {
        if (isGeneric) {
            removeGenericInstance();            
        } else {
            removeSingleInstance();
        }
    }
    /**
     * This method remove the generic instance.
     */
    private void removeGenericInstance() {
        removeSingleInstance();
    }
    /**
     * This method create a generic instance.
     * @throws MissingHandlerException 
     * @throws UnacceptableConfiguration 
     */
    private void createGenericInstance() throws ConfigurationException, UnacceptableConfiguration, MissingHandlerException{
        createSingleInstance();
    }
    /**
     * This hardcode add manipulation info to a generic binding.
     */
    private void updateGenericManipulationInfo()throws ConfigurationException {
        if (isGeneric) {
            Element manip = new Element("manipulation", null);
            //Add parent class.
            manip.addAttribute(new Attribute("super", AbstractBindingService.class.getName()));
            //Add init method added to by ipojo manipulation.
            Element methodInit = new Element("method", null);
            methodInit.addAttribute(new Attribute("name", "init"));
            manip.addElement(methodInit);
            //Add getProperties method.
            Element methodGetProperties = new Element("method", null);
            methodGetProperties.addAttribute(new Attribute("name", "getProperties"));
            methodGetProperties.addAttribute(new Attribute("arguments", "java.util.Dictionary,java.util.Dictionary,fr.liglab.adele.cilia.Binding"));
            methodGetProperties.addAttribute(new Attribute("return", "java.util.Dictionary"));
            manip.addElement(methodGetProperties);
            //Add interface info.
            Element interfaceInfo = new Element("interface", null);
            interfaceInfo.addAttribute(new Attribute("name", CiliaBindingService.class.getName()));
            manip.addElement(interfaceInfo);
            //Add manip Element to metadata.
            m_componentMetadata.addElement(manip);
        }
    }

    private void addProvidedSpecification() {
        //Add provides element.
        Element provides = null;
        Element[] providesArray = m_componentMetadata.getElements("Provides");
        if (providesArray != null) {
            provides = providesArray[0];
        } else {
            provides = new Element("Provides", null);
        }

        Element protocolType = new Element("Property", null);
        protocolType.addAttribute(new Attribute("name", "cilia.binding.type"));
        protocolType.addAttribute(new Attribute("type", "String"));
        protocolType.addAttribute(new Attribute("value", getComponentName()));
        provides.addElement(protocolType);

        Element protocolName = new Element("Property", null);
        protocolName.addAttribute(new Attribute("name", "cilia.binding.protocol"));
        protocolName.addAttribute(new Attribute("type", "String"));
        protocolName.addAttribute(new Attribute("value", getComponentName()));
        provides.addElement(protocolName);
        
        if (providesArray == null) {
            m_componentMetadata.addElement(provides);
        }

    }
    /**
     * 
     * @param config
     *  Configuration to pass to the binding instance.
     * @throws org.apache.felix.ipojo.ConfigurationException
     * @throws MissingHandlerException 
     * @throws UnacceptableConfiguration 
     */
    private void createSingleInstance () throws org.apache.felix.ipojo.ConfigurationException, UnacceptableConfiguration, MissingHandlerException {
        if (nature != CiliaBindingService.NATURE_UNASSIGNED) {
            bindingComponentInstance = createComponentInstance(null);
        }

    }
    /**
     * Removing the binding service instance.
     */
    private void removeSingleInstance() {
        bindingComponentInstance.stop();
        bindingComponentInstance.dispose();
    }



    public void stateChanged(Factory factory, int newState) {
        switch(newState) {
            case INVALID: {
                log.debug("removing binding specification" + this.getComponentName());
                removingSpecification();
                break;
            }
            case VALID: {
                log.debug("adding binding specification" + this.getComponentName());
                try {
                    startingSpecification();
                } catch (UnacceptableConfiguration e) {
                    log.error("invalid configuration",e.getStackTrace());
                } catch (MissingHandlerException e) {
                    log.error("MissingHandler",e.getStackTrace());
                }
                break;
            }
        }
    }
}
