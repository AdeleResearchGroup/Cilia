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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the abstract collector.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public abstract class AbstractCollector implements ICollector {

    private String dataSource;
    /**
     * Cilia Scheduler to notify when collecting data.
     */
    IScheduler m_scheduler;
    /**
     * Set the scheduler to call when collect data.
     *
     * @param scheduler Cilia Scheduler.
     */
    private static Logger log = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

    public void setScheduler(IScheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * Notify to the Scheduler when data has arrive.
     *
     * @param data Data collected.
     */
    public Data notifyDataArrival(Data data) {
        if (dataSource != null && data != null) {
            data.setLastReceivingPort(dataSource);
        }
        if (m_scheduler != null) {
            m_scheduler.notifyData(data);
        } else {
            log.error("Unable to notify data arriva, Scheduler is not available");
        }
        return data;
    }

    /**
     * Add source name to the collector instance.
     *
     * @param sn
     */
    public void setSourceName(String sn) {
        this.dataSource = sn;
    }

}
