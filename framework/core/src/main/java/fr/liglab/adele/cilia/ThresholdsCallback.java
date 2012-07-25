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
 * Callback variable out of viability area veryLow < Low < value < High <
 * VeryHigh
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ThresholdsCallback {
	static int NONE = 0 ;
	static int VERY_LOW = 1;
	static int LOW = 2;
	static int HIGH = 3;
	static int VERY_HIGH = 4;

	/**
	 * @param evt
	 *            event threshold number {VERY_LOW,LOW,HIGH,VERY_HIGH}
	 * @param variable
	 *            variable name
	 * @param measure
	 *            measure received
	 */
	void onThreshold(Node node, String variable, Measure measure, int thresholdType);

}
