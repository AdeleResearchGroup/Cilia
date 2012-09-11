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
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

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

	private static final Logger log = LoggerFactory.getLogger("cilia.ipojo.runtime");

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
		this.modelChain.deleteObserver(this);
		disposeControllers();
		if (dataContainer != null) {
			dataContainer.stop();
			dataContainer = null;
		}
		mutex.writeLock().release();
	}

	/**
	 * Start all the mediators added to this chain.
	 */
	private void startMediators() {
		// mediators.
		Iterator it = mediators.values().iterator();
		while (it.hasNext()) {
			MediatorControllerImpl mc = (MediatorControllerImpl) it.next();
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
			AdapterControllerImpl mc = (AdapterControllerImpl) it.next();
			mc.start();
		}
	}

	/**
	 * Start all the bindings controllers.
	 */
	private void startBindings() {
		Iterator it = bindings.values().iterator();
		while (it.hasNext()) {
			BindingControllerImpl bc = (BindingControllerImpl) it.next();
			bc.start();
		}
	}

	/**
	 * Stop all Mediators controllers and clear all mediators set.
	 */
	private void deleteMediators() {
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		Iterator it = mediators.values().iterator();
		while (it.hasNext()) {
			MediatorControllerImpl mc = (MediatorControllerImpl) it.next();
			mc.stop();
		}
		mediators.clear();
		mutex.writeLock().release();
	}

	/**
	 * Stop all Adapters controllers and clear all Adapters set.
	 */
	private void deleteAdapters() {
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
		}
		Iterator it = adapters.values().iterator();
		while (it.hasNext()) {
			AdapterControllerImpl mc = (AdapterControllerImpl) it.next();
			mc.stop();
		}
		adapters.clear();
		mutex.writeLock().release();
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
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			Iterator it = mediators.values().iterator();
			while (it.hasNext()) {
				MediatorControllerImpl mc = (MediatorControllerImpl) it.next();
				mc.stop();
			}
		}finally{
			mutex.readLock().release();
		}
	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopAdapters() {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			Iterator it = adapters.values().iterator();
			while (it.hasNext()) {
				AdapterControllerImpl mc = (AdapterControllerImpl) it.next();
				mc.stop();
			}
		}finally{
			mutex.readLock().release();
		}
	}

	/**
	 * Stop all the mediators added to this chain.
	 */
	private void stopBindings() {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {		}
		try{
			Iterator it = bindings.values().iterator();
			while (it.hasNext()) {
				BindingControllerImpl bc = (BindingControllerImpl) it.next();
				bc.stop();
			}
		}finally{
			mutex.readLock().release();
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
		mediators = modelChain.getMediators();
		try{
			if (mediators != null) {
				Iterator it = mediators.iterator();
				while (it.hasNext()) {
					Mediator mediatorModel = (Mediator) it.next();
					if (log.isDebugEnabled())
						log.debug("creating mediator " + mediatorModel.getId());
					createMediatorController(mediatorModel);
				}
			}
		}finally{
			mutex.readLock().release();
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
		try{
			if (adapters != null) {
				Iterator it = adapters.iterator();
				while (it.hasNext()) {
					Adapter adapterModel = (Adapter) it.next();
					if (log.isDebugEnabled())
						log.debug("creating mediator " + adapterModel.getId());
					createAdapterController(adapterModel);
				}
			}
		}finally{
			mutex.readLock().release();
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
		try{
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
		}finally{
			mutex.readLock().release();
		}
	}

	public void createBindingController(Binding binding) {
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
		}
		if (bindings.containsKey(binding.getId())) {
			return;
		}

		mutex.readLock().release();			

		BindingControllerImpl bindingController = new BindingControllerImpl(bcontext,
				binding);
		MediatorComponent smediator = binding.getSourceMediator();
		MediatorComponent tmediator = binding.getTargetMediator();
		MediatorControllerImpl targetController = null;
		MediatorControllerImpl sourceController = null;
		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			bindingController.start();
		}
		mutex.readLock().release();
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {	}
		bindings.put(binding.getId(), bindingController);
		mutex.writeLock().release();
		eventFirer.fireEventNode(FirerEvents.EVT_BIND,
				smediator, tmediator);
	}

	public void createMediatorController(Mediator mediator) {
		boolean localStarted = false;
		MediatorControllerImpl mc = null;

		try {
			mutex.readLock().acquire();
		} catch (InterruptedException e) {	}

		if (!mediators.containsKey(mediator.getId())) {
			mc = new MediatorControllerImpl(bcontext, mediator, creator,eventFirer);
			try {
				mutex.writeLock().acquire();
			} catch (InterruptedException e) {		}

			mediators.put(mediator.getId(), mc);
			mutex.writeLock().release();
			eventFirer.fireEventNode(FirerEvents.EVT_ARRIVAL,
					mediator);
			if (isStarted) {
				localStarted = true;
			}
		}
		mutex.readLock().release();
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
		if (!adapters.containsKey(adapter.getId())) {
			mc = new AdapterControllerImpl(bcontext, adapter, creator,eventFirer);
			if (isStarted) {
				localStarted = true;
			}
			try {
				mutex.writeLock().acquire();
			} catch (InterruptedException e) {	}
			adapters.put(adapter.getId(), mc);
			mutex.writeLock().release();
			eventFirer.fireEventNode(FirerEvents.EVT_ARRIVAL,
					adapter);
		}
		mutex.readLock().release();
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
		System.out.println("ChainControllerImpl:update: " + arg);
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

}
