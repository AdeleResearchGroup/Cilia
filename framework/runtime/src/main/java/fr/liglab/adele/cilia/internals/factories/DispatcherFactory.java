package fr.liglab.adele.cilia.internals.factories;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

public class DispatcherFactory extends CiliaComponentFactory {

    private static final String COMPONENT_TYPE = "dispatcher";    
    
    public DispatcherFactory(BundleContext context, Element element)
            throws ConfigurationException {
        super(context, element);
        
    }
    
    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    
}

