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

package fr.liglab.adele.cilia.knowledge.impl.specification;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.Node;
import fr.liglab.adele.cilia.knowledge.NodeCallback;
import fr.liglab.adele.cilia.knowledge.NodeRegistration;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.specification.ChainCallback;
import fr.liglab.adele.cilia.knowledge.specification.ChainRegistration;
import fr.liglab.adele.cilia.knowledge.util.SwingWorker;
import fr.liglab.adele.cilia.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class SpecificationListenerSupport implements ChainRegistration, NodeRegistration {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);
	private CopyOnWriteArrayList listenerNode = new CopyOnWriteArrayList();
	private CopyOnWriteArrayList listenerChain = new CopyOnWriteArrayList();

	public void removeAllListener() {
		listenerNode.clear();
	}

	public void addListener(NodeCallback listener) {
		if (listener != null)
			this.listenerNode.addIfAbsent(listener);
	}

	public void removeListener(NodeCallback listener) {
		if (listener != null)
			this.listenerNode.remove(listener);
	}

	public void fireEventNode(boolean arrival, Node component) {
		if (!listenerNode.isEmpty())
			new Firer(arrival, component).start();
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class Firer extends SwingWorker {
		private boolean arrival;
		private Node component;

		public Firer(boolean arrival, Node component) {
			this.arrival = arrival;
			this.component = component;
		}

		protected Object construct() throws Exception {
			Iterator it = listenerNode.listIterator();
			while (it.hasNext()) {
				try {
					NodeCallback subscriber = (NodeCallback) it.next();
					if (arrival)
						subscriber.arrival(component);
					else
						subscriber.departure(component);
				} catch (Exception e) {
					logger.error("error while dispatching 'fireEvent'");
				}
			}
			return null;
		}

	}

	/* ----------- */

	public void addListener(ChainCallback listener) {
		if (listener != null)
			this.listenerChain.addIfAbsent(listener);
	}

	public void removeListener(ChainCallback listener) {
		if (listener != null)
			this.listenerChain.remove(listener);
	}

	public void fireEventChain(int evt, String name) {
		if (!listenerNode.isEmpty())
			new FirerChainEvent(evt, name);
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class FirerChainEvent extends SwingWorker {
		private int evt;
		private String chainId;

		public FirerChainEvent(int evt, String name) {
			this.evt = evt;
			this.chainId = name;
		}

		protected Object construct() throws Exception {
			Iterator it = listenerNode.listIterator();
			while (it.hasNext()) {
				try {
					ChainCallback subscriber = (ChainCallback) it.next();
					switch (evt) {
					case 0:
						subscriber.arrival(chainId);
						break;
					case 1:
						subscriber.departure(chainId);
						break;
					case 2:
						subscriber.started(chainId);
						break;
					case 3:
						subscriber.stopped(chainId);
						break;
					}
				} catch (Exception e) {
					logger.error("error while dispatching 'fireEvent'");
				}
			}
			return null;
		}

	}

}
