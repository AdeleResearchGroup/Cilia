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

package fr.liglab.adele.cilia.knowledge.eventbus;

import java.util.Dictionary;

/**
 *  Veto an event notification 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface OnVeto {

	/**
	 * Event veto
	 * 
	 * @param evt
	 *            event number
	 * @param source
	 *            urn ( uuid or urn:uuid:state-var
	 * @param param
	 *            event parameters
	 * @return true, the event will not be notified
	 */
	boolean shouldVeto(int evt, String source, Dictionary param);
}
