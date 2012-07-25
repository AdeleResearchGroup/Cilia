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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a Pojo object, their instance and their factory.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CiliaInstanceWrapper extends Observable implements CiliaInstance,
		TrackerCustomizer, InstanceStateListener {

	private Map properties = new Hashtable();
	private String m_instanceName = null;
	// private Factory m_iPojoFactory = null;
	private BundleContext m_context;
	private ComponentInstance componentInstance;
	private String m_filter;

	private final Object lockObject = new Object();

	Tracker factoryTracker;

	private static final Logger log = LoggerFactory
			.getLogger("cilia.ipojo.runtime");

	/**
	 * Create an iPOJO wrap instance.
	 * 
	 * @param context
	 *            The OSGi bundle context.
	 * @param instanceName
	 *            The name of the instance, this is not the iPOJO instance name,
	 *            is an abstract name.
	 * @param filter
	 *            A filter to search the iPOJO factory which will create the
	 *            instance.
	 * @param props
	 *            Properties used when creating the instance.
	 * @param obs
	 *            The observer reference.
	 */
	public CiliaInstanceWrapper(BundleContext context, String instanceName,
			String filter, Dictionary props, Observer obs) {
		m_context = context;
		m_instanceName = instanceName;
		m_filter = filter;
		if (props != null) {
			properties = new Hashtable((Map) props);
			properties.remove("instance.name");
		}
		if (obs != null) {
			this.addObserver(obs);
		}
	}

	/**
	 * Create the instance usgin the iPOJO Factory
	 */
	protected void createInstance(Factory ipojoFactory) {
		boolean created = false;
		synchronized (lockObject) {
			try {
				Hashtable prs = new Hashtable(properties);
				componentInstance = ipojoFactory.createComponentInstance(prs);
				componentInstance.addInstanceStateListener(this);
				created = true;
			} catch (UnacceptableConfiguration e) {
				e.printStackTrace();
				componentInstance = null;
				refresh();
			} catch (MissingHandlerException e) {
				e.printStackTrace();
				componentInstance = null;
				refresh();
			} catch (ConfigurationException e) {
				e.printStackTrace();
				componentInstance = null;
				refresh();
			} catch (RuntimeException e) {
				componentInstance = null;
				refresh();
				e.printStackTrace();
			}
		}
		if (created) {
			stateChanged(componentInstance, getState());
		}
	}

	/**
	 * Obtain a copy of the instance properties.
	 */
	public Dictionary getInstanceProperties() {
		synchronized (lockObject) {
			return new Hashtable(properties);
		}
	}

	/**
	 * Obtain the instance property.
	 */
	public Object getInstanceProperty(Object key) {
		Object props = null;
		synchronized (lockObject) {
			props = properties.get(key);
		}
		return props;
	}

	/**
	 * Get the object instance. return the object instance. Null when the
	 * instance is not valid.
	 */
	public Object getObject() {
		Object object = null;
		synchronized (lockObject) {
			if (getState() == CiliaInstance.VALID) {
				object = ((InstanceManager) componentInstance).getPojoObject();
			}
		}
		if (object == null) {
			log.error("Component {} is not valid {}", getName());
		}
		return object;
	}

	/**
	 * Get the instance state.
	 */
	public int getState() {
		synchronized (lockObject) {
			if (componentInstance == null) {
				System.out.println("Component Instance is null");
				return CiliaInstance.INVALID;
			}
			return componentInstance.getState();
		}
	}

	/**
	 * Get the state value as a string.
	 */
	public String getStateAsString() {
		int state;
		synchronized (lockObject) {
			state = getState();
		}
		switch (state) {
		case CiliaInstance.INVALID:
			return "INVALID";
		case CiliaInstance.STOPPED:
			return "STOPPED";
		case CiliaInstance.VALID:
			return "VALID";
		default:
			return "INVALID";
		}
	}

	/**
	 * Will notify the observers there is a change in the instance status. It
	 * notify only true, when the instance is valid, and false, when the
	 * instance is invalid.
	 */
	public void refresh() {
		Integer arg = null;
		synchronized (lockObject) {
			arg = getState();
		}
		setChanged();
		notifyObservers(arg);
	}

	/**
	 * Get the instance name. return name The instance name.
	 */
	public String getName() {
		synchronized (lockObject) {
			return m_instanceName;
		}
	}

	/**
	 * Start the tracker.
	 */
	public void start() {
		registerTracker();
	}

	/**
	 * Stop the Instance wrapper, it unregister the tracker and dispose the
	 * instance.
	 */
	public void stop() {
		unregisterTracker();
		try {
		} catch (IllegalStateException ex) {
		}
		disposeInstance();
	}

	/**
	 * Dispose the ipojo instance.
	 */
	private void disposeInstance() {
		synchronized (lockObject) {
			if (componentInstance != null) {
				componentInstance.removeInstanceStateListener(this);
				componentInstance.stop();
				componentInstance.dispose();
			}
			componentInstance = null;
		}
	}

	public void updateInstanceProperties(Dictionary properties) {
		properties.remove("instance.name");
		synchronized (lockObject) {
			if (componentInstance != null) {
				componentInstance.reconfigure(properties);
			}
		}
	}

	public void stateChanged(ComponentInstance instance, int newState) {
		refresh();
		log.debug("state changed " + m_instanceName);
	}

	public ComponentInstance getInstanceManager() {
		synchronized (lockObject) {
			return componentInstance;
		}
	}

	/** Tracker Customizer methods **/
	/**
	 * Registring the factory tracker.
	 */
	private void registerTracker() {
		if (factoryTracker == null) {
			try {
				factoryTracker = new Tracker(m_context, m_context.createFilter(m_filter),
						this);
				factoryTracker.open();
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Unregistering the factory tracker.
	 */
	protected void unregisterTracker() {
		if (factoryTracker != null) {
			factoryTracker.close();
			factoryTracker = null;
		}
	}

	/**
	 * When the factory service is tracked.
	 */
	public void addedService(ServiceReference reference) {
		disposeInstance();
		Factory m_iPojoFactory = (Factory) m_context.getService(reference);
		createInstance(m_iPojoFactory);
		m_context.ungetService(reference);
	}

	/**
	 * Bassed on the filter, the addingService must be the good one.
	 */
	public boolean addingService(ServiceReference reference) {
		return true;
	}

	/**
	 * Modifying the factory service, nothing to do.
	 */
	public void modifiedService(ServiceReference reference, Object service) {
	}

	/**
	 * When removing the factory service, it must remove the instance.
	 */
	public void removedService(ServiceReference reference, Object service) {
		disposeInstance();
		refresh();
	}
}
