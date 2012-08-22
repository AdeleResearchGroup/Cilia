/* Copyright Adele Team LIG
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

package fr.liglab.adele.cilia.knowledge;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.runtime.WorkQueue;

/**
 * This class is in charge listen data published and store data in the model <br>
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BaseLevelListener implements EventHandler {

	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOGGER_KNOWLEDGE);

	private final BundleContext bundleContext;

	private ServiceRegistration serviceEventAdmin;
	private final ListNodes registry;

	public BaseLevelListener(BundleContext bc, final ListNodes registry) {
		this.bundleContext = bc;
		this.registry = registry;
	}

	/* Start receiving state variable */
	public void start() {
		registerEventAdmin();
	}

	/* Stop receiving state variable value */
	public void stop() {
		unregisterEventAdmin();
	}

	/* Register event admin */
	private void registerEventAdmin() {
		Dictionary dico = new Hashtable();
		dico.put(EventConstants.EVENT_TOPIC, ConstRuntime.TOPIC_HEADER + "*");

		if (serviceEventAdmin != null) {
			serviceEventAdmin.unregister();
		}
		serviceEventAdmin = bundleContext.registerService(EventHandler.class.getName(),
				this, dico);
	}

	private void unregisterEventAdmin() {
		if (serviceEventAdmin != null) {
			serviceEventAdmin.unregister();
		}
	}

	/* handle data published by the mediator handler */
	public void handleEvent(Event event) {

		Dictionary dico = new Hashtable();

		String[] keys = event.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				if (!keys[i].equalsIgnoreCase("event.topics")) {
					dico.put(keys[i], event.getProperty(keys[i]));
				}
			}
			/* Asynchronous execution */
			Integer type = (Integer) dico.get(ConstRuntime.EVENT_TYPE);
			asynchronousExecution(new HandleDataReceived(dico, type.intValue()));
		}
	}

	/* Execute asynchronlously */
	private void asynchronousExecution(Runnable event) {
		ServiceReference refs[] = null;
		try {
			refs = bundleContext.getServiceReferences(WorkQueue.class.getName(),
					"(cilia.pool.scope=application)");
		} catch (InvalidSyntaxException e) {
			logger.error("Unable to get WorkQueue Service");
			return;
		}
		if (refs != null && refs.length > 0) {
			WorkQueue worker = (WorkQueue) bundleContext.getService(refs[0]);
			worker.execute(event);
			bundleContext.ungetService(refs[0]);
		}
	}

	private class HandleDataReceived implements Runnable {
		private Dictionary dico;
		private int type;

		public HandleDataReceived(Dictionary dico, int type) {
			this.dico = dico;
			this.type = type;
		}

		public void run() {
			switch (type) {
			case ConstRuntime.TYPE_DATA:
				eventData(dico);
				break;
			case ConstRuntime.TYPE_STATUS_VARIABLE:
				eventStatusVariable(dico);
				break;
			}
		}

		private void eventData(Dictionary dico) {
			String uuid;
			String stateVariable;
			Object value;
			long ticksCount;
			/* state variable name */
			stateVariable = (String) dico.get(ConstRuntime.VARIABLE_ID);
			/* uuid : mediator / adapter source */
			uuid = (String) dico.get(ConstRuntime.UUID);
			/* value published */
			value = dico.get(ConstRuntime.VALUE);
			/* timestamp in ticks */
			ticksCount = ((Long) dico.get(ConstRuntime.TIMESTAMP)).longValue();

			/* Retrieve the node and insert a new measure */
			MediatorMonitoring item;
			try {
				item = registry.getAndStore(uuid);
				if (item != null) {
					MeasureImpl m = new MeasureImpl(value, ticksCount);
					item.addMeasure(stateVariable, m);

				}
			} catch (CiliaIllegalStateException e) {
				logger.error(e.getMessage());
			}
		}

		private void eventStatusVariable(Dictionary dico) {
			String uuid;
			Object value;
			/* state variable name */
			String stateVariable = (String) dico.get(ConstRuntime.VARIABLE_ID);
			/* uuid : mediator / adapter source */
			uuid = (String) dico.get(ConstRuntime.UUID);
			/* value published */
			value = dico.get(ConstRuntime.VALUE);
			/* Retrieve the node and insert a new measure */
			MediatorMonitoring item;
			try {
				item = registry.getAndStore(uuid);
				if (item != null) {
					/* store the new state */
					item.setVariableStatus(stateVariable,
							(((Boolean) value).booleanValue()));
				}
			} catch (CiliaIllegalStateException e) {
				logger.error(e.getMessage());
			}
		}
	}
}
