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

package fr.liglab.adele.cilia.runtime.dynamic;

import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.framework.monitor.statevariable.ComponentStateVarProperties;
import fr.liglab.adele.cilia.framework.monitor.statevariable.ComponentStateVarService;
import fr.liglab.adele.cilia.runtime.ConstRuntime;

/**
 * Track mediator/adapter at runtime
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class NodeDiscoveryImpl implements TrackerCustomizer, ComponentStateVarProperties {

	private final BundleContext bundleContext;

	private RuntimeRegistry registry;

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);
	private final NodeCallback callback ;
	private Tracker tracker;


	public NodeDiscoveryImpl(BundleContext bc,NodeCallback cb) {
		this.bundleContext = bc;
		this.callback=cb;
	}

	public void setRegistry(RuntimeRegistry r) {
		this.registry = r;
	}

	/*
	 * Start , Register the tracker ( tracks all components ) Register
	 * EventAdmin ( receives data from component tracked) (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.runtime.introspection.MonitoringChain#start()
	 */
	public synchronized void start() {
		registerTracker();
		logger.info("ModelS@RunTime 'Node discovery' - started");
	}

	public synchronized void stop() {
		unregisterTracker();
		logger.info("ModelS@RunTime 'Node discovery' - stopped");
	}

	/*
	 * Register the tracker for dynamic discovery of relevant Component
	 */
	private void registerTracker() {
		if (tracker == null) {
			try {
				tracker = new Tracker(bundleContext,
						bundleContext.createFilter(SERVICE_TRACKED), this);
				tracker.open();
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void unregisterTracker() {
		if (tracker != null) {
			tracker.close();
		}
	}

	public boolean addingService(ServiceReference reference) {
		return true;
	}

	public void addedService(ServiceReference reference) {
		insert((ComponentStateVarService) bundleContext.getService(reference));
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
		remove((ComponentStateVarService) bundleContext.getService(reference));
	}

	private void remove(ComponentStateVarService mediatorHandler) {
		/* retreives uuid, chain.id, component.id */
		String uuid = (String) mediatorHandler.getProperty(MONITOR_UUID);
		RegistryItem item = (RegistryItem) registry.findByUuid(uuid);
		if (item != null) {
			callback.onDeparture(item) ;
			logger.debug("Node [{}] disappear", item.toString());
		}
	}

	private void insert(ComponentStateVarService mediatorHandler) {

		String chainId = (String) mediatorHandler.getProperty(MONITOR_CHAIN_ID);
		String mediatorId = (String) mediatorHandler.getProperty(MONITOR_NODE_ID);
		String uuid = (String) mediatorHandler.getProperty(MONITOR_UUID);

		/* Creates the object stored i nthe registry */
		RegistryItemImpl item = new RegistryItemImpl(uuid, chainId, mediatorId);
		item.setRuntimeReference(mediatorHandler);
		registry.register(item);
		callback.onArrival(item) ;
		logger.debug("Node [{}] discovered", item.toString());
	}
}
