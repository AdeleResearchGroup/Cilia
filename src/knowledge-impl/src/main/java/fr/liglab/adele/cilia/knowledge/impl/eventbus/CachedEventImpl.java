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

import fr.liglab.adele.cilia.knowledge.eventbus.CachedEvent;

/**
 * Event stored in the cache
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class CachedEventImpl implements CachedEvent {
	private final int  event ;
	private final long timeStamp ;
	private final String uuid ;
	private final String chainId;
	private final String nodeId ;
	

	
	public CachedEventImpl(int event,long timestamp,String uuid,String chainId, String nodeId) {
		this.event=event ;
		this.timeStamp=timestamp;
		this.uuid = uuid;
		this.chainId=chainId;
		this.nodeId = nodeId;
	}
	
	public int eventNumber() {
		return event ;
	}

	public long tickNumber() {
		return timeStamp ;
	}
	
	public String chainId() {
		return chainId;
	}

	public String nodeId() {
		return nodeId ;
	}

	public String uuid() {
		return uuid ;
	}

	
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("event #").append(event);
		sb.append("tick number #").append(timeStamp);
		sb.append("Node [ uuid").append(uuid);
		sb.append(", qualified id ").append(chainId).append("/").append(nodeId);
		sb.append("]");
		return sb.toString();
	}


}
