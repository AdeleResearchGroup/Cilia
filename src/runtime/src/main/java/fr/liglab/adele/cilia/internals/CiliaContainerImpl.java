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

package fr.liglab.adele.cilia.internals;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.ChainListener;
import fr.liglab.adele.cilia.CiliaContainer;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.builder.impl.BuilderImpl;
import fr.liglab.adele.cilia.event.CiliaEvent;
import fr.liglab.adele.cilia.internals.controller.ChainControllerImpl;
import fr.liglab.adele.cilia.internals.controller.CreatorThread;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.ChainRuntime;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.impl.ChainRuntimeImpl;
import fr.liglab.adele.cilia.runtime.impl.CiliaFrameworkEventPublisher;
import fr.liglab.adele.cilia.runtime.impl.MediatorRuntimeSpecification;
import fr.liglab.adele.cilia.specification.MediatorSpecification;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * This class is the entry point to Cilia, CiliaContextImpl is in charge de
 * handle the life cycle of a set of chains.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a> NA:ST
 * 
 */
public class CiliaContainerImpl implements CiliaContainer{
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);
	/**
	 * The creatorThread creates pojo instances using a given model.
	 */
	private final CreatorThread creator;

	/* Model */
	private final Map /* <ChainInstance> */chainInstances;
	/* Run time information */
	private final Map chainRuntime ;

	private final BundleContext bcontext;

	private static final Logger log = LoggerFactory.getLogger("cilia.ipojo.runtime");

	private final Map listeners;

	private final Object lockObject = new Object();

	private CiliaFrameworkEventPublisher eventNotifier;
	
	private final ReadWriteLock mutex ;
	


	/**
	 * Create a CiliaContext instance. This instance is created by iPOJO.
	 * 
	 * @param context
	 *            OSGi Bundle Context.
	 */
	public CiliaContainerImpl(BundleContext context) {
		bcontext = context;
		creator = new CreatorThread();
		chainInstances = new Hashtable();
		listeners = new Hashtable();
		chainRuntime = new Hashtable();
		mutex = new ReentrantWriterPreferenceReadWriteLock() ;
	}

	/**
	 * Add a chain to the CiliaContext. If a chain with the same id is in the
	 * context, it will not be created.
	 * 
	 * @return the added chain.
	 */
	public Chain addChain(Chain chain) {
		String chainName;
		String msg;
		boolean state = false;

		if (chain == null) {
			msg = "Chain must not be Null";
			log.error(msg);
			throw new NullPointerException(msg);
		}
		chainName = chain.getId();
		synchronized (lockObject) {
			if (!chainInstances.containsKey(chain.getId())) {
				ChainControllerImpl chainInstance = new ChainControllerImpl(bcontext, chain,
						creator);
				chainInstances.put(chainName, chainInstance);
				chainRuntime.put(chainName, new ChainRuntimeImpl());
				eventNotifier.publish(chainName, CiliaEvent.EVENT_CHAIN_ADDED);
				logger.info("Chain [{}] added",chainName) ;
				
			} else {
				msg = "Chain with the same id already in the CiliaContext";
				log.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		notifyAdd(chain);
		log.debug("chain [" + chainName + "] created OK");
		return chain;
	}

	/**
	 * Get a chain contained in the CiliaContext.
	 * 
	 * @param chainId
	 *            The chain identificator to localize.
	 * @return the localised chain.
	 */
	public Chain getChain(String chainId) {
		Chain returningChain = null;
		synchronized (lockObject) {
			if (chainInstances.containsKey(chainId)) {
				ChainControllerImpl chaininstance = getChainInstance(chainId);
				returningChain = chaininstance.getChain();
			}
		}
		return returningChain;
	}

	/**
	 * Get the chainController of the given chain Id.
	 * 
	 * @param chainId
	 * @return
	 */
	private ChainControllerImpl getChainInstance(String chainId) {
		synchronized (lockObject) {
			ChainControllerImpl chi = null;
			chi = (ChainControllerImpl) chainInstances.get(chainId);
			return chi;
		}
	}

	/**
	 * Get a set of all the chains contained in the CiliaContext.
	 * 
	 * @return the Set of chains.
	 */
	public Set getAllChains() {
		Set chainModels = null;
		Set tchainInstances = null;
		synchronized (lockObject) {
			chainModels = new HashSet(chainInstances.size());
			tchainInstances = chainInstances.keySet();
		}
		Iterator it = tchainInstances.iterator();
		while (it != null && it.hasNext()) {
			String chid = (String) it.next();
			chainModels.add(getChain(chid));
		}
		return chainModels;
	}

	/**
	 * Initialize a chain. Initialize the chain controllers to create the
	 * executing mediator instances.
	 * 
	 * @param chain
	 *            the chain to initialize.
	 * @deprecated Use instead void startChain(String chainId).
	 */
	public void startChain(Chain chain) {
		if (chain != null) {
			startChain(chain.getId());
		}
	}

	/**
	 * Initialize a chain. Initialize the chain controllers to create the
	 * executing mediator instances.
	 * 
	 * @param chainId
	 *            the chain Id to initialize.
	 */
	public void startChain(String chainId) {
		ChainControllerImpl cinstance = null;
		synchronized (lockObject) {
			if (chainInstances.containsKey(chainId)) {
				cinstance = (ChainControllerImpl) chainInstances.get(chainId);
			}
		}
		if (cinstance != null) {
			cinstance.start();
			ChainRuntimeImpl chainRt=(ChainRuntimeImpl)chainRuntime.get(chainId) ;
			chainRt.setState(ChainRuntime.STATE_STARTED) ;
			chainRt.setLastDate();
			eventNotifier.publish(chainId, CiliaEvent.EVENT_CHAIN_STARTED);
			logger.info("Chain [{}] started",chainId) ;
		}

	}

	/**
	 * Remove a chain from the Cilia Context.
	 * 
	 * @param chain
	 *            the chain model to remove.
	 * @deprecated Use instead void removeChain(String chainId).
	 */
	public void removeChain(Chain chain) {
		if (chain != null) {
			removeChain(chain.getId());
		}
	}

	/**
	 * Remove a chain from the Cilia Context.
	 * 
	 * @param chainId
	 *            the chainId model to remove.
	 */
	public void removeChain(String chainId) {
		ChainControllerImpl toBeRemoved = null;
		Chain chain = null;
		if (chainId != null) {
			synchronized (lockObject) {
				if (chainInstances.containsKey(chainId)) {
					toBeRemoved = (ChainControllerImpl) chainInstances.remove(chainId);
				}
			}
			if (toBeRemoved != null) {
				chain = toBeRemoved.getChain();
				notifyRemove(chain);
				toBeRemoved.dispose();
				toBeRemoved = null;
				ChainImpl.class.cast(chain).dispose();
				chain = null;
				eventNotifier.publish(chainId, CiliaEvent.EVENT_CHAIN_REMOVED);
				logger.info("Chain [{}] removed",chainId) ;
			}
		} else {
			log.error("remove chain", new NullPointerException(
					"ChainId must not be null."));
		}
	}

	/**
	 * Stop a chain.
	 * 
	 * @param chain
	 *            The chain to stop.
	 * @deprecated Use instead stopChain(String chainId).
	 */
	public void stopChain(Chain chain) {
		if (chain != null) {
			stopChain(chain.getId());
		}
	}

	/**
	 * Stop a chain.
	 * 
	 * @param chainId
	 *            The id of the chain to stop.
	 */
	public void stopChain(String chainId) {
		String msg;
		ChainControllerImpl ci = null;
		if (chainId != null) {
			synchronized (lockObject) {
				if (chainInstances.containsKey(chainId)) {
					ci = (ChainControllerImpl) chainInstances.get(chainId);
				}
			}
			if (ci != null) {
				ci.stop();
				ChainRuntimeImpl chainRt = (ChainRuntimeImpl)chainRuntime.get(chainId);
				chainRt.setState(ChainRuntime.STATE_STOPPED);
				chainRt.setLastDate();
				eventNotifier.publish(chainId, CiliaEvent.EVENT_CHAIN_STOPPED);
				logger.info("Chain [{}] stopped",chainId);
			} else {
				msg = "There is any chain with the given id " + chainId;
				log.error(msg);
				throw new NullPointerException(msg);
			}
		} else {
			msg = "ChainId must not be null";
			log.error(msg);
			throw new NullPointerException(msg);
		}
	}

	/**
	 * 
	 */
	public void stop() {
		creator.shutdown();
		try {
			disposeChains();
		} catch (Exception ex) {
		}
	}

	private void disposeChains() {
		Set tchainInstances = null;
		synchronized (lockObject) {
			// this method is called when the ciliaContext is stoped, so there
			// is not possible to add chains.
			tchainInstances = new HashSet(chainInstances.keySet());
		}
		Iterator it = tchainInstances.iterator();
		while (it != null && it.hasNext()) {
			String chid = (String) it.next();
			ChainControllerImpl cc = getChainInstance(chid);
			cc.dispose();
		}
	}

	public void start() {
		eventNotifier = new CiliaFrameworkEventPublisher(bcontext);
		creator.initialize();
	}

	public MediatorSpecification createMediatorSpecification(String name,
			String namespace, String category) {
		MediatorRuntimeSpecification ms = new MediatorRuntimeSpecification(name,
				namespace, category, bcontext);
		return ms;
	}

	public void addChainListener(String chainId, ChainListener listener) {
		Chain chain = null;
		if (listener == null) {
			return;
		}
		Set listListeners = null;
		synchronized (lockObject) {
			chain = getChain(chainId);
			if (listeners.containsKey(chainId)) {
				listListeners = (Set) listeners.get(chainId);
			} else {
				listListeners = new HashSet();
				listeners.put(chainId, listListeners);
			}
			listListeners.add(listener);
		}
		if (chain != null) {
			listener.onAddingChain(chain);
		}
	}

	public void removeChainListener(String chainId, ChainListener listener) {
		if (listener == null) {
			return;
		}
		Set listListeners = null;
		synchronized (lockObject) {
			if (listeners.containsKey(chainId)) {
				listListeners = (Set) listeners.get(chainId);
				listListeners.remove(listener);
			}
		}
	}

	private Set getSetListener(String chainId) {
		Set copyListeners = null;
		Set listListeners = null;
		// Obtain the list of listeners.
		synchronized (lockObject) {
			if (listeners.containsKey(chainId)) {
				listListeners = (Set) listeners.get(chainId);
				copyListeners = new HashSet(listListeners);
			}
		}
		return copyListeners;
	}

	private void notifyAdd(Chain chain) {
		Set list = getSetListener(chain.getId());
		if (list != null) {
			Iterator it = list.iterator();
			while (it.hasNext()) {
				ChainListener listener = (ChainListener) it.next();
				listener.onAddingChain(chain);
			}
		}
		eventNotifier.publish(chain, CiliaEvent.EVENT_CHAIN_ADDED);
	}

	private void notifyRemove(Chain chain) {
		Set list = getSetListener(chain.getId());
		if (list == null) {
			return;
		}
		Iterator it = list.iterator();
		while (it.hasNext()) {
			ChainListener listener = (ChainListener) it.next();
			listener.onRemovingChain(chain);
		}
		eventNotifier.publish(chain, CiliaEvent.EVENT_CHAIN_REMOVED);
	}

	public ReadWriteLock getMutex() {
		return mutex ;
	}

	public ChainRuntime getChainRuntime(String chainId) {
		ChainRuntime chain=null ;
		if (chainId != null) {
			chain = (ChainRuntime)chainRuntime.get(chainId) ;
		}
		return chain ;
	}


}
