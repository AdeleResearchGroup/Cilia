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
package fr.liglab.adele.cilia.runtime.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.IFieldMonitor;
import fr.liglab.adele.cilia.framework.monitor.IMonitor;
import fr.liglab.adele.cilia.framework.monitor.IProcessorMonitor;
import fr.liglab.adele.cilia.framework.monitor.IServiceMonitor;
import fr.liglab.adele.cilia.framework.monitor.ProcessorNotifier;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings("rawtypes")
public class MonitorHandler extends PrimitiveHandler implements IProcessorMonitor,
		IServiceMonitor,IFieldMonitor {

	CopyOnWriteArrayList listeners = new CopyOnWriteArrayList();
	private String field = null;


	public void configure(Element metadata, Dictionary configuration)
			throws ConfigurationException {
		Element[] elem = metadata.getElements("method", Const.CILIA_NAMESPACE);
		if (elem != null) {
			/* retreive the field to inject */
			field = elem[0].getAttribute("notifier");
			PojoMetadata pojoMeta = getPojoMetadata();
			FieldMetadata fm = pojoMeta.getField(field);
			if (fm != null) {
				/* Then check that the field is a ProcessorNotifier field */
				if (!fm.getFieldType().equals(ProcessorNotifier.class.getName())) {
					field = null;
				}
			}
			else field=null ;
		}
	}

	public void stop() {
		listeners.clear();
	}

	public void start() {
	}


	public void addListener(IMonitor listener) {
		listeners.addIfAbsent(listener);

	}

	public void removeListener(IMonitor listener) {
			listeners.remove(listener);
	}

	public void notifyOnProcessEntry(List<Data> data) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onProcessEntry(data);
	}

	public void notifyOnProcessExit(List<Data> data) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onProcessExit(data);
	}

	public void notifyOnDispatch(List<Data> data) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onDispatch(data);
	}

	public void notifyOnProcessError(List<Data> data, Exception ex) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onProcessError(data, ex);
	}


	public void fireEvent(Map info) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).fireEvent(info);
	}

	public void notifyOnCollect(Data data) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onCollect(data);
	}

	public void onCreation(Object instance) {
		/* injeted the monitor handler reference */
		if (field != null) {
			try {
				/* field has already been tested */
				Field fieldToInject = instance.getClass().getField(field);
				try { 
					boolean isAccessible;
					if (!Modifier.isPublic(fieldToInject.getModifiers())) { 
						fieldToInject.setAccessible(true);
						isAccessible=false ;
					}
					else isAccessible=true ;
					fieldToInject.set(instance, new ProcessorNotifier(this));
					fieldToInject.setAccessible(isAccessible);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			}
		}
	}

	public void onServiceArrival(Map info) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onServiceArrival(info);
	}

	public void onServiceDeparture(Map info) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onServiceDeparture(info);
	}

	public void onFieldGet(String field, Object o) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onFieldGet(field, o);
	}

	public void onFieldSet(String field, Object o) {
		Iterator it= listeners.listIterator() ;
		while (it.hasNext())
			((IMonitor )it.next()).onFieldSet(field, o);		
	}
}
