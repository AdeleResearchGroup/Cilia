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

package fr.liglab.adele.cilia.util;

import java.util.Dictionary;
import java.util.Enumeration;

/**
 * Unmodifiable dictionnary
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class UnModifiableDictionary extends Dictionary {
	private final Dictionary dico;

	public UnModifiableDictionary(Dictionary dictionary) {
		this.dico = dictionary;
	}

	public Enumeration elements() {
		return dico.elements();
	}

	public Object get(Object key) {
		return dico.get(key);
	}

	public boolean isEmpty() {
		return dico.isEmpty();
	}

	public Enumeration keys() {
		return dico.keys();
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return dico.size();
	}
	
	public String toString() {
		return dico.toString();
	}
		
}
