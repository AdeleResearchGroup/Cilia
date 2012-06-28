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
package fr.liglab.adele.cilia.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import fr.liglab.adele.cilia.util.Watch;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.ModelExtension;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.util.Uuid;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public abstract class MediatorComponentImpl extends ComponentImpl implements
		MediatorComponent {

	/**
	 * Reference to the parent chain which contains this mediator representation
	 * model.
	 */
	protected volatile Chain chain = null;
	/**
	 * ComponentImpl category.
	 */
	private volatile String category = null;
	/**
	 * Scheduler representation model contained in the mediator.
	 */
	private volatile Scheduler scheduler = null;
	/**
	 * Dispatcher representation model contained in the mediator.
	 */
	private volatile Dispatcher dispatcher = null;

	private volatile Set exitBindings = new HashSet();

	private volatile Set entryBindings = new HashSet();

	/**
	 * List of port where the collectors will be asociated.
	 */
	private Map/* <PortImpl> */inPorts = new Hashtable(4);
	/**
	 * List of port where the senders will be asociated.
	 */
	private Map/* <PortImpl> */outPorts = new Hashtable(4);

	protected final Object lockObject = new Object();

	private String version = null;

	private final String uuid = Uuid.generate().toString();

	private final static long creationTimeStamp = System.currentTimeMillis();
	
	private Map additionnalModel = new HashMap(1);

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param nspace
	 *            classname of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            ChainImpl where this mediator will be.
	 */

	public MediatorComponentImpl(String id, String type, String nspace, String catego,
			String version, Dictionary properties, Chain chain) {
		super(id, type, nspace, properties);
		this.category = catego;
		this.version = version;
		setChain(chain);
		createInitialOutPorts();
	}

	/**
	 * Create the initial out ports in this mediator. This method is called when
	 * creating a new mediator object.
	 */
	protected void createInitialOutPorts() {
		Port perror = new PortImpl("error", PortType.OUTPUT, this);
		Port plog = new PortImpl("log", PortType.OUTPUT, this);
		Port pdebug = new PortImpl("debug", PortType.OUTPUT, this);
		outPorts.put("error", perror);
		outPorts.put("log", plog);
		outPorts.put("debug", pdebug);
	}

	/**
	 * Set the chain representation model which will contain this mediator.
	 * 
	 * @param chain
	 *            chain which will contain this mediator.
	 */
	public abstract void setChain(Chain chain);

	/**
	 * Get the chain representation model which contains this mediator.
	 * 
	 * @return
	 */
	public Chain getChain() {
		synchronized (lockObject) {
			return this.chain;
		}
	}

	/**
	 * Set the given scheduler to the mediator.
	 * 
	 * @param sched
	 *            Scheduler representation model to add to the mediator.
	 */
	public void setScheduler(Scheduler sched) {
		if (sched != null) {
			synchronized (lockObject) {
				if (this.scheduler != null) {
					this.scheduler.setMediator(null);
				}
				sched.setMediator(this);
				this.scheduler = sched;
			}
			setChanged();
			notifyObservers(new UpdateEvent(UpdateActions.UPDATE_SCHEDULER, sched));
		}
	}

	/**
	 * Get the scheduler representation model contained in the current mediator.
	 * 
	 * @return the scheduler representation model.
	 */
	public Scheduler getScheduler() {
		synchronized (lockObject) {
			return this.scheduler;
		}
	}

	/**
	 * Set the dispatcher representation model to the current mediator.
	 * 
	 * @param disp
	 *            Dispatcher representation model to add.
	 */
	public void setDispatcher(Dispatcher disp) {
		if (disp != null) {
			synchronized (lockObject) {
				if (this.dispatcher != null) {
					this.dispatcher.setMediator(null);
				}
				disp.setMediator(this);
				this.dispatcher = disp;
			}
			setChanged();
			notifyObservers(new UpdateEvent(UpdateActions.UPDATE_DISPATCHER, disp));
		}
	}

	/**
	 * Get the current dispatcher representation model contained in the current
	 * mediator.
	 * 
	 * @return the dispatcher representation model.
	 */
	public Dispatcher getDispatcher() {
		return this.dispatcher;
	}

	/**
	 * Create a port in the MediatorImpl with the given name. If there exist a
	 * port with the given name, it will not create a new one, it will return
	 * the one which exist.
	 * 
	 * @param name
	 *            PortImpl Name to create.
	 * @return the PortImpl with the given port Name.
	 */
	public Port getInPort(String name) {
		Port rport = null;
		boolean alreadyCreated = false;
		synchronized (inPorts) {
			if (inPorts.containsKey(name)) {
				rport = (PortImpl) inPorts.get(name);
				alreadyCreated = true;
			}
		}
		if (!alreadyCreated) {
			rport = createInPort(name);
		}
		return rport;
	}

	public Port getOutPort(String name) {
		Port rport = null;
		boolean alreadyCreated = false;
		synchronized (outPorts) {
			if (outPorts.containsKey(name)) {
				rport = (PortImpl) outPorts.get(name);
				alreadyCreated = true;
			}
		}
		if (!alreadyCreated) {
			rport = createOutPort(name);
		}
		return rport;

	}

	/**
	 * Create an In port in the MediatorImpl with the given name. If there exist
	 * a port with the given name, it will not create a new one, it will return
	 * the one which exist.
	 * 
	 * @param name
	 *            PortImpl Name to create.
	 * @return the PortImpl with the given port Name.
	 */
	public Port createInPort(String name) {
		PortImpl nport = null;
		synchronized (inPorts) {
			if (inPorts.containsKey(name)) {
				nport = (PortImpl) inPorts.get(name);
			} else {
				nport = new PortImpl(name, PortType.INPUT, this);
				inPorts.put(name, nport);
			}
		}
		return nport;
	}

	/**
	 * Create an out port in the MediatorImpl with the given name. If there
	 * exist a port with the given name, it will not create a new one, it will
	 * return the one which exist.
	 * 
	 * @param name
	 *            PortImpl Name to create.
	 * @return the PortImpl with the given port Name.
	 */
	public Port createOutPort(String name) {
		PortImpl nport = null;
		synchronized (outPorts) {
			if (outPorts.containsKey(name)) {
				nport = (PortImpl) outPorts.get(name);
			} else {
				nport = new PortImpl(name, PortType.OUTPUT, this);
				outPorts.put(name, nport);
			}
		}
		return nport;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		synchronized (lockObject) {
			this.category = category;
		}

	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		synchronized (lockObject) {
			return category;
		}
	}

	/**
	 * Get an array of all the bindings added to the mediator.
	 * 
	 * @return
	 */
	public Binding[] getInBindings() {
		Collection col = null;
		BindingImpl[] ss = null;
		synchronized (entryBindings) {
			col = new ArrayList(entryBindings);
			ss = (BindingImpl[]) col.toArray(new BindingImpl[entryBindings.size()]);
		}
		return ss;
	}

	public Binding[] getOutBindings() {
		Collection col = null;
		Binding[] ss = null;
		synchronized (exitBindings) {
			col = new ArrayList(exitBindings);
			ss = (Binding[]) col.toArray(new BindingImpl[exitBindings.size()]);
		}
		return ss;
	}

	public void addOutBinding(Binding bindingToAdd) {
		synchronized (exitBindings) {
			exitBindings.add(bindingToAdd);
		}
	}

	public void addInBinding(Binding bindingToAdd) {
		synchronized (entryBindings) {
			entryBindings.add(bindingToAdd);
		}
	}

	public boolean removeInBinding(Binding binding) {
		boolean isRemoved = false;
		synchronized (entryBindings) {
			isRemoved = entryBindings.remove(binding);
		}
		return isRemoved;
	}

	public boolean removeOutBinding(Binding binding) {
		boolean isRemoved = false;
		synchronized (exitBindings) {
			isRemoved = exitBindings.remove(binding);
		}
		return isRemoved;
	}

	public Binding[] getBinding(Port port) {
		Binding[] allBindings = null;
		if (port.equals(PortType.INPUT)) {
			allBindings = getInBindings();
		} else {
			allBindings = getOutBindings();
		}
		List returningBindings = new ArrayList();
		for (int i = 0; allBindings != null && i < allBindings.length; i++) {
			Port sport = allBindings[i].getSourcePort();
			Port tport = allBindings[i].getTargetPort();
			if (sport == port || tport == port) {
				returningBindings.add(allBindings[i]);
			}
		}
		return (BindingImpl[]) returningBindings
				.toArray(new BindingImpl[returningBindings.size()]);
	}

	public void lockRuntime() {
		this.setProperty(ConstModel.PROPERTY_LOCK_UNLOCK, ConstModel.SET_LOCK);

	}

	public void unLockRuntime() {
		this.setProperty(ConstModel.PROPERTY_LOCK_UNLOCK, ConstModel.SET_UNLOCK);
	}

	public synchronized boolean isLocked() {
		boolean isLocked = false;
		String lock = (String) this.getProperty(ConstModel.PROPERTY_LOCK_UNLOCK);
		if (lock != null) {
			isLocked = lock.equals(ConstModel.SET_LOCK);
		}
		return isLocked;
	}

	public void dispose() {
		super.dispose();
		this.category = null;
		this.chain = null;
		this.dispatcher = null;
		this.scheduler = null;

		this.entryBindings.clear();
		this.entryBindings = null;

		this.exitBindings.clear();
		this.exitBindings = null;

		this.inPorts.clear();
		this.outPorts.clear();

	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	public String nodeId() {
		return getId();
	}

	public String chainId() {
		return getChain().getId();
	}

	public String uuid() {
		return uuid;
	}

	public long timeStamp() {
		return creationTimeStamp;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(FrameworkUtils.makeQualifiedId(chainId(), nodeId(), uuid()));
		sb.append("creation date :"+Watch.formatDateIso8601(creationTimeStamp));
		sb.append(",properties:").append(super.getProperties());
		return sb.toString();
	}
	

	public String[] extendedModelName() {
		final Set keys = additionnalModel.keySet();
		return (String[]) keys.toArray(new String[keys.size()]);
	}

	
	public ModelExtension getModel(String modelName) {
		ModelExtension modelExtension = null ;
		if (modelName !=null) {
			modelExtension = (ModelExtension)additionnalModel.get(modelName) ;
		}
		return modelExtension ;
	}
	
	public void addModel(String modelName,ModelExtension modelExtension) {
		if ((modelName !=null) && (modelName.length()>0))
			additionnalModel.put(modelName, modelExtension) ;
	}
	
	public void removeModel(String modelName) {
		if ((modelName !=null) && (modelName.length()>0))
			additionnalModel.remove(modelName) ;
	}
	
	
}
