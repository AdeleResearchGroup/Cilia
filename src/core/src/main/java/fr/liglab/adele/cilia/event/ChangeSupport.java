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

package fr.liglab.adele.cilia.event;

import java.util.Iterator;

import fr.liglab.adele.cilia.util.concurrent.CopyOnWriteArrayList;

public class ChangeSupport implements ChangeStateListener {

	private CopyOnWriteArrayList m_listener = new CopyOnWriteArrayList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.event.ChangeSupport#addChangeListener(fr.liglab
	 * .adele.cilia.event.ChangeListener)
	 */
	public void addChangeListener(ChangeStateEvent listener) {
		if (listener == null)
			throw new NullPointerException();
				m_listener.addIfAbsent(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.event.ChangeSupport#removeChangeListener(fr.liglab
	 * .adele.cilia.event.ChangeListener)
	 */
	public void removeChangeListener(ChangeStateEvent listener) {
		if (listener == null)
			throw new NullPointerException();
		m_listener.remove(listener);
	}

	public void removeAllChangeListener() {
		m_listener.clear() ;
	
	}
	
	/**
	 * 
	 * @return number of listener 
	 */
	public int size() {
		return m_listener.size();
	}
	
	/**
	 * Notifies all listeners
	 * 
	 * @param source
	 */
	public void fireChangeEvent(Object source) {
		ChangeEvent event = new ChangeEvent(source);
		Iterator it = m_listener.iterator();
		while (it.hasNext()) {
			try {
				((ChangeStateEvent) it.next()).stateChanged(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
