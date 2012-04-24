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

import java.util.Set;

import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.model.ChainRuntime;
import fr.liglab.adele.cilia.specification.MediatorSpecification;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;

/**
 * CiliaAdmin: the Control interface of all the mediators within the Gateway
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
//@SuppressWarnings("rawtypes")
public interface CiliaContext {

	static final String CILIA_VERSION = "1.0.0";
		
	String getCiliaVersion();

	Chain addChain(Chain chain) ;
	
	Chain getChain(String chainId) ;
	
	Set getAllChains() ;
	
	void startChain(Chain chain) ;
	void startChain(String chainId);
	void stopChain(Chain chain) ;
	void stopChain(String chainId);
	void removeChain(Chain chain) ;
	void removeChain(String chainId);
	
	void addChainListener(String chainId, ChainListener listener);
	
	void removeChainListener(String chainId, ChainListener listener);
	
	
	void start();
	
	void stop();
	
	MediatorSpecification createMediatorSpecification(String name, String namespace, String category);
	
	/* Mutual exclusion access on ciliaContext  */
	ReadWriteLock getMutex();
	
	/*  Runtime informations , level chain */ 
	public ChainRuntime getChainRuntime(String chainId) ;
	
}
