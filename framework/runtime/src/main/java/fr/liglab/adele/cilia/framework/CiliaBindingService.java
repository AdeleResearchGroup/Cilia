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

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Component;

import java.util.Dictionary;


public interface CiliaBindingService {

    public static String CILIA_COLLECTOR_PROPERTIES = "cilia.collector.properties";
    public static String CILIA_SENDER_PROPERTIES = "cilia.sender.properties";

    /**
     * unasigned
     */
    short NATURE_UNASSIGNED = 0;
    /**
     * receive-only
     */
    short NATURE_IN = 1;
    /**
     * send-only
     */
    short NATURE_OUT = 2;
    /**
     * Send-receive
     */
    short NATURE_INOUT = 3;


    public Component getCollectorModel(Dictionary props);

    public Component getSenderModel(Dictionary props);

    public void setSenderType(String senderType);

    public void setCollectorType(String collectorType);

    public void setSenderNS(String senderNS);

    public void setCollectorNS(String collectorNS);

    public Dictionary getProperties(Dictionary collectorProperties, Dictionary senderProperties, Binding binding) throws CiliaException;

    public void setBindingNature(String nature);

    public short getBindingNature();
}
