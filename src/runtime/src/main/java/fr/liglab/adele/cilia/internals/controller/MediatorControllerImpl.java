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

package fr.liglab.adele.cilia.internals.controller;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import org.apache.felix.ipojo.ComponentInstance;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.internals.factories.MediatorComponentManager;
import fr.liglab.adele.cilia.knowledge.MediatorMonitoring;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.BindingImpl;
import fr.liglab.adele.cilia.model.impl.ConstModel;
import fr.liglab.adele.cilia.model.impl.MediatorComponentImpl;
import fr.liglab.adele.cilia.model.impl.UpdateActions;
import fr.liglab.adele.cilia.model.impl.UpdateEvent;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.FirerEvents;
import fr.liglab.adele.cilia.util.FrameworkUtils;

/**
 * This class will observe the mediator model and will act as an itermediator
 * between the mediator model and the mediator instance.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class MediatorControllerImpl implements Observer {

	/**
	 * Mediator representation model that will be observed.
	 */
	protected MediatorComponentImpl mediatorModel;
	/**
	 * OSGi Bundle Context
	 */
	protected BundleContext bcontext;
	/**
	 * iPOJO Cilia instance wrapper, to wrap the mediator iPOJO instance.
	 */
	protected CiliaInstanceWrapper mediatorInstance;


	protected Hashtable addedCollectors = new Hashtable();

	protected Hashtable addedSenders = new Hashtable();

	private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);

	protected CreatorThread creator;

	protected final Object lockObject = new Object();

	protected final String filter;

	// protected CiliaFrameworkEventPublisher eventNotifier;
	private FirerEvents eventFirer;

	/**
	 * Create a mediator model controller.
	 * 
	 * @param model
	 *            Mediator model to handle.
	 */
	public MediatorControllerImpl(BundleContext context, MediatorComponent model,
			CreatorThread creat, FirerEvents notifier) {
		bcontext = context;
		creator = creat;
		mediatorModel = (MediatorComponentImpl) model;
		filter = createComponentFilter(mediatorModel);
		updateProperties();
		mediatorModel.addObserver(this);
		/* add extended Model : "monitoring" */
		MediatorMonitoring monitoring = (MediatorMonitoring) mediatorModel
				.getModel(MediatorMonitoring.NAME);
		if (monitoring == null) {
			monitoring = new MediatorMonitoring();
			mediatorModel.addModel(MediatorMonitoring.NAME, monitoring);
			monitoring.setModel(mediatorModel);
		}
		monitoring.setFirerEvent(notifier);

		eventFirer = notifier;
	}

	protected void updateProperties() {
		mediatorModel.setProperty(ConstModel.PROPERTY_COMPONENT_ID, mediatorModel.nodeId());
		mediatorModel.setProperty(ConstModel.PROPERTY_CHAIN_ID, mediatorModel.chainId());
		mediatorModel.setProperty(ConstModel.PROPERTY_UUID, mediatorModel.uuid());
	}

	/**
	 * Create the mediator instance when the mediator Controller is started.
	 */
	private void createMediatorInstance() {
		// if there is an instance defined, destroy it and create a new one with
		// the given model.
		synchronized (lockObject) {
			if (mediatorInstance != null) {
				mediatorInstance.stop();
				mediatorInstance = null;
			}
			mediatorInstance = new CiliaInstanceWrapper(bcontext, mediatorModel.getId(),
					filter, mediatorModel.getProperties(), this);
		}
		mediatorInstance.start();
	}
	/**
	 * Create the filter to locate the mediator factory.
	 * @param mediator
	 * @return
	 */
	protected String createComponentFilter(MediatorComponent mediator) {
		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(");
		filter.append("mediator.name=");
		filter.append(mediator.getType());
		filter.append(")");
		if (mediator.getNamespace() != null) {
			filter.append("(");
			filter.append("mediator.namespace=");
			filter.append(mediator.getNamespace());
			filter.append(")");
		}
		if (mediator.getCategory() != null) {
			filter.append("(");
			filter.append("mediator.category=");
			filter.append(mediator.getCategory());
			filter.append(")");
		}
		filter.append("(factory.state=1)");
		filter.append(")");
		return filter.toString();
	}

	/**
	 * Update the mediator instance using model information.
	 */
	private void updateMediatorInstance() {
		if (mediatorInstance.getState() == ComponentInstance.VALID) {
			createCollectorInstances();
			createSenderInstances();
		}
	}

	/**
	 * Create collector instances from the model.
	 */
	private void createCollectorInstances() {
		BindingImpl[] bs = (BindingImpl[]) mediatorModel.getInBindings();
		for (int i = 0; i < bs.length; i++) {
			Component collector = bs[i].getCollector();
			if (collector != null) {
				createCollector(collector);
			}
		}
	}

	/**
	 * Create sender instances from the model.
	 */
	private void createSenderInstances() {
		BindingImpl[] bs = (BindingImpl[]) mediatorModel.getOutBindings();
		for (int i = 0; i < bs.length; i++) {
			Component sender = bs[i].getSender();
			if (sender != null) {
				createSender(sender);
			}
		}

	}

	/**
	 * update mediator properties.
	 */
	public void updateInstanceProperties(Dictionary properties) {
		if (mediatorInstance == null) {
			throw new RuntimeException(
					"Updating Mediator Model properties when runtime instance is null");
		}
		if (mediatorInstance.getState() != ComponentInstance.VALID) {
			throw new RuntimeException(
					"Updating Mediator instance when object is not valid" + getState());
		}
		mediatorInstance.updateInstanceProperties(properties);
		eventFirer.fireEventNode(FirerEvents.EVT_MODIFIED, mediatorModel);
	}

	/**
	 * Stop the mediator instance.
	 */
	public void stop() {
		synchronized (lockObject) {
			mediatorModel.deleteObserver(this);
			if (mediatorInstance != null) {
				mediatorInstance.deleteObserver(this);
				mediatorInstance.stop();
				logger.info("Component [{}] stopped",
						FrameworkUtils.makeQualifiedId(mediatorModel));
				mediatorModel.dispose();
			}
		}
	}

	/**
	 * Start the mediator instance.
	 */
	public void start() {
		mediatorModel.setRunningState(MediatorComponent.INVALID);
		createMediatorInstance();
		logger.info("Component [{}] started",
				FrameworkUtils.makeQualifiedId(mediatorModel));
	}



	/**
	 * Add collector instance to the mediator.
	 * 
	 * @param collector
	 *            collector model to add to the mediator.
	 */
	public void createCollector(Component collector) {
		boolean toAdd = true;
		synchronized (lockObject) {
			if (!addedCollectors.contains(collector)) {
				addedCollectors.put(collector.getId(), collector);
				toAdd = true;
			} else {
				//logger.warn(" (addCollector) Object instance in " + mediatorModel
				//		+ " already exist " + collector.getId());
				toAdd = false;
			}
			if (toAdd) { // create
				getMediatorManager().addCollector(collector.getId(), collector);
				//scheduler.addCollector(collectorType, portName, properties);
			}
		}
	}

	/**
	 * Create the Sender instance from mthe given model.
	 * 
	 * @param senderm
	 *            the sender model added to the mediator model.
	 */
	public void createSender(Component senderm) {
		boolean toAdd = true;
		synchronized (lockObject) {

			if (!addedSenders.contains(senderm)) {
				addedSenders.put(senderm.getId(), senderm);
				toAdd = true;
			} else {
				//logger.warn(" (addSender) Object instance in " + mediatorModel
				//		+ "already exist" + senderm.getId());
				toAdd = false;
			}
			if (toAdd) { // create
				getMediatorManager().addSender(senderm.getId(), senderm);
			}
		}
	}

	/**
	 * Remove the given collector to the mediator instance.
	 * 
	 * @param collector
	 *            Collector to remove.
	 */
	public void removeCollector(Component collector) {
		synchronized (lockObject) {
			if (collector != null) {
				String id = (String) collector.getProperty("cilia.collector.identifier");
				String port = (String) collector.getProperty("cilia.collector.port");
				addedCollectors.remove(collector);
				MediatorComponentManager mm = getMediatorManager();
				if (mm != null){
					mm.removeCollector(port, collector);
				}
			}
		}
	}

	/**
	 * Remove the given sender to the mediator instance.
	 * 
	 * @param sender
	 *            Sender to remove.
	 */
	public void removeSender(Component sender) {
		synchronized (lockObject) {
			if (sender != null) {
				String id = (String) sender.getProperty("cilia.sender.identifier");
				String port = (String) sender.getProperty("cilia.sender.port");
				addedSenders.remove(sender);
				MediatorComponentManager mm = getMediatorManager();
				if (mm != null){
					mm.removeSender(port, sender);
				}
			}
		}
	}

	/**
	 * get the mediator instance state.
	 */
	public int getState() {
		if (mediatorInstance != null) {
			MediatorComponentManager mm = (MediatorComponentManager)mediatorInstance.getInstanceManager();
			return mm.getCiliaState();
		}
		return CiliaInstance.INVALID;
	}

	/**
	 * Method called when some event happend in the mediator model.
	 * 
	 * @param mediator
	 *            Mediator model observed.
	 * @param arg
	 *            Observer method parapeters.
	 */
	public void update(Observable mediator, Object arg) {
		logger.debug(" update");
		if (mediator instanceof MediatorComponent) {
			logger.debug(" update, OK");
			MediatorComponent md = ((MediatorComponent) mediator);
			UpdateEvent event = (UpdateEvent) arg;
			if (event != null) {
				int action = event.getUpdateAction();
				switch (action) {
				case UpdateActions.UPDATE_PROPERTIES: {
					logger.debug(" update instance property");
					updateInstanceProperties(md.getProperties());
				}
				}
			}
		} else if (mediator instanceof CiliaInstanceWrapper) {
			int state = getState();
			mediatorModel.setRunningState(state);
			switch (state) {
			case CiliaInstance.VALID: {
				updateMediatorInstance();
			}
			break;
			case CiliaInstance.DISPOSED:
			case CiliaInstance.STOPPED:
			case CiliaInstance.INVALID: {
				cleanInstances();
			}
			break;
			}
		}
		synchronized (creator) {
			creator.notifyAll();
		}
	}

	private void cleanInstances() {
		synchronized (lockObject) {
			addedCollectors.clear();
			addedSenders.clear();
		}
	}

	private MediatorComponentManager getMediatorManager(){
		if(mediatorInstance.getInstanceManager() instanceof MediatorComponentManager) {
			MediatorComponentManager mm = (MediatorComponentManager)mediatorInstance.getInstanceManager();
			return mm;
		}
		return null;
	}
	/**
	 * Get the In port description of the executing component.
	 * @param name Name of the port.
	 * @return the port.
	 */
	public Port getInPort(String name){
		if(mediatorInstance.getInstanceManager() instanceof MediatorComponentManager) {
			MediatorComponentManager mm = (MediatorComponentManager)mediatorInstance.getInstanceManager();
			return mm.getInPort(name);
		}
		logger.error("Unable to retrieve In-port '{}' on '{}'", name, this.mediatorModel.getId());
		logger.error("Instance Manager {} ", mediatorInstance.getInstanceManager());
		return null;
	}
	/**
	 * Get the Out port description of the executing component.
	 * @param name Name of the port.
	 * @return the port.
	 */	
	public Port getOutPort(String name) {
		if(mediatorInstance.getInstanceManager() instanceof MediatorComponentManager) {
			MediatorComponentManager mm = (MediatorComponentManager)mediatorInstance.getInstanceManager();
			return mm.getOutPort(name);
		}
		logger.error("Unable to retrieve Out-port '{}' on '{}' ", name, this.mediatorModel.getId());
		logger.error("Instance Manager {} ", mediatorInstance.getInstanceManager());
		return null;
	}
}
