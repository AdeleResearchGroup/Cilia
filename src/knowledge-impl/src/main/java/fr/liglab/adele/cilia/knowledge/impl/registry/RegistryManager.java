/* Copyright Adele Team LIG
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

package fr.liglab.adele.cilia.knowledge.impl.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.Constants;
import fr.liglab.adele.cilia.knowledge.UniformResourceName;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.registry.RegistryItem;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;
import fr.liglab.adele.cilia.util.concurrent.Mutex;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.SyncMap;

/**
 * Implements a 'Registry' Constains all Cilia objects discovered at runtime.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class RegistryManager implements RuntimeRegistry {

	private final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	/* m_registry association uuid <-> RegistryItem */
	private SyncMap registry;
	private Map locked_uuid;

	public RegistryManager(BundleContext bc) {
		registry = new SyncMap(new HashMap(),
				new ReentrantWriterPreferenceReadWriteLock());
		locked_uuid = new ConcurrentHashMap();
	}

	public void start() {
		logger.info("ModelS@RunTime 'Registry' - started");
	}

	public void stop() {
		logger.info("ModelS@RunTime 'Registry' - stopped");
		registry.clear();
		locked_uuid.clear();
	}

	/*
	 * Insert a new object in the registry (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#register
	 * (fr.liglab.adele.cilia.knowledge.core.registry.RegistryItem)
	 */
	public void register(RegistryItem obj) {
		if (obj == null)
			return;
		registry.put(obj.getProperties().get(Constants.UUID), obj);
		logger.debug("[{}] registered", obj.toString());
	}

	/*
	 * Remove an entry and wait if this entry is locked (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#unregister
	 * (java.lang.String)
	 */
	public synchronized void unregister(String uuid) {
		if ((uuid == null))
			return;
		try {
			Mutex mutex = (Mutex) locked_uuid.get(uuid);
			if (mutex != null) {
				/* avoid infinite wait */
				if (mutex.attempt(10000) == false) {
					unlock_uuid(uuid);
					logger.error("uuid is locked since 10 seconds, automatic unlock done," +
							     "to avoid infinite lock");
				}
			}
			RegistryItemImpl item = (RegistryItemImpl) registry.remove(uuid);
			if (item != null) {
				item.clear();
				logger.debug("Object [{}] unRegistered", item.toString());
			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * Return the number of object stored in the registry (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#size()
	 */
	public int size() {
		return registry.size();
	}

/*
	public String dumpRegistry() {
		StringBuffer sb = new StringBuffer();
		try {
			registry.readerSync().acquire();
			try {
				Iterator it = registry.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					RegistryItem item = (RegistryItem) pairs.getValue();
					sb.append(item.toString()).append("\n");
				}
				return sb.toString();
			} finally {
				registry.readerSync().release();
			}
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}
*/
	/*
	 * This uuid is locked temporaly (max 10 10seconds) from removal
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#lock_uuid
	 * (java.lang.String)
	 */
	public void lock_uuid(String uuid) {
		if (uuid == null)
			return;

		if (!locked_uuid.containsKey(uuid)) {
			try {
				Mutex mutex = new Mutex();
				mutex.acquire();
				locked_uuid.put(uuid, mutex);
			} catch (Exception e) {
				locked_uuid.remove(uuid);
				Thread.currentThread().interrupt();
				throw new UnsupportedOperationException(e.getMessage());
			}
		}
	}

	/*
	 * Unlock uuid from removal (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#unlock_uuid
	 * (java.lang.String)
	 */
	public void unlock_uuid(String uuid) {
		if (uuid == null)
			return;
		Mutex mutex = (Mutex) locked_uuid.remove(uuid);
		if (mutex != null)
			mutex.release();
	}

	/*
	 * Retreive all registry entries matching the filter (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#findByFilter
	 * (java.lang.String)
	 */
	public RegistryItem[] findByFilter(String ldap) throws InvalidSyntaxException {
		Set itemfound = new HashSet();
		Filter filter = Knowledge.createFilter(ldap);
		try {
			registry.readerSync().acquire();
			try {
				Iterator it = registry.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					RegistryItem item = (RegistryItem) pairs.getValue();
					if (filter.match(item.getProperties())) {
						itemfound.add(item);
					}
				}
				return (RegistryItem[]) itemfound.toArray(new RegistryItem[itemfound
						.size()]);
			} finally {
				registry.readerSync().release();
			}
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * Retreive the registry matching uuid (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#findByUuid
	 * (java.lang.String)
	 */
	public RegistryItem findByUuid(String uuid) {
		RegistryItem item;
		if (uuid == null)
			item = null;
		else
			item = (RegistryItem) registry.get(uuid);
		return item;
	}

}
