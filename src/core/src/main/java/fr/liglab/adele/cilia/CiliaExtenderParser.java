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

import fr.liglab.adele.cilia.exceptions.CiliaParserException;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public interface CiliaExtenderParser {
	/**
	 * Creates or modifies a ComponentImpl based on the component description.
	 * @param componentDescription
	 * @return
	 */
	Component getComponent(Object componentDescription, Component currentComponent) throws CiliaParserException; 

	/**
	 * See if the parser could handle the component description
	 * @return
	 */
	boolean canHandle(Object mediatorDescription);
	
	
}

