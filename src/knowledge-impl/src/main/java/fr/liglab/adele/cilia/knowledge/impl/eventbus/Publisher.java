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

package fr.liglab.adele.cilia.knowledge.impl.eventbus;

import java.util.Map;

import fr.liglab.adele.cilia.knowledge.Node;

/**
 * Publisher to the event bus
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface Publisher {

	/**
	 * 
	 * @param topic
	 *            topic to publish to
	 * @param param
	 *            Parameters to publish
	 */
	public void publish(String topic, Map param);

	/**
	 * Event will not be stored in the cache
	 * 
	 * @param topic
	 *            topic to publish to
	 * @param evt
	 *            event number
	 * @param urn
	 *            = uuid or urn:uuid:state-var
	 * @param timestamp
	 *            , number of ticks
	 */
	//public void publish(String topic, int evt, String urn, long timestamp);

	/**
	 * 
	 * @param topic
	 *            topic to publish to
	 * @param evt
	 *            event number
	 * @param urn
	 *            = uuid or urn:uuid:state-var
	 * @param timestamp
	 *            , number of ticks
	 * @param isEventCached
	 *            true event will be stored in the cache
	 */
	//public void publish(String topic, int evt, String urn, long timestamp,
	//		boolean isEventCached);
	
	public void publish(String topic, Node node,int evt,long timestamp) ;
	public void publish(String topic, Node node,int evt,long timestamp,boolean isEventCached) ;
}
