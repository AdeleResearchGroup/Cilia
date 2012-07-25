package fr.liglab.adele.cilia.internals.factories;

import java.util.Dictionary;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.framework.monitor.IMonitor;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.MediatorHandler;
import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
import fr.liglab.adele.cilia.runtime.impl.MonitorHandler;
import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;

public class MediatorManager extends MediatorComponentManager implements ComponentInstance, InstanceStateListener {

	private static final Logger logger = LoggerFactory.getLogger("cilia.ipojo.runtime");


	/**
	 * The instance factory.
	 */
	private final ProcessorFactory pfactory;

	

	private BundleContext m_context;


	private ComponentInstance pinstance;

	
	public MediatorManager(MediatorComponentFactory mefactory, ProcessorFactory factory, BundleContext context,
			HandlerManager[] handlers) {
		super(mefactory, context, handlers);
		this.pfactory = factory;
		this.m_context = context;

	}

	public void start() {
		super.startManagers();
		super.start();
		if (pinstance != null) {
			pinstance.addInstanceStateListener(this);
		}

	}



	public void stop() {
		super.stop();
		if (pinstance != null){
			pinstance.stop();
		}
		super.stopManagers();
	}

	public void dispose() {
		super.dispose();
		if (pinstance != null){
			pinstance.dispose();
		}
		synchronized (this) {
			if (pfactory != null) {
				pfactory.disposed(this);
			}
			if (mfactory != null) {
				mfactory.disposed(this);
			}
		}
	}



	public ComponentFactory getFactory() {
		return mfactory;
	}

	public BundleContext getContext() {
		return m_context;
	}


	public void configure(Element metadata, Dictionary config) throws ConfigurationException {
		this.configuration = config;
		// Add the name
		m_name = (String) config.get("instance.name");
		m_name = m_name + "-Mediator";

		// Create the standard handlers and add these handlers to the list
		SchedulerHandler sch = (SchedulerHandler) ((InstanceManager)pinstance).getHandler(Const.ciliaQualifiedName("scheduler"));
		MonitorHandler monitor = (MonitorHandler) ((InstanceManager)pinstance).getHandler(Const.ciliaQualifiedName("monitor-handler"));
		DispatcherHandler dsp = (DispatcherHandler) ((InstanceManager)pinstance).getHandler(Const.ciliaQualifiedName("dispatcher"));
		for (int i = 0; i < m_handlers.length; i++) {
			m_handlers[i].init(this, metadata, config);
			//Add subscription.
			Handler handler = m_handlers[i].getHandler();
			// add the monitor, to listen the scheduler/dispatcher events.
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
	}


	public void reconfigure(Dictionary config) {
		super.reconfigure(config);
		if (pinstance != null) {
			pinstance.reconfigure(config);
		}
		this.configuration = config;
	}


	public void createProcessor(Dictionary config) throws UnacceptableConfiguration, MissingHandlerException, ConfigurationException {
		pinstance = pfactory.createComponentInstance(config);

	}

	public void stateChanged(ComponentInstance instance, int newState) {
		if (pinstance == null || pinstance.getState() != VALID) {
			return;
		}
		super.stateChanged(instance, newState);
	}


	public Object[] getPojoObjects() {
		return null;
	}
	protected Object createObject() {
		return null;
	}

	public Object createPojoObject() {
		return null;
	}

	public ComponentInstance getProcessorInstance() {
		return pinstance;
	}
}
