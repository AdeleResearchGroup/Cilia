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

import java.util.Collection;

/**
 * Event bus , cache access 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface Cache extends CacheRegistration {

	/**
	 * enable / disable the cache service
	 * @param e
	 */
	void enable(boolean e) ;
	/** 
	 * 
	 * @return true , the cache is enable 
	 */
	boolean enable() ;
	/**
	 * modify the cache size ;
	 * 
	 * @param cacheSize
	 *            #item stored
	 */
	void size(int newSize);

	/**
	 * 
	 * @return the capacity size (max events sotred)
	 */
	int size();

	/**
	 * 
	 * @return current number of events stored in the cache
	 */
	int cachedEventCount();

	/**
	 * clear all events in the cache
	 */
	void clearCache();

	/**
	 * retunrn a collection  of events cached
	 * 
	 * @return all event cached
	 */
	CachedEvent[] cachedEvent();

}
