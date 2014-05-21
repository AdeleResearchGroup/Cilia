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
/**
 *
 */
package fr.liglab.adele.cilia.helper.impl;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractScheduler;
import fr.liglab.adele.cilia.helper.SchedulerProcessorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class SchedulerProcessorHelperImpl extends AbstractScheduler implements SchedulerProcessorHelper {

    List datal = new ArrayList();

    public void trigger() {
        List datatp = new ArrayList(datal);
        datal.clear();
        process(datatp);
    }

    @Override
    public void notifyData(Data data) {
        datal.add(data);
    }

}
