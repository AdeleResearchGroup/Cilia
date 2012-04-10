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
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaException;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.MonitorHandler;
import fr.liglab.adele.cilia.framework.utils.Const;
import fr.liglab.adele.cilia.framework.utils.ProcessorMetadata;
import fr.liglab.adele.cilia.framework.utils.WorkQueue;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.runtime.AbstractCiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceManager;
import fr.liglab.adele.cilia.runtime.impl.CiliaInstanceManagerSet;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;

public class DispatcherHandler extends PrimitiveHandler implements InstanceStateListener,
		Observer, IDispatcherHandler {

	CiliaInstance dispatcherComponent;

	Component dispatcherDescription;

	private ThreadLocal thLProcessor = new ThreadLocal();
	/**
	 * Sender Manager reference.
	 */
	private CiliaInstanceManager senderManager = new CiliaInstanceManagerSet();
	/**
	 * Method process Metadata. To intercept.
	 */
	private MethodMetadata m_methodProcessMetadata;

	private Logger logger;

	private ReadWriteLock m_lock = new WriterPreferenceReadWriteLock();

	/**
	 * Cilia namespace.
	 */

	private final static String CILIA_DISPATCHER_HANDLERNAME = "dispatcher";

	/**
	 * Flag to see if dispatcher is started or not.
	 */
	private boolean isStarted = false;

	protected MonitorHandler monitor = null;

	public String lastSenderName = "";

	public Data lastDataSended = null;

	/* This reference will be injected by iPOJO */
	private WorkQueue m_applicationQueue;
	/*
	 * Private class holding autonomic properties
	 */
	ExtendedProperties extendedProperties;

	/**
	 * Check if the handler is well configured.
	 */
	public void initializeComponentFactory(ComponentTypeDescription cd, Element metadata)
			throws ConfigurationException {

		Element[] dispatcherHanlerMetadata = metadata.getElements(
				CILIA_DISPATCHER_HANDLERNAME, Const.CILIA_NAMESPACE);

		if (dispatcherHanlerMetadata != null && dispatcherHanlerMetadata.length == 1) {

			for (int i = 0; i < dispatcherHanlerMetadata.length; i++) {
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
			}

		} else {
			throw new ConfigurationException("Error in configuration"
					+ " this handler should be configured with one handler name:"
					+ CILIA_DISPATCHER_HANDLERNAME + " and HandlerNamespace:"
					+ Const.CILIA_NAMESPACE);
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
		Element dispatcherMetadata = metadata.getElements(CILIA_DISPATCHER_HANDLERNAME,
				Const.CILIA_NAMESPACE)[0];
		// it will obtain dispatcher description from dictionary.

		initiainitializeProperties(properties);

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
		AbstractCiliaInstance ciliaSender = null;
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
			ciliaSender = new AbstractCiliaInstance(getInstanceManager().getContext(),
					identifier, filter, dictionary, senderManager);
			ciliaSender.start();
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
			if (returnedObj == null) {
				logger.warn("Dispatching empty dataset");
				list = null;
			} else if (returnedObj instanceof List) {
				list = new ArrayList((List) returnedObj);
			} else if (returnedObj instanceof Data) {
				list = new ArrayList();
				Data ndata = (Data) returnedObj;
				list.add(ndata);
			} else {
				msg = new StringBuffer().append("Unable to identify returned data type ")
						.append(returnedObj);
				logger.error(msg.toString());
				new CiliaException(msg.toString());
			}
			final List rList = list;
			notifyOnProcessExit(list);
			try {
				dispatch(rList);
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
		dispatcherDescription = new Component("dispatcher", dispatcher, dispatcherNS,
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
		String schedulerName = "dispatcher";
		String schedulerFilter = createFilter();
		Dictionary dispProperties = getDispatcherProperties(dictionary);
		dispatcherComponent = new AbstractCiliaInstance(context, schedulerName,
				schedulerFilter, dispProperties, this);
	}

	private Dictionary getDispatcherProperties(Dictionary dictionary) {
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
		filter.append("(dispatcher.name=" + dispatcherDescription.getType() + ")");
		if (dispatcherDescription.getNamespace() != null) {
			filter.append("(dispatcher.namespace=" + dispatcherDescription.getNamespace()
					+ ")");
		}

		filter.append(")");
		return filter.toString();
	}

	public void update(Observable o, Object arg) {
		Boolean state = (Boolean) arg;
		if (state.booleanValue()) {
			getHandlerManager().setState(ComponentInstance.VALID);
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
		CiliaDispatcher im = (CiliaDispatcher) ref; // all dispatchers must be
		// extend CiliaDispatcher
		im.setDispatcher(this);
	}

	public void dispatch(List dataset) throws CiliaException {
		IDispatcher dispatcher = (IDispatcher) dispatcherComponent.getObject();
		if (dispatcher == null) {
			logger.warn("Dispatcher is not valid when dispatching, waiting to be valid");
			return;
		}
		dispatcher.dispatch(dataset);
	}

	public void start() {
	}

	public void stop() {
	}

	public void unvalidate() {
		logger.debug("stop dispatcher");
		isStarted = false;
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
		isStarted = true;
		if (dispatcherComponent != null) {
			synchronized (dispatcherComponent) {
				dispatcherComponent.start();
				updateDispatcherReference();
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
