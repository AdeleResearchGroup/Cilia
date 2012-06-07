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

package fr.liglab.adele.cilia.runtime.dynamic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.util.concurrent.ConcurrentReaderHashMap;
import fr.liglab.adele.cilia.util.concurrent.Mutex;

/**
 * Implements a 'Registry' Constain all Cilia objects discovered at runtime.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RuntimeRegistryImpl implements RuntimeRegistry {

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOG_NAME);

	/* m_registry association uuid <-> RegistryItem */
	private Map registry;
	private Map locked_uuid;

	public RuntimeRegistryImpl(BundleContext bc) {
		registry = new ConcurrentReaderHashMap();
		locked_uuid = new ConcurrentReaderHashMap();
	}

	public void start() {
	}

	public void stop() {
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
		if ((obj == null) || (obj.uuid() == null)) {
			logger.error("object or uuid is null , cannot be registered ");
			return;
		}
		registry.put(obj.uuid(), obj);
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
		if ((uuid == null)) {
			logger.error("uuid is null, cannot unregister object");
			return;
		}
		try {
			Mutex mutex = (Mutex) locked_uuid.get(uuid);
			if (mutex != null) {
				/* avoid infinite wait */
				if (mutex.attempt(10000) == false) {
					unlock_uuid(uuid);
					logger.error("uuid is locked since 10 seconds, automatic unlock is done,"
							+ "to avoid infinite lock");
				}
			}
			RegistryItemImpl item = (RegistryItemImpl) registry.remove(uuid);
			if (item != null) {
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
	 * This uuid is locked temporaly (max 10 seconds) from removal (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.registry.RuntimeRegistry#lock_uuid
	 * (java.lang.String)
	 */
	public synchronized void lock_uuid(String uuid) {
		if (uuid == null) {
			logger.error("uuid is null , cannot lock uuid");
			return;
		}
		if (!locked_uuid.containsKey(uuid)) {
			try {
				Mutex mutex = new Mutex();
				mutex.acquire();
				locked_uuid.put(uuid, mutex);
			} catch (InterruptedException e) {
				locked_uuid.remove(uuid);
				Thread.currentThread().interrupt();
				throw new RuntimeException(e.getMessage());
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
		if (uuid == null) {
			logger.error("uuid is null , cannot perform un_lock");
			return;
		}
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
	public RegistryItem[] findByFilter(String ldap) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {
		Set itemfound = new HashSet();
		Filter filter = ConstRuntime.createFilter(ldap);

		Iterator it = registry.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			RegistryItem item = (RegistryItem) pairs.getValue();
			if (filter.match(item.getProperties())) {
				itemfound.add(item);
			}
		}
		return (RegistryItem[]) itemfound.toArray(new RegistryItem[itemfound.size()]);
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
