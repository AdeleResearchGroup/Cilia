package fr.liglab.adele.cilia.internals.factories;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.IPojoContext;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.cilia.model.Component;

public class MediatorFactory extends MediatorComponentFactory implements
		TrackerCustomizer {


	private Factory processorFactory;

	private Tracker constituantTracker = null;


	
	protected Hashtable inPorts;
	
	protected Hashtable outPorts;

	private final static String COMPONENT_TYPE = "mediator";

	private final static String CONSTITUANT_CATEGORY = ".category";

	private final static String CONSTITUANT_NAME = ".name";

	private final static String CONSTITUANT_NAMESPACE = ".namespace";

	private static final String CILIA_SCHEDULER_NAME = "cilia.scheduler.name";

	private static final String CILIA_SCHEDULER_NAMESPACE = "cilia.scheduler.namespace";

	private static final String CILIA_DISPATCHER_NAME = "cilia.dispatcher.name";

	private static final String CILIA_DISPATCHER_NAMESPACE = "cilia.dispatcher.namespace";

	/**
	 * This Mediator Component Factory manages mediator component instances.
	 * 
	 * @param context
	 *            OSGi Bundle Context.
	 * @param element
	 *            element to configure the mediator definition.
	 * @throws ConfigurationException
	 */
	public MediatorFactory(BundleContext context, Element element)
			throws ConfigurationException {

		super(context, element);

	}

	/**
	 * Check if the mediator configuration is valid.
	 */
	public void check(Element metadata) throws ConfigurationException {
		String name = metadata.getAttribute("name");
		if (name == null) {
			String msg = "a mediator component (adapter or mediator) needs a name : " + metadata;
			logger.error(msg);
			throw new ConfigurationException(msg);
		}
		computeConstituantsDescriptions();
		computePorts();
	}

	/**
	 * Mediator perse does not have a class.
	 */
	public String getClassName() {
		return COMPONENT_TYPE;
	}

	/**
	 * When creating a mediator instance, the factory will create an processor
	 * instance.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ComponentInstance createInstance(Dictionary config,
			IPojoContext context, HandlerManager[] handlers)
			throws org.apache.felix.ipojo.ConfigurationException {
		String filter = createConstituantFilter(processorDescription);
		String msg;

		try {
			ServiceReference[] serv = m_context.getAllServiceReferences(
					Factory.class.getName(), filter);
			if (serv != null && serv.length != 0) {
				processorFactory = (Factory) m_context.getService(serv[0]);
			}
		} catch (InvalidSyntaxException e1) {
			msg = "unable to retrieve Processor factory";
			logger.error(msg);
			throw new ConfigurationException(msg); // should
			// never
			// happend.
		}

		if (processorFactory == null) {
			msg = "unable to obtain processor factory " + filter;
			logger.error(msg);
			throw new ConfigurationException(msg);
		}
		// obtain the scheduler configuration. from the mediator description
		if (schedulerDescription.getId() != null
				&& config.get(CILIA_SCHEDULER_NAME) == null) {
			config.put(CILIA_SCHEDULER_NAME, schedulerDescription.getId());
		}
		if (schedulerDescription.getNamespace() != null
				&& config.get(CILIA_SCHEDULER_NAMESPACE) == null) {
			config.put(CILIA_SCHEDULER_NAMESPACE,
					schedulerDescription.getNamespace());
		}
		// obtain the dispatcher configuration. from the mediator description
		if (dispatcherDescription.getId() != null
				&& config.get(CILIA_DISPATCHER_NAME) == null) {
			config.put(CILIA_DISPATCHER_NAME, dispatcherDescription.getId());
		}
		if (dispatcherDescription.getNamespace() != null
				&& config.get(CILIA_DISPATCHER_NAMESPACE) == null) {
			config.put(CILIA_DISPATCHER_NAMESPACE,
					dispatcherDescription.getNamespace());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("creating mediator instance with " + config);
		}
		return createMediatorInstance(config, context, handlers);

		// return processorFactory.createComponentInstance(config);

	}

	@SuppressWarnings("rawtypes")
	protected MediatorManager createMediatorInstance(Dictionary config,
			IPojoContext context, HandlerManager[] handlers)
			throws ConfigurationException {
		MediatorManager instance = new MediatorManager(this,
				(ProcessorFactory) processorFactory, context, handlers);
		try {
			instance.createProcessor(config);
			instance.configure(m_componentMetadata, config);
			instance.start();
			return instance;
		} catch (ConfigurationException e) {
			// An exception occurs while executing the configure or start
			// methods.
			if (instance != null) {
				instance.dispose();
				instance = null;
			}
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Throwable e) { // All others exception are handled here.
			if (instance != null) {
				instance.dispose();
				instance = null;
			}
			logger.error(e.getMessage(), e);
			throw new ConfigurationException(e.getMessage());
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getRequiredHandlerList() {
		List handlerList;
		List returnedList = new ArrayList();
		handlerList = super.getRequiredHandlerList();
		Iterator it = handlerList.iterator();
		// Delete required handlers (processor, scheduler, dispatcher)
		while (it.hasNext()) {
			RequiredHandler req = (RequiredHandler) it.next();
			if ((!req.equals(new RequiredHandler("scheduler", null))) // "org.apache.felix.ipojo:scheduler"
					&& !(req.equals(new RequiredHandler("processor", null))) // "org.apache.felix.ipojo:processor"
					&& !(req.equals(new RequiredHandler("dispatcher", null))) // "org.apache.felix.ipojo:dispatcher"
					&& !(req.equals(new RequiredHandler("dispatcher",DEFAULT_NAMESPACE))) // "fr.liglab.adele.cilia:dispatcher"
					&& !(req.equals(new RequiredHandler("processor",DEFAULT_NAMESPACE))) // "fr.liglab.adele.cilia:processor"
					&& !(req.equals(new RequiredHandler("scheduler",DEFAULT_NAMESPACE))) // "fr.liglab.adele.cilia:scheduler"
					&& !(req.equals(new RequiredHandler("ports",null))) // "org.apache.felix.ipojo:ports"
					&& !(req.equals(new RequiredHandler("ports",DEFAULT_NAMESPACE))) // "fr.liglab.adele.cilia:ports"
			) {
				if (!returnedList.contains(req)) {
					returnedList.add(req);
				}
			}
		}
		// Add requires handler.
		try {
			computeConstituantsDescriptions();
		} catch (Exception e) {
		} // will never throw an exception.
		RequiredHandler req = new RequiredServiceHandler("processor-tracker",
				DEFAULT_NAMESPACE, processorDescription);
		if (!returnedList.contains(req)) {
			returnedList.add(req);
		}
		RequiredHandler reqd = new RequiredServiceHandler("dispatcher-tracker",
				DEFAULT_NAMESPACE, dispatcherDescription);
		if (!returnedList.contains(reqd)) {
			returnedList.add(reqd);
		}
		RequiredHandler reqs = new RequiredServiceHandler("scheduler-tracker",
				DEFAULT_NAMESPACE, schedulerDescription);
		if (!returnedList.contains(reqs)) {
			returnedList.add(reqs);
		}

		RequiredHandler reqm = new RequiredHandler("monitor-statevar-handler",
				DEFAULT_NAMESPACE);
		if (!returnedList.contains(reqm)) {
			returnedList.add(reqm);
		}
		return returnedList;
	}


	public void starting() {
		if (constituantTracker == null) {
			try {
				String filter = createTrackingFilter();
				constituantTracker = new Tracker(m_context,
						m_context.createFilter(filter), this);
				constituantTracker.open();
			} catch (InvalidSyntaxException e) {
				logger.error("a factory filter is not valid: " + e.getMessage()); // Holding
				// the
				// lock
				// should
				// not
				// be
				// an
				// issue here.
				stop();
			}
		}
		super.starting(); // Parent Tracker will search handlers. but this
		// mediator doesnt have any external handler.
	}

	/**
	 * This method is called when a matching service has been added to the
	 * tracker, we can no compute the factory state. This method is synchronized
	 * to avoid concurrent calls to method modifying the factory state.
	 * 
	 * @param reference
	 *            the added service reference.
	 * @see org.apache.felix.ipojo.util.TrackerCustomizer#addedService(org.osgi.framework.ServiceReference)
	 */
	public synchronized boolean addingService(ServiceReference reference) {
		// we test if is a processor

		String procName = (String) reference.getProperty("processor.name");
		if (procName == null) { // we see if it is a scheduler
			procName = (String) reference.getProperty("scheduler.name");
		}
		if (procName == null) { // we see if is a dispatcher
			procName = (String) reference.getProperty("dispatcher.name");
		}
		if (procName == null) { // if is neither scheduler, processor nor
			// dispatcher, its a normal handler.
			return super.addingService(reference);
		}
		return true; // return true if is a scheduler, processor or dispatcher.
	}

	public void stopping() {
		super.stopping();// The parent will close Tracker. This component type
		// doesnt track handlers services.
		if (constituantTracker != null) {
			constituantTracker.close();
			constituantTracker = null;
		}
	}

	

	protected String createConstituantFilter(Component constituant) {

		StringBuffer filterBuffer = new StringBuffer();
		filterBuffer.append("(&(" + constituant.getType() + CONSTITUANT_NAME
				+ "=" + constituant.getId() + ")");
		if (constituant.getNamespace() != null) {
			filterBuffer.append("(" + constituant.getType()
					+ CONSTITUANT_NAMESPACE + "=" + constituant.getNamespace()
					+ ")");
		}
		// End Filter.
		filterBuffer.append("(factory.state=1))");
		return filterBuffer.toString();
	}

	protected String createTrackingFilter() {
		StringBuffer filterBuffer = new StringBuffer();
		filterBuffer.append("(|");
		filterBuffer.append(createConstituantFilter(processorDescription));
		filterBuffer.append(createConstituantFilter(dispatcherDescription));
		filterBuffer.append(createConstituantFilter(schedulerDescription));
		// End Filter.
		filterBuffer.append(")");
		return filterBuffer.toString();
	}

	
	
	
	private class RequiredServiceHandler extends RequiredHandler {

		Component constituantToTrack;
		String filter;

		public RequiredServiceHandler(String name, String namespace,
				Component constituant) {
			super(name, namespace);
			this.constituantToTrack = constituant;
			filter = createConstituantFilter(constituantToTrack);
		}

		public ServiceReference getReference() {
			ServiceReference sreference = super.getReference();
			// See if the processor facotry is valid.
			try {
				ServiceReference[] serv = m_context.getAllServiceReferences(
						Factory.class.getName(), filter);
				if (serv != null && serv.length != 0) {
					return sreference;
				}
			} catch (InvalidSyntaxException e1) {
				// throw new
				// ConfigurationException("Unable to retrieve Processor factory");
				// //should never happen.
			}
			return null;
		}

		public String getFullName() {
			return getNamespace() + ":" + getName() + "( " + constituantToTrack
					+ " )";
		}

	}

	public String getComponentType() {
		return COMPONENT_TYPE;
	}

}
