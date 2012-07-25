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

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import fr.liglab.adele.cilia.Data;


public interface IScheduler {
    /**
     * 
     * @param data notified data.
     */
	public void notifyData(Data data);
	
	public void process(List/*<Data>*/ dataSet);
	
	public List getSourcesIds();
	
	//

	public void fireEvent(Map map);
	
	public Map getData();
	
	/* init , application level */
	public void init() ;
	

}
