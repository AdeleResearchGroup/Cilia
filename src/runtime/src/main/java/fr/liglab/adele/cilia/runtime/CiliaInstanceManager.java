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

import java.util.Dictionary;
import java.util.Observer;
import java.util.Set;



public interface CiliaInstanceManager extends Observer {
    /**
     * Obtain the pojo reference contained in a CiliaInstance
     * @param key to search the pojo
     * @return
     */
    Object getPojo(String key);
    
    Set getKeys();
    /**
     * Add a CiliaInstance 
     * @param key 
     * @param _obj
     */
    void addInstance(String key, Object _obj);
    /**
     * Remove all the instances.
     */
    void removeAllInstances();
    /**
     * check tha availability of all components used.
     */
    boolean checkAvailability();

    /**
     * Reconfigure all instances. 
     * @param props
     */
    void reconfigurePOJOS(Dictionary props);

    void removeInstance(String group, String instanceName);

    void startInstances();
    
}
