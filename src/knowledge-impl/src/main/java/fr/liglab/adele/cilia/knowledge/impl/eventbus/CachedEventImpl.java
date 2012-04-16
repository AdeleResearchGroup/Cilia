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
	private final String source ;
	
	public CachedEventImpl(int event,long timestamp,String source) {
		this.event=event ;
		this.timeStamp=timestamp;
		this.source=source;
	}
	
	public int eventNumber() {
		return event ;
	}

	public long tickNumber() {
		return timeStamp ;
	}
	
	public String source() {
		return source;
	}
	
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("event #").append(event);
		sb.append("tick number #").append(timeStamp);
		sb.append("Source [").append(source).append("]");
		return sb.toString();
	}

}
