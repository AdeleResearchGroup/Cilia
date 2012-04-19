package fr.liglab.adele.cilia.internals.factories;

import java.util.Dictionary;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.IPojoContext;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.CiliaAdapter;
import fr.liglab.adele.cilia.framework.monitor.IMonitor;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.DispatcherHandler;
import fr.liglab.adele.cilia.runtime.MediatorHandler;
import fr.liglab.adele.cilia.runtime.MonitorHandler;
import fr.liglab.adele.cilia.runtime.SchedulerHandler;

public class IOAdapterFactory extends ProcessorFactory {

	private static final String COMPONENT_TYPE = "adapter";

	public IOAdapterFactory(BundleContext context, Element element)
	throws ConfigurationException {
		super(context, element);
	}

	protected void updateMetadata() {
		Element scheduler = null;
		Element dispatcher = null;
		Class clazz = null;
		if (!m_componentMetadata.containsElement("scheduler", DEFAULT_NAMESPACE)) {
			scheduler = new Element("scheduler", DEFAULT_NAMESPACE);
		} else {
			scheduler = m_componentMetadata.getElements("scheduler", DEFAULT_NAMESPACE)[0];
		}

		if (!m_componentMetadata.containsElement("dispatcher", DEFAULT_NAMESPACE)) {
			dispatcher = new Element("dispatcher", DEFAULT_NAMESPACE);
		} else {
			dispatcher = m_componentMetadata.getElements("dispatcher", DEFAULT_NAMESPACE)[0]; 
		}
		try {
			clazz = super.loadClass(getClassName());
		} catch (ClassNotFoundException e) {
			log.error("Error when analysing Class:" + getClassName() , e);
			return;//anything to do.
		}

		//Check if the manipulated class herite from CiliaAdapter
		if (CiliaAdapter.class.isAssignableFrom(clazz)) {
			dispatcher.addAttribute(new Attribute("method", "dispatchData"));
			dispatcher.addAttribute(new Attribute("data.type", Data.class.getName()));

			scheduler.addAttribute(new Attribute("method", "receiveData"));
			scheduler.addAttribute(new Attribute("in.data.type", Data.class.getName()));
		} 

		m_componentMetadata.addElement(dispatcher);
		m_componentMetadata.addElement(scheduler);    
	}

	public String getComponentType() {
		return COMPONENT_TYPE;
	}

	public ComponentInstance createInstance(Dictionary config,
			IPojoContext context, HandlerManager[] handlers)
	throws org.apache.felix.ipojo.ConfigurationException {
		config.put("cilia.scheduler.name", "immediate-scheduler");
		config.put("cilia.scheduler.namespace", DEFAULT_NAMESPACE);
		config.put("cilia.dispatcher.name", "multicast-dispatcher");
		config.put("cilia.dispatcher.namespace", DEFAULT_NAMESPACE);
		InstanceManager im = (InstanceManager) super.createInstance(config, context, handlers);

		SchedulerHandler sch = (SchedulerHandler) im.getHandler(Const.ciliaQualifiedName("scheduler"));
		MonitorHandler monitor = (MonitorHandler) im.getHandler(Const.ciliaQualifiedName("monitor-handler"));
		DispatcherHandler dsp = (DispatcherHandler) im.getHandler(Const.ciliaQualifiedName("dispatcher"));
		Handler m_handlers[] = im.getRegistredHandlers(); 
		for (int i = 0; i < m_handlers.length; i++) {
			//Add subscription.
			Handler handler = m_handlers[i];
			//add the monitor, to listen the scheduler/dispatcher events.
			if (handler instanceof IMonitor) {
				if (monitor != null) {
					monitor.addListener((IMonitor)handler);
				}
			}
			//Add the scheduler/dispatcher references to the mediator handler.
			if (handler instanceof MediatorHandler) {
				MediatorHandler mh = (MediatorHandler)handler;
				if (dsp != null) {
					mh.setDispatcher(dsp);
				}
				if (sch != null) {
					mh.setScheduler(sch);
				}
			}
		}

		return im;
	}

}
