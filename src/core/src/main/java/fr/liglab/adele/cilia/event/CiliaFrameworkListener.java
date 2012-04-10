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

public interface CiliaFrameworkListener {
	/**
	 * Register to a specific mediator
	 * 
	 * @param chain
	 * @param mediator
	 * @param listener
	 * @return true if success
	 */
	boolean register(String chainId, String mediatorId, CiliaFrameworkEvent listener,
			int listEvents);

	/**
	 * Register to a specific chain ( and relevant mediator)
	 * 
	 * @param chain
	 * @param listener
	 * @return true if success
	 */
	boolean register(String chainId, CiliaFrameworkEvent listener, int intListEvent);

	/**
	 * Register to all chain
	 * 
	 * @param listener
	 * @return if success
	 */
	boolean register(CiliaFrameworkEvent listener, int listEvents);

	/**
	 * Unregister previously listener
	 * 
	 * @param listener
	 * @return true if success
	 */
	boolean unregister(CiliaFrameworkEvent listener);

	/**
	 * Set the List of events fired
	 * 
	 * @param listener
	 * @param evts
	 * @return true if all events are set correctly
	 */
	boolean setListEvent(CiliaFrameworkEvent listener, int evts);

	/**
	 * Gives the current list of events configured to be fired
	 * 
	 * @param listener
	 * @return -1 if error or list of events 
	 */
	int getListEvent(CiliaFrameworkEvent listener);

	/**
	 * Convert event number into string format
	 * 
	 * @param ListEvts
	 * @return
	 */
	String evtNumberToString(int ListEvts);
}
