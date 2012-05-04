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

package fr.liglab.adele.cilia.knowledge.impl.runtime;

import fr.liglab.adele.cilia.dynamic.Measure;

/**
 * Class holding values generated by runtime
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class MeasureImpl implements Measure {
	private final Object value;
	private final long tickCounts;

	public MeasureImpl(Object value, long ticks) {
		this.value = value;
		this.tickCounts = ticks;
	}

	public Object value() {
		return value;
	}

	public long tickCounts() {
		return tickCounts;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("value=").append(value);
		sb.append(",ticks count=").append(tickCounts);
		return sb.toString();
	}

}
