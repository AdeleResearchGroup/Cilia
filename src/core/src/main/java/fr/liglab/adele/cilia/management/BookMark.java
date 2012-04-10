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

package fr.liglab.adele.cilia.management;

public interface BookMark {
	
	/**
	 * @return Creation time in milliseconds 
	 */
	long getTimeMs();

	/**
	 * 
	 * @return chain Id
	 * 
	 */
	String getChainId();

	/**
	 * 
	 * @return Component Id
	 */
	String getComponentId();

	/**
	 * 
	 * @return return full id (chainId/mediatorId) or (ChainId)
	 */
	String getQualifiedId();

	/**
	 * 
	 * @return Event number ( CiliaEvent )
	 */
	int getEventNumber();

	/**
	 * @return Event string format
	 */
	String getEventString();
	
	/**
	 * @return Internal sequence number
	 */
	int getSequenceNumber();

	/**
	 * 
	 * @return String representation of a bookmark
	 */
	String toString();
}