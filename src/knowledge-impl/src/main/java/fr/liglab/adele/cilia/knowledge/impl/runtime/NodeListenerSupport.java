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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.NodeCallback;
import fr.liglab.adele.cilia.knowledge.NodeRegistration;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.runtime.ThresholdsCallback;
import fr.liglab.adele.cilia.knowledge.runtime.ThresholdsRegistration;
import fr.liglab.adele.cilia.knowledge.util.SwingWorker;
import fr.liglab.adele.cilia.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class NodeListenerSupport implements NodeRegistration, ThresholdsRegistration {
	protected final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private CopyOnWriteArrayList listenerNode = new CopyOnWriteArrayList();
	private CopyOnWriteArrayList listenerThreshold = new CopyOnWriteArrayList();

	public NodeListenerSupport() {
	}

	public void removeAllListenerNode() {
		listenerNode.clear();
	}

	public void removeAllListenerThreshold() {
		listenerThreshold.clear();
	}

	/**
	 * 
	 * @return number of listener
	 */
	protected int sizeListenerNode() {
		return listenerNode.size();
	}

	protected int sizeListenerThreshold() {
		return listenerThreshold.size();
	}

	/**
	 * Notifies all listeners
	 * 
	 * @param source
	 */
	public void fireNodeEvent(boolean departure, Node source) {
		if (!listenerNode.isEmpty())
			new NodeFirer(departure, source).start();
	}

	/**
	 * Notifies all listeners
	 * 
	 * @param source
	 */
	public void fireThresholdEvent(int evt, String urn) {
		if (!listenerThreshold.isEmpty())
			new ThresholdFirer(evt, urn).start();
	}

	public void addListener(ThresholdsCallback listener) {
		if (listener != null)
			this.listenerThreshold.addIfAbsent(listener);
	}

	public void removeListener(ThresholdsCallback listener) {
		if (listener != null)
			this.listenerThreshold.remove(listener);
	}

	public void addListener(NodeCallback listener) {
		if (listener != null)
			this.listenerNode.addIfAbsent(listener);
	}

	public void removeListener(NodeCallback listener) {
		if (listener != null)
			this.listenerNode.remove(listener);
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class NodeFirer extends SwingWorker {

		private boolean departure;
		private Node source;

		public NodeFirer(boolean departure, Node source) {
			this.departure = departure;
			this.source = source;
		}

		protected Object construct() throws Exception {
			Iterator it = listenerNode.listIterator();
			NodeCallback subscriber;
			while (it.hasNext()) {
				try {
					subscriber = (NodeCallback) it.next();
					if (departure)
						subscriber.departure(source);
					else
						subscriber.arrival(source);
				} catch (Exception e) {
					logger.error("error while dispatching 'fireNodeEvent' node=" + source);
				}
			}
			return null;
		}
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class ThresholdFirer extends SwingWorker {
		private int evt;
		private String urn;

		public ThresholdFirer(int evt, String urn) {
			this.evt = evt;
			this.urn = urn;
		}

		protected Object construct() throws Exception {
			Iterator it = listenerNode.listIterator();
			while (it.hasNext()) {
				try {
					((ThresholdsCallback) it.next()).onThreshold(evt, urn);
				} catch (Exception e) {
					logger.error("error while dispatching fireThresholdEvent");
				}
			}
			return null;
		}
	}
}
