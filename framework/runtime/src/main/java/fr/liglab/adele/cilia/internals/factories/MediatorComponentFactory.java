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
package fr.liglab.adele.cilia.internals.factories;

import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.model.impl.PortImpl;
import fr.liglab.adele.cilia.model.impl.PortType;
import fr.liglab.adele.cilia.util.Const;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public abstract class MediatorComponentFactory extends CiliaComponentFactory {


    protected static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);

    private Hashtable inPorts;
    private Hashtable outPorts;

    protected Component processorDescription;

    protected Component schedulerDescription;

    protected Component dispatcherDescription;

    /**
     * @return the schedulerDescription
     */
    protected Component getSchedulerDescription() {
        return schedulerDescription;
    }

    /**
     * @return the dispatcherDescription
     */
    protected Component getDispatcherDescription() {
        return dispatcherDescription;
    }

    /**
     * @param context
     * @param element
     * @throws ConfigurationException
     */
    public MediatorComponentFactory(BundleContext context, Element element)
            throws ConfigurationException {
        super(context, element);
    }

    protected void computePorts() {
        inPorts = new Hashtable();
        outPorts = new Hashtable();
        Element ports[] = m_componentMetadata.getElements("ports",
                DEFAULT_NAMESPACE); // cilia namespace
        if (ports == null) { //try to use ipojo namespace
            ports = m_componentMetadata.getElements("ports");
        }
        for (int i = 0; ports != null && i < ports.length; i++) {
            Element allports[] = ports[i].getElements();
            computePorts(allports);
        }
    }

    protected void computePorts(Element[] ports) {
        for (int i = 0; ports != null && i < ports.length; i++) {
            String name = ports[i].getAttribute("name");
            String type = ports[i].getAttribute("type");
            if (ports[i].getName().compareTo("in-port") == 0 && name != null) {
                Port pt = new PortImpl(name, type, PortType.INPUT, null);
                inPorts.put(name, pt);
            } else if (ports[i].getName().compareTo("out-port") == 0 && name != null) {
                Port pt = new PortImpl(name, type, PortType.OUTPUT, null);
                outPorts.put(name, pt);
            }
        }
    }

    public Port getInPort(String name) {
        return (Port) inPorts.get(name);
    }

    public Port getOutPort(String name) {
        return (Port) outPorts.get(name);
    }

    protected void computeConstituantsDescriptions()
            throws ConfigurationException {
        processorDescription = computeDescription("processor");
        schedulerDescription = computeDescription("scheduler");
        dispatcherDescription = computeDescription("dispatcher");
    }

    protected Component computeDescription(String constituantType)
            throws ConfigurationException {
        String msg;
        String name = null;
        String namespace = null;
        Element elem[] = m_componentMetadata.getElements(constituantType,
                DEFAULT_NAMESPACE); // cilia namespace
        if (elem == null) {
            elem = m_componentMetadata.getElements(constituantType, null); // ipojo
            // namespace
        }
        if (elem == null) {
            msg = "a mediator must have one " + constituantType + " : "
                    + m_componentMetadata;
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
        if (elem.length != 1) {
            msg = "a mediator must have only one " + constituantType + " : "
                    + m_componentMetadata;
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
        Element procElement = elem[0];
        // obtain processor name. (not optional)
        if (procElement.containsAttribute("name")) {
            name = procElement.getAttribute("name");
        } else {
            msg = "a " + constituantType + " in mediator must have a name : "
                    + m_componentMetadata;
            logger.error(msg);
            throw new ConfigurationException(msg);
        }

        // obtain processor namespace. optional
        if (procElement.containsAttribute("namespace")) {
            namespace = procElement.getAttribute("namespace");
        }
        return new ComponentImpl(name, constituantType, namespace, null);
    }

}
