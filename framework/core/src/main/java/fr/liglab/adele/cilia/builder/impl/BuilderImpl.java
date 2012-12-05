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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.util.Const;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class BuilderImpl implements Builder {

	CiliaContainer container;

	CiliaContext context;

	ArchitectureImpl architecture = null;
	
	private static Logger log = LoggerFactory.getLogger(Const.LOGGER_CORE);

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
		if (architecture != null && !((ArchitectureImpl)architecture).getChainId().equalsIgnoreCase(chainId) ) {
			throw new BuilderException("There is a Builder Configuration for a Chain with id :" + ((ArchitectureImpl)architecture).getChainId());
		}
		if (architecture == null) {
			architecture =  new ArchitectureImpl(chainId, Architecture.MODIFY);
		}
		return architecture;
	}

	public Builder done() throws BuilderException, BuilderPerformerException {
		setInvalid();//So it is impossible to modify again this builder.
		if(!architecture.toCreate() && container.getChain(architecture.getChainId()) == null){
			try {
				log.warn("Will wait until chain is ready: " + architecture.getChainId());
				context.getApplicationRuntime().addListener("(chain="+architecture.getChainId()+")", new ChainListenerImpl());
			} catch (CiliaException e) {
				e.printStackTrace();
				throw new BuilderPerformerException(e.getMessage());
			}
		} else {
			performDone();
		}

		return this;
	}

	private Builder performDone() throws BuilderException, BuilderPerformerException {
		try {
			container.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try{
			BuilderPerformer perf = new BuilderPerformer(architecture, container, context);
			perf.perform();
			return this;
		}finally{
			container.getMutex().writeLock().release();
		}
	}

	private void setInvalid() throws BuilderException {
		if (architecture == null) {
			throw new BuilderException("Unable to build an invalid architecture chain: Architecture is null");
		}
		((ArchitectureImpl)architecture).checkValidation();
		((ArchitectureImpl)architecture).setValid(false);
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

	private class ChainListenerImpl implements ChainCallback {

		public void onAdded(String chainId) {
			try {
				performDone();
			} catch (BuilderException e) {
				e.printStackTrace();
			} catch (BuilderPerformerException e) {
				e.printStackTrace();
			}
		}

		public void onRemoved(String chainId) {	}

		public void onStateChange(String chainId, boolean event) {	}
	}
}
