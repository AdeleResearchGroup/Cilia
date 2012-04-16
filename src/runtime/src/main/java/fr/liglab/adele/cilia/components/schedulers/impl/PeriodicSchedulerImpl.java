package fr.liglab.adele.cilia.components.schedulers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.CiliaScheduler;

public class PeriodicSchedulerImpl extends CiliaScheduler implements Runnable {

	/**
	 * delay time to start processing.
	 */
	private long delay;

	/**
	 * Periodic time to launch processing.
	 */
	private long period;
	private Object _lock;
	/**
	 * The POOL_SIZE, this scheduler will handle only one thread.
	 */
	private static final int POOL_SIZE = 1;

	protected static Logger logger = LoggerFactory.getLogger("cilia.ipojo.compendium");

	/**
	 * Pool of scheduler threads.
	 */
	private ScheduledThreadPoolExecutor schedulerPoolExecutor = new ScheduledThreadPoolExecutor(
			POOL_SIZE);

	/**
	 * Flag used to trigger processing only when pojo is started.
	 */
	private volatile boolean isStarted = false;

	public void run() {
		if (isStarted) {
			logger.debug("periodic-scheduler will trigger processing");
			synchronized (_lock) {
				List dataList = (List)getData().get("data.periodic.scheduler") ;
				process(new ArrayList(dataList));
				dataList.clear();
			}
		}
	}

	/**
	 * Method called when stopping the component.
	 */
	public void stop() {
		logger.debug("Stopping periodic scheduler");
		isStarted = false;
		schedulerPoolExecutor.shutdown();
	}

	/**
	 * Method to start the scheduling. It will schedule processing at fixed
	 * period rate.
	 */
	private void startTimer() {
		schedulerPoolExecutor.scheduleAtFixedRate(this, delay, period,
				TimeUnit.MILLISECONDS);
	}

	/**
	 * Method called when component iPOJO is starting.
	 */
	public void start() {
		isStarted = true;
		getData().put("data.periodic.scheduler",new ArrayList());
		logger.debug("Starting periodic scheduler at " + period + " with a delay of " + delay);
		startTimer();
	}

	/**
	 * Method called by collectors when they collect new Data.
	 */
	public void notifyData(Data data) {
		synchronized (_lock) {
			if (data != null) {
			    List dataList=(List)getData().get("data.periodic.scheduler");
				dataList.add(data);
			}
		}
	}
	
}
