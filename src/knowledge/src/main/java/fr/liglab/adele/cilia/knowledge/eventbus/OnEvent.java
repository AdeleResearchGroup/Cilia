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

import fr.liglab.adele.cilia.knowledge.exception.IllegalParameterException;

/**
 * Bus event , call bacjk subsciption (cache overrun )
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface OnEvent {
	/**
	 * notify event
	 * 
	 * @param eventNumber
	 *            , event number
	 * @param source resource identfication
	 *            , = uuid or urn:uuid:state.var.name 
	 * @param timeStamp
	 *            , event timestamp (#ticks)
	 * @param param
	 *            , additional parameters (setted during publishing)
	 */
	void onEvent(int eventNumber, String source, long timeStamp, Dictionary param) ;
}
