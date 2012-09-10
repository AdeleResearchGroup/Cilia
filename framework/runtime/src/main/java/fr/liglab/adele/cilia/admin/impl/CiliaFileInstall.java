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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.fileinstall.ArtifactInstaller;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.util.CiliaFileManager;

/**
 * This class will listen all deployed files using fileinstall,
 * and tt will create cialia mediation chain.
 * Some classes and code in this file are based in iPOJO/Apache Extender. 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class CiliaFileInstall implements ArtifactInstaller {

	/**
	 * OSGi Bundle Context.
	 */
	BundleContext bcontext;
	/**
	 * The Cilia logger.
	 */
	protected static Logger logger= LoggerFactory.getLogger("cilia.framework.deployer");

	private CiliaFileManager manager;

	private Set<File> handledFiles = new HashSet<File>();


	public CiliaFileInstall (BundleContext context) {
		bcontext = context;
	}

	public void start(){
	}

	public void stop() {
		Set<File> files = handledFiles;
		File filesArray[];
		filesArray = (File[])files.toArray(new File[files.size()]);
		for (int i = 0; i < filesArray.length; i++) {
			manager.unloadChain(filesArray[i]);
		}
	}

	public boolean canHandle(File file) {
		if (file.getName().endsWith(".dscilia") || file.getName().endsWith(".extcilia")){//For instance it use the same parser
			return true;
		}
		return false;
	}

	public void install(File arg0) throws Exception {
		manager.loadChain(arg0);

	}
	public void uninstall(File arg0) throws Exception {
		manager.unloadChain(arg0);
	}
	public void update(File arg0) throws Exception {
		manager.unloadChain(arg0);
		manager.loadChain(arg0);
	}

}
