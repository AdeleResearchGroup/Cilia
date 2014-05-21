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
import fr.liglab.adele.cilia.exceptions.CiliaException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public abstract class AbstractPullCollector extends AbstractCollector implements Runnable {

    /**
     * Specifies the initial delay of the poll execution.
     */
    private long initialDelay;
    /**
     * Specifies the period in miliseconds betwen each poll execution.
     */
    private long period;

    private Thread thread;

    private volatile boolean running;

    public void delay(long iDelay) {
        initialDelay = iDelay;
    }

    public void period(long lperiod) {
        period = lperiod;
    }


    public void start() {
        thread = new Thread(this);
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }

    protected abstract List/*<Data>*/ pullData() throws IOException;

    public void run() {
        while (running) {
            try {
                Thread.sleep(period);
                List ldata = pullData();
                for (int i = 0; ldata != null && i < ldata.size(); i++) {
                    super.notifyDataArrival((Data) ldata.get(i));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new CiliaException("Error when pulling data in collector").printStackTrace();
            }
        }
    }


}
