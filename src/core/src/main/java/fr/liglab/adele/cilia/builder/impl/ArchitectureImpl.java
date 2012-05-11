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
package fr.liglab.adele.cilia.builder.impl;

import java.util.ArrayList;
import java.util.List;

import fr.liglab.adele.cilia.CiliaContainer;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Binder;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.builder.Creator;
import fr.liglab.adele.cilia.builder.Modifier;
import fr.liglab.adele.cilia.builder.Remover;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class ArchitectureImpl implements Architecture {

	private CiliaContainer ccontext;
	private boolean creatingChain = false;
	private String chainId = null;
	private volatile boolean isValid = false;
	private Builder builder = null;

	private List bindings = new ArrayList();
	private List unbindings = new ArrayList();
	private List created = new ArrayList();
	private List removed = new ArrayList();
	private List modified = new ArrayList();

	// Creator temporal;

	/**
	 * @param chainid
	 * @param creating
	 */
	protected ArchitectureImpl(CiliaContainer cc, Builder builder, String chainid,
			boolean creating) {
		ccontext = cc;
		setChainId(chainid);
		setCreatingChain(creating);
		isValid = true;
		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.builder.Architecture#bind()
	 */
	public Binder bind() throws BuilderException {
		checkValidation();
		Binder bind = new BinderImpl(this);
		bindings.add(bind);
		return bind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.builder.Architecture#bind()
	 */

	public Binder unbind() throws BuilderException {
		checkValidation();
		Binder bind = new UnBinderImpl(this);
		unbindings.add(bind);
		return bind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.builder.Architecture#create()
	 */

	public Creator create() throws BuilderException {
		checkValidation();
		CreatorImpl creator = new CreatorImpl(this);
		created.add(creator);
		return creator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.builder.Architecture#remove()
	 */

	public Remover remove() throws BuilderException {
		checkValidation();
		Remover remover = new RemoverImpl(this);
		removed.add(remover);
		return remover;
	}

	public Modifier configure() throws BuilderException {
		checkValidation();
		InstanceModifierImpl modifier = new InstanceModifierImpl();
		modified.add(modifier);
		return modifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.cilia.builder.Architecture#done()
	 */
	protected Builder done() throws BuilderPerformerException, BuilderException {
		checkValidation();
		isValid = false;
		BuilderPerformer perf = new BuilderPerformer(this, ccontext);
		perf.perform();
		return builder;
	}

	private void checkValidation() throws BuilderException {
		if (!isValid) {
			throw new BuilderException(
					"Unable to build in an invalid builder configuration");
		}
	}

	/**
	 * @return the creatingChain
	 */
	public boolean isCreatingChain() {
		return creatingChain;
	}

	/**
	 * @param creatingChain the creatingChain to set
	 */
	public void setCreatingChain(boolean creatingChain) {
		this.creatingChain = creatingChain;
	}

	/**
	 * @return the chainId
	 */
	public String getChainId() {
		return chainId;
	}

	/**
	 * @param chainId the chainId to set
	 */
	public void setChainId(String chainId) {
		this.chainId = chainId;
	}

	/**
	 * @return the bindings
	 */
	protected List getBindings() {
		return bindings;
	}

	/**
	 * @return the unbindings
	 */
	protected List getUnbindings() {
		return unbindings;
	}

	/**
	 * @return the created
	 */
	protected List getCreated() {
		return created;
	}

	/**
	 * @return the removed
	 */
	protected List getRemoved() {
		return removed;
	}

	/**
	 * @return the modified
	 */
	protected List getModified() {
		return modified;
	}


}
