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

package fr.liglab.adele.cilia.runtime.knowledge;

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
import fr.liglab.adele.cilia.EventsConfiguration;
import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.ThresholdsCallback;
import fr.liglab.adele.cilia.VariableCallback;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.runtime.FirerEvents;
import fr.liglab.adele.cilia.util.SwingWorker;
import fr.liglab.adele.cilia.util.concurrent.ConcurrentReaderHashMap;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EventsManagerImpl implements TrackerCustomizer, EventsConfiguration,
		FirerEvents {
	private static int TIMEOUT = 1000; /*
										 * Max time allowed per subscribers : 1
										 * second
										 */

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOGGER_KNOWLEDGE);

	private static final String NODE_APPLICATION_LISTENER = "cilia.application.node";
	private static final String CHAIN_APPLICATION_LISTENER = "cilia.application.chain";
	private static final String NODE_MEASURE = "cilia.runtime.node.data";
	private static final String MEASURE_THRESHOLD = "cilia.runtime.node.threshold";
	private static final String FILTER = "(|(cilia.application.node=*)(cilia.application.chain=*)"
			+ "(cilia.runtime.node.data=*)(cilia.runtime.node.threshold=*))";

	private Map listenerNode;
	private Map listenerChain;
	private Map listenerVariable;
	private Map listenerThreshold;
	private BundleContext bundleContext;
	private Tracker tracker;

	public EventsManagerImpl(BundleContext bc) {
		bundleContext = bc;
		listenerNode = new ConcurrentReaderHashMap();
		listenerChain = new ConcurrentReaderHashMap();
		listenerVariable = new ConcurrentReaderHashMap();
		listenerThreshold = new ConcurrentReaderHashMap();
	}

	public void start() {
		registerTracker();
	}

	public void stop() {
		unRegisterTracker();
		listenerNode.clear();
		listenerChain.clear();
		listenerVariable.clear();
		listenerThreshold.clear();
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

		ArrayList array, old;
		array = new ArrayList(1);
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

	/* Notify a proxy weak reference Node */
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
			this.node = (Node) MediatorModelProxy.getInstance().makeNode(node);
			this.callback = callback;
			this.filters = filters;
		}

		public NodeFirerTimed(int event, Node from, Node to, NodeCallback callback,
				ArrayList filters) {
			this(event, from, callback, filters);
			this.dest = (Node) MediatorModelProxy.getInstance().makeNode(to);
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
					logger.error("TimeOut callback application specification 'node' ", ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ", e);
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
					logger.error("TimeOut callback application specification 'node' ", ex);
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
				logger.error("Cannot add a listener ", e);
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
				logger.error("Cannot add a listener ", e);
			}
		}
		ldapFilter = (String) reference.getProperty(NODE_MEASURE);
		if (ldapFilter != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof VariableCallback) {
					addFilterListener(listenerVariable, ldapFilter, service);
				}
			} catch (Exception e) {
				logger.error("Cannot add a listener ", e);
			}
		}
		ldapFilter = (String) reference.getProperty(MEASURE_THRESHOLD);
		if (ldapFilter != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof ThresholdsCallback) {
					addFilterListener(listenerThreshold, ldapFilter, service);
				}
			} catch (Exception e) {
				logger.error("Cannot add a listener ", e);
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

	public void addListener(String ldapfilter, VariableCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(listenerVariable, ldapfilter, listener);
	}

	public void removeListener(VariableCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(listenerVariable, listener);
	}

	/**
	 * Notify all listeners matching the filter
	 * 
	 * the notified Node is a proxy weak Reference
	 * 
	 * @param node
	 * @param variableId
	 */
	public void fireEventMeasure(Node node, String variableId, Measure m) {

		if (listenerVariable.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = listenerVariable.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new MeasureFirerTimed(node, variableId, m, (VariableCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}

	}

	private class MeasureFirerTimed extends SwingWorker {
		private Node node;
		private VariableCallback callback;
		private ArrayList filters;
		private Measure measure;
		private String variableId;

		public MeasureFirerTimed(Node node, String variableId, Measure m,
				VariableCallback callback, ArrayList filters) {
			super(TIMEOUT);
			this.node = (Node) MediatorModelProxy.getInstance().makeNode(node);
			this.callback = callback;
			this.filters = filters;
			this.measure = m.clone();
			this.variableId = variableId;
		}

		protected Object construct() throws InterruptedException {
			for (int i = 0; i < filters.size(); i++) {
				if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node)) {
					callback.onUpdate(node, variableId, measure);
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
					logger.error("TimeOut callback application runtime 'measure' ", ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ", e);
			}
		}
	}
	
	/**
	 * Notify all listeners matching the filter
	 * 
	 * the notified Node is a proxy weak Reference
	 * 
	 * @param node
	 * @param variableId
	 */
	public void fireEventVariableStatus(Node node, String variableId, boolean enable) {

		if (listenerVariable.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = listenerVariable.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new VariableStatusTimed(node, variableId, enable, (VariableCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}

	}

	private class VariableStatusTimed extends SwingWorker {
		private Node node;
		private VariableCallback callback;
		private ArrayList filters;
		private boolean enable;
		private String variableId;

		public VariableStatusTimed(Node node, String variableId, boolean enable,
				VariableCallback callback, ArrayList filters) {
			super(TIMEOUT);
			this.node = (Node) MediatorModelProxy.getInstance().makeNode(node);
			this.callback = callback;
			this.filters = filters;
			this.enable = enable;
			this.variableId = variableId;
		}

		protected Object construct() throws InterruptedException {
			for (int i = 0; i < filters.size(); i++) {
				if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node)) {
					callback.onStateChange(node, variableId, enable);
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
					logger.error("TimeOut callback application runtime 'measure' ", ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ", e);
			}
		}
	}


	public void addListener(String ldapfilter, ThresholdsCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(listenerThreshold, ldapfilter, listener);
	}

	public void removeListener(ThresholdsCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(listenerThreshold, listener);
	}

	public void fireThresholdEvent(Node node, String variableId, Measure measure, int evt) {

		if (listenerThreshold.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = listenerThreshold.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new ThresholdFirerTimed(node, variableId, measure, evt,
					(ThresholdsCallback) pairs.getKey(), (ArrayList) pairs.getValue())
					.start();
		}

	}

	private class ThresholdFirerTimed extends SwingWorker {
		private Node node;
		private ThresholdsCallback callback;
		private ArrayList filters;
		private Measure measure;
		private String variableId;
		private int evt;
		private Object o = new Object();

		public ThresholdFirerTimed(Node node, String variableId, Measure m, int evt,
				ThresholdsCallback callback, ArrayList filters) {
			super(TIMEOUT);
			this.node = (Node) MediatorModelProxy.getInstance().makeNode(node);
			this.callback = callback;
			this.filters = filters;
			this.measure = m.clone();
			this.variableId = variableId;
			this.evt = evt;
		}

		protected Object construct() throws InterruptedException {
			for (int i = 0; i < filters.size(); i++) {
				if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node)) {
					callback.onThreshold(node, variableId, measure, evt);
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
					logger.error("TimeOut callback application runtime 'measure' ", ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ", e);
			}
		}
	}

}
