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
package fr.liglab.adele.cilia.runtime.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.framework.AbstractScheduler;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.framework.IScheduler;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.ISchedulerHandler;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class SchedulerInstanceManager extends ConstituentInstanceManager {

	protected ISchedulerHandler handler;
	
	/**
	 * @param context
	 */
	public SchedulerInstanceManager(BundleContext context, ISchedulerHandler handler) {
		super(context);
		this.handler = handler;

	}

	public IScheduler getScheduler(){
		return (IScheduler)constituant.getObject();
	}
	
	
	
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.runtime.impl.ConstituantInstanceManager#createFilter()
	 */
	@Override
	protected String createFilter() {
		StringBuffer filter = new StringBuffer("(&(factory.state=1)");
		filter.append("(scheduler.name=" + constituantInfo.getType() + ")");
		if (constituantInfo.getNamespace() != null) {
			filter.append("(scheduler.namespace=" + constituantInfo.getNamespace()
					+ ")");
		}
		filter.append(")");
		return filter.toString();
	}
	
	private void updateSchedulerReference() {
		if (constituant == null) {
			logger.debug("Scheduler is not valid, waiting to be valid");
			return;
		}
		IScheduler ref = getScheduler();
		if (ref == null) {
			logger.debug("Scheduler is not valid, waiting to be valid");
			return;
		}
		logger.debug("Scheduler is now valid, updating references");
		AbstractScheduler im = (AbstractScheduler) ref; // all scheduleres must extends AbstractScheduler
		im.setConnectedScheduler(handler);
	}

	private void addSchedulerToCollectors() {
		synchronized (lockObject) { // Lock all the iteration. :S unable to add
			// a collector when performing this
			// opperation.
			Set keys = getKeys();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				List collectorList = (List) getPojo((String) obj);
				Iterator itCollectors = collectorList.iterator();
				while (itCollectors.hasNext()) {
					CiliaInstance cicol = (CiliaInstance) itCollectors.next();
					ICollector collector = (ICollector) cicol.getObject();
					if (collector == null) {
						logger.warn("Some Sender is null or invalid when some sender state has changed");
					} else {
						collector.setScheduler(handler);
					}
				}
			}
		}
	}
	

}
