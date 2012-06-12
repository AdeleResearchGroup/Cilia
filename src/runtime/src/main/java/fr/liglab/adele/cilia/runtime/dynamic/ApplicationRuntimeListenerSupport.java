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

package fr.liglab.adele.cilia.runtime.dynamic;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.MeasureCallback;
import fr.liglab.adele.cilia.MeasuresRegistration;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.NodeRegistration;
import fr.liglab.adele.cilia.ThresholdsCallback;
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
public class ApplicationRuntimeListenerSupport implements TrackerCustomizer,
		NodeRegistration, MeasuresRegistration {
	protected final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);

	private static int TIMEOUT = 1000; /*
										 * Max time allowed per subscribers : 1
										 * second
										 */
	public static final int EVT_ARRIVAL = 1;
	public static final int EVT_DEPARTURE = 2;
	public static final int EVT_MODIFIED = 3;

	private static final String NODE_LISTENER = "cilia.runtime.node";
	private static final String NODE_DATA = "cilia.runtime.node.data";
	private static final String NODE_DATA_THRESHOLD = "cilia.runtime.node.threshold";
	private static final String FILTER = "(|(cilia.runtime.node=*)(cilia.runtime.node.data=*)(cilia.runtime.node.threshold=*))";

	private Map nodeListeners;
	private Map thresholdListeners;
	private Map measureListeners;

	private Tracker tracker;
	private BundleContext bundleContext;

	public ApplicationRuntimeListenerSupport(BundleContext bc) {
		bundleContext = bc;
		nodeListeners = new ConcurrentReaderHashMap();
		thresholdListeners = new ConcurrentReaderHashMap();
		measureListeners = new ConcurrentReaderHashMap();
	}

	/* insert a new listener ,associated to a ldap filter */
	private void addFilterListener(Map map, String filter, Object listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		ArrayList old, array;
		if (listener == null)
			throw new CiliaIllegalParameterException("listener is null");
		/* mostly length = 1 */
		array = new ArrayList(1);
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

	/*
	 * Node registration , add a listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.NodeRegistration#addListener(java.lang
	 * .String, fr.liglab.adele.cilia.knowledge.NodeCallback)
	 */
	public void addListener(String ldapfilter, NodeCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(nodeListeners, ldapfilter, listener);
	}

	/*
	 * Node registration , remove a listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.NodeRegistration#removeListener(fr.liglab
	 * .adele.cilia.knowledge.NodeCallback)
	 */
	public void removeListener(NodeCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(nodeListeners, listener);
	}

	/**
	 * Notifies all node listeners
	 * 
	 * @param source
	 */
	public void fireNodeEvent(int event, Node source) {

		if (nodeListeners.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = nodeListeners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new NodeFirerTimed(event, source, (NodeCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}
	}

	// private void runEvent(Runnable event) {
	// ServiceReference refs[] = null;
	// try {
	// refs = bundleContext.getServiceReferences(WorkQueue.class.getName(),
	// "(cilia.pool.scope=application)");
	// } catch (InvalidSyntaxException e) {
	// logger.error("Unable to get WorkQueue Service");
	// return;
	// }
	// if (refs != null && refs.length > 0) {
	// WorkQueue worker = (WorkQueue) bundleContext.getService(refs[0]);
	// worker.execute(event);
	// bundleContext.ungetService(refs[0]);
	// }
	// }

	private class NodeFirerTimed extends SwingWorker {
		private int event;
		private Node node;
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
		
		private void fire() {
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
			}
		}

		protected Object construct() throws InterruptedException {
			for (int i = 0; i < filters.size(); i++) {
				if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node)) {
					/* Fire only once the same subscriber */
					fire();
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
					logger.error("TimeOut callback application runtime 'node' ",ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ",e);
			}
		}
	}

	/* ---- Measures Listener Support --- */

	/*
	 * Measure listener, add a listener
	 */
	public void addListener(String ldapfilter, MeasureCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(measureListeners, ldapfilter, listener);
	}

	/*
	 * Measure listener, Remove a listener (non-Javadoc)
	 */
	public void removeListener(MeasureCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(measureListeners, listener);
	}

	/**
	 * Notify all listeners matching the filter
	 * 
	 * @param node
	 * @param variableId
	 */
	public void fireMeasureReceived(Node node, String variableId, Measure m) {

		if (measureListeners.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = measureListeners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			new MeasureFirerTimed(node, variableId, m, (MeasureCallback) pairs.getKey(),
					(ArrayList) pairs.getValue()).start();
		}

	}

	private class MeasureFirerTimed extends SwingWorker {
		private Node node;
		private MeasureCallback callback;
		private ArrayList filters;
		private Measure measure;
		private String variableId;

		public MeasureFirerTimed(Node node, String variableId, Measure m,
				MeasureCallback callback, ArrayList filters) {
			super(TIMEOUT);
			this.node = node;
			this.callback = callback;
			this.filters = filters;
			this.measure = m;
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
					logger.error("TimeOut callback application runtime 'measure' ",ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ",e);
			}
		}
	}

	/*
	 * Threshold listener, add a listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.runtime.MeasuresRegistration#addListener
	 * (java.lang.String,
	 * fr.liglab.adele.cilia.knowledge.runtime.ThresholdsCallback)
	 */
	public void addListener(String ldapfilter, ThresholdsCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		addFilterListener(thresholdListeners, ldapfilter, listener);
	}

	/*
	 * Remove a threshold listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.runtime.MeasuresRegistration#removeListener
	 * (fr.liglab.adele.cilia.knowledge.runtime.ThresholdsCallback)
	 */
	public void removeListener(ThresholdsCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(thresholdListeners, listener);
	}

	public void fireThresholdEvent(Node node, String variableId, Measure measure, int evt) {

		if (thresholdListeners.isEmpty())
			return;
		/* Iterator assumes a valid copy of listeners */
		Iterator it = thresholdListeners.entrySet().iterator();
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

		public ThresholdFirerTimed(Node node, String variableId, Measure m, int evt,
				ThresholdsCallback callback, ArrayList filters) {
			super(TIMEOUT);
			this.node = node;
			this.callback = callback;
			this.filters = filters;
			this.measure = m;
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
					logger.error("TimeOut callback application runtime 'measure' ",ex);
				}
			} catch (InterruptedException e) {
				logger.error("Interruped thread ",e);
			}
		}
	}

	public void start() {
		registerTracker();
	}

	public void stop() {
		unRegisterTracker();
		nodeListeners.clear();
		measureListeners.clear();
		thresholdListeners.clear();
	}

	private void registerTracker() {
		if (tracker == null) {
			try {
				tracker = new Tracker(bundleContext, bundleContext.createFilter(FILTER),
						this);
				tracker.open();
			} catch (InvalidSyntaxException e) {
				/* never happens */
			}
		}
	}

	private void unRegisterTracker() {
		if (tracker != null) {
			tracker.close();
		}
	}

	/* insert a new listener tracked */
	private void insertService(ServiceReference reference) {
		String property;
		Object service = bundleContext.getService(reference);

		property = (String) reference.getProperty(NODE_LISTENER);
		if (property != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof NodeCallback) {
					addFilterListener(nodeListeners, property, service);
				}
			} catch (Throwable e) {
				logger.error("Cannot add a listener ",e);
			}
		}
		property = (String) reference.getProperty(NODE_DATA);
		if (property != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof MeasureCallback) {
					addFilterListener(measureListeners, property, service);
				}
			} catch (Throwable e) {
				logger.error("Cannot add a listener ",e);
			}
		}
		property = (String) reference.getProperty(NODE_DATA_THRESHOLD);
		if (property != null) {
			/* checks if the interface is implemented */
			try {
				if (service instanceof ThresholdsCallback) {
					addFilterListener(thresholdListeners, property, service);
				}
			} catch (Throwable e) {
				logger.error("Cannot add a listener ",e);
			}
		}
	}

	/* remove a listener tracked */
	private void extractService(Object service) {
		try {
			removeFilterListener(measureListeners, service);
			removeFilterListener(nodeListeners, service);
			removeFilterListener(thresholdListeners, service);
		} catch (CiliaIllegalParameterException e) {
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
