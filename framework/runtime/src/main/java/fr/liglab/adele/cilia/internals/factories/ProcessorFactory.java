package fr.liglab.adele.cilia.internals.factories;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ProcessorFactory extends CiliaComponentFactory {


    private static final String COMPONENT_TYPE = "processor";


    public ProcessorFactory(BundleContext context, Element element)
            throws ConfigurationException {
        super(context, element);

        updateMetadata();
    }

    protected void updateMetadata() {
        Element scheduler = null;
        Element dispatcher = null;
        Element monitor = null;
        if (!m_componentMetadata.containsElement("scheduler", DEFAULT_NAMESPACE)) {
            scheduler = new Element("scheduler", DEFAULT_NAMESPACE);
            m_componentMetadata.addElement(scheduler);
        } else {
            scheduler = m_componentMetadata.getElements("scheduler", DEFAULT_NAMESPACE)[0];
        }
        if (!m_componentMetadata.containsElement("dispatcher", DEFAULT_NAMESPACE)) {
            dispatcher = new Element("dispatcher", DEFAULT_NAMESPACE);
            m_componentMetadata.addElement(dispatcher);
        } else {
            dispatcher = m_componentMetadata.getElements("dispatcher", DEFAULT_NAMESPACE)[0];
        }

        if (!m_componentMetadata.containsElement("monitor-handler", DEFAULT_NAMESPACE)) {
            monitor = new Element("monitor-handler", DEFAULT_NAMESPACE);
            m_componentMetadata.addElement(monitor);
        } else {
            monitor = m_componentMetadata.getElements("monitor-handler", DEFAULT_NAMESPACE)[0];
        }

    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List getRequiredHandlerList() {
        List handlerList;
        List returnedList = new ArrayList();
        handlerList = super.getRequiredHandlerList();
        Iterator it = handlerList.iterator();
        // Delete required handlers (processor, scheduler, dispatcher)
        while (it.hasNext()) {
            RequiredHandler req = (RequiredHandler) it.next();
            if (!(req.equals(new RequiredHandler("method", null))) && !(req.equals(new RequiredHandler("method", DEFAULT_NAMESPACE)))) {
                if (!returnedList.contains(req)) {
                    returnedList.add(req);
                }
            }
            if (req.equals(new RequiredHandler("method", null))) {
                m_logger.log(Logger.WARNING, "Method in mediator must be configured with cilia namespace (" + DEFAULT_NAMESPACE + ")");
            }
        }
        // Add requires handler.
        RequiredHandler reqs = new RequiredHandler("scheduler",
                DEFAULT_NAMESPACE);
        if (!returnedList.contains(reqs)) {
            returnedList.add(reqs);
        }
        RequiredHandler reqd = new RequiredHandler("dispatcher",
                DEFAULT_NAMESPACE);
        if (!returnedList.contains(reqd)) {
            returnedList.add(reqd);
        }
        // Add requires handler.
        RequiredHandler reqm = new RequiredHandler("monitor-handler",
                DEFAULT_NAMESPACE);
        if (!returnedList.contains(reqm)) {
            returnedList.add(reqm);
        }
        return returnedList;
    }
}
