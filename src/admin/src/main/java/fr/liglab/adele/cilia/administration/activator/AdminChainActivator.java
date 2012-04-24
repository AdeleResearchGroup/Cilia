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
package fr.liglab.adele.cilia.administration.activator;


import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.ext.ContentBasedRouting;
import fr.liglab.adele.cilia.model.AdapterImpl;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.MediatorImpl;
import fr.liglab.adele.cilia.model.PatternType;
/**
 * AdminChainActivator: Generate and initialize the cilia-admin chan. 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class AdminChainActivator {
	/**
	 * The cilia context, to add a mediation chain and initialize.
	 */
	CiliaContext ccontext;
	/**
	 * The cilia-admin chain.
	 */
	ChainImpl adminChain;
	/**
	 * Method called when initializing the component.
	 * When the component is started, the cilia-admin chain is generated and initialized.
	 */
	protected void start() {
		adminChain = createChain();
		ccontext.addChain(adminChain);
		ccontext.startChain(adminChain);
	}
	/**
	 * when the component is stopped, the chain must be removed.
	 */
	protected void stop() {
		ccontext.removeChain(adminChain.getId());
	}
	/**
	 * Create the cilia-admin chain and its mediators.
	 * @return the cilia-admin chain reference.
	 */
	private ChainImpl createChain(){
		ChainImpl chain = new ChainImpl("admin-chain", "AdministrationChain", "fr.liglab.adele.cilia", null);
		AdapterImpl adapter = getAdapter();
		MediatorImpl entry = getEntryMediator();
		MediatorImpl creator = getCreatorMediator();
		MediatorImpl shower =  getShowMediator();
		MediatorImpl remover = getRemoverMediator();
		MediatorImpl starter = getStarterMediator();
		MediatorImpl stop =    getStopMediator();
		MediatorImpl modifier =    getModifierMediator();
		MediatorImpl loader =    getLoaderMediator();
		MediatorImpl replacer = getReplacerMediator() ;
		MediatorImpl copier = getCopierMediator();
		
		chain.add(adapter);
		chain.add(entry);
		chain.add(creator);
		chain.add(shower);
		chain.add(remover);
		chain.add(starter);
		chain.add(stop);
		chain.add(modifier);
		chain.add(loader);
		chain.add(replacer) ;
		chain.add(copier) ;
		
		chain.bind(adapter.getOutPort("adpsrv"), entry.getInPort("std"));
		chain.bind(entry.getOutPort("create"),creator.getInPort("creatorIn"));
		chain.bind(entry.getOutPort("remove"),remover.getInPort("removerIn"));
		chain.bind(entry.getOutPort("start"),starter.getInPort("starterIn"));
		chain.bind(entry.getOutPort("replace"),replacer.getInPort("replacerIn"));
		chain.bind(entry.getOutPort("copy"),copier.getInPort("copierIn"));
		chain.bind(entry.getOutPort("stop"),stop.getInPort("stopIn"));
		chain.bind(entry.getOutPort("show"),shower.getInPort("showIn"));
		chain.bind(entry.getOutPort("modify"),modifier.getInPort("modifyIn"));
		chain.bind(entry.getOutPort("load"),loader.getInPort("loaderIn"));
		chain.bind(entry.getOutPort("unload"),loader.getInPort("unloaderIn"));
		return chain;
	}

	/**
	 * @return
	 */
	private AdapterImpl getAdapter() {
		// TODO Auto-generated method stub
		return new AdapterImpl("service-adapter", "cilia-admin-service-adapter", "fr.liglab.adele.cilia", null, null, null, PatternType.IN_ONLY);
	}
	private MediatorImpl getEntryMediator() {
		MediatorImpl creator = new MediatorImpl("admin-entry-mediator", "CiliaAdminCBMediator","fr.liglab.adele.cilia.admin",null, null, null, null);
		ContentBasedRouting routing = new ContentBasedRouting(creator);
		routing.evaluator("ldap");
		routing.condition("(data.content=create)").to("create");
		routing.condition("(data.content=remove)").to("remove");
		routing.condition("(data.content=start)").to("start");
		routing.condition("(data.content=stop)").to("stop");
		routing.condition("(data.content=show)").to("show");
		routing.condition("(data.content=modify)").to("modify");
		routing.condition("(data.content=load)").to("load");
		routing.condition("(data.content=unload)").to("unload");
		routing.condition("(data.content=replace)").to("replace");
		routing.condition("(data.content=copy)").to("copy");
		return creator;
	}
	
	private MediatorImpl getCreatorMediator(){
		return new MediatorImpl("admin-creator", "CiliaAdminCreator","fr.liglab.adele.cilia.admin", null, null, null, null);
	}

	private MediatorImpl getRemoverMediator(){
		return new MediatorImpl("admin-remove", "CiliaAdminRemover","fr.liglab.adele.cilia.admin", null, null, null, null);
	}

	private MediatorImpl getShowMediator(){
		return new MediatorImpl("admin-show", "CiliaAdminShow","fr.liglab.adele.cilia.admin", null, null, null, null);
	}

	private MediatorImpl getStarterMediator(){
		return  new MediatorImpl("admin-starter", "CiliaAdminStarter","fr.liglab.adele.cilia.admin", null, null, null, null);
	}

	private MediatorImpl getStopMediator(){
		return new MediatorImpl("admin-stop", "CiliaAdminStop","fr.liglab.adele.cilia.admin", null, null, null, null);
	}
	
	private MediatorImpl getModifierMediator(){
		return new MediatorImpl("admin-modify", "CiliaAdminModifier","fr.liglab.adele.cilia.admin", null, null, null, null);
	}

	private MediatorImpl getLoaderMediator(){
		return new MediatorImpl("admin-loader", "CiliaAdminLoader","fr.liglab.adele.cilia.admin", null, null, null, null);
	}
	
	private MediatorImpl getReplacerMediator() {
		return new MediatorImpl("admin-replacer", "CiliaAdminReplacer","fr.liglab.adele.cilia.admin", null, null, null, null);	
	}
	private MediatorImpl getCopierMediator() {
		return new MediatorImpl("admin-copier", "CiliaAdminCopier","fr.liglab.adele.cilia.admin", null, null, null, null);
	}
}
