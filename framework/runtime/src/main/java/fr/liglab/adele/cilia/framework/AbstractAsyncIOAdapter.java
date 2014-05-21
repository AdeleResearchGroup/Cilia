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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class AbstractAsyncIOAdapter {

    protected List<Data> currentData = new ArrayList<Data>();

    private volatile boolean hasData = false;


    /**
     * @param data
     */
    public synchronized void receiveData(Data data) {
        currentData.add(data);
        hasData = true;
    }

    public Data dispatchData(Data data) {
        return data;
    }

    public synchronized boolean hasData() {
        return hasData;
    }

    public synchronized List<Data> getData() {
        List<Data> ndata = null;
        synchronized (this) {
            hasData = false;
            ndata = new ArrayList<Data>(currentData);
            currentData.clear();
        }
        return ndata;
    }

    public synchronized int messageCount() {
        if (hasData()) {
            return currentData.size();
        }
        return 0;
    }
}
