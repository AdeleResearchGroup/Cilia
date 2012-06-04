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

package fr.liglab.adele.cilia;

/**
 * callback events level chain
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ChainCallback {

	/**
	 * Callback upon new chain arrival
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onAdded(String chainId);

	/**
	 * Callback upon chain departure
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onRemoved(String chainId);

	/**
	 * Chain started
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onStarted(String chainId);

	/**
	 * Chain stopped
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onStopped(String chainId);

}
