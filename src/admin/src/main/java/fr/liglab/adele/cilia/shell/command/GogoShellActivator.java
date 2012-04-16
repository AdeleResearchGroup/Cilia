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
package fr.liglab.adele.cilia.shell.command;

import java.util.Properties;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.ChainListener;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.model.AdapterImpl;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.MediatorImpl;
import fr.liglab.adele.cilia.model.PatternType;

/**
 * GogoShellActivator: Creates the adapter instance and bind it to the cilia-admin
 * chain.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class GogoShellActivator implements ChainListener {

    /**
     * The Cilia context
     * 
     * @Injected
     */
    private CiliaContext ccontext;

    /**
     * The Felix Gogo shell adapter.
     */
    private Adapter adapter;

    /**
     * The Cilia-Admin chain.
     */
    private Chain mchain;

    /**
     * Initialize the shell activator.
     */
    private void start() {
        // Start listening to the admin chain
        ccontext.addChainListener("admin-chain", this);
    }

    /**
     * Stop the shell activator.
     */
    private void stop() {
        if (mchain != null) {
            // Remove the adapter listening to the admin chain
            mchain.removeAdapter(adapter.getId());
        }
        // Stop listening to the admin chain
        ccontext.removeChainListener("admin-chain", this);
    }

    /**
     * When the chain is added, it add the adapter to it.
     * 
     * @param chain
     *            .
     */
    public void onAddingChain(Chain chain) {
        // Now bound to the Cilia-Admin chain
        mchain = chain;
        Properties config = new Properties();
        // Create the Felix Gogo shell adapter and add it in the chain
        adapter = new AdapterImpl("gogo-command-adapter", "felix-admin-gogo-shell",
                "fr.liglab.adele.cilia", config, PatternType.IN_ONLY);
        mchain.add(adapter);
        Mediator mep = mchain.getMediator("admin-entry-mediator");
        if (mep != null) {
            mchain.bind(adapter.getOutPort("std"), mep.getInPort("std"));
        }
    }

    /**
     * Nothing to do.
     */
    public void onRemovingChain(Chain chain) {
        // Now unbound from the Cilia-Admin chain
        adapter = null;
        mchain = null;
    }

}
