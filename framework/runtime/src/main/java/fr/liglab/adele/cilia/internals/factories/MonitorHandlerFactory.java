package fr.liglab.adele.cilia.internals.factories;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.HandlerManagerFactory;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

public class MonitorHandlerFactory extends HandlerManagerFactory {

    public MonitorHandlerFactory(BundleContext context, Element metadata)
            throws ConfigurationException {
        super(context, metadata);

    }

}
