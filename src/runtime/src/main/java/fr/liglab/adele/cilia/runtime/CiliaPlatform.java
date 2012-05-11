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

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Application;
import fr.liglab.adele.cilia.Platform;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.builder.impl.BuilderImpl;
import fr.liglab.adele.cilia.internals.CiliaContainerImpl;
import fr.liglab.adele.cilia.model.ChainRuntime;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class CiliaPlatform implements Platform {

	BundleContext bcontext;
	
	CiliaContainerImpl container = null;
	
	public CiliaPlatform(BundleContext bc) {
		bcontext = bc;
		container = new CiliaContainerImpl(bcontext);
	}
	
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.Platform#getVersion()
	 */
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.Platform#getBuilder()
	 */
	public Builder getBuilder(){
		return new BuilderImpl(container);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.Platform#getApplication()
	 */
	public Application getApplication() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.Platform#getChainRuntime(java.lang.String)
	 */
	public ChainRuntime getChainRuntime(String chainId) {
		// TODO Auto-generated method stub
		return null;
	}

}
