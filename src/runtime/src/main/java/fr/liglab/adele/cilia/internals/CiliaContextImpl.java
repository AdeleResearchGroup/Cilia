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
package fr.liglab.adele.cilia.internals;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.ApplicationSpecification;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.builder.impl.BuilderImpl;
import fr.liglab.adele.cilia.dynamic.ApplicationRuntime;
import fr.liglab.adele.cilia.runtime.application.ApplicationSpecificationImpl;
import fr.liglab.adele.cilia.runtime.dynamic.ApplicationRuntimeImpl;

/**
 * Main Cilia Service implementation. It contains methods to retrieve information of mediation
 * applications and also to modify Chains. It allows to modify mediation chains
 * by using a retrieved builder and to inspect applications in both: Structural
 * information and Executing Information.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class CiliaContextImpl implements CiliaContext {

	private BundleContext bcontext = null;

	private CiliaContainerImpl container = null;

	private ApplicationSpecificationImpl application = null;


	private ApplicationRuntimeImpl dynamicProperties = null;

	private final static String version = "2.0.1";


	public CiliaContextImpl(BundleContext bc) {
		bcontext = bc;
		container = new CiliaContainerImpl(bcontext);
		application = new ApplicationSpecificationImpl(bcontext, container);
		dynamicProperties = new ApplicationRuntimeImpl(bcontext, container);
	}

	private void start() {
		container.start();
		application.start();
		dynamicProperties.start();
	}


	private void stop() {
		container.stop();
		application.stop();
		dynamicProperties.stop();

	}

	/**
	 * Get the version of the executing Cilia.
	 * 
	 * @return the version as an String.
	 */
	public String getVersion() {
		return version;
	}


	public Builder getBuilder() {
		return new BuilderImpl(container);

	}

	/**
	 * Retrieve the ApplicationRuntime instance which allows to inspect the
	 * runtime information of mediation chains.
	 * 
	 * @return the ApplicationSpecification instance.
	 * */
	public ApplicationRuntime getApplicationRuntime() {
		return dynamicProperties;
	}

	/**
	 * Retrieve the ApplicationSpecification instance which allows to inspect
	 * the structure of mediation chains and its properties.
	 * 
	 * @return the ApplicationSpecification instance.
	 */

	public ApplicationSpecification getApplicationSpecification() {
		return application;
	}


}
