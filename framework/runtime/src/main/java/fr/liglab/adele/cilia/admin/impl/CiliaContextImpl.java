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
package fr.liglab.adele.cilia.admin.impl;

import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.builder.impl.BuilderImpl;
import fr.liglab.adele.cilia.internals.CiliaContainerImpl;
import fr.liglab.adele.cilia.knowledge.EventsManagerImpl;
import fr.liglab.adele.cilia.knowledge.KEngineImpl;
import org.osgi.framework.BundleContext;

import java.util.Date;

/**
 * Main Cilia Service implementation. It contains methods to retrieve information of mediation
 * applications and also to modify Chains. It allows to modify mediation chains
 * by using a retrieved builder and to inspect applications in both: Structural
 * information and Executing Information.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaContextImpl implements CiliaContext {

    private BundleContext bcontext = null;

    private CiliaContainerImpl container = null;

    private final static String version = "2.0.1";

    private final static Date startup = new Date(System.currentTimeMillis());

    private final EventsManagerImpl eventManager;
    private final KEngineImpl KEngine;

    public CiliaContextImpl(BundleContext bc) {
        bcontext = bc;
        /* Fire events related to operating modes level chain / mediator / bindings */
        eventManager = new EventsManagerImpl(bc);
        container = new CiliaContainerImpl(bcontext, eventManager);
        /* provides configuration interfaces for building the knowledge base */
        KEngine = new KEngineImpl(bc, container, eventManager);
    }

    private void start() {
        eventManager.start();
        container.start();
        KEngine.start();
    }


    private void stop() {
        eventManager.stop();
        container.stop();
        KEngine.stop();
    }

    /**
     * Get the version of the executing Cilia.
     *
     * @return the version as an String.
     */
    public String getVersion() {
        return version;
    }

    public Date getDateStartUp() {
        return startup;
    }

    public Builder getBuilder() {
        return new BuilderImpl(this, container);
    }


    /**
     * Retrieve the ApplicationRuntime instance which allows to inspect the
     * runtime information of mediation chains.
     *
     * @return the ApplicationSpecification instance.
     */
    public ApplicationRuntime getApplicationRuntime() {
        return KEngine;
    }

}
