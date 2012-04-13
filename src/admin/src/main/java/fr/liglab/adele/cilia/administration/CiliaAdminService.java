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
package fr.liglab.adele.cilia.administration;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.model.AdapterImpl;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.MediatorImpl;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface CiliaAdminService {

	// Show
	public Chain getChain(String chainid);

	public Mediator getMediator(String chainId, String mediatorId);

	public Adapter getAdapter(String chainId, String adapterId);

	// Create
	public void createEmptyChain(String chainId);

	public void createMediator(String chainId, String mediatorType, String mediatorId);

	public void createAdapter(String chainId, String adapterType, String adapterId);

	public void createBinding(String chainId, String from, String to);

	// Start
	public void startChain(String id);

	// Stop

	public void stopChain(String id);

	// Modify
	// chain id=chainid property=propertyName value=propertyvalue
	// type=[primitive|Array|Map]
	public void chainProperty(String chainId, String propname, String value, String type);

	public void mediatorProperty(String chainId, String mediatorId, String propname,
			String value, String type);

	public void adapterProperty(String chainId, String adapterId, String propname,
			String value, String type);

	public void bindingProperty(String chainId, String from, String to, String propname,
			String value, String type);

	// Remove
	public void removeChain(String chainId);

	public void removeMediator(String chainId, String mediatorId);

	public void removeAdapter(String chainId, String adapterId);

	public void removeBinding(String chainID, String from, String to);

	// Replace
	public void replaceMediator(String chainId, String mediatorSource, String mediatorDest);

	public void replaceAdapter(String chainId, String adapterSource, String adapterDest);
	
	// Copy 
	public void copyMediator(String chainId, String mediatorSource, String mediatorDest);

	public void copyAdapter(String chainId, String adapterSource, String adapterDest);
	
	//generic command
	public void execute(String line);

}
