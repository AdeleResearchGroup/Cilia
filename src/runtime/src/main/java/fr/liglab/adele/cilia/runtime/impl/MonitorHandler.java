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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.IMonitor;
import fr.liglab.adele.cilia.framework.monitor.INotifier;
import fr.liglab.adele.cilia.framework.monitor.IProcessorMonitor;
import fr.liglab.adele.cilia.framework.monitor.IServiceMonitor;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */

public class MonitorHandler extends PrimitiveHandler implements IProcessorMonitor,
IServiceMonitor {

	List<IMonitor> listeners = new ArrayList<IMonitor>();

	@SuppressWarnings("rawtypes")
	public void configure(Element metadata, Dictionary configuration)
	throws ConfigurationException {
	}

	public void stop() {
		removeListeners();
	}

	public void start() {
	}

	private void removeListeners() {
		if(isEmpty()){
			return;
		}
		synchronized (listeners) {
			listeners.clear();
		}
	}

	public void addListener(IMonitor listener) {
		synchronized (listener) {
			listeners.add(listener);
		}
	}

	public void removeListener(IMonitor listener) {
		if(isEmpty()){
			return;
		}
		synchronized (listener) {
			listeners.remove(listener);
		}
	}

	public void notifyOnProcessEntry(List<Data> data) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onProcessEntry(data);
		}
	}

	public void notifyOnProcessExit(List<Data> data) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onProcessExit(data);
		}
	}

	public void notifyOnDispatch(List<Data> data) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onDispatch(data);
		}
	}


	public void notifyOnProcessError(List<Data> data, Exception ex) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onProcessError(data, ex);
		}
	}

	@SuppressWarnings("rawtypes")
	public void fireEvent(Map info) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.fireEvent(info);
		}
	}

	public void notifyOnCollect(Data data) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onCollect(data);
		}
	}

	public void onCreation(Object instance) {
		if (instance instanceof INotifier) {
			INotifier pojo = (INotifier) instance;
			pojo.setMonitor(this);
		}
	}

	@SuppressWarnings("rawtypes")
	public void onServiceArrival(Map info) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onServiceArrival(info);
		}
	}

	@SuppressWarnings("rawtypes")
	public void onServiceDeparture(Map info) {
		if(isEmpty()){
			return;
		}
		List<IMonitor> copyListeners = null;
		synchronized (listeners) {
			copyListeners = new ArrayList<IMonitor>(listeners);
		}
		for (int i = 0; i < copyListeners.size(); i++) {
			IMonitor listener = copyListeners.get(i);
			listener.onServiceDeparture(info);
		}
	}
	private boolean isEmpty(){ 
		//if any listeners, return immediately.
		synchronized (listeners) {
			if(listeners.isEmpty()) {
				return true;
			}
		}
		return false;
	}


}
