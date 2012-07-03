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

import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.ext.ContentBasedRouting;

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

	private String chainName = "admin-chain";

	private String namespace = "fr.liglab.adele.cilia.admin";

	/**
	 * Method called when initializing the component. When the component is
	 * started, the cilia-admin chain is generated and initialized.
	 */
	protected void start() {
		try {
			createChain();

			ccontext.getApplicationRuntime().startChain(chainName) ;
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
		} catch (BuilderConfigurationException e) {
			e.printStackTrace();
		} catch (BuilderException e) {
			e.printStackTrace();
		} catch (BuilderPerformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CiliaIllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * when the component is stopped, the chain must be removed.
	 */
	protected void stop() {
		if (ccontext != null) {
			try {

				ApplicationRuntime ar = ccontext.getApplicationRuntime();
				if (ar != null){
					ar.stopChain(chainName);
				}
				removeChain();
			} catch (CiliaIllegalParameterException e) {
				e.printStackTrace();
			} catch (CiliaIllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	private void unbindContext() {
		ApplicationRuntime ar = ccontext.getApplicationRuntime();
		if (ar != null){
			try {
				ar.stopChain(chainName);
			} catch (CiliaIllegalParameterException e) {
				e.printStackTrace();
			} catch (CiliaIllegalStateException e) {
				e.printStackTrace();
			}
		}
	}
	private void removeChain() {
		try {
			if (ccontext != null){
				Builder builder = ccontext.getBuilder().remove(chainName);
				builder.done();
			}
		} catch (BuilderException e) {
			e.printStackTrace();
		} catch (BuilderPerformerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}

	}

	/**
	 * Create the cilia-admin chain and its mediators.
	 * 
	 * @return the cilia-admin chain reference.
	 * @throws BuilderException 
	 * @throws BuilderConfigurationException 
	 * @throws BuilderPerformerException 
	 */
	private void createChain() throws BuilderConfigurationException, BuilderException, BuilderPerformerException {
		Builder builder = ccontext.getBuilder();
		// create chain.
		Architecture chain = builder.create(chainName);
		// Add components.
		chain.create().adapter().type("cilia-admin-service-adapter")
		.namespace(namespace).id("service-adapter");

		chain.create().adapter().type("felix-admin-gogo-shell").namespace(namespace).id("gogo-command-adapter");

		chain.create()
		.mediator()
		.type("CiliaAdminCBMediator")
		.id("admin-entry-mediator")
		.configure()
		.dispatcher(
				new ContentBasedRouting().evaluator("ldap")
				.condition("(data.content=create)")
				.to("create")
				.condition("(data.content=remove)")
				.to("remove").condition("(data.content=start)")
				.to("start").condition("(data.content=stop)")
				.to("stop").condition("(data.content=show)")
				.to("show").condition("(data.content=modify)")
				.to("modify").condition("(data.content=load)")
				.to("load").condition("(data.content=unload)")
				.to("unload")
				.condition("(data.content=replace)")
				.to("replace").condition("(data.content=copy)")
				.to("copy"));

		chain.create().mediator().type("CiliaAdminCreator").namespace(namespace).id("admin-creator");

		chain.create().mediator().type("CiliaAdminRemover").namespace(namespace).id("admin-remove");

		chain.create().mediator().type("CiliaAdminShow").namespace(namespace).id("admin-show");

		chain.create().mediator().type("CiliaAdminStarter").namespace(namespace).id("admin-starter");

		chain.create().mediator().type("CiliaAdminStop").namespace(namespace).id("admin-stop");

		chain.create().mediator().type("CiliaAdminModifier").namespace(namespace).id("admin-modify");

		chain.create().mediator().type("CiliaAdminLoader").namespace(namespace).id("admin-loader");

		chain.create().mediator().type("CiliaAdminReplacer").namespace(namespace).id("admin-replacer");

		chain.create().mediator().type("CiliaAdminCopier").namespace(namespace).id("admin-copier");

		chain.bind().from("service-adapter:unique").to("admin-entry-mediator:unique");

		chain.bind().from("gogo-command-adapter:unique").to("admin-entry-mediator:unique");

		chain.bind().from("admin-entry-mediator:create").to("admin-creator:creatorIn");

		chain.bind().from("admin-entry-mediator:remove").to("admin-remove:removerIn");

		chain.bind().from("admin-entry-mediator:start").to("admin-starter:starterIn");

		chain.bind().from("admin-entry-mediator:replace").to("admin-replacer:replacerIn");

		chain.bind().from("admin-entry-mediator:copy").to("admin-copier:copierIn");

		chain.bind().from("admin-entry-mediator:stop").to("admin-stop:stopIn");

		chain.bind().from("admin-entry-mediator:show").to("admin-show:showIn");

		chain.bind().from("admin-entry-mediator:modify").to("admin-modify:modifyIn");

		chain.bind().from("admin-entry-mediator:load").to("admin-loader:loaderIn");

		chain.bind().from("admin-entry-mediator:unload").to("admin-loader:unloaderIn");

		builder.done();

	}

}
