package fr.liglab.adele.cilia.internals.factories;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.framework.monitor.IMonitor;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.MediatorHandler;
import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
import fr.liglab.adele.cilia.runtime.impl.MonitorHandler;
import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;

public class MediatorManager extends MediatorComponentManager implements ComponentInstance, InstanceStateListener {

	private static final Logger logger = LoggerFactory.getLogger("cilia.ipojo.runtime");
	/**
	 * The handler object list.
	 */
	protected final HandlerManager[] m_handlers;

	/**
	 * The instance factory.
	 */
	private final ProcessorFactory pfactory;

	

	private BundleContext m_context;

	private int m_state;

	/**
	 * Is the component instance state changing?
	 */
	private boolean m_inTransition = false;

	/**
	 * The queue of stored state changed.
	 */
	private List m_stateQueue = new ArrayList();

	private String m_name;

	private ComponentInstance pinstance;

	private List m_listeners;



	public MediatorManager(MediatorComponentFactory mefactory, ProcessorFactory factory, BundleContext context,
			HandlerManager[] handlers) {
		super(mefactory, context, handlers);
		this.m_handlers = handlers;
		this.pfactory = factory;
		this.m_context = context;

	}

	public void start() {
		synchronized (this) {
			if (m_state != STOPPED) { // Instance already started
				return;
			} else {
				m_state = -2; // Temporary state.
			}
		}
		if (pinstance != null) {
			pinstance.addInstanceStateListener(this);
		}
		// Plug handler descriptions
		//        Handler[] handlers = getRegistredHandlers();
		//        for (int i = 0; i < handlers.length; i++) {
		//            m_description.addHandler(handlers[i].getDescription());
		//        }

		for (int i = 0; i < m_handlers.length; i++) {
			m_handlers[i].addInstanceStateListener(this);
			try {
				m_handlers[i].start();
			} catch (IllegalStateException e) {
				logger.error(e.getMessage());
				stop();
				throw e;
			}
		}

		//        // Is an object already contained (i.e. injected)
		//        if (m_pojoObjects != null && ! m_pojoObjects.isEmpty()) {
		//            managedInjectedObject();
		//        }

		for (int i = 0; i < m_handlers.length; i++) {
			if (m_handlers[i].getState() != VALID) {
				setState(INVALID);
				return;
			}
		}
		setState(VALID);
	}



	/**
	 * Sets the state of the component instance.
	 * If the state changes, calls the {@link PrimitiveHandler#stateChanged(int)} method on the attached handlers.
	 * This method has a reentrant mechanism. If in the flow of the first call the method is called another times,
	 * the second call is stored and executed after the first one finished.
	 * @param state the new state
	 */
	public void setState(int state) {
		int originalState = -2;
		List listeners = null;

		synchronized (this) {
			if (m_inTransition) {
				m_stateQueue.add(new Integer(state));
				return;
			}

			if (m_state != state) {
				m_inTransition = true;
				originalState = m_state; // Stack confinement.
				m_state = state;
				if (m_listeners != null) {
					listeners = new ArrayList(m_listeners); // Stack confinement.
				}
			}
		}

		// This section can be executed only by one thread at the same time. The m_inTransition pseudo semaphore block access to this section.
		if (m_inTransition) { // Check that we are really changing.
			if (state > originalState) {
				// The state increases (Stopped = > IV, IV => V) => invoke handlers from the higher priority to the lower
				try {
					for (int i = 0; i < m_handlers.length; i++) {
						m_handlers[i].getHandler().stateChanged(state);
					}
				} catch (IllegalStateException e) {
					// When an illegal state exception happens, the instance manager must be stopped immediately.
					stop();
					return;
				}
			} else {
				// The state decreases (V => IV, IV = > Stopped, Stopped => Disposed)
				try {
					for (int i = m_handlers.length - 1; i > -1; i--) {
						m_handlers[i].getHandler().stateChanged(state);
					}
				} catch (IllegalStateException e) {
					// When an illegal state exception happens, the instance manager must be stopped immediately.
					stop();
					return;
				}
			}
		}

		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				((InstanceStateListener) listeners.get(i)).stateChanged(this, state);
			}
		}

		synchronized (this) {
			m_inTransition = false;
			if (!m_stateQueue.isEmpty()) {
				int newState = ((Integer) (m_stateQueue.remove(0))).intValue();
				setState(newState);
			}
		}
	}

	public void stop() {
		synchronized (this) {
			if (m_state == STOPPED) { // Instance already stopped
				return;
			}
			m_stateQueue.clear();
			m_inTransition = false;
		}

		setState(INVALID); // Must be called outside a synchronized block.

		// Stop all the handlers
		for (int i = m_handlers.length - 1; i > -1; i--) {
			m_handlers[i].stop();
		}
		if (pinstance != null){
			pinstance.stop();
		}
		synchronized (this) {
			m_state = STOPPED;
		}
	}

	public void dispose() {
		int state = -2; // Temporary state
		List listeners = null;
		synchronized (this) {
			state = m_state; // Stack confinement
			if (m_listeners != null) {
				listeners = new ArrayList(m_listeners); // Stack confinement
			}
			m_listeners = null;
		}

		if (state > STOPPED) { // Valid or invalid
			stop(); // Does not hold the lock.
		}

		synchronized (this) {
			m_state = DISPOSED;
		}
		for (int i = 0; listeners != null && i < listeners.size(); i++) {
			((InstanceStateListener) listeners.get(i)).stateChanged(this, DISPOSED);
		}

		for (int i = m_handlers.length - 1; i > -1; i--) {
			m_handlers[i].dispose();
		}
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

	public synchronized int getState() {
		return m_state;
	}



	public ComponentFactory getFactory() {
		return mfactory;
	}

	public BundleContext getContext() {
		return m_context;
	}

	public String getInstanceName() {
		return m_name; // Immutable
	}

	public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {

		// Add the name
		m_name = (String) configuration.get("instance.name");
		m_name = m_name + "-Mediator";

		// Create the standard handlers and add these handlers to the list
		SchedulerHandler sch = (SchedulerHandler) ((InstanceManager)pinstance).getHandler(Const.ciliaQualifiedName("scheduler"));
		MonitorHandler monitor = (MonitorHandler) ((InstanceManager)pinstance).getHandler(Const.ciliaQualifiedName("monitor-handler"));
		DispatcherHandler dsp = (DispatcherHandler) ((InstanceManager)pinstance).getHandler(Const.ciliaQualifiedName("dispatcher"));
		for (int i = 0; i < m_handlers.length; i++) {
			m_handlers[i].init(this, metadata, configuration);
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

	public synchronized boolean isStarted() {
		return m_state > STOPPED;
	}

	public void reconfigure(Dictionary configuration) {
		for (int i = 0; i < m_handlers.length; i++) {
			m_handlers[i].getHandler().reconfigure(configuration);
		}
		if (pinstance != null) {
			pinstance.reconfigure(configuration);
		}
		// We synchronized the state computation.
		synchronized (this) {
			if (m_state == INVALID) {
				// Try to revalidate the instance after reconfiguration
				for (int i = 0; i < m_handlers.length; i++) {
					if (m_handlers[i].getState() != VALID) {
						return;
					}
				}
				setState(VALID);
			}
		}
	}

	public void addInstanceStateListener(InstanceStateListener listener) {
		if (m_listeners == null) {
			m_listeners = new ArrayList();
		}
		m_listeners.add(listener);
	}

	public void removeInstanceStateListener(InstanceStateListener listener) {
		if (m_listeners != null) {
			m_listeners.remove(listener);
			if (m_listeners.isEmpty()) {
				m_listeners = null;
			}
		}
	}

	public void createProcessor(Dictionary config) throws UnacceptableConfiguration, MissingHandlerException, ConfigurationException {
		pinstance = pfactory.createComponentInstance(config);

	}

	public void stateChanged(ComponentInstance instance, int newState) {
		int state;
		synchronized (this) {
			if (m_state <= STOPPED) {
				return;
			} else {
				state = m_state; // Stack confinement
			}
		}

		// Update the component state if necessary
		if (newState == INVALID && state == VALID) {
			// Need to update the state to UNRESOLVED
			setState(INVALID);
			return;
		}
		if (pinstance == null || pinstance.getState() != VALID) {
			return;
		}
		if (newState == VALID && state == INVALID) {
			// An handler becomes valid => check if all handlers are valid
			for (int i = 0; i < m_handlers.length; i++) {
				if (m_handlers[i].getState() != VALID) {
					return;
				}
			}
			setState(VALID);
			return;
		}
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
