package fr.liglab.adele.cilia.internals.factories;

import java.util.Dictionary;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.framework.utils.Const;


public abstract class CiliaComponentFactory extends ComponentFactory {

    private String componentName;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private String category;

    private String namespace;

    private final  String CATEGORY = getComponentType()  + ".category";

    private final  String NAME = getComponentType()  + ".name";

    private final  String NAMESPACE = getComponentType()  + ".namespace";

    protected final  String DEFAULT_NAMESPACE = Const.CILIA_NAMESPACE;

    private final  String DEFAULT_CATEGORY = "generic";
    
    protected static Logger log =LoggerFactory.getLogger("cilia.ipojo.runtime");


    public CiliaComponentFactory(BundleContext context, Element element)
    throws ConfigurationException {
        super(context, element);
        // Get the name
        componentName = element.getAttribute("name");
        if (componentName == null) {
            throw new ConfigurationException("An "+getComponentType()+" needs a name");
        }
        Attribute[] at = element.getAttributes();
        // at[0].
        // Get the category
        String cat = element.getAttribute("category");
        if (cat != null) {
            category = cat;
        } else {
            category = DEFAULT_CATEGORY;
        }

        String nspace = element.getAttribute("namespace");
        if (nspace != null) {
            namespace = nspace.toLowerCase();
        } else {
            namespace = DEFAULT_NAMESPACE;
        }

        addRequiresHandler();
    }

    protected void addRequiresHandler() {
        boolean addRequires = false;
        Element props[] = m_componentMetadata.getElements("Properties");
        Element properties;
        if (props != null && props.length > 0) {
            properties = props[0];
        } else {
            return;
        }
        Element property[] = properties.getElements("Property");
        for (int i = 0; property != null && i < property.length ; i++) {
            if (property[i].containsAttribute("service") && property[i].getAttribute("service").compareToIgnoreCase("true") == 0) {
                Attribute atts[] = property[i].getAttributes();
                Element requires = new Element("Requires", null);
                for (int j = 0; j < atts.length ; j++) {
                    requires.addAttribute(atts[j]);
                }
                //see if requires uses callbacks.
                if (property[i].containsElement("Callback")) {
                    Element callbacks[] = property[i].getElements("Callback");
                    for (int j = 0; j < atts.length ; j++) {
                        requires.addElement(callbacks[j]);
                    }
                }
                m_componentMetadata.addElement(requires);
                properties.removeElement(property[i]);
                addRequires = true;
            } else { //is a normal property
                String name = property[i].getAttribute("name");
                if (! name.startsWith(getComponentType() ) && ("scheduler".compareToIgnoreCase(name) == 0 ) || ("dispatcher".compareToIgnoreCase(name) == 0 ) || ("processor".compareToIgnoreCase(name) == 0 )) {
                    String nname = getComponentType() + "." + name; //we add the new prefix name (e.g. scheduler.period, dispatcher.language)
                    property[i].addAttribute(new Attribute("name", nname));
                }
            }
        }
        if (addRequires) {
            RequiredHandler reqd = new RequiredHandler("Requires",null);
            if (!m_requiredHandlers.contains(reqd)) {
                m_requiredHandlers.add(reqd);
            }
        }
    }

    public abstract String getComponentType() ;

    public ComponentTypeDescription getComponentTypeDescription() {
        return new CiliaTypeDescription(this);
    }

    private class CiliaTypeDescription extends ComponentTypeDescription {

        public CiliaTypeDescription(IPojoFactory factory) {
            super(factory);
        }

        public Dictionary getPropertiesToPublish() {
            Dictionary dict = super.getPropertiesToPublish();
            if (getClassName() != null) {
                dict.put("component.class", getClassName());
            }
            dict.put(CATEGORY, category);
            dict.put(NAMESPACE, namespace);
            dict.put(NAME, componentName);
            return dict;
        }

        public Element getDescription() {
            Element elem = super.getDescription();
            elem.addAttribute(new Attribute("Implementation-Class", getClassName()));
            return elem;
        }

    }
}