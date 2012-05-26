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

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.NodeRegistration;
import fr.liglab.adele.cilia.dynamic.Measure;
import fr.liglab.adele.cilia.dynamic.MeasureCallback;
import fr.liglab.adele.cilia.dynamic.MeasuresRegistration;
import fr.liglab.adele.cilia.dynamic.ThresholdsCallback;
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
public class NodeListenerSupport implements TrackerCustomizer, NodeRegistration,
		MeasuresRegistration {
	protected final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);
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
	private WorkQueue workQueue;

	private Tracker tracker;
	private BundleContext bundleContext;

	public NodeListenerSupport(BundleContext bc) {
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
		if (!nodeListeners.isEmpty())
			workQueue.execute(new NodeFirer(event, source));
	}

	private class NodeFirer implements Runnable {

		private int event;
		private Node node;

		public NodeFirer(int event, Node node) {
			this.event = event;
			this.node = node;
		}

		public void run() {
			/* No need to synchronize ! */
			/* Iterator over all listener */
			Iterator it = nodeListeners.entrySet().iterator();
			while (it.hasNext()) {
				/* iterator over all filter per listener */
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean toFire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node)) {
						toFire = true;
						break;
					}
				}
				/* call only once the same subscriber */
				if (toFire) {
					switch (event) {
					case EVT_ARRIVAL:
						((NodeCallback) pairs.getKey()).onArrival(node);
						break;
					case EVT_DEPARTURE:
						((NodeCallback) pairs.getKey()).onDeparture(node);
						break;
					case EVT_MODIFIED:
						((NodeCallback) pairs.getKey()).onModified(node);
					}
				}
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
		if (!measureListeners.isEmpty())
			workQueue.execute(new MeasureFirer(node, variableId, m));
	}

	public class MeasureFirer implements Runnable {
		private Node node;
		private String variableId;
		private Measure measure;

		public MeasureFirer(Node node, String variableId, Measure m) {
			this.node = node;
			this.variableId = variableId;
			this.measure = m;
		}

		public void run() {
			Iterator it = measureListeners.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean tofire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node,variableId)) {
						tofire = true;
						break;
					}
				}
				if (tofire)
					((MeasureCallback) pairs.getKey())
							.onUpdate(node, variableId, measure);
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
		if (!thresholdListeners.isEmpty())
			workQueue.execute(new ThresholdFirer(node, variableId, measure, evt));
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class ThresholdFirer implements Runnable {
		private int evt;
		private String variable;
		private Node node;
		private Measure measure;

		public ThresholdFirer(Node node, String variable, Measure measure, int evt) {
			this.evt = evt;
			this.variable = variable;
			this.node = node;
			this.measure = measure;
		}

		public void run() {

			Iterator it = thresholdListeners.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean tofire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (ConstRuntime.isFilterMatching((Filter) filters.get(i), node,variable)) {
						tofire = true;
						break;
					}
				}
				if (tofire)
					((ThresholdsCallback) pairs.getKey()).onThreshold(node, variable,
							measure, evt);
			}
		}
	}

	protected void start(WorkQueue wq) {
		registerTracker();
		workQueue = wq;
	}

	protected void stop() {
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
				logger.error("Cannot add a listener ");
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
				logger.error("Cannot add a listener ");
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
				logger.error("Cannot add a listener ");
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
