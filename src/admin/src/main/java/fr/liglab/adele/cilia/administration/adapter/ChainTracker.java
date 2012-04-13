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

import java.util.HashSet;
import java.util.Set;

import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.ChainListener;
import fr.liglab.adele.cilia.model.ChainImpl;
/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class ChainTracker implements ChainListener {
	/**
	 * List of instructions to perform when chain is present.
	 */
	Set addingInstructions = new HashSet();
	/**
	 * List of instructions to perform when the chain is not longer present.
	 */
	Set removingInstructions = new HashSet();
	/**
	 * AdapterImpl used to perform instructions.
	 */
	CiliaInstructionsAdapter adapter ;
	/**
	 * 
	 * @param adding
	 * @param removing
	 * @param ad
	 */
	public ChainTracker (CiliaInstructionsAdapter ad, Set adding, Set removing) {
		addingInstructions = adding;
		removingInstructions = removing;
		adapter = ad;
	}
	/**
	 * 
	 */
	public void onAddingChain(Chain chain) {
		if (addingInstructions != null) {
			adapter.performInstructions(addingInstructions);
		}
	}
	/**
	 * 
	 */
	public void onRemovingChain(Chain chain) {
		if (removingInstructions != null){
			adapter.performInstructions(removingInstructions);
		}
	}

}
