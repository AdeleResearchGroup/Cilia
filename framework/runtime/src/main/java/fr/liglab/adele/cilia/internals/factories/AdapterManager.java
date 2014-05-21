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
package fr.liglab.adele.cilia.internals.factories;

import org.apache.felix.ipojo.HandlerManager;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class AdapterManager extends MediatorComponentManager {

    /**
     * @param factory
     * @param context
     * @param handlers
     */
    public AdapterManager(MediatorComponentFactory factory, BundleContext context,
                          HandlerManager[] handlers) {
        super(factory, context, handlers);
    }

    public void stop() {
        super.stop();
        super.stopManagers();
    }

    public void start() {
        super.startManagers();
        super.start();
    }
}
