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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.AbstractDispatcher;
import fr.liglab.adele.cilia.framework.IDispatcher;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.impl.Dispatcher;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceManager;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.IDispatcherHandler;
import fr.liglab.adele.cilia.runtime.ProcessorMetadata;
import fr.liglab.adele.cilia.runtime.WorkQueue;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;
/**
 * This class is in charge of acting as a bridge between the dispatcher logic, and the
 * sender instances.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class DispatcherHandler extends PrimitiveHandler implements InstanceStateListener,
Observer, IDispatcherHandler {
	/**
	 * Reference to the dispatcher logic component.
	 */
	CiliaInstance dispatcherComponent;
	/**
	 * Meta information of the dispatcher.
	 */
	Component dispatcherDescription;

	private ThreadLocal thLProcessor = new ThreadLocal();
	/**
	 * Sender Manager reference.
	 */
	private CiliaInstanceManager senderManager = new CiliaInstanceManagerSet();
	/**
	 * Method process Meta-data. To intercept.
	 */
	private MethodMetadata m_methodProcessMetadata;

	private Logger logger;

	private ReadWriteLock m_lock = new WriterPreferenceReadWriteLock();

	/**
	 * Cilia namespace.
	 */

	private final static String CILIA_DISPATCHER_HANDLERNAME = "dispatcher";

	protected MonitorHandler monitor = null;

	public String lastSenderName = "";

	public Data lastDataSended = null;

	/* This reference will be injected by iPOJO */
	private WorkQueue m_applicationQueue;
	/*
	 * Private class holding asynchronous properties
	 */
	ExtendedProperties extendedProperties;

	private void configureHandlerMonitoring(InstanceManager im, String handlerName) {
		Handler handler = (Handler) (im).getHandler(Const.ciliaQualifiedName(handlerName));
		if (handler != null) {
			Properties props = new Properties();
			props.put("cilia.monitor.handler", getMonitor());
			handler.reconfigure(props);
		}
	}
	
	private void configureHandlersMonitoring(CiliaInstance instance) {
		ComponentInstance im =((CiliaInstanceWrapper)instance).getInstanceManager();
		configureHandlerMonitoring((InstanceManager)im,"dependency") ;
		configureHandlerMonitoring((InstanceManager)im,"audit") ;
	}

	/**
	 * Check if the handler is well configured.
	 */
	public void initializeComponentFactory(ComponentTypeDescription cd, Element metadata)
			throws ConfigurationException {

		Element[] dispatcherHanlerMetadata = metadata.getElements(
				CILIA_DISPATCHER_HANDLERNAME, Const.CILIA_NAMESPACE);

		if (dispatcherHanlerMetadata != null ) {

			Element procesorMetadata = null;
			if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
				procesorMetadata = metadata.getElements("method",
						Const.CILIA_NAMESPACE)[0];
			} else if (metadata.containsElement("method")) {
				procesorMetadata = metadata.getElements("method")[0];
			} else {
				procesorMetadata = dispatcherHanlerMetadata[0];
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
			String returnDataType = methodMetadata.getMethodReturn();
			if (returnDataType.compareTo(cm.getReturnedDataType()[0]) != 0) {
				throw new ConfigurationException("Method " + cm.getMethod()
						+ " in pojo should " + "return "
						+ cm.getReturnedDataType()[0]);
			}

		} else {
			throw new ConfigurationException("Error in configuration"
					+ " this handler should be configured with one handler name:"
					+ CILIA_DISPATCHER_HANDLERNAME + " and HandlerNamespace:"
					+ Const.CILIA_NAMESPACE + metadata);

		}
	}

	/**
	 * Initialize handler properties.
	 * 
	 * @param dictionary
	 */
	private void initiainitializeProperties(Dictionary dictionary) {
		logger = LoggerFactory.getLogger("cilia.ipojo.runtime");
	}

	public void configure(Element metadata, Dictionary properties)
			throws ConfigurationException {

		extentedConfiguration();
		initiainitializeProperties(properties);
		// it will obtain dispatcher description from dictionary.

		((Observable) senderManager).addObserver(this);

		Element handlerMetadata = metadata.getElements(CILIA_DISPATCHER_HANDLERNAME,
				Const.CILIA_NAMESPACE)[0];

		Element procesorMetadata = null;
		if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
			procesorMetadata = metadata.getElements("method", Const.CILIA_NAMESPACE)[0];
		} else if (metadata.containsElement("method")) {
			procesorMetadata = metadata.getElements("method")[0];
		} else {
			procesorMetadata = handlerMetadata;
		}
		ProcessorMetadata dm = new ProcessorMetadata(procesorMetadata);

		subscribeProcesor(dm);
		getInstanceManager().addInstanceStateListener(this);
		createDispatcherDescription(properties);
		createDispatcherInstance(properties);
	}

	/**
	 * Add Sender Instance.
	 * 
	 * @param dictionary
	 *            Dictionary where sender is defined.
	 */
	public void addSender(String senderType, String portname, Dictionary dictionary) {
		CiliaInstanceWrapper ciliaSender = null;
		String identifier = null;
		if (dictionary == null) {
			dictionary = new Properties();
		}
		dictionary.remove("instance.name");
		// get collector identifier
		identifier = (String) dictionary.get("cilia.sender.identifier");
		if (identifier == null) {
			identifier = portname;
		}
		if (senderType != null) { // Sender Factory
			if (portname == null) { // Sender Id, must be unique in mediator.
				portname = senderType;
			}

			String filter = createSenderFilter(senderType);
			ciliaSender = new CiliaInstanceWrapper(getInstanceManager().getContext(),
					identifier, filter, dictionary, senderManager);
			ciliaSender.start();
			// TODO FIXE ME :
			configureHandlersMonitoring(ciliaSender) ;
			senderManager.addInstance(portname, ciliaSender);
		}
	}

	private String createSenderFilter(String type) {
		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(");
		filter.append("sender.name=");
		filter.append(type);
		filter.append(")");
		filter.append("(factory.state=1)");
		filter.append(")");
		return filter.toString();
	}

	/**
	 * Remove a given sender.
	 * 
	 * @param senderName
	 *            sender to remove.
	 */
	public void removeSender(String portname, String senderName) {
		try {
			synchronized (senderManager) {
				try {
					m_lock.writeLock().acquire();
					senderManager.removeInstance(portname, senderName);
				} finally {
					m_lock.writeLock().release();
				}

			}
		} catch (Exception ex) {
		}
	}

	private void subscribeProcesor(ProcessorMetadata dm) {
		m_methodProcessMetadata = getPojoMetadata().getMethod(dm.getMethod(),
				dm.getParameterDataType());
		getInstanceManager().register(m_methodProcessMetadata, this);
		logger.debug("registring method:" + m_methodProcessMetadata.getMethodName());
	}

	public void onError(Object pojo, Method method, Throwable throwable) {
		Exception ex = null;
		if (Exception.class.isAssignableFrom(throwable.getClass())) {
			ex = Exception.class.cast(throwable);
		} else {
			ex = new CiliaException("Unknown Error Exception");
		}
		notifyOnProcessError((List) thLProcessor.get(), ex);
		throwable.printStackTrace();
	}

	public void onEntry(Object pojo, Method method, Object[] args) {
		List list = null;

		if ((args != null) && (args.length > 0)) {
			if (args[0] instanceof List) {
				list = new ArrayList((List) args[0]);
			} else if (args[0] instanceof Data) {
				list = new ArrayList();
				Data ndata = (Data) args[0];
				list.add(ndata);
			}
		}
		thLProcessor.set(list);
		notifyOnProcessEntry(list);
	}

	public void onExit(Object pojo, Method method, Object returnedObj) {
		StringBuffer msg;

		if (method.getName().compareTo(m_methodProcessMetadata.getMethodName()) == 0) {
			List list = null;
			Data ndata = null;
			boolean isList = false;
			if (returnedObj == null) {
				logger.warn("Dispatching empty dataset");
				list = null;
			} else if (returnedObj instanceof List) {
				list = new ArrayList((List) returnedObj);
				isList = true;
			} else if (returnedObj instanceof Data) {
				list = new ArrayList();
				ndata = (Data) returnedObj;
				list.add(ndata);
				isList = false;
			} else {
				msg = new StringBuffer().append("Unable to identify returned data type ")
						.append(returnedObj);
				logger.error(msg.toString());
			}
			final List rList = list;
			notifyOnProcessExit(list);
			try {
				if (isList) {
					dispatch(rList);
				} else {
					dispatch(ndata);
				}
			} catch (CiliaException e) {
				logger.error("Error while dispatching :"+e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
			notifyOnDispatch(rList);
		}
	}

	public void stateChanged(ComponentInstance instance, int newState) {
		logger.debug("State Instance Manager has changed" + newState);
		switch (newState) {
		case ComponentInstance.VALID:
			Set keys = senderManager.getKeys();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				Object obj = it.next();

				List senderList = (List) senderManager.getPojo((String) obj);
				Iterator itSenders = senderList.iterator();
				while (itSenders.hasNext()) {
					CiliaInstance cisender = (CiliaInstance) itSenders.next();
					ISender sender = (ISender) cisender.getObject();
					if (sender == null) {
						// If there is some sender invalid and or null, set
						// handler manager invalid.
						getHandlerManager().setState(ComponentInstance.INVALID);
						logger.warn("Some Sender is null or invalid when some sender state has changed");
					}
				}

			}
		}
	}

	/**
	 * Send the specified data to the sender specified by their name.
	 * 
	 * @param senderName
	 *            Sender to be used to send data.
	 * @param data
	 *            Data to send.
	 */
	public void send(final String senderName, final Data data) throws CiliaException {
		lastSenderName = senderName;
		data.setLastDeliveryPort(senderName);
		lastDataSended = data;
		boolean synchronous = extendedProperties.isModeSynchronous;
		List pojoList = (List) senderManager.getPojo(senderName);
		if (pojoList == null) {
			logger.error("There is any sender present in port : "
					+ String.valueOf(senderName));

		} else {
			List senders = new ArrayList(pojoList);
			Iterator it = senders.iterator();
			int iteration = 0;
			while (it.hasNext()) {
				CiliaInstance ci = (CiliaInstance) it.next();
				ISender msender = (ISender) ci.getObject();
				if (msender != null) {
					if (logger.isDebugEnabled())
						logger.debug("[" + (iteration++) + "]Sending using:"
								+ ci.getName());
					if (synchronous == true) {
						msender.send(data);
					} else {
						m_applicationQueue.execute(new AsynchronousSend(msender, data));
					}
				} else {
					logger.error("Sending throw port:" + senderName + " " + ci.getName()
							+ " is not valid");
				}
			}
		}
	}

	/**
	 * Send the specified data to the sender specified by their name.
	 * 
	 * @param senderName
	 *            sender used to send data.
	 * @param properties
	 *            Properties to reconfigure sender before send.
	 * @param data
	 *            Data to send.
	 */
	public void send(final String senderName, final Properties properties, final Data data)
			throws CiliaException {
		synchronized (senderManager) {
			senderManager.reconfigurePOJOS(properties);
			send(senderName, data);
		}
	}

	public MonitorHandler getMonitor() {
		return (MonitorHandler) getInstanceManager().getHandler(
				Const.ciliaQualifiedName("monitor-handler"));
	}

	/**
	 * get Senders ids
	 * 
	 * 
	 * @return List of senders ids
	 */
	public List getSendersIds() {
		Set keys = senderManager.getKeys();
		List ports = new ArrayList();
		Iterator it = keys.iterator();
		while(it.hasNext()) {
			String key = String.valueOf(it.next());
			if (!key.startsWith("error")){
				ports.add(key);
			}
		}
		return new ArrayList(ports);
	}

	public void notifyOnProcessError(List data, Exception ex) {
		logger.error("processing error with: " + data);
		if (monitor == null) {
			monitor = getMonitor();
		}
		if (monitor != null) {
			monitor.notifyOnProcessError(data, ex);
		}
	}

	protected void notifyOnProcessExit(List data) {
		StringBuffer msg = new StringBuffer().append("process entry");
		if (logger.isDebugEnabled()) {
			msg.append(" data=").append(String.valueOf(data));
		}
		logger.debug(msg.toString());
		if (monitor == null) {
			monitor = getMonitor();
		}
		if (monitor != null) {
			monitor.notifyOnProcessExit(data);
		}
	}

	protected void notifyOnProcessEntry(List data) {
		StringBuffer msg = new StringBuffer().append("process entry");
		if (logger.isDebugEnabled()) {
			msg.append(" data=").append(String.valueOf(data)); 
		}
		logger.debug(msg.toString());
		if (monitor == null) {
			monitor = getMonitor();
		}
		if (monitor != null) {
			monitor.notifyOnProcessEntry(data);
		}
	}

	protected void notifyOnDispatch(List data) {
		StringBuffer msg = new StringBuffer().append("dispatch ");
		if (logger.isDebugEnabled()) {
			msg.append(" data=").append(String.valueOf(data)); 
		}
		logger.debug(msg.toString());
		if (monitor == null) {
			monitor = getMonitor();
		}
		if (monitor != null) {
			monitor.notifyOnDispatch(data);
		}	
	}


	public void fireEvent(Map info) {
		StringBuffer msg = new StringBuffer().append("fire event");
		if (logger.isDebugEnabled()) {
			msg.append(" parameter=").append(info.toString());
		}
		logger.debug(msg.toString());
		if (monitor == null) {
			monitor = getMonitor();
		}
		if (monitor != null) {
			monitor.fireEvent(info);
		}
	}

	private void createDispatcherDescription(Dictionary dictionary)
			throws ConfigurationException {
		String dispatcher = (String) dictionary.get("cilia.dispatcher.name");
		String dispatcherNS = (String) dictionary.get("cilia.dispatcher.namespace");
		if (dispatcher == null) {
			throw new ConfigurationException("Cilia Dispatcher must have at least a name");
		}
		dispatcherDescription = new Dispatcher("dispatcher", dispatcher, dispatcherNS,
				null);
	}

	public void reconfigure(Dictionary props) {
		logger.debug("reconfiguration");
		extendedReconfiguration(props);
		dispatcherComponent.updateInstanceProperties(getDispatcherProperties(props));
		senderManager.reconfigurePOJOS(props);
		initiainitializeProperties(props);
	}

	private void createDispatcherInstance(Dictionary dictionary) {
		BundleContext context = getInstanceManager().getContext();
		String dispatcherName = "dispatcher";
		String dispatcherFilter = createFilter();
		Dictionary dispProperties = getDispatcherProperties(dictionary);
		dispatcherComponent = new CiliaInstanceWrapper(context, dispatcherName,
				dispatcherFilter, dispProperties, this);
	}

	private Dictionary getDispatcherProperties(Dictionary dictionary) {
		Dictionary dispatcherProperties;
		
		Object properties = dictionary.get("dispatcher.properties");
		if (properties != null && properties instanceof Dictionary) {
			dispatcherProperties = (Dictionary) properties;
		} else {
			dispatcherProperties = dictionary;
		}
		return dispatcherProperties;
	}

	private String createFilter() {
		StringBuffer filter = new StringBuffer("(&(factory.state=1)");
		filter.append("(dispatcher.name=" + dispatcherDescription.getType() + ")");
		if (dispatcherDescription.getNamespace() != null) {
			filter.append("(dispatcher.namespace=" + dispatcherDescription.getNamespace()
					+ ")");
		}

		filter.append(")");
		return filter.toString();
	}

	public void update(Observable o, Object arg) {
		Integer state = (Integer) arg;
		if (state.intValue() == CiliaInstance.VALID) {
			getHandlerManager().setState(CiliaInstance.VALID);
			/* il faut tester ici ..l'etat du dispatcher */
			updateDispatcherReference();
		} else {
			// getHandlerManager().setState(ComponentInstance.INVALID);
		}
	}

	private void updateDispatcherReference() {
		if (dispatcherComponent == null) {
			logger.debug("Dispatcher is not valid, waiting to be valid");
			return;
		}
		IDispatcher ref = (IDispatcher) dispatcherComponent.getObject();
		if (ref == null) {
			logger.debug("Dispatcher is not valid, waiting to be valid");
			return;
		}
		logger.debug("Dispatcher is now valid, updating reference");
		AbstractDispatcher im = (AbstractDispatcher) ref; // all dispatchers must be
		// extend AbstractDispatcher
		im.setDispatcher(this);
	}

	public void dispatch(Data data) throws CiliaException {
		IDispatcher dispatcher = (IDispatcher) dispatcherComponent.getObject();
		if (dispatcher == null) {
			logger.warn("Dispatcher is not valid when dispatching, waiting to be valid");
			return;
		}
		dispatcher.dispatch(data);
	}

	private void dispatch(List dataset) throws CiliaException {
		IDispatcher dispatcher = (IDispatcher) dispatcherComponent.getObject();
		if (dispatcher == null) {
			logger.warn("Dispatcher is not valid when dispatching, waiting to be valid");
			return;
		}
		for (int i = 0; i < dataset.size(); i ++) {
			Data data = (Data)dataset.get(i);
			dispatch(data);
		}
	}

	public void start() {
	}

	public void stop() {
	}

	public void unvalidate() {
		logger.debug("stop dispatcher");
		senderManager.removeAllInstances();
		if (dispatcherComponent != null) {
			synchronized (dispatcherComponent) {
				dispatcherComponent.stop();
			}
			// dispatcherComponent = null;
		}
	}

	public void validate() {
		logger.debug("start dispatcher");
		if (dispatcherComponent != null) {
			synchronized (dispatcherComponent) {
				dispatcherComponent.start();
				updateDispatcherReference();
				configureHandlersMonitoring(dispatcherComponent);
			}
		}
	}

	/* Initialization by default */
	private void extentedConfiguration() {
		extendedProperties = new ExtendedProperties();
	}

	/*
	 * Extended reconfiguration decode parameters : "mediator.mode.synchrone",
	 * value true ->set mode synchrone,value=false-> set mode asynchrone "
	 */
	private void extendedReconfiguration(Dictionary props) {
		if (props != null) {
			Object obj;
			obj = props.get("mediator.mode.synchrone");
			if ((obj != null) && (obj instanceof Boolean)) {
				logger.debug("Mediator 'asychronous' mode");
				extendedProperties.isModeSynchronous = ((Boolean) obj).booleanValue();
			}
			/* reset the queue size to the default 'norm' value */
			String value = (String) props.get("task.queue.application");
			if ((value != null) && (value instanceof String)) {
				int i = Integer.parseInt(value);
				extendedProperties.oldMaxjobQueued = i;
				if (logger.isDebugEnabled())
					logger.debug("set 'asynchronous queue' size to ("
							+ extendedProperties.oldMaxjobQueued + ")");
			}

		}
	}

	/*
	 * Class holding extended properties
	 */
	private class ExtendedProperties {
		private boolean isModeSynchronous;
		private int maxJobQueued;
		private int oldMaxjobQueued;

		ExtendedProperties() {
			isModeSynchronous = true;
			maxJobQueued = 0;
			oldMaxjobQueued = 0;
		}

		public void evaluate() {
			if (!isModeSynchronous) {
				maxJobQueued = Math.max(maxJobQueued,
						m_applicationQueue.sizeMaxjobQueued());
				if (oldMaxjobQueued < maxJobQueued) {
					fireEvent(Collections.singletonMap("task.queue.application",
							new Integer(maxJobQueued)));
					oldMaxjobQueued = maxJobQueued;
				}
			}
		}
	}

	/* private class for sending in asynchronous way */
	private class AsynchronousSend implements Runnable {
		ISender msender;
		Data data;

		AsynchronousSend(ISender msender, final Data data) {
			this.msender = msender;
			this.data = data;
		}

		public void run() {
			msender.send(data);
			/* checks is the size of events queued has reach a new maximum */
			extendedProperties.evaluate();
		}
	}

}
