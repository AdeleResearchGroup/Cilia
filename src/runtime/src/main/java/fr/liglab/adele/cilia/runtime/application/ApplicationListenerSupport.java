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

package fr.liglab.adele.cilia.runtime.application;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.ChainRegistration;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.NodeRegistration;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.runtime.WorkQueue;
import fr.liglab.adele.cilia.util.concurrent.ConcurrentReaderHashMap;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ApplicationListenerSupport implements TrackerCustomizer, ChainRegistration,
		NodeRegistration {
	public static final int EVT_ARRIVAL = 1;
	public static final int EVT_DEPARTURE = 2;
	public static final int EVT_MODIFIED = 3;

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);

	private static final String NODE_APPLICATION_LISTENER = "cilia.application.node";
	private static final String CHAIN_APPLICATION_LISTENER = "cilia.application.chain";
	private static final String FILTER = "(|(cilia.application.node=*)(cilia.application.chain=*))";

	private Map listenerNode;
	private Map listenerChain;
	private BundleContext bundleContext;
	private Tracker tracker;
	private WorkQueue workQueue;

	public ApplicationListenerSupport(BundleContext bc) {
		bundleContext = bc;
		listenerNode = new ConcurrentReaderHashMap();
		listenerChain = new ConcurrentReaderHashMap();
	}

	public void start(WorkQueue wq) {
		registerTracker();
		workQueue = wq;
	}

	public void stop() {
		unRegisterTracker();
		listenerNode.clear();
		listenerChain.clear();
	}

	/* register the listener tracker */
	private void registerTracker() {
		if (tracker == null) {
			try {
				tracker = new Tracker(bundleContext, bundleContext.createFilter(FILTER),
						this);
				tracker.open();
			} catch (InvalidSyntaxException e) {
			}
		}

	}

	/* unregister the listener tracker */
	private void unRegisterTracker() {
		if (tracker != null) {
			tracker.close();
		}
	}

	/* insert a new listener ,associated to a ldap filter */
	private void addFilterListener(Map map, String filter, Object listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		if (listener == null)
			throw new CiliaIllegalParameterException("listener is null");
		ArrayList array = new ArrayList(1);
		ArrayList old;
		array.add(ConstRuntime.createFilter(filter));
		/* Efficient with ConcurrentReaderHashMap */
		old = (ArrayList) map.put(listener, array);
		if (old != null) {
			array.addAll(old);
		}
	}

	/* Remove a listener */
	private void removeFilterListener(Map map, Object listener)
			throws CiliaIllegalParameterException {
		if (listener == null)
			throw new CiliaIllegalParameterException("listener is null");
		map.remove(listener);
	}

	/**
	 * Node listener
	 * 
	 * @throws InvalidSyntaxException
	 * @throws CiliaIllegalParameterException
	 */
	public void addListener(String ldapFilter, NodeCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(listenerNode, ldapFilter, listener);
	}

	/**
	 * remove a listener
	 */
	public void removeListener(NodeCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(listenerNode, listener);
	}

	public void fireEventNode(int event, Node component) {
		if (!listenerNode.isEmpty())
			workQueue.execute(new NodeFirer(event, component));
	}

	private class NodeFirer implements Runnable {
		private int event;
		private Node node;

		public NodeFirer(int event, Node node) {
			this.event = event;
			this.node = node;
		}

		public void run() {
			/* iterates over listeners */
			Iterator it = listenerNode.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean tofire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node)) {
						tofire = true;
						break;
					}
				}
				if (tofire) {
					switch (event) {
					case EVT_ARRIVAL:
						((NodeCallback) pairs.getKey()).onArrival(node);
						break;
					case EVT_DEPARTURE:
						((NodeCallback) pairs.getKey()).onDeparture(node);
						break;
					case EVT_MODIFIED:
						((NodeCallback) pairs.getKey()).onModified(node);
						break;
					}
				}
			}
		}
	}

	/**
	 * Add a listener chain
	 */
	public void addListener(String ldapFilter, ChainCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(listenerChain, ldapFilter, listener);
	}

	/**
	 * Remove a listener chain
	 */
	public void removeListener(ChainCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(listenerChain, listener);
	}

	public void fireEventChain(int evt, String name) {
		if ((!listenerChain.isEmpty()) && name != null)
			workQueue.execute(new FirerChainEvent(evt, name));
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class FirerChainEvent implements Runnable {
		private int evt;
		private Dictionary dico = new Hashtable(1);

		public FirerChainEvent(int evt, String name) {
			this.evt = evt;
			dico.put(ConstRuntime.CHAIN_ID, name);
		}

		public void run() {

			Iterator it = listenerChain.keySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean tofire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (((Filter) filters.get(i)).match(dico)) {
						tofire = true;
						break;
					}
				}
				if (tofire) {
					ChainCallback cb = (ChainCallback) pairs.getKey();
					String chainId = (String) dico.get(ConstRuntime.CHAIN_ID);
					switch (evt) {
					case 0:
						cb.onArrival(chainId);
						break;
					case 1:
						cb.onDeparture(chainId);
						break;
					case 2:
						cb.onStarted(chainId);
						break;
					case 3:
						cb.onStopped(chainId);
						break;
					}
				}
			}
		}

	}

	/* remove a listener tracked */
	private void extractService(Object service) {
		try {
			removeFilterListener(listenerChain, service);
			removeFilterListener(listenerNode, service);
		} catch (CiliaIllegalParameterException e) {
		}
	}

	/* insert a new listener tracked */
	private void insertService(ServiceReference reference) {
		String ldapFilter;
		Object service = bundleContext.getService(reference);

		ldapFilter = (String) reference.getProperty(NODE_APPLICATION_LISTENER);
		if (ldapFilter != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof NodeCallback) {
					addFilterListener(listenerNode, ldapFilter, service);
				}
			} catch (Exception e) {
				logger.error("Cannot add a listener ");
			}
		}
		ldapFilter = (String) reference.getProperty(CHAIN_APPLICATION_LISTENER);
		if (ldapFilter != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof ChainCallback) {
					addFilterListener(listenerChain, ldapFilter, service);
				}
			} catch (Exception e) {
				logger.error("Cannot add a listener ");
			}
		}
	}

	public boolean addingService(ServiceReference reference) {
		return true;
	}

	public void addedService(ServiceReference reference) {
		insertService(reference);
	}

	public void modifiedService(ServiceReference reference, Object service) {
		extractService(service);
		insertService(reference);
	}

	public void removedService(ServiceReference reference, Object service) {
		extractService(service);
	}
}
