package fr.liglab.adele.cilia.internals.factories;

import fr.liglab.adele.cilia.model.impl.CollectorImpl;
import fr.liglab.adele.cilia.model.impl.PatternType;
import fr.liglab.adele.cilia.model.impl.SenderImpl;
import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;
import fr.liglab.adele.cilia.util.Const;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

public class AdapterFactory extends MediatorFactory implements AdapterFactoryI {

    private static final Logger logger = LoggerFactory
            .getLogger(Const.LOGGER_RUNTIME);

    private static final String COMPONENT_TYPE = "adapter";

    private volatile PatternType adapterType = PatternType.UNASSIGNED;

    /**
     * @return the adapterType
     */
    public PatternType getPattern() {
        return adapterType;
    }

    private Element constituent;

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public AdapterFactory(BundleContext context, Element element)
            throws ConfigurationException {

        super(context, element);
        settingUpPattern();
    }

    /**
     * Check if the Adapter configuration is valid.
     */
    public void check(Element metadata) throws ConfigurationException {
        StringBuffer msg;
        String name = metadata.getAttribute("name");
        if (name == null) {
            msg = new StringBuffer().append("An adapter needs a name : ")
                    .append(metadata);
            logger.error(msg.toString());
            throw new ConfigurationException(msg.toString());
        }

        String pattern = metadata.getAttribute("pattern");
        if (pattern == null) {
            msg = new StringBuffer().append(name).append(" adapter needs a pattern : ").append(
                    metadata);
            logger.error(msg.toString());
            throw new ConfigurationException(msg.toString());
        }
        computeConstituantsDescriptions();
        computePorts();
    }

    public List getRequiredHandlerList() {

        addDefaultElements();

        List handlerList;
        List returnedList = new ArrayList();
        handlerList = super.getRequiredHandlerList();
        Iterator it = handlerList.iterator();
        while (it.hasNext()) {
            RequiredHandler req = (RequiredHandler) it.next();
            if ((!req.equals(new RequiredHandler("sender", null)))
                    && (!req.equals(new RequiredHandler("collector", null)))) {
                if (!returnedList.contains(req)) {
                    returnedList.add(req);
                }
            }
        }
        return returnedList;
    }

    private void addDefaultElements() {
        if (!m_componentMetadata.containsElement("scheduler", DEFAULT_NAMESPACE)) {
            Element scheduler = new Element("scheduler", DEFAULT_NAMESPACE);
            scheduler.addAttribute(new Attribute("name", "immediate-scheduler"));
            scheduler.addAttribute(new Attribute("namespace", DEFAULT_NAMESPACE));
            m_componentMetadata.addElement(scheduler);
        }

        if (!m_componentMetadata.containsElement("processor", DEFAULT_NAMESPACE)) {
            Element dispatcher = new Element("processor", DEFAULT_NAMESPACE);
            dispatcher.addAttribute(new Attribute("name", "simple-processor"));
            dispatcher.addAttribute(new Attribute("namespace", DEFAULT_NAMESPACE));
            m_componentMetadata.addElement(dispatcher);
        }

        if (!m_componentMetadata.containsElement("dispatcher", DEFAULT_NAMESPACE)) {
            Element dispatcher = new Element("dispatcher", DEFAULT_NAMESPACE);
            dispatcher.addAttribute(new Attribute("name", "multicast-dispatcher"));
            dispatcher.addAttribute(new Attribute("namespace", DEFAULT_NAMESPACE));
            m_componentMetadata.addElement(dispatcher);
        }
    }

    private void settingUpPattern() throws ConfigurationException {
        String msg;
        if (!m_componentMetadata.containsAttribute("pattern")) {
            msg = getComponentName() + " Adapter must contain a defined pattern";
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
        String pattern = m_componentMetadata.getAttribute("pattern");
        if ("in-only".compareToIgnoreCase(pattern) == 0) {
            adapterType = PatternType.IN_ONLY;
        } else if ("out-only".compareToIgnoreCase(pattern) == 0) {
            adapterType = PatternType.OUT_ONLY;
        } else if ("in-out".compareToIgnoreCase(pattern) == 0) {
            adapterType = PatternType.IN_OUT;
        } else {
            msg = getComponentName() + "adapter must contain a valid pattern (in-only, out-only, in-out)";
            logger.error("[{}] adapter must contain a valid pattern (in-only, out-only, in-out)", getComponentName());
            throw new ConfigurationException(msg);
        }

        getConstituents();

    }

    private void getConstituents() throws ConfigurationException {
        Element[] constituents = null;
        if (adapterType.equals(PatternType.IN_ONLY)) {
            if (m_componentMetadata.containsElement("collector", DEFAULT_NAMESPACE)) {
                constituents = m_componentMetadata.getElements("collector", DEFAULT_NAMESPACE);
            } else if (m_componentMetadata.containsElement("collector")) {
                constituents = m_componentMetadata.getElements("collector");
            }
            if ((constituents == null) || (constituents.length != 1)) {
                String msg = "Adapter must contain only one valid collector";
                throw new ConfigurationException(msg);
            }
            constituent = constituents[0];
        } else if (adapterType.equals(PatternType.OUT_ONLY)) {
            if (m_componentMetadata.containsElement("sender", DEFAULT_NAMESPACE)) {
                constituents = m_componentMetadata.getElements("sender", DEFAULT_NAMESPACE);
            } else if (m_componentMetadata.containsElement("sender")) {
                constituents = m_componentMetadata.getElements("sender");
            }

            if ((constituents == null) || (constituents.length != 1)) {
                String msg = "Adapter must contain only one valid sender";
                logger.error(msg);
                throw new ConfigurationException(msg);
            }
            constituent = constituents[0];
        }
    }

    public ComponentInstance createInstance(Dictionary config, IPojoContext context,
                                            HandlerManager[] handlers)
            throws org.apache.felix.ipojo.ConfigurationException {
        StringBuffer msg;
        MediatorManager mi = (MediatorManager) super.createInstance(config, context,
                handlers);
        InstanceManager pi = (InstanceManager) mi.getProcessorInstance();
        if (constituent.getAttribute("type") == null) {
            msg = new StringBuffer().append(
                    "sender or collector type is undefined for the adapter type ")
                    .append(getComponentName());
            logger.error(msg.toString());
            throw new org.apache.felix.ipojo.ConfigurationException(msg.toString());
        }
        if (adapterType.equals(PatternType.IN_ONLY)) {
            SchedulerHandler scheduler = (SchedulerHandler) pi
                    .getHandler(Const.ciliaQualifiedName("scheduler"));
            if (scheduler == null) {
                logger.warn("scheduler handler is invalid");
            }
            mi.addCollector("std", new CollectorImpl(constituent.getAttribute("type"), constituent.getAttribute("type"), null, "unique", config));

        }
        if (adapterType.equals(PatternType.OUT_ONLY)) {
            DispatcherHandler dispatcher = (DispatcherHandler) pi
                    .getHandler(Const.ciliaQualifiedName("dispatcher"));
            if (dispatcher == null) {
                logger.warn("dispatcher handler is invalid");
            }
            mi.addSender("std", new SenderImpl(constituent.getAttribute("type"), constituent.getAttribute("type"), null, "unique", config));
        }
        return mi;
    }
}
