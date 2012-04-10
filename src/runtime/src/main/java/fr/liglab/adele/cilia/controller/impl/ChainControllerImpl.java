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

package fr.liglab.adele.cilia.controller.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.controller.AdapterController;
import fr.liglab.adele.cilia.controller.BindingController;
import fr.liglab.adele.cilia.controller.ChainController;
import fr.liglab.adele.cilia.controller.MediatorController;
import fr.liglab.adele.cilia.event.CiliaEvent;
import fr.liglab.adele.cilia.framework.utils.AdminData;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.UpdateActions;
import fr.liglab.adele.cilia.model.UpdateEvent;
import fr.liglab.adele.cilia.runtime.AbstractCiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.impl.CiliaFrameworkEventPublisher;

/**
 * This class is used to control a model chain and will act as an intermediator
 * between the chain model and the chain runtime.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class ChainControllerImpl implements ChainController, Observer {
	/**
	 * Model chain this controller will observe.
	 */
	private final Chain modelChain;

	private CreatorThread creator;

	private volatile boolean isStarted = false;
	/**
	 * Mediators Controllers instances.
	 */
	private volatile Map/* <String, MediatorController> */mediators = new Hashtable();

	/**
	 * Mediators Controllers instances.
	 */
	private volatile Map/* <String, AdapterController> */adapters = new Hashtable();
	/**
	 * Bindings Controllers instances.
	 */
	private volatile Map/* <String, BindingController> */bindings = new Hashtable();;
	/**
	 * OSGi Bundle Context used to instantiate mediators.
	 */
	private BundleContext bcontext = null;

	private CiliaInstance dataContainer = null;

	private final Object lockObject = new Object();

	private CiliaFrameworkEventPublisher eventNotifier;

	private static final Logger log = LoggerFactory.getLogger("cilia.ipojo.runtime");

	/**
	 * Create a new Chain controller.
	 * 
	 * @param context
	 *            OSGi Bundle Context.
	 * @param model
	 *            chain model.
	 */
	public ChainControllerImpl(BundleContext context, Chain model, CreatorThread crea) {
		bcontext = context;
		modelChain = model;
		creator = crea;
		eventNotifier = new CiliaFrameworkEventPublisher(context);
	}

	private void createControllers() {
		createMediators();
		createAdapters();
		createBindings();
	}

	private void disposeControllers() {
		deleteMediators();
		deleteAdapters();
		deleteBindings();
	}

	/**
	 * Remove all mediators and binding references.
	 */
	public void dispose() {
		stop();
		disposeControllers();
	}

	/**
	 * Get the chain model this current object handle.
	 */
	public Chain getChain() {
		synchronized (lockObject) {
			return modelChain;
		}
	}

	/**
	 * Create mediator and binding observers and start observing the chain
	 * model.
	 */
	public void start() {
		createDataContainer();
		createControllers();
		startControllers();
		synchronized (lockObject) {
			/* Message (data) container for mediators managed by that chain */
			isStarted = true;
			modelChain.addObserver(this);
		}

	}

	private void startControllers() {
		if (dataContainer != null)
			dataContainer.start();
		startMediators();
		startAdapters();
		startBindings();
	}

	/**
	 * Stop observing mediators and observer models and stop observing the chain
	 * model.
	 */
	public void stop() {
		synchronized (lockObject) {
			isStarted = false;
			this.modelChain.deleteObserver(this);
		}
		// stopControllers();
		deleteAdapters();
		deleteMediators();
		deleteBindings();
		if (dataContainer != null) {
			dataContainer.stop();
			dataContainer = null;
		}
	}

	/**
	 * Stop all the controllers.
	 */
	private void stopControllers() {
		stopMediators();
		stopAdapters();
		stopBindings();
	}

	/**
	 * Start all the mediators added to this chain.
	 */
	private void startMediators() {
		// mediators.
		Iterator it = mediators.values().iterator();
		while (it.hasNext()) {
			MediatorController mc = (MediatorController) it.next();
			mc.start();
		}
	}

	/**
	 * Start all the adapters added to this chain.
	 */
	private void startAdapters() {
		// mediators.
		Iterator it = adapters.values().iterator();
		while (it.hasNext()) {
			AdapterController mc = (AdapterController) it.next();
			mc.start();
		}
	}

	/**
	 * Start all the bindings controllers.
	 */
	private void startBindings() {
		Iterator it = bindings.values().iterator();
		while (it.hasNext()) {
			BindingController bc = (BindingController) it.next();
			bc.start();
		}
	}

	/**
	 * Stop all Mediators controllers and clear all mediators set.
	 */
	private void deleteMediators() {
		Iterator it = mediators.values().iterator();
		while (it.hasNext()) {
			MediatorController mc = (MediatorController) it.next();
			mc.stop();
		}
		mediators.clear();
	}

	/**
	 * Stop all Adapters controllers and clear all Adapters set.
	 */
	private void deleteAdapters() {
		Iterator it = adapters.values().iterator();
		while (it.hasNext()) {
			AdapterController mc = (AdapterController) it.next();
			mc.stop();
		}
		adapters.clear();
	}

	/**
	 * Delete all Bindings
	 */
	private void deleteBindings() {
		bindings.clear();
	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopMediators() {
		Iterator it = mediators.values().iterator();
		while (it.hasNext()) {
			MediatorController mc = (MediatorController) it.next();
			mc.stop();
		}
	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopAdapters() {
		Iterator it = adapters.values().iterator();
		while (it.hasNext()) {
			AdapterController mc = (AdapterController) it.next();
			mc.stop();

		}
	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopBindings() {
		Iterator it = bindings.values().iterator();
		while (it.hasNext()) {
			BindingController bc = (BindingController) it.next();
			bc.stop();
		}
	}

	/**
	 * Create all mediators controllers contained in the current model.
	 */
	private void createMediators() {
		Set mediators = null;
		synchronized (lockObject) {
			mediators = modelChain.getMediators();
		}
		if (mediators != null) {
			Iterator it = mediators.iterator();
			while (it.hasNext()) {
				Mediator mediatorModel = (Mediator) it.next();
				if (log.isDebugEnabled())
					log.debug("creating mediator " + mediatorModel.getId());
				createMediatorController(mediatorModel);
			}
		}
	}

	/**
	 * Create all mediators controllers contained in the current model.
	 */
	private void createAdapters() {
		Set adapters = modelChain.getAdapters();
		if (adapters != null) {
			Iterator it = adapters.iterator();
			while (it.hasNext()) {
				Adapter adapterModel = (Adapter) it.next();
				if (log.isDebugEnabled())
					log.debug("creating mediator " + adapterModel.getId());
				createAdapterController(adapterModel);
			}
		}
	}

	/**
	 * Create all bindings controllers contained in the chain model.
	 */
	private void createBindings() {
		// initial bindings
		Set bindings = modelChain.getBindings();
		if (bindings != null) {
			Iterator it = bindings.iterator();
			if (log.isDebugEnabled())
				log.debug(" Total bindings " + bindings.size());
			while (it.hasNext()) {
				Binding binding = (Binding) it.next();
				createBindingController(binding);
			}
		}
	}

	public void createBindingController(Binding binding) {

		if (bindings.containsKey(binding.getId())) {
			return;
		}

		BindingController bindingController = new BindingControllerImpl(bcontext, binding);
		MediatorComponent smediator = binding.getSourceMediator();
		MediatorComponent tmediator = binding.getTargetMediator();
		MediatorController targetController = null;
		MediatorController sourceController = null;
		synchronized (lockObject) {
			if (smediator != null) {
				sourceController = (MediatorController) mediators.get(smediator.getId());
				if (sourceController == null) {
					sourceController = (AdapterController) adapters
							.get(smediator.getId());
				}
			}
			if (tmediator != null) {
				targetController = (MediatorController) mediators.get(tmediator.getId());
				if (targetController == null) {
					targetController = (AdapterController) adapters
							.get(tmediator.getId());
				}
			}

			bindingController.setTargetController(targetController);
			bindingController.setSourceController(sourceController);
			if (isStarted) {
				bindingController.start();
			}
			bindings.put(binding.getId(), bindingController);
		}
	}

	public void createMediatorController(Mediator mediator) {
		boolean localStarted = false;
		MediatorController mc = null;
		synchronized (lockObject) {
			if (!mediators.containsKey(mediator.getId())) {
				mc = new MediatorControllerImpl(bcontext, mediator, creator);
				mediators.put(mediator.getId(), mc);
				eventNotifier.publish(mediator, CiliaEvent.EVENT_MEDIATOR_ADDED);
				if (isStarted) {
					localStarted = true;
				}
			}
		}
		// Initialize the component in a block separate from the synchronized
		// block
		if (localStarted) {
			mc.start();
		}
	}

	public void createAdapterController(Adapter adapter) {
		boolean create = true;
		boolean localStarted = false;
		AdapterController mc = null;
		synchronized (lockObject) {
			if (!adapters.containsKey(adapter.getId())) {
				mc = new AdapterControllerImpl(bcontext, adapter, creator);
				if (isStarted) {
					localStarted = true;
				}
				adapters.put(adapter.getId(), mc);
				eventNotifier.publish(adapter, CiliaEvent.EVENT_ADAPTER_ADDED);
			}
		}
		// Initialize the component in a block separate from the synchronized
		// block
		if (localStarted) {
			mc.start();
		}
	}

	public void removeBinding(Binding binding) {
		if (!bindings.containsKey(binding.getId())) {
			return;
		}
		BindingController bindingController = (BindingController) bindings.remove(binding
				.getId());
		if (bindingController != null) {
			bindingController.stop();
		}

	}

	public synchronized void removeMediator(Mediator mediator) {
		String mediatorId = mediator.getId();

		MediatorController mediatorController = (MediatorController) mediators
				.remove(mediatorId);
		if (mediatorController != null) {
			mediatorController.stop();
		}
		clearDataMediator(mediatorId);
		eventNotifier.publish(modelChain.getId(), mediatorId,
				CiliaEvent.EVENT_MEDIATOR_REMOVED);
	}

	public void removeAdapter(Adapter adapter) {
		String adapterId = adapter.getId();

		AdapterController adapterController = (AdapterController) adapters
				.remove(adapterId);
		if (adapterController != null) {
			adapterController.stop();
		}
		clearDataMediator(adapter.getId());
		eventNotifier.publish(modelChain.getId(), adapterId,
				CiliaEvent.EVENT_ADAPTER_REMOVED);
	}

	public void updateInstanceProperties(Dictionary properties) {

	}

	public void update(Observable o, Object arg) {
		log.debug("update");
		if (o instanceof Chain) {
			log.debug(" update, OK");
			Chain md = ((Chain) o);
			UpdateEvent event = (UpdateEvent) arg;
			if (event != null) {
				int action = event.getUpdateAction();
				switch (action) {
				case UpdateActions.UPDATE_PROPERTIES: {
					log.debug(" update instance property");
					updateInstanceProperties(md.getProperties());
				}
					break;
				case UpdateActions.ADD_MEDIATOR: {
					Mediator mediator = (Mediator) event.getSource();
					log.debug(" add mediator");
					if (mediator != null) {
						createMediatorController(mediator);
					}
				}
					break;
				case UpdateActions.ADD_ADAPTER: {
					Adapter adapter = (Adapter) event.getSource();
					log.debug(" add mediator");
					if (adapter != null) {
						createAdapterController(adapter);
					}
				}

					break;
				case UpdateActions.REMOVE_MEDIATOR: {
					Mediator mediator = (Mediator) event.getSource();
					log.debug(" remove mediator");
					if (mediator != null) {
						removeMediator(mediator);
					}
				}
					break;
				case UpdateActions.REMOVE_ADAPTER: {
					Adapter adapter = (Adapter) event.getSource();
					log.debug(" remove adapter");
					if (adapter != null) {
						removeAdapter(adapter);
					}
				}
					break;
				case UpdateActions.ADD_BINDING: {
					log.debug(" Add binding");
					Binding binding = (Binding) event.getSource();
					createBindingController(binding);
				}
					break;
				case UpdateActions.REMOVE_BINDING: {
					log.debug(" Remove binding");
					Binding binding = (Binding) event.getSource();
					removeBinding(binding);
				}
					break;
				}
			}
		}
	}

	private String createDataContainerFilter() {
		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(factory.name=AdminData)");
		filter.append("(factory.state=1)");
		filter.append(")");
		return filter.toString();
	}

	private void createDataContainer() {
		if (!modelChain.getId().equals("admin-chain")) {
			String filter = createDataContainerFilter();
			Dictionary props = new Hashtable();
			props.put("chain.name", modelChain.getId());
			dataContainer = new AbstractCiliaInstance(bcontext, "AdminData-"
					+ modelChain.getId(), createDataContainerFilter(), props, null);
			dataContainer.start();
		}
	}

	/*
	 * Retreive the data container hosting the mediator clearing all data
	 * belonging to the mediator
	 */
	private void clearDataMediator(String mediatorId) {
		AdminData dataContainer;
		ServiceReference[] refs = null;
		try {
			refs = bcontext.getServiceReferences(AdminData.class.getName(),
					"(chain.name=" + modelChain.getId() + ")");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Admin data service lookup unrecoverable error");
		}
		if (refs != null) {
			dataContainer = (AdminData) bcontext.getService(refs[0]);
			dataContainer.clearData(mediatorId);
			bcontext.ungetService(refs[0]);
		}
	}

}
