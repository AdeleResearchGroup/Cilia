package fr.liglab.adele.cilia.runtime;

import java.util.Dictionary;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cilia.framework.IDispatcherHandler;
import fr.liglab.adele.cilia.framework.ISchedulerHandler;

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
