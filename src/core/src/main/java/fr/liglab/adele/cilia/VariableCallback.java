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
 * Callback on data received from the monitoring
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 * 
 */
public interface VariableCallback {


	void onStateChange(Node node, String variable , boolean enable) ;
	/**
	 * 
	 * @param node 
	 * @param variable , variable id updated
	 * @param Measure measure received :
	 */
	void onUpdate(Node node, String variable,Measure m);
}
