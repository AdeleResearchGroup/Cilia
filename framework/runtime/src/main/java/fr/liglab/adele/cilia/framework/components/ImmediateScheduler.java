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
package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractScheduler;
import fr.liglab.adele.cilia.framework.IScheduler;
import fr.liglab.adele.cilia.runtime.WorkQueue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class ImmediateScheduler extends AbstractScheduler {

    IScheduler scheduler;
    public String testAudit;
    public WorkQueue wq;
    protected Map dataMap = new HashMap();

    private int threadPoolSize;
    LinkedBlockingDeque<Runnable> workingQueue = new LinkedBlockingDeque<Runnable>();
    private ExecutorService executor;

    public void setConnectedScheduler(IScheduler sched) {
        scheduler = sched;
    }

    public void notifyData(Data data) {
        executor.submit(new ProcessorExecutor(data));
    }

    public void process(List dataSet) {
        if (scheduler == null) {
            appLogger.error("Unable to process data, Scheduler reference is not valid.");
            return;
        }
        testAudit = " testScheduler" + System.currentTimeMillis();
        scheduler.process(dataSet);
    }

    public List getSourcesIds() {
        return scheduler.getSourcesIds();
    }

    public void fireEvent(Map map) {
        appLogger.info("fireEvent " + map);
        if (scheduler != null)
            scheduler.fireEvent(map);
    }

    public Map getData() {
        return scheduler.getData();
    }

    public void validate() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        executor = new ThreadPoolExecutor(threadPoolSize,
                threadPoolSize * 2, 5, TimeUnit.SECONDS, workingQueue);
    }

    public void invalidate() {
        try {
            executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ProcessorExecutor implements Runnable {

        private final Data data;

        private ProcessorExecutor(final Data data) {
            this.data = data;
        }

        public void run() {
            appLogger.debug("WorkingQueue will execute the mediation processing");
            process(Collections.singletonList(data));
        }
    }

}
