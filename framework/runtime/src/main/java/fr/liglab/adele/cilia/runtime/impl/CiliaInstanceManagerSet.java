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

package fr.liglab.adele.cilia.runtime.impl;

import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.util.Const;
import org.apache.felix.ipojo.ComponentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * This class is in charge to handle several CiliaInstance. It is used to
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class CiliaInstanceManagerSet implements Observer {


    private Map /*<String,List<CiliaInstance>>*/instances;

    private Boolean generalState = null;

    protected final Object lockObject = new Object();

    protected Logger logger = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);

    public CiliaInstanceManagerSet() {
        instances = Collections.synchronizedMap(new HashMap());
    }

    protected void addInstance(String key, Object _obj) {
        List instancesList = null;
        synchronized (lockObject) {
            if (instances.containsKey(key)) {
                instancesList = (List) instances.get(key);
            } else {
                instancesList = new ArrayList();
                instances.put(key, instancesList);
            }
            CiliaInstanceWrapper aci = (CiliaInstanceWrapper) _obj;
            aci.addObserver(this);
            instancesList.add(aci);
        }
    }

    public boolean checkAvailability() {
        boolean valid = true;
        Integer state;
        CiliaInstance component = null;
        synchronized (lockObject) {
            Iterator it = instances.keySet().iterator();
            while (it.hasNext()) {
                List instanceList = (List) instances.get(it.next());
                Iterator componentsInstances = instanceList.iterator();
                while (componentsInstances.hasNext()) {
                    component = (CiliaInstance) componentsInstances.next();
                    if (component.getState() != ComponentInstance.VALID) {
                        valid = false;
                    }
                }
            }
        }
        if (valid) {
            state = ComponentInstance.VALID;
        } else {
            state = ComponentInstance.INVALID;
        }
        generalState = Boolean.valueOf(valid);


        return valid;
    }

    public Set getKeys() {
        Set keys = null;
        synchronized (lockObject) {
            keys = new HashSet(instances.keySet());
        }
        return keys;
    }

    public Object getPojo(String key) {
        List _ciliaInstance = null;
        synchronized (lockObject) {
            _ciliaInstance = (List) instances.get(key);
        }
        return _ciliaInstance;
    }

    public void reconfigurePOJOS(Dictionary props) {
        synchronized (lockObject) {
            Iterator it = instances.keySet().iterator();
            while (it.hasNext()) {
                List instanceList = (List) instances.get(it.next());
                Iterator componentsInstances = instanceList.iterator();
                while (componentsInstances.hasNext()) {
                    CiliaInstance component = (CiliaInstance) componentsInstances.next();
                    if (component != null) {
                        component.updateInstanceProperties(props);
                    }
                }
            }
        }
    }

    public void removeAllInstances() {
        synchronized (lockObject) {
            Iterator it = instances.keySet().iterator();
            while (it.hasNext()) {
                List instanceList = (List) instances.get(it.next());
                Iterator componentsInstances = instanceList.iterator();
                while (componentsInstances.hasNext()) {
                    CiliaInstance component = (CiliaInstance) componentsInstances.next();
                    component.stop();
                }
                instanceList.clear();
            }
            instances.clear();
        }
    }

    public boolean removeInstance(String portname, String instanceName) {
        List instanceList = null;
        boolean removed = false;
        synchronized (lockObject) {
            if (portname != null) {
                instanceList = (List) instances.get(portname);
            }
        }
        if (instanceList != null) {
            Iterator componentsInstances = instanceList.iterator();
            while (componentsInstances.hasNext()) {
                CiliaInstance component = (CiliaInstance) componentsInstances.next();
                if (component.getName().compareTo(instanceName) == 0) {
                    component.stop();
                    componentsInstances.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }


    public void startInstances() {
        synchronized (lockObject) {
            Iterator it = instances.keySet().iterator();
            while (it.hasNext()) {
                List instanceList = (List) instances.get(it.next());
                Iterator componentsInstances = instanceList.iterator();
                while (componentsInstances.hasNext()) {
                    CiliaInstance component = (CiliaInstance) componentsInstances.next();
                    component.start();
                }
            }
        }
    }


    public void update(Observable o, Object arg) {
        checkAvailability();
    }
}
