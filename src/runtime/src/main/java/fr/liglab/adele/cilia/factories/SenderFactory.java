package fr.liglab.adele.cilia.factories;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

public class SenderFactory extends CiliaComponentFactory {
   
    private static final String COMPONENT_TYPE = "sender";
    
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
    
    public SenderFactory(BundleContext context, Element element)
            throws ConfigurationException {
        super(context, element);
    }
}

