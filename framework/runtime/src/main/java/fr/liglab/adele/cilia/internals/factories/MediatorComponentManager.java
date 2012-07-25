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
package fr.liglab.adele.cilia.internals.factories;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.HandlerManager;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.Dispatcher;
import fr.liglab.adele.cilia.model.impl.Scheduler;
import fr.liglab.adele.cilia.runtime.Const;
import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
import fr.liglab.adele.cilia.runtime.impl.DispatcherInstanceManager;
import fr.liglab.adele.cilia.runtime.impl.MonitorHandler;
import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;
import fr.liglab.adele.cilia.runtime.impl.SchedulerInstanceManager;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public abstract class MediatorComponentManager extends InstanceManager {

	protected final MediatorComponentFactory mfactory;

	protected SchedulerInstanceManager schedulerManager;

	protected DispatcherInstanceManager dispatcherManager;

	private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);

	private MonitorHandler monitor;

	protected volatile int ciliaState = MediatorComponent.INVALID;

	Dictionary configuration;

	/**
	 * @param factory
	 * @param context
	 * @param handlers
	 */
	public MediatorComponentManager(MediatorComponentFactory factory,
			BundleContext context, HandlerManager[] handlers) {
		super(factory, context, handlers);
		mfactory = factory;
	}

	public Port getInPort(String name) {
		return this.mfactory.getInPort(name);
	}

	public Port getOutPort(String name) {
		return this.mfactory.getOutPort(name);
	}

	public void startManagers() {
		updateSchedulerManager();
		updateDispatcherManager();
		configureHandlersMonitoring();
	}

	public void stopManagers() {
		stopSchedulerManager();
		stopDispatcherManager();

	}

	private void configureHandlerMonitoring(InstanceManager im,
			String handlerName) {
		Handler handler = (Handler) (im).getHandler(Const
				.ciliaQualifiedName(handlerName));
		if (handler != null) {
			Properties props = new Properties();
			props.put("cilia.monitor.handler", getMonitor());
			handler.reconfigure(props);
		}
	}

	private MonitorHandler getMonitor() {
		if (monitor == null) {
			monitor = (MonitorHandler) getInstanceManager().getHandler(
					Const.ciliaQualifiedName("monitor-handler"));
		}
		return monitor;
	}

	private void configureHandlersMonitoring() {
		InstanceManager im = getInstanceManager();
		configureHandlerMonitoring(im, "dependency");
		configureHandlerMonitoring(im, "audit");
	}

	private synchronized void updateSchedulerManager() {
		if (schedulerManager != null) {
			return;
		}
		String schedulerName = mfactory.getSchedulerDescription().getId();
		String schedulerNS = mfactory.getSchedulerDescription().getNamespace();

		Scheduler schedulerDescription = new Scheduler("scheduler",
				schedulerName, schedulerNS, configuration);

		schedulerManager = new SchedulerInstanceManager(getContext(),
				getScheduler(), schedulerDescription, this);
		schedulerManager.start();
	}

	private synchronized void stopSchedulerManager() {
		if (schedulerManager != null) {
			schedulerManager.stop();
		}
	}

	private synchronized void updateDispatcherManager() {
		if (dispatcherManager != null) {
			return;
		}

		String dispatcherName = mfactory.getDispatcherDescription().getId();
		String dispatcherNS = mfactory.getDispatcherDescription()
				.getNamespace();

		Dispatcher dispatcherDescription = new Dispatcher("dispatcher",
				dispatcherName, dispatcherNS, configuration);

		dispatcherManager = new DispatcherInstanceManager(getContext(),
				getDispatcher(), dispatcherDescription, this);
		dispatcherManager.start();
	}

	private synchronized void stopDispatcherManager() {
		if (dispatcherManager != null) {
			dispatcherManager.stop();
		}
	}

	public synchronized void removeCollector(String port, Component collector) {
		if (schedulerManager == null) {
			logger.warn("Unable to remove Collector {} : Manager is invalid",
					collector.getId());
			return;
		}
		schedulerManager.removeComponent(port, collector);
	}

	public synchronized void addCollector(String port, Component collector) {
		updateSchedulerManager();
		schedulerManager.addComponent(port, collector);
	}

	public synchronized void removeSender(String port, Component sender) {
		if (dispatcherManager == null) {
			logger.warn("Unable to remove Sender {} : Manager is invalid",
					sender.getId());
			return;
		}
		dispatcherManager.removeComponent(port, sender);
	}

	public synchronized void addSender(String port, Component sender) {
		updateDispatcherManager();
		dispatcherManager.addComponent(port, sender);
	}

	private SchedulerHandler getScheduler() {
		SchedulerHandler r = null;
		InstanceManager im = getInstanceManager();
		r = (SchedulerHandler) im.getHandler(Const
				.ciliaQualifiedName("scheduler"));
		return r;
	}

	private DispatcherHandler getDispatcher() {
		DispatcherHandler r = null;
		InstanceManager im = getInstanceManager();
		r = (DispatcherHandler) im.getHandler(Const
				.ciliaQualifiedName("dispatcher"));
		return r;
	}

	private InstanceManager getInstanceManager() {
		InstanceManager im = null;
		ComponentInstance componentInstance = this;
		if (componentInstance instanceof InstanceManager) {
			im = (InstanceManager) componentInstance;
		}
		if (componentInstance instanceof MediatorManager) {
			MediatorManager mm = (MediatorManager) componentInstance;
			if (mm != null) {
				im = (InstanceManager) mm.getProcessorInstance();
			}
		}
		return im;
	}

}
