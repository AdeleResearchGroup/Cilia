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

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.administration.CiliaAdminService;
import fr.liglab.adele.cilia.administration.util.CiliaInstructionConverter;
import fr.liglab.adele.cilia.administration.util.ParserUtils;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.Mediator;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class CiliaAdminServiceImpl extends AbstractCollector implements CiliaAdminService  {

	CiliaContext ccontext;
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#getChain(java.lang.String)
	 */
	public Chain getChain(String chainid) {
		return ccontext.getChain(chainid);

	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#getMediator(java.lang.String, java.lang.String)
	 */
	public Mediator getMediator(String chainId, String mediatorId) {
		Chain ch = ccontext.getChain(chainId);
		if (ch != null)
			return ch.getMediator(mediatorId);
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#getAdapter(java.lang.String, java.lang.String)
	 */
	public Adapter getAdapter(String chainId, String adapterId) {
		Chain ch = ccontext.getChain(chainId);
		if (ch != null)
			return ch.getAdapter(adapterId);
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#createEmptyChain(java.lang.String)
	 */
	public void createEmptyChain(String chainId) {
		ccontext.addChain(new ChainImpl(chainId, null, null, null));
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#createMediator(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void createMediator(String chainId, String mediatorType,
			String mediatorId) {
		Data data = new Data("create");
		data.setProperty("element", "mediator");
		data.setProperty("chain",  chainId);
		data.setProperty("type", mediatorType);
		data.setProperty("id", mediatorId);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#createAdapter(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void createAdapter(String chainId, String adapterType,
			String adapterId) {
		Data data = new Data("create");
		data.setProperty("element", "adapter");
		data.setProperty("chain",  chainId);
		data.setProperty("type", adapterType);
		data.setProperty("id", adapterId);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#createBinding(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void createBinding(String chainId, String from, String to) {
		Data data = new Data("create");
		data.setProperty("element", "binding");
		data.setProperty("chain", chainId);
		data.setProperty("from", from);
		data.setProperty("to", to);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#startChain(java.lang.String)
	 */
	public void startChain(String id) {
		Data data = new Data("start");
		data.setProperty("element", "chain");
		data.setProperty("id", id);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#stopChain(java.lang.String)
	 */
	public void stopChain(String id) {
		Data data = new Data("stop");
		data.setProperty("element", "chain");
		data.setProperty("id", id);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#chainProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void chainProperty(String chainId, String propname, String value,
			String type) {
		System.out.println("NOT IMPLEMENTED!!");
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#mediatorProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void mediatorProperty(String chainId, String mediatorId,
			String propname, String value, String type) {
		Data data = new Data("modify");
		data.setProperty("element", "mediator");
		data.setProperty("chain", chainId);
		data.setProperty("id", mediatorId);
		data.setProperty("name", propname);
		Object obvalue = ParserUtils.getProperty(value, type);
		data.setProperty("value", obvalue);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#adapterProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void adapterProperty(String chainId, String adapterId,
			String propname, String value, String type) {
		Data data = new Data("modify");
		data.setProperty("element", "adapter");
		data.setProperty("chain", chainId);
		data.setProperty("id", adapterId);
		data.setProperty("name", propname);
		Object obvalue = ParserUtils.getProperty(value, type);
		data.setProperty("value", obvalue);
		super.notifyDataArrival(data);

	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#bindingProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void bindingProperty(String chainId, String from, String to,
			String propname, String value, String type) {
		System.out.println("NOT IMPLEMENTED!!");
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#removeChain(java.lang.String)
	 */
	public void removeChain(String chainId) {
		Data data = new Data("remove");
		data.setProperty("element", "chain");
		data.setProperty("id", chainId);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#removeMediator(java.lang.String, java.lang.String)
	 */
	public void removeMediator(String chainId, String mediatorId) {
		Data data = new Data("remove");
		data.setProperty("element", "mediator");
		data.setProperty("chain", chainId);
		data.setProperty("id", mediatorId);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#removeAdapter(java.lang.String, java.lang.String)
	 */
	public void removeAdapter(String chainId, String adapterId) {
		Data data = new Data("remove");
		data.setProperty("element", "adapter");
		data.setProperty("chain", chainId);
		data.setProperty("id", adapterId);
		super.notifyDataArrival(data);
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.administration.CiliaAdminService#removeBinding(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void removeBinding(String chainID, String from, String to) {
		Data data = new Data("remove");
		data.setProperty("element", "binding");
		data.setProperty("chain", chainID);
		data.setProperty("from", from);
		data.setProperty("to", to);
		super.notifyDataArrival(data);
	}

	public void replaceMediator(String chainID, String mediatorSource, String mediatorDest) {
		Data data = new Data("replace");
		data.setProperty("element", "mediator");
		data.setProperty("chain", chainID);
		data.setProperty("id", mediatorSource);
		data.setProperty("by", mediatorDest);
		super.notifyDataArrival(data);		
	}
	
	public void replaceAdapter(String chainID, String adapterSource, String adapterDest) {
		Data data = new Data("replace");
		data.setProperty("element", "adapter");
		data.setProperty("chain", chainID);
		data.setProperty("id", adapterSource);
		data.setProperty("by",adapterDest);
		super.notifyDataArrival(data);		
	}
	
	public void copyMediator(String chainID, String mediatorSource, String mediatorDest) {
		Data data = new Data("copy");
		data.setProperty("element", "mediator");
		data.setProperty("chain", chainID);
		data.setProperty("from", mediatorSource);
		data.setProperty("to", mediatorDest);
		super.notifyDataArrival(data);		
	}
	
	public void copyAdapter(String chainID, String adapterSource, String adapterDest) {
		Data data = new Data("copy");
		data.setProperty("element", "adapter");
		data.setProperty("chain", chainID);
		data.setProperty("from", adapterSource);
		data.setProperty("to",adapterDest);
		super.notifyDataArrival(data);		
	}

	public void execute(String line){
		CiliaInstructionConverter cic = new CiliaInstructionConverter();
		Data result = cic.getDataFromInstruction(line);
		if (result != null) {
			super.notifyDataArrival(result);
		}
	}
}
