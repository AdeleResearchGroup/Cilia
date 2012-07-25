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
package fr.liglab.adele.cilia.administration.adapter;

import java.util.Iterator;
import java.util.Set;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.administration.util.CiliaInstructionConverter;
import fr.liglab.adele.cilia.framework.AbstractCollector;
/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class CiliaInstructionsAdapter extends AbstractCollector{
	
	/**
	 * 
	 */
	CiliaInstructionConverter cic = new CiliaInstructionConverter();
	
	/**
	 * 
	 * @param instructions
	 */
	public void performInstructions(Set instructions) {
		Iterator it = instructions.iterator();
		while (it != null && it.hasNext()) {
			String instruction = (String)it.next();
			Data ndata = cic.getDataFromInstruction(instruction);
			super.notifyDataArrival(ndata);
		}
	}
}
