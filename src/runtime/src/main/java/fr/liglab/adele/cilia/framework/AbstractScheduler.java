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

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public abstract class AbstractScheduler implements IScheduler {

	IScheduler scheduler;



	protected static Logger logger = LoggerFactory.getLogger("cilia.ipojo.runtime");

	public void setConnectedScheduler(IScheduler sched) {
		scheduler = sched;
	}

	public void notifyData(Data data) {
		if (logger.isTraceEnabled()) {
			logger.trace("notify data " + data);
		}
		process(Collections.singletonList(data));
	}

	public void process(List dataSet) {
		if (scheduler == null) {
			logger.error("Unable to process data, Scheduler reference is not valid.");
			return;
		}
		scheduler.process(dataSet);
	}

	public List getSourcesIds() {
		return scheduler.getSourcesIds();
	}

	public void addCollector(String collectorType, String collectorId,
			Dictionary dictionary) {
		if (logger.isDebugEnabled()) {
			logger.debug("Add collector '" + collectorType + "'");
		}
		scheduler.addCollector(collectorType, collectorId, dictionary);
	}

	public void fireEvent(Map map) {
		logger.info("fireEvent " + map);
		if (scheduler != null)
			scheduler.fireEvent(map);
	}

	public Map getData() {
		return scheduler.getData();
	}

	public void init() {
	}

}
