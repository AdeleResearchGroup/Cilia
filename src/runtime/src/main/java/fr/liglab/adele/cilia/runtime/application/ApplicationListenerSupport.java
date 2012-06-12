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

import java.lang.reflect.InvocationTargetException;
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
import fr.liglab.adele.cilia.util.SwingWorker;
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
	private static int TIMEOUT = 1000; /*
										 * Max time allowed per subscribers : 1
										 * second
										 */
	public static final int EVT_ARRIVAL = 1;
	public static final int EVT_DEPARTURE = 2;
	public static final int EVT_MODIFIED = 3;
	public static final int EVT_STARTED = 4;
	public static final int EVT_STOPPED = 5;
	public static final int EVT_BIND = 6;
	public static final int EVT_UNBIND = 7;

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);

	private static final String NODE_APPLICATION_LISTENER = "cilia.application.node";
	private static final String CHAIN_APPLICATION_LISTENER = "cilia.application.chain";
	private static final String FILTER = "(|(cilia.application.node=*)(cilia.application.chain=*))";

	private Map listenerNode;
	private Map listenerChain;
	private BundleContext bundleContext;
	private Tracker tracker;

	public ApplicationListenerSupport(BundleContext bc) {
		bundleContext = bc;
		listenerNode = new ConcurrentReaderHashMap();
		listenerChain = new ConcurrentReaderHashMap();
	}

	public void start() {
		registerTracker();
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
				/* Never happens */
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
		if (listenerNode.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = listenerNode.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new NodeFirerTimed(event, component, (NodeCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}
	}

	public void fireEventNode(int event, Node from, Node to) {
		if (listenerNode.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = listenerNode.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new NodeFirerTimed(event, from, to, (NodeCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}

	}

	private class NodeFirerTimed extends SwingWorker {
		private int event;
		private Node node, dest;
		private NodeCallback callback;
		private ArrayList filters;

		public NodeFirerTimed(int event, Node node, NodeCallback callback,
				ArrayList filters) {
			super(TIMEOUT);
			this.event = event;
			this.node = node;
			this.callback = callback;
			this.filters = filters;
		}

		public NodeFirerTimed(int event, Node from, Node to, NodeCallback callback,
				ArrayList filters) {
			this(event, from, callback, filters);
			this.dest = to;
		}

		protected Object construct() throws InterruptedException {
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
					callback.onArrival(node);
					break;
				case EVT_DEPARTURE:
					callback.onDeparture(node);
					break;
				case EVT_MODIFIED:
					callback.onModified(node);
					break;
				case EVT_BIND:
					callback.onBind(node, dest);
					break;

				case EVT_UNBIND:
					callback.onUnBind(node, dest);
					break;
				}
			}
			return null;
		}

		/* Wait end of asynchronous subscriber call */
		protected void finished() {
			try {
				get();
			} catch (InvocationTargetException e) {
				Throwable ex = e.getTargetException();
				if (ex instanceof InterruptedException) {
					logger.error("TimeOut callback application specification 'node' ",ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ",e);
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

	/* Call susbcribers in separated thread */
	public void fireEventChain(int evt, String chainId) {
		if (listenerChain.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = listenerChain.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new FiredChainTimed(evt, chainId, (ChainCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}

	}

	private class FiredChainTimed extends SwingWorker {

		private int evt;
		private ChainCallback callback;
		private ArrayList filters;
		private Dictionary dico = new Hashtable(1);

		public FiredChainTimed(int evt, String name, ChainCallback callback,
				ArrayList filters) {
			super(TIMEOUT);
			this.evt = evt;
			this.callback = callback;
			this.filters = filters;
			dico.put(ConstRuntime.CHAIN_ID, name);
		}

		protected Object construct() throws Exception {
			boolean tofire = false;
			for (int i = 0; i < filters.size(); i++) {
				if (((Filter) filters.get(i)).match(dico)) {
					tofire = true;
					break;
				}
			}
			if (tofire) {

				String chainId = (String) dico.get(ConstRuntime.CHAIN_ID);
				switch (evt) {
				case EVT_ARRIVAL:
					callback.onAdded(chainId);
					break;
				case EVT_DEPARTURE:
					callback.onRemoved(chainId);
					break;
				case EVT_STARTED:
					callback.onStarted(chainId);
					break;
				case EVT_STOPPED:
					callback.onStopped(chainId);
					break;
				}
			}
			return null;
		}

		protected void finished() {
			try {
				get();
			} catch (InvocationTargetException e) {
				Throwable ex = e.getTargetException();
				if (ex instanceof InterruptedException) {
					logger.error("TimeOut callback application specification 'node' ",ex);
				}
			} catch (InterruptedException e) {
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
				logger.error("Cannot add a listener ",e);
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
				logger.error("Cannot add a listener ",e);
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
