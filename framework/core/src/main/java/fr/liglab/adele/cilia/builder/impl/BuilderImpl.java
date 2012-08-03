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

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.model.CiliaContainer;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class BuilderImpl implements Builder {

	CiliaContainer container;

	CiliaContext context;

	ArchitectureImpl architecture = null;

	public BuilderImpl(CiliaContext context, CiliaContainer container) {
		this.container = container;
		this.context = context;
	}


	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Builder#create(java.lang.String)
	 */
	public Architecture create(String chainId) throws BuilderException {
		if (null == chainId) {
			throw new BuilderException("Unable to create chain with ID=null");
		}
		if (container.getChain(chainId) != null) {
			throw new BuilderException("Unable to create new chain with an existing ID:" + chainId);
		}
		if (architecture != null) {
			throw new BuilderException("Builder with existing configuration");
		} else {
			architecture = new ArchitectureImpl(chainId, Architecture.CREATE);
		}
		return architecture;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Builder#get(java.lang.String)
	 */
	public Architecture get(String chainId) throws BuilderException {
		if (null == chainId) {
			throw new BuilderException("Unable to retrieve null chain");
		}
		if (container.getChain(chainId) == null && architecture == null) {
			throw new BuilderException("There is any Chain with id :" + chainId);
		}
		if (architecture != null && !((ArchitectureImpl)architecture).getChainId().equalsIgnoreCase(chainId) ) {
			throw new BuilderException("There is a Builder Configuration for a Chain with id :" + ((ArchitectureImpl)architecture).getChainId());
		}
		if (architecture == null) {
			architecture =  new ArchitectureImpl(chainId, Architecture.MODIFY);
		}
		return architecture;
	}

	public Builder done() throws BuilderException, BuilderPerformerException {
		try {
			container.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try {

			if (architecture == null) {
				throw new BuilderException("Unable to build an invalid architecture chain: Architecture is null");
			}
			((ArchitectureImpl)architecture).checkValidation();
			//Change validation, it cant' be modified.
			((ArchitectureImpl)architecture).setValid(false);
			BuilderPerformer perf = new BuilderPerformer(architecture, container, context);
			perf.perform();
			return this;
		}finally{
			container.getMutex().writeLock().release();
		}
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Builder#undo()
	 */
	public Builder undo() throws BuilderException {
		throw new UnsupportedOperationException("undo operation is currently unsupported");
	}
	/**
	 * Return the current chain id this builder is working on.
	 * Return null if there is any configuration on this builder.
	 */
	public String current() {
		return architecture.getChainId();
	}
	/**
	 * Remove a mediation chain.
	 * @param chainId the id of the chain to remove.
	 * @throws BuilderException 
	 */
	public Builder remove(String chainId) throws BuilderException{
		if (null == chainId) {
			throw new BuilderException("Unable to retrieve null chain");
		}
		if (container.getChain(chainId) == null && architecture == null) {
			throw new BuilderException("There is any Chain with id :" + chainId);
		}
		if (architecture != null && !((ArchitectureImpl)architecture).getChainId().equalsIgnoreCase(chainId) ) {
			throw new BuilderException("There is a Builder Configuration for a Chain with id :" + ((ArchitectureImpl)architecture).getChainId());
		}
		if (architecture == null) {
			architecture =  new ArchitectureImpl(chainId, Architecture.REMOVE);
		}
		return this;
	}
}
