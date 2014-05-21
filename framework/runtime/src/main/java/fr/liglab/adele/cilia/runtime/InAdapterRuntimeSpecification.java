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
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.specification.AbstractInAdapterSpecification;
import fr.liglab.adele.cilia.specification.InAdapterSpecification;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class InAdapterRuntimeSpecification extends AbstractInAdapterSpecification {

    BundleContext context;

    private AdapterFactory factory;

    public InAdapterRuntimeSpecification(String name, String namespace,
                                         String category) {
        super(name, namespace, category);
    }

    public InAdapterRuntimeSpecification(String name, String namespace,
                                         String category, BundleContext c) {
        this(name, namespace, category);
        setContext(c);
    }

    protected void setContext(BundleContext bc) {
        context = bc;
    }

    public InAdapterSpecification initializeSpecification() {
        Element meta = generateMetadata();
        try {
            factory = new AdapterFactory(context, meta);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        factory.start();
        return this;
    }

    public InAdapterSpecification stopSpecification() {
        factory.stop();
        return null;
    }

    private Element generateMetadata() {
        Element adapterMetadata = new Element("adapter", "");
        adapterMetadata.addAttribute(new Attribute("name", getName()));
        adapterMetadata.addAttribute(new Attribute("namespace", getNamespace()));
        adapterMetadata.addAttribute(new Attribute("pattern", "in-only"));

        Element collector = new Element("collector", "");
        collector.addAttribute(new Attribute("type", getCollectorName()));
        collector.addAttribute(new Attribute("name", getCollectorName()));
        collector.addAttribute(new Attribute("namespace", getCollectorNamespace()));
        adapterMetadata.addElement(collector);

        Element dispatcher = new Element("dispatcher", "");
        dispatcher.addAttribute(new Attribute("type", getDispatcherName()));
        dispatcher.addAttribute(new Attribute("name", getDispatcherName()));
        dispatcher.addAttribute(new Attribute("namespace", getDispatcherNamespace()));
        adapterMetadata.addElement(dispatcher);

        Element ports = new Element("ports", "");
        for (Object o : outPorts.keySet()) {
            Port outPort = (Port) outPorts.get(o);
            Element nPort = new Element("out-port", "");
            nPort.addAttribute(new Attribute("name", outPort.getName()));
            nPort.addAttribute(new Attribute("type", outPort.getDataType()));
            ports.addElement(nPort);
        }
        adapterMetadata.addElement(ports);
        return adapterMetadata;
    }

}
