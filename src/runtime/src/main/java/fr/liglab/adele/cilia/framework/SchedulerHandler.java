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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.architecture.PropertyDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.apache.felix.ipojo.util.Callback;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.IMonitor;
import fr.liglab.adele.cilia.framework.monitor.IProcessorMonitor;
import fr.liglab.adele.cilia.framework.monitor.MonitorHandler;
import fr.liglab.adele.cilia.framework.utils.AdminData;
import fr.liglab.adele.cilia.framework.utils.Const;
import fr.liglab.adele.cilia.framework.utils.ProcessorMetadata;
import fr.liglab.adele.cilia.framework.utils.WorkQueue;
import fr.liglab.adele.cilia.Component;
import fr.liglab.adele.cilia.model.ConstModel;
import fr.liglab.adele.cilia.model.Scheduler;
import fr.liglab.adele.cilia.runtime.AbstractCiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceManager;
import fr.liglab.adele.cilia.runtime.impl.CiliaInstanceManagerSet;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;

public class SchedulerHandler extends PrimitiveHandler implements ISchedulerHandler,
InstanceStateListener, Observer, Runnable {

	protected Logger logger;

	protected MonitorHandler monitor;
	private ReadWriteLock writeLock = new WriterPreferenceReadWriteLock();
	/**
	 * Method process metadata, to invoke processor.
	 */
	private MethodMetadata methodMetadata;

	private final CiliaInstanceManager collectorsManager = new CiliaInstanceManagerSet();

	private final Object lockObject = new Object();

	private boolean islist;

	private Callback callback;

	private CiliaInstance schedulerComponent;

	private Component schedulerDescription;

	private final static String HANDLER_NAME = "scheduler";

	private boolean isLocked = false;

	private ServiceReference m_refData = null;
	/* This reference will be injected by iPOJO */
	private WorkQueue m_systemQueue;

	private Dictionary m_dictionary;
	private Element m_metadata;

	/**
	 * This method is called by iPOJO to verify the metadata correctness.
	 * 
	 * @param cd
	 *            Component description.
	 * @param metadata
	 *            iPOJO metadata.
	 * @throws ConfigurationException
	 *             when there is an error configuring handler with the iPOJO.
	 */
	public void initializeComponentFactory(ComponentTypeDescription cd, Element metadata)
	throws ConfigurationException {

		Element[] schedulerHanlerMetadata = metadata.getElements(HANDLER_NAME,
				Const.CILIA_NAMESPACE);

		if (schedulerHanlerMetadata != null && schedulerHanlerMetadata.length == 1) {

			for (int i = 0; i < schedulerHanlerMetadata.length; i++) {
				Element procesorMetadata = null;
				if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
					procesorMetadata = metadata.getElements("method",
							Const.CILIA_NAMESPACE)[0];
				} else if (metadata.containsElement("method")) {
					procesorMetadata = metadata.getElements("method")[0];
				} else {
					procesorMetadata = schedulerHanlerMetadata[0];
				}
				ProcessorMetadata cm = new ProcessorMetadata(procesorMetadata);
				PojoMetadata pojometadata = getPojoMetadata();
				MethodMetadata methodMetadata;
				methodMetadata = pojometadata.getMethod(cm.getMethod(),
						cm.getParameterDataType());

				if (methodMetadata == null) {
					throw new ConfigurationException("Method " + cm.getMethod()
							+ " in pojo should " + "receive "
							+ cm.getReturnedDataType()[0]);
				}
			}
			// Add properties to component.
			PropertyDescription[] phd1 = getHandlerManager().getFactory()
			.getComponentDescription().getProperties();
			for (int i = 0; phd1 != null && i < phd1.length; i++) {
				if (!phd1[i].isImmutable()) {
					cd.addProperty(new PropertyDescription(phd1[i].getName(), phd1[i]
					                                                               .getType(), phd1[i].getValue()));
				}
			}

		} else {
			throw new ConfigurationException("Error in configuration"
					+ " this handler should be configured with one handler name:"
					+ HANDLER_NAME + " and HandlerNamespace:" + Const.CILIA_NAMESPACE);
		}
	}

	protected void initializeProperties(Dictionary dictionary) {
		logger = LoggerFactory.getLogger("cilia.ipojo.runtime");
	}

	/**
	 * Method called by iPOJO when creating an instance.
	 */
	public void configure(Element metadata, Dictionary dictionary)
	throws ConfigurationException {

		initializeProperties(dictionary);
		Element schedulerMetadata = metadata.getElements(HANDLER_NAME,
				Const.CILIA_NAMESPACE)[0];

		// it will obtain scheduler description from dictionary.
		createSchedulerDescription(schedulerMetadata, dictionary);

		// Moved to start() createSchedulerInstance(schedulerMetadata,
		// dictionary);

		Element subscribers = metadata.getElements(HANDLER_NAME, Const.CILIA_NAMESPACE)[0];
		this.m_dictionary = dictionary;
		this.m_metadata = metadata;
		// //////////////////////////////////////////////////////////
		getInstanceManager().addInstanceStateListener(this);

		((Observable) collectorsManager).addObserver(this);

		Element procesorMetadata = null;
		if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
			procesorMetadata = metadata.getElements("method", Const.CILIA_NAMESPACE)[0];
		} else if (metadata.containsElement("method")) {
			procesorMetadata = metadata.getElements("method")[0];
		} else {
			procesorMetadata = subscribers;
		}

		ProcessorMetadata sm = new ProcessorMetadata(procesorMetadata);
		PojoMetadata pojometadata = getPojoMetadata();
		methodMetadata = pojometadata
		.getMethod(sm.getMethod(), sm.getParameterDataType());

		// Moved to start addCollector(subscribers, (Map) dictionary);

		islist = (sm.getParameterDataType()[0].compareTo(java.util.List.class.getName()) == 0);
		callback = new Callback(methodMetadata, getInstanceManager());
	}

	/**
	 * This method will invoke the process method in mediator.
	 * 
	 * @param dataSet
	 *            Data set used to invoke process in mediator.
	 * @return null.
	 */
	public final void process(final List /* data */dataList) {
		if (getInstanceManager().getState() == ComponentInstance.VALID) {
			// stock triggered data to be used later (log/stats/...).
			List list = null;
			Object args[] = new Object[1];// it support only one parametter.
			synchronized (dataList) {
				list = new ArrayList(dataList);
			}

			try {
				if (islist) {
					args[0] = list;
					callback.call(args);
				} else { // iterate in all the elements.
					for (int i = 0; i < list.size(); i++) {
						args[0] = list.get(i);
						callback.call(args);
					}
					if (list.size() == 0) { // It means that there is any Data,
						// but we call it with null.
						args[0] = null;
						callback.call(args);
					}
				}
			} catch (Exception e) {
				logger.error(e.getStackTrace().toString());
			}
		} else {
			logger.error("Trying to process but processor is not valid");
		}
	}

	public void removeCollector(String portname, String identifier) {
		logger.debug("remove collector '" + identifier + "'");
		synchronized (lockObject) {
			collectorsManager.removeInstance(portname, identifier);
		}
	}

	/**
	 * Add collector Instance.
	 * 
	 * @param dictionary
	 *            Dictionary where collector is defined.
	 */
	public void addCollector(String collectorType, String portname, Dictionary dictionary) {
		AbstractCiliaInstance ciliaCollector = null;
		String identifier = null;
		if (dictionary == null) {
			dictionary = new Properties();
		}
		dictionary.remove("instance.name");
		// Adding
		dictionary.put("collector.sourceName", portname);

		// get collector identifier
		identifier = (String) dictionary.get("cilia.collector.identifier");
		if (identifier == null) {
			identifier = portname;
		}

		if (collectorType == null) {
			logger.error("Adding collector to scheduler failed, collector must have a type");
			return;
		}

		if (collectorType != null) { // Collector Factory
			if (portname == null) { // Collector Id, must be unique in mediator.
				portname = collectorType;
			}
			String filter = createCollectorFilter(collectorType);
			logger.debug("Creating collector in mediator " + portname);
			synchronized (lockObject) {

				ciliaCollector = new AbstractCiliaInstance(getInstanceManager()
						.getContext(), identifier, filter, dictionary, collectorsManager);
				collectorsManager.addInstance(portname, ciliaCollector);
			}
			ciliaCollector.start();
			/* Retreive the dependency handler */
			//TODO: TO FIX BUG IF IM IS NULL
			InstanceManager im = (InstanceManager) ciliaCollector.getInstanceManager();
			Handler dependency = null;
			if(im != null) {
				dependency = (Handler) (im)
				.getHandler(Const.ciliaQualifiedName("dependency"));
			}

			if (dependency != null) {
				Properties props = new Properties();
				props.put("cilia.monitor.handler", getMonitor());
				/* Set monitor handler reference to the dependency handler */
				dependency.reconfigure(props);
			}

			ICollector col = (ICollector) ciliaCollector.getObject();
			if (col != null) {
				col.setScheduler(this);
			}
		}
	}

	private String createCollectorFilter(String type) {
		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(");
		filter.append("collector.name=");
		filter.append(type);
		filter.append(")");
		filter.append("(factory.state=1)");
		filter.append(")");
		return filter.toString();
	}

	/**
	 * Add collector Instance.
	 * 
	 * @param element
	 *            Element where collector is defined.
	 */
	private void addCollector(Element element, Map properties) {

		Element[] collectors = element.getElements(Const.INSTANCE_TYPE_COLLECTOR);
		int sizeElement = 0;
		/**
		 * Add collector Instances when they are defined
		 */
		if (collectors != null) {
			sizeElement = collectors.length;
		}

		Properties pojoProperties = new Properties();
		if (properties != null) {
			pojoProperties.putAll((Map) properties);
		}
		pojoProperties.remove("instance.name");

		for (int i = 0; i < sizeElement; i++) {
			String componentName = collectors[i].getAttribute(Const.NAME);
			if (componentName == null) {
				componentName = collectors[i].getAttribute("type");
			}
			String componentId = collectors[i].getAttribute(Const.ID);
			Properties collectorProperties = getProperties(collectors[i]);
			collectorProperties.putAll(pojoProperties);
			addCollector(componentName, componentId, collectorProperties);
		}

	}

	public List getSourcesIds() {
		synchronized (lockObject) {
			return new ArrayList(collectorsManager.getKeys());
		}
	}

	private Properties getProperties(Element element) {
		Properties props = new Properties();
		Element[] propertiesElement = element.getElements("property");
		if (propertiesElement != null) {
			for (int i = 0; i < propertiesElement.length; i++) {
				if (propertiesElement[i].containsAttribute("name")) {
					String propertyName = propertiesElement[i].getAttribute("name");
					String propertyValueString = null;
					Map propertyValueMap = null;
					Object propertyValue = null;
					// obtain the property value
					if (propertiesElement[i].containsAttribute("value")) {
						propertyValueString = propertiesElement[i].getAttribute("value");
						propertyValue = propertyValueString;
					} else {
						// If there is not any value, so they value could be a
						// Map
						propertyValueMap = getProperties(propertiesElement[i]);
						propertyValue = propertyValueMap;
					}
					props.put(propertyName, propertyValue);
				}
			}
		}
		return props;
	}

	public void reconfigure(Dictionary props) {
		String value;
		synchronized (lockObject) {
			schedulerComponent.updateInstanceProperties(getSchedulerProperties(props));
			collectorsManager.reconfigurePOJOS(props);
		}
		initializeProperties(props);
		/* Check property lock */
		value = (String) props.get(ConstModel.PROPERTY_LOCK_UNLOCK);
		if ((value != null) && (value.equals(ConstModel.SET_LOCK)))
			lock();
		else {
			if ((value != null) && (value.equals(ConstModel.SET_UNLOCK)))
				unlock();
		}
	}

	public void stateChanged(ComponentInstance instance, int newState) {
		logger.debug("State Instance Manager has changed" + newState);
		switch (newState) {
		case ComponentInstance.VALID:
			addSchedulerToCollectors();
		}
	}

	private void addSchedulerToCollectors() {
		synchronized (lockObject) { // Lock all the iteration. :S unable to add
			// a collector when performing this
			// opperation.
			Set keys = collectorsManager.getKeys();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				List collectorList = (List) collectorsManager.getPojo((String) obj);
				Iterator itCollectors = collectorList.iterator();
				while (itCollectors.hasNext()) {
					CiliaInstance cicol = (CiliaInstance) itCollectors.next();
					ICollector collector = (ICollector) cicol.getObject();
					if (collector == null) {
						// If there is some sender invalid and or null, set
						// handler manager invalid.
						getHandlerManager().setState(ComponentInstance.INVALID);
						logger.warn("Some Sender is null or invalid when some sender state has changed");
					} else {
						collector.setScheduler(this);
					}
				}
			}
		}
	}

	private void startCollectors() {
		logger.debug("start collector");
		collectorsManager.startInstances();
		addSchedulerToCollectors();
	}

	private void stopCollectors() {
		logger.debug("stop collector");
		collectorsManager.removeAllInstances();
	}

	public void notifyOnCollect(Data data) {

		StringBuffer msg = new StringBuffer().append("data collected");
		if (logger.isDebugEnabled()) {
			if (data != null)
				msg.append("=").append(data.toString());

			logger.debug(msg.toString());
		}
		MonitorHandler mon = getMonitor();
		if (mon != null) {
			mon.notifyOnCollect(data);
		} else {
			logger.error("Monitor is null");
		}

	}

	public void fireEvent(Map info) {
		StringBuffer msg = new StringBuffer().append("fire event");
		MonitorHandler mon = getMonitor();
		if (logger.isDebugEnabled()) {
			if (info != null)
				msg.append(" parameter=").append(info.toString());
			logger.debug(msg.toString());
		}
		if (mon != null) {
			mon.fireEvent(info);
		} else {
			logger.error("Monitor is null");
		}
	}

	private MonitorHandler getMonitor() {
		if (monitor == null) {
			monitor = (MonitorHandler) getInstanceManager().getHandler(
					Const.ciliaQualifiedName("monitor-handler"));
		}
		return monitor;
	}

	// call scheduler object reference.
	public void notifyData(Data data) {
		IScheduler scheduler = (IScheduler) schedulerComponent.getObject();
		if (scheduler == null) {
			logger.error("Some Error happend in scheduler, scheduler object is not present ");
			return;
		}
		if (isLocked())
			storeMessage(data);
		else {
			try {
				writeLock.readLock().acquire();
				notifyOnCollect(data);
				scheduler.notifyData(data);
			} catch (InterruptedException e) {
			} finally {
				writeLock.readLock().release();
			}
		}
	}

	private void createSchedulerDescription(Element scheduler, Dictionary dictionary)
	throws ConfigurationException {
		String schedulerName = null;
		String schedulerNS = null;

		schedulerName = (String) dictionary.get("cilia.scheduler.name");

		if (schedulerName == null) { // if is not in the instance properties
			// (adding by mediator factory). We see
			// if is in the processor attributes.
			if (scheduler.containsAttribute("name")) {
				schedulerName = scheduler.getAttribute("name");
			}

		}
		if (schedulerName == null) {
			logger.error("with the configuration" + dictionary);
			throw new ConfigurationException("Cilia Scheduler must have at least a name");
		}

		schedulerNS = (String) dictionary.get("cilia.scheduler.namespace");
		if (schedulerNS == null) {
			if (scheduler.containsAttribute("namespace")) {
				schedulerNS = scheduler.getAttribute("namespace");
			}
		}

		schedulerDescription = new Scheduler("scheduler", schedulerName, schedulerNS,
				null);
		logger.debug("create scheduler '" + schedulerName + "'");
	}

	private void createSchedulerInstance(Element scheduler, Dictionary dictionary) {
		BundleContext context = getInstanceManager().getContext();
		String schedulerName = "scheduler";
		String schedulerFilter = createFilter();
		schedulerComponent = new AbstractCiliaInstance(context, schedulerName,
				schedulerFilter, getSchedulerProperties(dictionary), this);
		schedulerComponent.start();
		updateSchedulerReference();
	}

	private Dictionary getSchedulerProperties(Dictionary dictionary) {
		Dictionary dispatcherProperties;
		Object properties = dictionary.get("dispatcher");
		if (properties != null && properties instanceof Dictionary) {
			dispatcherProperties = (Dictionary) properties;
		} else {
			dispatcherProperties = dictionary;
		}
		return dispatcherProperties;
	}

	private String createFilter() {
		StringBuffer filter = new StringBuffer("(&(factory.state=1)");
		filter.append("(scheduler.name=" + schedulerDescription.getType() + ")");
		if (schedulerDescription.getNamespace() != null) {
			filter.append("(scheduler.namespace=" + schedulerDescription.getNamespace()
					+ ")");
		}
		filter.append(")");
		return filter.toString();
	}

	public void update(Observable o, Object arg) {
		Boolean state = (Boolean) arg;
		if (state.booleanValue()) {
			updateSchedulerReference();
			/* --> Teste l'etat du Scheduler --- */
			getHandlerManager().setState(ComponentInstance.VALID);
			addSchedulerToCollectors();
		} else {
			// getHandlerManager().setState(ComponentInstance.INVALID);
		}
	}

	private void updateSchedulerReference() {
		if (schedulerComponent == null) {
			logger.debug("Scheduler is not valid, waiting to be valid");
			return;
		}
		IScheduler ref = (IScheduler) schedulerComponent.getObject();
		if (ref == null) {
			logger.debug("Scheduler is not valid, waiting to be valid");
			return;
		}
		logger.debug("Scheduler is now valid, updating references");
		CiliaScheduler im = (CiliaScheduler) ref; // all scheduleres must be
		// extend CiliaScheduler
		im.setConnectedScheduler(this);
	}

	public void start() {
	}

	public void stop() {
	}

	public void unvalidate() {
		logger.debug("stop scheduler");
		stopCollectors();
		synchronized (lockObject) {
			if (schedulerComponent != null) {
				schedulerComponent.stop();
			}
		}
		// schedulerComponent = null;
		m_refData = null;
	}

	public void validate() {
		logger.debug("start scheduler");
		m_refData = null;
		createSchedulerInstance(
				m_metadata.getElements(HANDLER_NAME, Const.CILIA_NAMESPACE)[0],
				m_dictionary);
		addCollector(m_metadata.getElements(HANDLER_NAME, Const.CILIA_NAMESPACE)[0],
				(Map) m_dictionary);
		startCollectors();
		synchronized (lockObject) {
			if (schedulerComponent != null) {
				schedulerComponent.start();
			}
		}
	}

	private void storeMessage(Data data) {
		if (getAdminData(false).size() == 0) {
			getAdminData(false).put("cilia.stored.messages", new ArrayList());
		}
		((ArrayList) getAdminData(false).get("cilia.stored.messages")).add(data);
		logger.trace("#Message stored ="
				+ ((ArrayList) getAdminData(false).get("cilia.stored.messages")).size());
	}

	private ArrayList getStoredMessage() {
		ArrayList storedMessages = null;
		Map map = getAdminData(false);

		if (map != null) {
			storedMessages = (ArrayList) map.get("cilia.stored.messages");
		}
		return storedMessages;
	}

	public void lock() {
		isLocked = true;
		logger.debug("Scheduler now is locked");
	}

	private void injectDataStored(ArrayList dataStored) {
		Data data;
		IScheduler scheduler = (IScheduler) schedulerComponent.getObject();
		if (scheduler != null) {
			logger.trace("inject Data size=" + dataStored.size());
			for (int i = 0; i < dataStored.size(); i++) {
				data = (Data) dataStored.get(i);
				logger.trace("Inject data = " + data.getContent());
				notifyOnCollect(data);
				scheduler.notifyData(data);
			}
		}

	}

	public void unlock() {
		logger.trace("unlock called");
		m_systemQueue.execute(this);
	}

	public boolean isLocked() {
		return isLocked;
	}

	private void retreiveAdminService() {
		ServiceReference[] refs = null;
		BundleContext context = getInstanceManager().getContext();
		try {
			refs = context.getServiceReferences(AdminData.class.getName(), "(chain.name="
					+ (String) m_dictionary.get(ConstModel.PROPERTY_CHAIN_ID) + ")");
		} catch (InvalidSyntaxException e) {
			logger.warn("Admin data service lookup unrecoverable error");
			refs = null;
		}
		if (refs != null)
			m_refData = refs[0];
		else {
			logger.warn("Admin data service not found");
			return;
		}
	}

	private Map getAdminData(boolean isRunning) {
		AdminData dataContainer;
		Map data;
		if (m_refData == null)
			retreiveAdminService();
		dataContainer = (AdminData) getInstanceManager().getContext().getService(
				m_refData);
		data = dataContainer.getData(
				(String) m_dictionary.get(ConstModel.PROPERTY_COMPONENT_ID), isRunning);
		getInstanceManager().getContext().ungetService(m_refData);
		return data;
	}

	private void clearData() {
		AdminData dataContainer;
		Map data;
		if (m_refData == null)
			retreiveAdminService();
		dataContainer = (AdminData) getInstanceManager().getContext().getService(
				m_refData);
		dataContainer.clearData((String) m_dictionary
				.get(ConstModel.PROPERTY_COMPONENT_ID));
		getInstanceManager().getContext().ungetService(m_refData);
	}

	public Map getData() {
		/* Return the buffer */
		return getAdminData(true);
	}

	public void run() {
		/* Firstly , re -treat datas stored */
		init();
		/* Reinject new data */
		ArrayList storedMessages = getStoredMessage();
		if (storedMessages != null) {
			injectDataStored(storedMessages);
		}
		isLocked = false;
	}

	public void init() {
		IScheduler scheduler = (IScheduler) schedulerComponent.getObject();
		if (scheduler != null) {
			logger.trace("init called");
			scheduler.init();
		}
	}

}
