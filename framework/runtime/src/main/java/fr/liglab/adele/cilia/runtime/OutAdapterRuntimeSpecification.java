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
package fr.liglab.adele.cilia.runtime;

import fr.liglab.adele.cilia.internals.factories.AdapterFactory;
import fr.liglab.adele.cilia.internals.factories.MediatorFactory;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.specification.AbstractOutAdapterSpecification;
import fr.liglab.adele.cilia.specification.OutAdapterSpecification;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class OutAdapterRuntimeSpecification extends AbstractOutAdapterSpecification {

    BundleContext context;

    private MediatorFactory factory;

    public OutAdapterRuntimeSpecification(String name, String namespace,
                                          String category) {
        super(name, namespace, category);
    }

    public OutAdapterRuntimeSpecification(String name, String namespace,
                                          String category, BundleContext c) {
        this(name, namespace, category);
        setContext(c);
    }

    protected void setContext(BundleContext bc) {
        context = bc;
    }

    public OutAdapterSpecification initializeSpecification() {
        Element meta = generateMetadata();
        try {
            factory = new AdapterFactory(context, meta);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        factory.start();
        return this;
    }

    public OutAdapterSpecification stopSpecification() {
        factory.stop();
        return null;
    }

    private Element generateMetadata() {
        Element adapterMetadata = new Element("adapter", "");
        adapterMetadata.addAttribute(new Attribute("name", getName()));
        adapterMetadata.addAttribute(new Attribute("category", getCategory()));
        adapterMetadata.addAttribute(new Attribute("namespace", getNamespace()));
        adapterMetadata.addAttribute(new Attribute("pattern", "out-only"));

        Element scheduler = new Element("scheduler", "");
        scheduler.addAttribute(new Attribute("type", getSchedulerName()));
        scheduler.addAttribute(new Attribute("name", getSchedulerName()));
        scheduler.addAttribute(new Attribute("namespace", getSchedulerNamespace()));
        adapterMetadata.addElement(scheduler);

        Element sender = new Element("sender", "");
        sender.addAttribute(new Attribute("type", getSenderName()));
        sender.addAttribute(new Attribute("name", getSenderName()));
        sender.addAttribute(new Attribute("namespace", getSenderNamespace()));
        adapterMetadata.addElement(sender);

        Element ports = new Element("ports", "");
        for (Object o : inPorts.keySet()) {
            Port inport = (Port) inPorts.get((String) o);
            Element nport = new Element("in-port", "");
            nport.addAttribute(new Attribute("name", inport.getName()));
            nport.addAttribute(new Attribute("type", inport.getDataType()));
            ports.addElement(nport);
        }


        adapterMetadata.addElement(ports);
        return adapterMetadata;
    }
}
