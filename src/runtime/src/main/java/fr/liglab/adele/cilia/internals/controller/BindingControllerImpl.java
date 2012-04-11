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

package fr.liglab.adele.cilia.internals.controller;

import java.util.Dictionary;

import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaException;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Collector;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.Sender;
import fr.liglab.adele.cilia.runtime.CiliaBindingService;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class BindingControllerImpl implements TrackerCustomizer {
	/**
	 * Binding model to handle.
	 */
	private Binding modelBinding;

	private MediatorControllerImpl sourceController;

	private MediatorControllerImpl targetController;
	
	private final Object lockObject = new Object();

	public void setSourceController(MediatorControllerImpl sourceController) {
		this.sourceController = sourceController;
	}

	public void setTargetController(MediatorControllerImpl targetController) {
		this.targetController = targetController;
	}

	/**
	 * Reference to the chain controller.
	 */
	private BundleContext bcontext;

	private static Logger log = LoggerFactory.getLogger("cilia.ipojo.runtime");
	private Tracker bindingTracker;
	/**
	 * Create a new binding controller.
	 * 
	 * @param binding
	 *            to handle.
	 */
	private static String DEFAULT_TYPE = "direct";

	public BindingControllerImpl(BundleContext context, Binding binding) {
		modelBinding = binding;
		bcontext = context;
		settingUp();
		// start();
	}

	/**
	 * Starting observing model.
	 */
	public void start() {
		// stop();
		registerTracker();
	}

	/**
	 * @throws CiliaException
	 * 
	 */
	private void createModels(ServiceReference ref) throws CiliaException {

		CiliaBindingService cbs = null;
		Collector collector = null;
		Sender sender = null;
		String bindingId = null;
		cbs = (CiliaBindingService) bcontext.getService(ref);

		if (log.isDebugEnabled())
			log.debug("Adding Binding:" + getBindingType());
		Collector collectorModel = cbs.getCollectorModel(modelBinding.getProperties());
		Sender senderModel = cbs.getSenderModel(modelBinding.getProperties());
		Dictionary colProps = null;
		Dictionary senProps = null;
		if (collectorModel != null) {
			colProps = collectorModel.getProperties();
		}
		if (senderModel != null) {
			senProps = senderModel.getProperties();
		}
		Dictionary componentProperties = cbs.getProperties(colProps, senProps,
				modelBinding);

		colProps = (Dictionary) componentProperties.get("cilia.collector.properties");
		senProps = (Dictionary) componentProperties.get("cilia.sender.properties");

		Port port1 = modelBinding.getSourcePort();
		Port port2 = modelBinding.getTargetPort();
		if (port1 != null) {
			if (log.isDebugEnabled())
				log.debug("Source Port in model binding is NOT null" + port1.getType()
						+ " " + port1.getName());
		} else {
			senderModel = null;
		}
		if (port2 != null) {
			if (log.isDebugEnabled())
				log.debug("Target Port in model binding is NOT null " + port2.getType()
						+ " " + port2.getName());
		} else {
			collectorModel = null;
		}

		bindingId = modelBinding.getSourceMediator().getId() + ":"
				+ modelBinding.getSourcePort().getName() + ":"
				+ modelBinding.getTargetMediator().getId() + ":"
				+ modelBinding.getTargetPort().getName() ;
				

		if (collectorModel != null) {
			colProps.put("cilia.collector.identifier", bindingId);
			colProps.put("cilia.collector.port", modelBinding.getTargetPort().getName());
			collector = new Collector(modelBinding.getTargetPort().getName(),
					collectorModel.getType(), colProps);
		}
		if (senderModel != null) {
			senProps.put("cilia.sender.identifier", bindingId);
			senProps.put("cilia.sender.port", modelBinding.getSourcePort().getName());
			sender = new Sender(modelBinding.getSourcePort().getName(),
					senderModel.getType(), senProps);
		}
		addModels(sender, collector);
	}

	/**
	 * Add sender and collector models to the mediators.
	 * 
	 * @param senderm
	 *            the sender model.
	 * @param collectorm
	 *            the collector model.
	 */
	private void addModels(Sender senderm, Collector collectorm) {
		MediatorComponent sm = modelBinding.getSourceMediator();
		MediatorComponent tm = modelBinding.getTargetMediator();

		if (sm != null && senderm != null) {
			synchronized (lockObject) {
				modelBinding.addSender(senderm);
				sourceController.createSender(senderm);
			}
		}

		if (tm != null && collectorm != null) {
			synchronized (lockObject) {
				modelBinding.addCollector(collectorm);
				targetController.createCollector(collectorm);
			}
		}
	}

	/**
	 * Get the binding type.
	 * 
	 * @return the binding type.
	 */
	private String getBindingType() {
		String bindingType = modelBinding.getType();
		if (bindingType == null) {
			bindingType = DEFAULT_TYPE;
		}
		return bindingType;
	}

	/**
	 * Stop observing model.
	 */
	public void stop() {
		unregisterTracker();
		if (sourceController != null) {
			Sender s = modelBinding.getSender(); 
			modelBinding.addSender(null);
			sourceController.removeSender(s);
		}
		if (targetController != null) {
			Collector c = modelBinding.getCollector();
			modelBinding.addCollector(null);
			targetController.removeCollector(c);
		}
	}

	/**
	 * Update binding properties.
	 * 
	 * @param binding
	 *            binding to modify.
	 */
	public void updateInstanceProperties(Binding binding) {
		Dictionary properties = binding.getProperties();
		if (sourceController != null) {
			sourceController.updateInstanceProperties(properties);
		}
		if (targetController != null) {
			targetController.updateInstanceProperties(properties);
		}
	}

	protected void registerTracker() {
		String filter = createFilter();
		if (bindingTracker == null) {
			try {
				bindingTracker = new Tracker(bcontext, bcontext.createFilter(filter),
						this);
				bindingTracker.open();
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	protected void unregisterTracker() {
		if (bindingTracker != null) {
			bindingTracker.close();
		}
	}

	private String createFilter() {
		String filter = "(|(cilia.binding.protocol=" + getBindingType()
				+ ")(cilia.binding.type=" + getBindingType() + "))";
		return filter;
	}

	public void settingUp() {
		String defaultProtocol = null;
		if (bcontext != null) {
			defaultProtocol = bcontext.getProperty("cilia.default.protocol");
		}
		if (defaultProtocol != null) {
			DEFAULT_TYPE = defaultProtocol;
		}
	}

	/**
	 * Methods from TrackerCustomizer.
	 */
	public void addedService(ServiceReference reference) {
		try {
			createModels(reference);
		} catch (CiliaException e) {
			e.printStackTrace();
		}
	}

	public boolean addingService(ServiceReference reference) {
		Object cbs = null;
		cbs = bcontext.getService(reference);
		if (cbs instanceof CiliaBindingService) {
			return true;
		}
		return false;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
		if (sourceController != null) {
			sourceController.removeSender(modelBinding.getSender());
		}
		if (targetController != null) {
			targetController.removeCollector(modelBinding.getCollector());
		}
	}

}
