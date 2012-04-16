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

/**
 * Event bus , constants definition
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public interface EventProperties {
	
	/**
	 * topic for Application model update 
	 */
	static final String TOPIC_APPLICATION ="cilia/runtime/management/application" ;
	
	/**
	 * topic for  mediator/adapter instance component arrival/departure 
	 */
	static final String TOPIC_DYN_PROPERTIES = "cilia/runtime/management/component";	

	/**
	 * topic for data update 
	 */
	static final String TOPIC_DATA_UPDATE = "cilia/runtime/data/update";
	
	/* Events related to topic 'TOPIC_DYN_PROPERTIES */
	static final int REGISTER = 1;
	static final int UNREGISTER = 2 ;
	/* Events related to topic 'TOPIC_DATA_UPDATE' */
	static final int DATA_UPDATE = 3 ;
	static final int DATA_VERY_LOW = 4 ;
	static final int DATA_LOW = 5 ;
	static final int DATA_HIGH = 6 ;
	static final int DATA_VERY_HIGH = 7 ;
	/* events cache, event fired when the cache start loosing events  */
	static final int CACHE_EVENT_LOST = 8 ;
	
	/* Events related to the Model update */
	static final int MODEL_CHAIN_CREATE = 9 ;
	static final int MODEL_CHAIN_DELETE =10 ;
	static final int MODEL_CHAIN_START = 11 ;
	static final int MODEL_CHAIN_STOP = 12 ;
	static final int MODEL_MEDIATOR_CREATE = 13 ;
	static final int MODEL_MEDIATOR_DELETE = 14 ;
	static final int MODEL_ADAPTER_CREATE = 15 ;
	static final int MODEL_ADAPTER_DELETE = 16 ;
	
	/* content of an event in the Event bus */
	static final String EVENT_SOURCE_ID = "event.source.id" ;  /* String format */
	static final String EVENT_TICK_NUMBER = "tick.number"; /* Long */
	static final String EVENT_NUMBER ="event.number" ; /* Integer */
	static final String EVENT_CACHED ="event.cached"; /* Boolean */

}