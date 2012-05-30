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
package fr.liglab.adele.cilia;

import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.model.impl.ChainRuntime;

/**
 * Main Cilia Service. It contains methods to retrieve information of mediation
 * applications and also to modify Chains. It allows to modify mediation chains
 * by using a retrieved builder and to inspect applications in both: Structural
 * information and Executing Information.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface CiliaContext {
	/**
	 * Get the version of the executing Cilia.
	 * 
	 * @return the version as an String.
	 */
	String getVersion();

	/**
	 * Retrieve a builder instance to modify/create a mediation chain and its
	 * components. This method always return a new Builder object. And this
	 * objects have an internal state.
	 * 
	 * @return the new Builder object.
	 */
	Builder getBuilder();
	
	/**
	 * Retrieve the ApplicationSpecification instance which allows to inspect the structure
	 * of mediation chains and its properties. 
	 * @return the ApplicationSpecification instance.
	 */
	ApplicationSpecification getApplicationSpecification();
	
	/**
	 * Retrieve the ApplicationRuntime instance which allows to inspect the runtime information
	 * of mediation chains. 
	 * @return the ApplicationSpecification instance.
	 * */
	ApplicationRuntime getApplicationRuntime();

}
