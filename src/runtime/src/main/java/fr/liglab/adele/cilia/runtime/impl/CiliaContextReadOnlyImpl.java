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

package fr.liglab.adele.cilia.runtime.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fr.liglab.adele.cilia.ChainListener;
import fr.liglab.adele.cilia.ChainReadOnly;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.CiliaContextReadOnly;
import fr.liglab.adele.cilia.model.Chain;

public class CiliaContextReadOnlyImpl implements CiliaContextReadOnly {

	/* this reference will be injected by ipojo */
	CiliaContext ciliaContext;

	public String getCiliaVersion() {
		return ciliaContext.getCiliaVersion();
	}

	public ChainReadOnly getChain(String chainId) {
		Chain chain = ciliaContext.getChain(chainId);
		return new ChainReadOnlyImpl(chain);
	}

	public Set getAllChains() {
		Set allChains, allChainsRO = null;

		allChains = ciliaContext.getAllChains();
		if (allChains != null) {
			allChainsRO = new HashSet(allChains.size());
			Iterator it = allChains.iterator();

			while (it.hasNext()) {
				Chain chaine = (Chain) it.next();
				if (!chaine.getId().equals("admin-chain"))
					allChainsRO.add(getChain(chaine.getId()));
			}
		}
		return allChainsRO;
	}

	public void addChainListener(String chainId, ChainListener listener) {
		ciliaContext.addChainListener(chainId, listener);
	}

	public void removeChainListener(String chainId, ChainListener listener) {
		ciliaContext.removeChainListener(chainId, listener);

	}

	public void start() {
	}

	public void stop() {
	}

}
