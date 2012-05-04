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

package fr.liglab.adele.cilia.knowledge.impl.runtime;

import java.util.ArrayList;
import java.util.HashMap;
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
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.knowledge.NodeCallback;
import fr.liglab.adele.cilia.knowledge.NodeRegistration;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.runtime.MeasureCallback;
import fr.liglab.adele.cilia.knowledge.runtime.MeasuresRegistration;
import fr.liglab.adele.cilia.knowledge.runtime.ThresholdsCallback;
import fr.liglab.adele.cilia.knowledge.util.SwingWorker;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.SyncMap;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class NodeListenerSupport implements TrackerCustomizer, NodeRegistration,
		MeasuresRegistration {
	protected final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private static final String NODE_LISTENER = "cilia.runtime.node";
	private static final String NODE_DATA = "cilia.node.data";
	private static final String NODE_DATA_THRESHOLD = "cilia.node.data.threshold";
	private static final String FILTER = "(|(cilia.runtime.node=*)(cilia.node.data=*)(cilia.node.data.threshold=*))";

	private SyncMap nodeListeners;
	private SyncMap thresholdListeners;
	private SyncMap measureListeners;

	private Tracker tracker;
	private BundleContext bundleContext;

	public NodeListenerSupport(BundleContext bc) {
		bundleContext = bc;
		nodeListeners = new SyncMap(new HashMap(),
				new ReentrantWriterPreferenceReadWriteLock());
		thresholdListeners = new SyncMap(new HashMap(),
				new ReentrantWriterPreferenceReadWriteLock());
		measureListeners = new SyncMap(new HashMap(),
				new ReentrantWriterPreferenceReadWriteLock());
	}

	/* insert a new listener ,associated to a ldap filter */
	protected void addFilterListener(Map map, String filter, Object listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		if (listener == null)
			throw new CiliaIllegalParameterException("listener is null");
		if (!map.containsKey(listener)) {
			map.put(listener, new ArrayList());
		}
		((ArrayList) map.get(listener)).add(Knowledge.createFilter(filter));
	}

	/* Remove a listener */
	protected void removeFilterListener(Map map, Object listener)
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
	public void fireNodeEvent(boolean arrival, Node source) {
		if (!nodeListeners.isEmpty())
			new NodeFirer(arrival, source).start();
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class NodeFirer extends SwingWorker {

		private boolean arrival;
		private Node node;

		public NodeFirer(boolean arrival, Node node) {
			this.arrival = arrival;
			this.node = node;
		}

		protected Object construct() throws Exception {
			Iterator it = nodeListeners.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean toFire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (Knowledge.isNodeMatching((Filter) filters.get(i), node)) {
						toFire = true;
						break;
					}
				}
				/* call only one time the same subsriber */
				if (toFire) {
					if (arrival)
						((NodeCallback) pairs.getKey()).arrival(node);
					else
						((NodeCallback) pairs.getKey()).departure(node);
				}
			}

			return null;
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
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.runtime.MeasuresRegistration#removeListener
	 * (fr.liglab.adele.cilia.knowledge.runtime.MeasureCallback)
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
	public void fireMeasureReceived(Node node, String variableId) {
		if (!measureListeners.isEmpty())
			new MeasureFirer(node, variableId).start();
	}

	public class MeasureFirer extends SwingWorker {
		private Node node;
		private String variableId;

		public MeasureFirer(Node node, String variableId) {
			this.node = node;
			this.variableId = variableId;
		}

		protected Object construct() throws Exception {
			Iterator it = measureListeners.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean tofire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (Knowledge.isNodeMatching((Filter) filters.get(i), node)) {
						tofire = true;
						break;
					}
				}
				if (tofire)
					((MeasureCallback) pairs.getKey()).onUpdate(node, variableId);
			}
			return null;
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
	 * Remove a thrshold listener (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.runtime.MeasuresRegistration#removeListener
	 * (fr.liglab.adele.cilia.knowledge.runtime.ThresholdsCallback)
	 */
	public void removeListener(ThresholdsCallback listener)
			throws CiliaIllegalParameterException {
		removeFilterListener(thresholdListeners, listener);
	}

	public void fireThresholdEvent(Node node, String variableId, int evt) {
		if (!thresholdListeners.isEmpty())
			new ThresholdFirer(node, variableId, evt).start();
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class ThresholdFirer extends SwingWorker {
		private int evt;
		private String variable;
		private Node node;

		public ThresholdFirer(Node node, String variable, int evt) {
			this.evt = evt;
			this.variable = variable;
			this.node = node;
		}

		protected Object construct() throws Exception {
			Iterator it = thresholdListeners.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				ArrayList filters = (ArrayList) pairs.getValue();
				boolean tofire = false;
				for (int i = 0; i < filters.size(); i++) {
					if (Knowledge.isNodeMatching((Filter) filters.get(i), node)) {
						tofire = true;
						break;
					}
				}
				if (tofire)
					((ThresholdsCallback) pairs.getKey())
							.onThreshold(node, variable, evt);
			}
			return null;
		}
	}

	protected void start() {
		registerTracker();
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
			} catch (Exception e) {
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
			} catch (Exception e) {
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
			} catch (Exception e) {
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
