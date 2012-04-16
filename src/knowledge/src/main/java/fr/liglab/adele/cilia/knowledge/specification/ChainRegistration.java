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

package fr.liglab.adele.cilia.knowledge.specification;

/**
 * Registration to events level chain
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ChainRegistration {
	/**
	 * Adds a listener
	 * 
	 * @param listener
	 *            , listener to add
	 * 
	 */
	void addListener(ChainCallback listener);

	/**
	 * Removes a listener
	 * 
	 * @param listener
	 *            , listener to remove
	 */
	void removeListener(ChainCallback listener);
}
