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
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.framework.IDispatcher;
import fr.liglab.adele.cilia.framework.IScheduler;
import fr.liglab.adele.cilia.internals.factories.MediatorManager;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.BindingImpl;
import fr.liglab.adele.cilia.model.impl.ConstModel;
import fr.liglab.adele.cilia.model.impl.Dispatcher;
import fr.liglab.adele.cilia.model.impl.MediatorComponentImpl;
import fr.liglab.adele.cilia.model.impl.Scheduler;
import fr.liglab.adele.cilia.model.impl.UpdateActions;
import fr.liglab.adele.cilia.model.impl.UpdateEvent;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.application.ApplicationListenerSupport;
import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;


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

	protected static Logger log = LoggerFactory.getLogger("cilia.ipojo.runtime");

	private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);
	
	protected CreatorThread creator;

	protected final Object lockObject = new Object();

	protected final String filter;

	//protected CiliaFrameworkEventPublisher eventNotifier;
	private ApplicationListenerSupport applicationNotifier ;
	/**
	 * Create a mediator model controller.
	 * 
	 * @param model
	 *            Mediator model to handle.
	 */
	public MediatorControllerImpl(BundleContext context, MediatorComponent model,
			CreatorThread creat,ApplicationListenerSupport notifier) {
		bcontext = context;
		creator = creat;
		mediatorModel = (MediatorComponentImpl) model;
		filter = createComponentFilter(mediatorModel);
		updateProperties();
		mediatorModel.addObserver(this);

	}

	protected void updateProperties() {
		mediatorModel
				.setProperty(ConstModel.PROPERTY_COMPONENT_ID, mediatorModel.nodeId());
		mediatorModel.setProperty(ConstModel.PROPERTY_CHAIN_ID, mediatorModel.chainId());
		mediatorModel.setProperty(ConstModel.PROPERTY_UUID, mediatorModel.uuid());
	}

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
	 * Update mediator model from instance.
	 */
	private void updateMediatorModel() {
		Handler[] handlers = null;
		ComponentInstance componentInstance = mediatorInstance.getInstanceManager();
		InstanceManager im = null;

		if ((null != mediatorModel.getDispatcher())
				&& (null != mediatorModel.getScheduler())) {
			return;
		}
		if (componentInstance == null) {
			return;
		}
		if (componentInstance instanceof InstanceManager) {
			im = (InstanceManager) componentInstance;
			handlers = im.getRegistredHandlers();
		}
		if (componentInstance instanceof MediatorManager) {
			MediatorManager mm = (MediatorManager) componentInstance;
			if (mm != null) {
				im = (InstanceManager) mm.getProcessorInstance();
				handlers = im.getRegistredHandlers();
			}
		}

		// remove observer.
		mediatorModel.deleteObserver(this);

		if (im != null) {
			int totalHandlers = handlers.length;
			for (int i = 0; i < totalHandlers; i++) {
				// System.out.println("[INFO]handlers : " + mediatorModel +" " +
				// handlers[i].getDescription().getHandlerName());
				if (handlers[i] instanceof IScheduler) {
					String name = handlers[i].getDescription().getHandlerName();
					Scheduler sched = new Scheduler(name, name, null, null);
					mediatorModel.setScheduler(sched);
				}
				if (handlers[i] instanceof IDispatcher) {
					String name = handlers[i].getDescription().getHandlerName();
					Dispatcher disp = new Dispatcher(name, name, null, null);
					mediatorModel.setDispatcher(disp);
				}
			}

		}
		// restore observer.
		mediatorModel.addObserver(this);
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
		BindingImpl[] bs = (BindingImpl[])mediatorModel.getInBindings();
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
		BindingImpl[] bs = (BindingImpl[])mediatorModel.getOutBindings();
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
		applicationNotifier.fireEventNode(ApplicationListenerSupport.EVT_MODIFIED, mediatorModel);
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
				logger.info("Component [{}] stopped",mediatorModel.qualifiedId());
			}
		}
	}

	/**
	 * Start the mediator instance.
	 */
	public void start() {
		createMediatorInstance();
		updateMediatorModel();
		updateMediatorInstance();
		logger.info("Component [{}] started",mediatorModel.qualifiedId());
	}

	/**
	 * Add collector instance to the mediator.
	 * 
	 * @param collector
	 *            collector model to add to the mediator.
	 */
	public void createCollector(Component collector) {
		updateMediatorModel();
		SchedulerHandler scheduler = getScheduler();
		boolean toAdd = true;
		boolean readytoAdd = true;
		if (readytoAdd && mediatorInstance == null) {
			log.error("Adding collector to the model but mediator instance is null");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && mediatorInstance.getState() != ComponentInstance.VALID) {
			log.debug(" (addCollector) Object instance in " + mediatorModel
					+ "is not valid instance");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && mediatorModel == null) {
			log.error("Mediator Model is null in addCollector");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && mediatorModel.getScheduler() == null) {
			log.warn(" (addCollector) Scheduler in " + mediatorModel + "doesnt exist");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && scheduler == null) {
			log.warn(" (addCollector) Scheduler doesnt exist");
			readytoAdd = false;
			return;
		}
		// get collector information.
		if (readytoAdd) {
			String collectorType = collector.getType();
			String collectorId = collector.getId();
			Dictionary properties = collector.getProperties();
			// add collector to the mediator instance only if it exists.
			// if (!scheduler.existsCollector(collectorId)) {
			synchronized (lockObject) {
				if (!addedCollectors.contains(collector.getId())) {
					addedCollectors.put(collector.getId(), collector);
					log.debug(" adding collector with " + properties);
				} else {
					toAdd = false;
					log.warn(" (addCollector) Object instance in " + mediatorModel
							+ "already exist" + collectorId);
				}
				if (toAdd) { // create
					scheduler.addCollector(collectorType, collectorId, properties);
				}
			}
		} else { // Add to thread to wait
			// creator.addCollector(this, collector, true);
		}
	}

	/**
	 * Create the Sender instance from mthe given model.
	 * 
	 * @param senderm
	 *            the sender model added to the mediator model.
	 */
	public void createSender(Component senderm) {
		updateMediatorModel();
		DispatcherHandler dispatcher = getDispatcher();
		boolean readytoAdd = true;
		boolean toAdd = true;
		if (readytoAdd && mediatorInstance == null) {
			log.debug("Adding Sender to the model but mediator instance is null");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && mediatorInstance.getState() != ComponentInstance.VALID) {
			log.debug(" (addSender) Object instance in " + mediatorModel
					+ " is not valid instance");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && mediatorModel == null) {
			log.debug("Mediator Model is null in addSender");
			readytoAdd = false;
			return;
		}
		if (readytoAdd && mediatorModel.getDispatcher() == null) {
			log.debug(" (addSender) Dispatcher in " + mediatorModel + "doesnt exist");
			readytoAdd = false;
			return;
		}

		if (readytoAdd && dispatcher == null) {
			log.debug(" (addSender) dispatcher is not present");
			readytoAdd = false;
			return;
		}

		// get sender information.
		if (readytoAdd) {
			String senderType = senderm.getType();
			String senderId = senderm.getId();
			Dictionary properties = senderm.getProperties();
			// add sender to the mediator instance.
			synchronized (lockObject) {
				if (!addedSenders.contains(senderm.getId())) {
					log.debug(" adding sender with " + properties);
					addedSenders.put(senderm.getId(), senderm);
				} else {
					toAdd = false;
					log.warn(" (addSender) Object instance in " + mediatorModel
							+ "already exist" + senderId);
				}
				if (toAdd) {
					dispatcher.addSender(senderType, senderId, properties);
				}
			}
		} else {
			// creator.addSender(this, sender, true);
		}
	}

	/**
	 * Remove the given collector to the mediator instance.
	 * 
	 * @param collector
	 *            Collector to remove.
	 */
	public void removeCollector(Component collector) {
		String id = (String) collector.getProperty("cilia.collector.identifier");
		String port = (String) collector.getProperty("cilia.collector.port");
		;
		synchronized (lockObject) {
			if (collector != null) {
				addedCollectors.remove(collector);
				SchedulerHandler sc = getScheduler();
				if (sc != null && collector != null) {
					sc.removeCollector(port, id);
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
		String id = (String) sender.getProperty("cilia.sender.identifier");
		String port = (String) sender.getProperty("cilia.sender.port");
		synchronized (lockObject) {
			if (sender != null) {
				addedSenders.remove(sender);
				DispatcherHandler d = getDispatcher();
				if (d != null && sender != null) {
					try {
						d.removeSender(port, id);
					} finally {
					}
				}
			}
		}
	}

	private SchedulerHandler getScheduler() {
		SchedulerHandler r = null;
		InstanceManager im = null;
		ComponentInstance componentInstance = mediatorInstance.getInstanceManager();
		if (componentInstance instanceof InstanceManager) {
			im = (InstanceManager) componentInstance;
		}
		if (componentInstance instanceof MediatorManager) {
			MediatorManager mm = (MediatorManager) componentInstance;
			if (mm != null) {
				im = (InstanceManager) mm.getProcessorInstance();
			}
		}
		if (im != null)
			r = (SchedulerHandler) im.getHandler(Const.ciliaQualifiedName("scheduler"));
		return r;
	}

	private DispatcherHandler getDispatcher() {
		DispatcherHandler r = null;
		InstanceManager im = null;
		ComponentInstance componentInstance = mediatorInstance.getInstanceManager();
		if (componentInstance instanceof InstanceManager) {
			im = (InstanceManager) componentInstance;
		}
		if (componentInstance instanceof MediatorManager) {
			MediatorManager mm = (MediatorManager) componentInstance;
			if (mm != null) {
				im = (InstanceManager) mm.getProcessorInstance();
			}
		}
		if (im != null)
			r = (DispatcherHandler) im.getHandler(Const.ciliaQualifiedName("dispatcher"));
		return r;
	}

	/**
	 * Set the dispatcher to the mediator instance.
	 */
	public void setDispatcher(Dispatcher dispatcher) {
		throw new RuntimeException("setDispatcher not implemented");
	}

	/**
	 * Set the scheduler to the mediator instance
	 */
	public void setScheduler(Scheduler scheduler) {
		throw new RuntimeException("setScheduler not implemented");

	}

	/**
	 * get the mediator instance state.
	 */
	public int getState() {
		if (mediatorInstance != null) {
			return mediatorInstance.getState();
		}
		return ComponentInstance.INVALID;
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
		log.debug(" update");
		if (mediator instanceof Mediator) {
			log.debug(" update, OK");
			Mediator md = ((Mediator) mediator);
			UpdateEvent event = (UpdateEvent) arg;
			if (event != null) {
				int action = event.getUpdateAction();
				switch (action) {
				case UpdateActions.UPDATE_PROPERTIES: {
					log.debug(" update instance property");
					updateInstanceProperties(md.getProperties());
					applicationNotifier.fireEventNode(ApplicationListenerSupport.EVT_MODIFIED, md) ;
				}
					// break;
					// case UpdateActions.UPDATE_SCHEDULER: {
					// log.debug(" update scheduler");
					// Scheduler s = md.getScheduler();
					// if (s == null) {
					// setScheduler(null);
					// } else {
					// setScheduler(s);
					// }
					// }
					// break;
					// case UpdateActions.ADD_COLLECTOR: {
					// Collector collector = (Collector) event.getSource();
					// log.debug(" add collector");
					// if (collector != null) {
					// createCollector(collector);
					// }
					// }
					// break;
					// case UpdateActions.REMOVE_COLLECTOR: {
					// Collector collector = (Collector) event.getSource();
					// log.debug(" remove collector");
					// if (collector != null) {
					// removeCollector(collector);
					// }
					// }
					// break;
					// case UpdateActions.ADD_SENDER: {
					// Sender sender = (Sender) event.getSource();
					// log.debug(" add sender");
					// if (sender != null) {
					// createSender(sender);
					// }
					// }
					// break;
					// case UpdateActions.REMOVE_SENDER: {
					// Sender sender = (Sender) event.getSource();
					// log.debug(" remove sender");
					// if (sender != null) {
					// removeSender(sender);
					// }
					// }
					// break;
				}
			}
		} else if (mediator instanceof CiliaInstanceWrapper) {
			int state = getState();
			switch (state) {
			case ComponentInstance.VALID: {
				updateMediatorModel();
				updateMediatorInstance();
			}
				break;
			case ComponentInstance.DISPOSED:
			case ComponentInstance.STOPPED:
			case ComponentInstance.INVALID: {
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
			if (mediatorInstance != null) {
				DispatcherHandler dh = getDispatcher();
				if (dh != null)
					dh.stop();
				SchedulerHandler sh = getScheduler();
				if (sh != null)
					sh.stop();
				// mediatorInstance.stop();
				addedCollectors.clear();
				addedSenders.clear();
			}
		}
	}
}
