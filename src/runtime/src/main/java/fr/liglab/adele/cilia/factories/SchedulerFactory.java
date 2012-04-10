package fr.liglab.adele.cilia.factories;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

public class SchedulerFactory extends CiliaComponentFactory {

    private static final String COMPONENT_TYPE = "scheduler";
    
    public SchedulerFactory(BundleContext context, Element element)
            throws ConfigurationException {
        super(context, element);
    }
    
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
}
