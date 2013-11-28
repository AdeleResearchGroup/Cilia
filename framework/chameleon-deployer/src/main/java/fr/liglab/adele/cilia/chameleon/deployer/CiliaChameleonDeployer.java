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

package fr.liglab.adele.cilia.chameleon.deployer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.util.CiliaFileManager;
import org.ow2.chameleon.core.services.AbstractDeployer;

/**
 * This class will listen all deployed files using fileinstall,
 * and tt will create cialia mediation chain.
 * Some classes and code in this file are based in iPOJO/Apache Extender. 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
@Component
@Provides
@Instantiate
public class CiliaChameleonDeployer extends AbstractDeployer {

	/**
	 * OSGi Bundle Context.
	 */
	BundleContext bcontext;
	/**
	 * The Cilia logger.
	 */

    @Requires
	private CiliaFileManager manager;

	private Set<File> handledFiles = new HashSet<File>();


	public CiliaChameleonDeployer(BundleContext context) {
		bcontext = context;
	}

    @Validate
	public void start(){
	}

    @Invalidate
	public void stop() {
		Set<File> files = handledFiles;
		File filesArray[];
		filesArray = (File[])files.toArray(new File[files.size()]);
		for (int i = 0; i < filesArray.length; i++) {
			manager.unloadChain(filesArray[i]);
		}
	}


	private void update(File arg0) throws Exception {
		manager.unloadChain(arg0);
		manager.loadChain(arg0);
	}

    public boolean accept(File file) {
        if (file.getName().endsWith(".dscilia") || file.getName().endsWith(".cfgcilia") || file.getName().endsWith(".autocilia")){//For instance it use the same parser
            return true;
        }
        return false;
    }

    @Override
    public void onFileCreate(File file) {
        manager.loadChain(file);
    }

    @Override
    public void onFileChange(File file) {
        try {
            update(file);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void onFileDelete(File file) {
        manager.unloadChain(file);
    }

}
