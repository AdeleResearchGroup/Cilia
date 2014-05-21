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

package fr.liglab.adele.cilia.runtime;

import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.Node;

/**
 * Privates constants and static methods used for runtime (dynamic and
 * application)
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public interface FirerEvents {

    public static final int EVT_NONE = 0;
    public static final int EVT_ARRIVAL = 1;
    public static final int EVT_DEPARTURE = 2;
    public static final int EVT_MODIFIED = 3;
    public static final int EVT_STARTED = 4;
    public static final int EVT_STOPPED = 5;
    public static final int EVT_BIND = 6;
    public static final int EVT_UNBIND = 7;
    public static final int EVT_VALID = 8;
    public static final int EVT_INVALID = 9;

    void fireEventNode(int event, Node node);

    void fireEventNode(int event, Node from, Node to);

    void fireEventChain(int evt, String chainId);

    void fireEventMeasure(Node node, String variableId, Measure m);

    void fireThresholdEvent(Node node, String variableId, Measure measure, int evt);

    void fireEventVariableStatus(Node node, String variableId, boolean enable);
}
