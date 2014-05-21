package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractScheduler;
import fr.liglab.adele.cilia.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeriodicScheduler extends AbstractScheduler implements Runnable {

    /**
     * delay time to start processing.
     */
    private long delay;

    private volatile boolean firstTime = true;

    /**
     * Periodic time to launch processing.
     */
    private long period;
    private Object _lock = new Object();
    /**
     * The POOL_SIZE, this scheduler will handle only one thread.
     */
    private static final int POOL_SIZE = 5;

    protected static Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

    /**
     * Pool of scheduler threads.
     */
    private ScheduledThreadPoolExecutor schedulerPoolExecutor = new ScheduledThreadPoolExecutor(POOL_SIZE);

    /**
     * Flag used to trigger processing only when pojo is started.
     */
    private volatile boolean isStarted = false;

    public void run() {
        logger.debug("periodic-scheduler will trigger processing");
        if (isStarted) {
            synchronized (_lock) {
                List dataList = (List) getData().get("data.periodic.scheduler");
                if (dataList != null && dataList.size() > 0) {
                    process(new ArrayList(dataList));
                    dataList.clear();
                }
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
        schedulerPoolExecutor.scheduleAtFixedRate(this, 0, period,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Method called when component iPOJO is starting.
     */
    public void start() {
        isStarted = true;
        logger.info("Starting periodic scheduler at " + period + " with a delay of " + delay);
        startTimer();
    }

    /**
     * Method called by collectors when they collect new Data.
     */
    public void notifyData(Data data) {
        synchronized (_lock) {
            if (firstTime) {
                getData().put("data.periodic.scheduler", new ArrayList());
                firstTime = false;
            }
            if (data != null) {
                List dataList = (List) getData().get("data.periodic.scheduler");
                dataList.add(data);
            }
        }
    }


}
