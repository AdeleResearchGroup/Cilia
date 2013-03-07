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


package fr.liglab.adele.cilia.framework;

import fr.liglab.adele.cilia.Data;


/**
 * Cilia Collector Interface
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public interface ICollector {
	

	/**
	 * Set the scheduler to call when collect data.
	 * @param scheduler Cilia Scheduler.
	 */
	public void setScheduler(IScheduler scheduler);
	/**
	 * Notify to the Scheduler when data has arrive. 
	 * @param data Data collected.
	 */
	public Data notifyDataArrival(Data data) ;
	
	/**
	 * Add source name to the collector instance.
	 * @param sn
	 */
	public void setSourceName(String sn) ;
	
}
