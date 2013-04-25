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

import java.util.*;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.AdminData;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.ChainImpl;
import fr.liglab.adele.cilia.model.impl.UpdateActions;
import fr.liglab.adele.cilia.model.impl.UpdateEvent;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.FirerEvents;
import fr.liglab.adele.cilia.util.Const;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * This class is used to control a model chain and will act as an intermediator
 * between the chain model and the chain runtime.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class ChainControllerImpl implements Observer {
	/**
	 * Model chain this controller will observe.
	 */
	private final ChainImpl modelChain;

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

	private FirerEvents eventFirer;

	private static final Logger runtimeLog = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
	
	private static final Logger coreLog = LoggerFactory.getLogger(Const.LOGGER_CORE);

	private final ReadWriteLock mutex;
	/**
	 * Create a new Chain controller.
	 * 
	 * @param context
	 *            OSGi Bundle Context.
	 * @param model
	 *            chain model.
	 */
	public ChainControllerImpl(BundleContext context, Chain model, CreatorThread crea,
			FirerEvents notifier) {
		bcontext = context;
		modelChain = (ChainImpl) model;
		creator = crea;
		eventFirer = notifier;
		mutex = new ReentrantWriterPreferenceReadWriteLock();
		modelChain.addObserver(this);
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
		return modelChain;
	}

	/**
	 * Create mediator and binding observers and start observing the chain
	 * model.
	 */
	public void start() {
		createDataContainer();
		createControllers();
		startControllers();
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		/* Message (data) container for mediators managed by that chain */
		isStarted = true;
		mutex.writeLock().release();
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
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		isStarted = false;
		mutex.writeLock().release();
		this.modelChain.deleteObserver(this);
		disposeControllers();
		if (dataContainer != null) {
			dataContainer.stop();
			dataContainer = null;
		}

	}

	/**
	 * Start all the mediators added to this chain.
	 */
	private void startMediators() {
        Iterator it = null;
        try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {		}
		try{
			it = new HashSet(mediators.values()).iterator();
        } finally {
            mutex.readLock().release();
        }
			while (it.hasNext()) {
				MediatorControllerImpl mc = (MediatorControllerImpl) it.next();
				mc.start();
			}

	}

	/**
	 * Start all the adapters added to this chain.
	 */
	private void startAdapters() {
        Iterator it = null;
        try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {		}
		try{
			it = new HashSet(adapters.values()).iterator();
        }finally{
            mutex.readLock().release();
        }
			while (it.hasNext()) {
				AdapterControllerImpl mc = (AdapterControllerImpl) it.next();
				mc.start();
			}

	}

	/**
	 * Start all the bindings controllers.
	 */
	private void startBindings() {
        Iterator it = null;
        try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {		}
		try{
			it = new HashSet(bindings.values()).iterator();
        }finally{
            mutex.readLock().release();
        }

        while (it.hasNext()) {
				BindingControllerImpl bc = (BindingControllerImpl) it.next();
				bc.start();
			}

	}

	/**
	 * Stop all Mediators controllers and clear all mediators set.
	 */
	private void deleteMediators() {
        Iterator it = null;
        try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
        try{
            it = new HashSet(mediators.values()).iterator();
            mediators.clear();
        }finally{
            mutex.writeLock().release();
        }
		while (it.hasNext()) {
			MediatorControllerImpl mc = (MediatorControllerImpl) it.next();
			mc.stop();
		}

	}

	/**
	 * Stop all Adapters controllers and clear all Adapters set.
	 */
	private void deleteAdapters() {
        Iterator it = null;
        try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
        try{
            it = new HashSet(adapters.values()).iterator();
            adapters.clear();
        }finally {
            mutex.writeLock().release();
        }
		while (it.hasNext()) {
			AdapterControllerImpl mc = (AdapterControllerImpl) it.next();
			mc.stop();
		}

	}

	/**
	 * Delete all Bindings
	 */
	private void deleteBindings() {
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		bindings.clear();
		mutex.writeLock().release();
	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopMediators() {
        Iterator it = null;
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			it = new HashSet(mediators.values()).iterator();
        }finally{
            mutex.readLock().release();
        }
			while (it.hasNext()) {
				MediatorControllerImpl mc = (MediatorControllerImpl) it.next();
				mc.stop();
			}

	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopAdapters() {
        Iterator it = null;
        try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			it = new HashSet(adapters.values()).iterator();
        }finally{
            mutex.readLock().release();
        }
			while (it.hasNext()) {
				AdapterControllerImpl mc = (AdapterControllerImpl) it.next();
				mc.stop();
			}

	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopBindings() {
        Iterator it = null;

        try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {		}
		try{
			it = new HashSet(bindings.values()).iterator();
        }finally{
            mutex.readLock().release();
        }
			while (it.hasNext()) {
				BindingControllerImpl bc = (BindingControllerImpl) it.next();
				bc.stop();
			}

	}

	/**
	 * Create all mediators controllers contained in the current model.
	 */
	private void createMediators() {
		Set mediators = null;
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
        try{
		mediators = modelChain.getMediators();
        }finally{
        mutex.readLock().release();
        }

			if (mediators != null) {
				Iterator it = mediators.iterator();
				while (it.hasNext()) {
					Mediator mediatorModel = (Mediator) it.next();
					runtimeLog.debug("[{}] creating controller ", mediatorModel.getId());
					createMediatorController(mediatorModel);
				}
			}
    }


	/**
	 * Create all mediators controllers contained in the current model.
	 */
	private void createAdapters() {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
		Set adapters = modelChain.getAdapters();
        mutex.readLock().release();

			if (adapters != null) {
				Iterator it = adapters.iterator();
				while (it.hasNext()) {
					Adapter adapterModel = (Adapter) it.next();
					runtimeLog.debug("[{}] creating controller ", adapterModel.getId());
					createAdapterController(adapterModel);
				}
			}

	}

	/**
	 * Create all bindings controllers contained in the chain model.
	 */
	private void createBindings() {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}

			Set bindingsSet = modelChain.getBindings();

            mutex.readLock().release();

            if (bindingsSet != null) {
				Iterator it = bindingsSet.iterator();
				while (it.hasNext()) {
					Binding binding = (Binding) it.next();
					createBindingController(binding);
				}
			}
	}

	public void createBindingController(Binding binding) {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
        try{
            if (bindings.containsKey(binding.getId())) {
                return;
            }
        }finally {
            mutex.readLock().release();
        }


		BindingControllerImpl bindingController = new BindingControllerImpl(bcontext,
				binding, this);
		MediatorComponent smediator = binding.getSourceMediator();
		MediatorComponent tmediator = binding.getTargetMediator();
		MediatorControllerImpl targetController = null;
		MediatorControllerImpl sourceController = null;
		runtimeLog.debug("Creating binding controller from {} to {}", smediator.getId(), tmediator.getId());

		if (smediator != null) {
			sourceController = (MediatorControllerImpl) mediators.get(smediator
					.getId());
			if (sourceController == null) {
				sourceController = (AdapterControllerImpl) adapters.get(smediator
						.getId());
			}
		}
		if (tmediator != null) {
			targetController = (MediatorControllerImpl) mediators.get(tmediator
					.getId());
			if (targetController == null) {
				targetController = (AdapterControllerImpl) adapters.get(tmediator
						.getId());
			}
		}
		bindingController.setTargetController(targetController);
		bindingController.setSourceController(sourceController);
		if (isStarted) {
			runtimeLog.debug("Starting binding controller from {} to {}", smediator.getId(), tmediator.getId());
			bindingController.start();
		}

		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {	}
		bindings.put(binding.getId(), bindingController);
		mutex.writeLock().release();
		eventFirer.fireEventNode(FirerEvents.EVT_BIND,
				smediator, tmediator);
	}

	public void createMediatorController(Mediator mediator) {
		boolean created = false;
		boolean localStarted = false;
		MediatorControllerImpl mc = null;

		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {	}
		if (!mediators.containsKey(mediator.getId())) {
			created = true;
		} else {
			created = false;
		}
		mutex.readLock().release();
		
		if (created) {
			mc = new MediatorControllerImpl(bcontext, mediator, creator,eventFirer);
			try {
				mutex.writeLock().acquire();
			} catch (InterruptedException e) {		}
			mediators.put(mediator.getId(), mc);
			mutex.writeLock().release();
			eventFirer.fireEventNode(FirerEvents.EVT_ARRIVAL,
					mediator);
			try {
				mutex.readLock().acquire();
			} catch (InterruptedException e) {}
			if (isStarted) {
				localStarted = true;
			}
			mutex.readLock().release();
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
		AdapterControllerImpl mc = null;
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {	}
		try{
			if (!adapters.containsKey(adapter.getId())){
				create = true;
			} else {
				create = false;
			}
		}finally{
			mutex.readLock().release();
		}
		if (create) {
			mc = new AdapterControllerImpl(bcontext, adapter, creator,eventFirer);
			try {
				mutex.readLock().acquire();
			} catch (InterruptedException e) {	}
			if (isStarted) {
				localStarted = true;
			}
			mutex.readLock().release();

			try {
				mutex.writeLock().acquire();
			} catch (InterruptedException e) {	}
			adapters.put(adapter.getId(), mc);
			mutex.writeLock().release();
			eventFirer.fireEventNode(FirerEvents.EVT_ARRIVAL,
					adapter);
		}
		// Initialize the component in a block separate from the synchronized
		// block
		if (localStarted) {
			mc.start();
		}
	}

	public void removeBinding(Binding binding) {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {}
		try{
			if (!bindings.containsKey(binding.getId())) {
				return;
			}
		}finally{
			mutex.readLock().release();
		}
		eventFirer.fireEventNode(FirerEvents.EVT_UNBIND,
				binding.getSourceMediator(), binding.getTargetMediator());

		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {		}

		BindingControllerImpl bindingController = (BindingControllerImpl) bindings
				.remove(binding.getId());
		mutex.writeLock().release();
		if (bindingController != null) {
			bindingController.stop();
		}

	}

	public synchronized void removeMediator(Mediator mediator) {
		String mediatorId = mediator.getId();
		MediatorControllerImpl mediatorController = null; 
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			mediatorController = (MediatorControllerImpl) mediators
					.remove(mediatorId);
		}finally{
			mutex.writeLock().release();
		}
		if (mediatorController != null) {
			mediatorController.stop();
		}
		clearDataMediator(mediatorId);
		eventFirer.fireEventNode(FirerEvents.EVT_DEPARTURE,
				mediator);

	}

	public void removeAdapter(Adapter adapter) {
		String adapterId = adapter.getId();
		AdapterControllerImpl adapterController  = null;
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			adapterController = (AdapterControllerImpl) adapters
					.remove(adapterId);
		}finally{
			mutex.writeLock().release();
		}
		if (adapterController != null) {
			adapterController.stop();
		}
		clearDataMediator(adapter.getId());
		eventFirer.fireEventNode(FirerEvents.EVT_DEPARTURE,
				adapter);
	}

	public void updateInstanceProperties(Dictionary properties) {

	}

	public void update(Observable o, Object arg) {
		UpdateEvent event = (UpdateEvent) arg;
		if (o instanceof Chain) {
			Chain md = ((Chain) o);
			if (event != null) {
				int action = event.getUpdateAction();
				switch (action) {
				case UpdateActions.UPDATE_PROPERTIES: {
					updateInstanceProperties(md.getProperties());
				}
				break;
				case UpdateActions.ADD_MEDIATOR: {
					Mediator mediator = (Mediator) event.getSource();
					coreLog.debug("[{}] add mediator {}", modelChain.getId(), mediator.getId());
					if (mediator != null) {
						createMediatorController(mediator);
					}
				}
				break;
				case UpdateActions.ADD_ADAPTER: {
					Adapter adapter = (Adapter) event.getSource();
					coreLog.debug("[{}] add adapter {}", modelChain.getId(), adapter.getId());
					if (adapter != null) {
						createAdapterController(adapter);
					}
				}

				break;
				case UpdateActions.REMOVE_MEDIATOR: {
					Mediator mediator = (Mediator) event.getSource();
					coreLog.debug("[{}] remove mediator {}", modelChain.getId(), mediator.getId());
					if (mediator != null) {
						removeMediator(mediator);
					}
				}
				break;
				case UpdateActions.REMOVE_ADAPTER: {
					Adapter adapter = (Adapter) event.getSource();
					coreLog.debug("[{}] remove adapter {}", modelChain.getId(), adapter.getId());
					if (adapter != null) {
						removeAdapter(adapter);
					}
				}
				break;
				case UpdateActions.ADD_BINDING: {
					Binding binding = (Binding) event.getSource();
					StringBuffer msg = new StringBuffer("[");
					msg.append(modelChain.getId()).append("]").append(" add binding from ").
						append(binding.getSourceMediator().getId()).append(" to ").
						append(binding.getTargetMediator().getId());
					coreLog.debug(msg.toString());
					createBindingController(binding);
				}
				break;
				case UpdateActions.REMOVE_BINDING: {
					Binding binding = (Binding) event.getSource();
					StringBuffer msg = new StringBuffer("[");
					msg.append(modelChain.getId()).append("]").append(" remove binding from ").
						append(binding.getSourceMediator().getId()).append(" to ").
						append(binding.getTargetMediator().getId());
					coreLog.debug(msg.toString());
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
			dataContainer = new CiliaInstanceWrapper(bcontext, "AdminData-"
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

    protected MediatorControllerImpl getComponentcontroller(String id){
        MediatorControllerImpl controller = (MediatorControllerImpl) mediators.get(id);
        if (controller == null){
            controller = (MediatorControllerImpl) adapters.get(id);
        }
        return controller;
    }

}
