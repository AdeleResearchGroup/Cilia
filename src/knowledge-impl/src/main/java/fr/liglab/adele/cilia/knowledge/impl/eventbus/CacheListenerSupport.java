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

package fr.liglab.adele.cilia.knowledge.impl.eventbus;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.eventbus.CacheRegistration;
import fr.liglab.adele.cilia.knowledge.eventbus.OnCacheEvent;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.util.SwingWorker;
import fr.liglab.adele.cilia.util.concurrent.CopyOnWriteArrayList;

/**
 * Calback registration 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@SuppressWarnings({"rawtypes"})
public class CacheListenerSupport implements CacheRegistration {

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);
	private CopyOnWriteArrayList listener = new CopyOnWriteArrayList();

	public void removeAllListener() {
		listener.clear();
	}

	public void addListener(OnCacheEvent listener) {
		if (listener != null)
			this.listener.addIfAbsent(listener);
	}

	public void removeListener(OnCacheEvent listener) {
		if (listener != null)
			this.listener.remove(listener);
	}

	public void fireOverRun(long timeStamp) {
		if (!listener.isEmpty())
			new FirerOverrun(timeStamp).start();
	}

	/* Run in a thread MIN_PRIORITY+1 */
	private class FirerOverrun extends SwingWorker {
		private long timestamp;

		public FirerOverrun(long timestamp) {
			this.timestamp = timestamp;
		}

		protected Object construct() throws Exception {
			Iterator it = listener.listIterator();
			while (it.hasNext()) {
				try {
					((OnCacheEvent) it.next()).overrun(timestamp);
				} catch (Exception e) {
					logger.error("error while dispatching 'fireEvent'");
				}
			}
			return null;
		}

	}
}
