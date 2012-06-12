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

package fr.liglab.adele.cilia.framework.monitor;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.runtime.Const;

public class AuditHandler extends PrimitiveHandler {
	private final Logger logger = LoggerFactory.getLogger("cilia.runtime.audit-handler");

	private Set fieldsGet = new HashSet();
	private Set fieldsSet = new HashSet();
	private Map fieldsPrefix = new HashMap();

	private IFieldMonitor monitor = null;
	private Object _lock = new Object();

	public void configure(Element metadata, Dictionary configuration)
			throws ConfigurationException {
		Element[] elem = metadata.getElements("audit", Const.CILIA_NAMESPACE);
		Set fields = new HashSet();
		if (elem != null) {
			String field, rights, prefix;
			for (int i = 0; i < elem.length; i++) {
				field = elem[i].getAttribute("field");
				rights = elem[i].getAttribute("access");
				prefix = elem[i].getAttribute("namespace");
				if (field != null) {
					fields.add(field);
					if (prefix != null)
						fieldsPrefix.put(field, prefix + ":");
					if (rights == null) {
						fieldsGet.add(field);
						fieldsSet.add(field);
					} else {
						if (rights.toLowerCase().contains("r")) {
							fieldsGet.add(field);
						}
						if (rights.toLowerCase().contains("w")) {
							fieldsSet.add(field);
						}
					}
				}
			}
		}
		PojoMetadata pojoMeta = getPojoMetadata();
		Iterator it = fields.iterator();
		while (it.hasNext()) {
			String field = (String) it.next();
			FieldMetadata fm = pojoMeta.getField(field);
			getInstanceManager().register(fm, this);
		}
	}

	public void start() {
	}

	public void stop() {
	}

	private String getQualifiedId(String field) {
		String name;
		if (fieldsPrefix.containsKey(field)) {
			name = fieldsPrefix.get(field) + field;
		} else
			name = field;
		return name;
	}

	/**
	 * This method is called at each time the pojo 'get' a listened field. The
	 * method return the stored value.
	 * 
	 * @param pojo
	 *            : pojo object getting the field
	 * @param field
	 *            : field name.
	 * @param o
	 *            : previous value.
	 * @return the stored value.
	 * @see org.apache.felix.ipojo.PrimitiveHandler#getterCallback(java.lang.String,
	 *      java.lang.Object)
	 */
	public Object onGet(Object pojo, String field, Object o) {
		String qualifiedField = getQualifiedId(field);
		if (fieldsGet.contains(field)) {
			IFieldMonitor mon = getMonitor();
			if (mon != null) {
				mon.onFieldGet(qualifiedField, o);
			}
		}
		logger.info("onGet {}", qualifiedField);
		return o;
	}

	/**
	 * This method is called at each time the pojo 'set' a listened field. This
	 * method updates the local properties.
	 * 
	 * @param pojo
	 *            : pojo object setting the field
	 * @param field
	 *            : field name
	 * @param newvalue
	 *            : new value
	 * @see org.apache.felix.ipojo.PrimitiveHandler#setterCallback(java.lang.String,
	 *      java.lang.Object)
	 */
	public void onSet(Object pojo, String field, Object newvalue) {
		String qualifiedField = getQualifiedId(field);
		if (fieldsSet.contains(field)) {
			IFieldMonitor mon = getMonitor();
			if (mon != null)
				mon.onFieldSet(qualifiedField, newvalue);
		}
		logger.info("onSet {}", qualifiedField);
	}

	/* called by schedulerHandler and dispatcherHandler */
	public void reconfigure(Dictionary configuration) {
		synchronized (_lock) {
			if ((configuration != null) && (monitor == null)) {
				monitor = (IFieldMonitor) configuration.get("cilia.monitor.handler");
			}
		}
	}

	private IFieldMonitor getMonitor() {
		synchronized (_lock) {
			if (monitor == null) {
				monitor = (IFieldMonitor) getInstanceManager().getHandler(
						Const.ciliaQualifiedName("monitor-handler"));
			}
			return monitor;
		}
	}
}
