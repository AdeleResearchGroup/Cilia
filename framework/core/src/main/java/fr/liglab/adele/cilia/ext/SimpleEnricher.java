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
package fr.liglab.adele.cilia.ext;

import fr.liglab.adele.cilia.model.Mediator;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class SimpleEnricher {

    private Mediator mediator;

    private Object lockObject = new Object();

    private LocalConfiguration currentConfiguration;

    private String configName;

    public SimpleEnricher(Mediator mediator, String name) {
        this.mediator = mediator;
        configName = name;
    }

    public SimpleEnricher(Mediator mediator) {
        this(mediator, "content");
    }


    public SimpleEnricher key(String key) {
        synchronized (lockObject) {
            if (currentConfiguration == null) {
                currentConfiguration = new LocalConfiguration();
            }
            currentConfiguration.setKey(key);
        }
        return done();
    }

    public SimpleEnricher value(String value) {
        synchronized (lockObject) {
            if (currentConfiguration == null) {
                currentConfiguration = new LocalConfiguration();
            }
            currentConfiguration.setValue(value);
        }
        return done();
    }

    private SimpleEnricher done() {
        Dictionary configurations = null;
        boolean modified = false;
        synchronized (lockObject) {
            configurations = configurations();
            if (currentConfiguration != null && currentConfiguration.getKey() != null && currentConfiguration.getValue() != null) {
                configurations.put(currentConfiguration.getKey(), currentConfiguration.getValue());
                modified = true;
            }
            if (modified) {
                currentConfiguration = null;
            }
        }
        if (modified) { // it is tested outside the sync block.
            mediator.setProperty(configName, configurations);
        }
        return this;
    }

    public Dictionary configurations() {
        Dictionary configurations = null;
        synchronized (lockObject) {
            Object prop = mediator.getProperty(configName);
            if (prop != null && prop instanceof Dictionary) {
                configurations = (Dictionary) prop;
            } else {
                configurations = new Hashtable();
            }
        }
        return configurations;
    }

    private class LocalConfiguration {
        private String key;

        private Object value;

        /**
         * @param key the key to set
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * @param value the value to set
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public Object getValue() {
            return value;
        }
    }


}
