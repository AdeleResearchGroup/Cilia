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
package fr.liglab.adele.cilia.runtime;

import java.util.Dictionary;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings({"rawtypes"})
public class MediatorHandler extends PrimitiveHandler {

	private IDispatcherHandler dispatcher;

	private ISchedulerHandler scheduler;

	public void configure(Element metadata, Dictionary configuration)
			throws ConfigurationException {
	}

	public void stop() {
	}

	public void start() {
	}

	protected IDispatcherHandler getDispatcher() {
		return dispatcher;
	}

	protected ISchedulerHandler getScheduler() {
		return scheduler;
	}

	public void setDispatcher(IDispatcherHandler dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void setScheduler(ISchedulerHandler scheduler) {
		this.scheduler = scheduler;
	}

}
