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
import fr.liglab.adele.cilia.framework.AbstractPullCollector;

import java.io.IOException;
import java.util.*;

public class RandomNumberCollector extends AbstractPullCollector {

    private final Random random = new Random();

    @Override
    protected List<Data> pullData() throws IOException {
        List<Data> list = new ArrayList<Data>(1);
        long number = random.nextInt() % 100;
        Dictionary<String, Object> metadata = new Hashtable<String, Object>();
        Data data = new Data(new Long(number), "random_number", metadata);
        list.add(data);
        return list;
    }

    public void delay(long iDelay) {
        super.delay(iDelay);
    }

    public void period(long lperiod) {
        super.period(lperiod);
    }

    public void start() {
        super.start();
    }

    public void stop() {
        super.stop();
    }

}
