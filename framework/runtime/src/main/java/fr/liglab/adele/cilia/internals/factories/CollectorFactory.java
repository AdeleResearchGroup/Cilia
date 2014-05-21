package fr.liglab.adele.cilia.internals.factories;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

public class CollectorFactory extends CiliaComponentFactory {

    private static final String COMPONENT_TYPE = "collector";

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public CollectorFactory(BundleContext context, Element element)
            throws ConfigurationException {
        super(context, element);
        updateMetadata();
    }

    protected void updateMetadata() {
        Element setSourceProp = null;
        Element prop = null;
        if (!m_componentMetadata.containsElement("properties", null)) {
            setSourceProp = new Element("properties", null);
            prop = new Element("property", null);
            prop.addAttribute(new Attribute("name", "collector.sourceName"));
            prop.addAttribute(new Attribute("method", "setSourceName"));
            prop.addAttribute(new Attribute("type", "String"));
            setSourceProp.addElement(prop);
            m_componentMetadata.addElement(setSourceProp);
        } else {
            setSourceProp = m_componentMetadata.getElements("properties", null)[0];
            prop = new Element("property", null);
            prop.addAttribute(new Attribute("name", "collector.sourceName"));
            prop.addAttribute(new Attribute("method", "setSourceName"));
            prop.addAttribute(new Attribute("type", "String"));
            setSourceProp.addElement(prop);
        }
    }

}

