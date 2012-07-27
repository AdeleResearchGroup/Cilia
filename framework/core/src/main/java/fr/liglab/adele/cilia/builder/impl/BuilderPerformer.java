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

import java.util.Iterator;
import java.util.List;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.AdapterImpl;
import fr.liglab.adele.cilia.model.impl.BindingImpl;
import fr.liglab.adele.cilia.model.impl.ChainImpl;
import fr.liglab.adele.cilia.model.impl.MediatorComponentImpl;
import fr.liglab.adele.cilia.model.impl.MediatorImpl;
import fr.liglab.adele.cilia.model.impl.PatternType;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class BuilderPerformer {

	private CiliaContainer container;
	private ArchitectureImpl architecture;
	private ChainImpl chain;

	/**
	 * @param architectureImpl
	 */
	protected BuilderPerformer(ArchitectureImpl arch, CiliaContainer context) {
		container = context;
		architecture = arch;
	}

	/**
	 * @throws BuilderException
	 * 
	 */
	public void perform() throws BuilderPerformerException {
		chain = getChain();
		if (architecture.toRemove()) {
			container.removeChain(chain.getId());
		}
		verifyOperations();
		doCreate();
		doRemove();
		doModify();
		doBind();
		doUnbind();
		doReplace();
		if (architecture.toCreate()) {
			container.addChain(chain);
		}
	}

	private ChainImpl getChain() throws BuilderPerformerException {
		ChainImpl chain = null;
		if (architecture.toCreate()) {
			if (container.getChain(architecture.getChainId()) != null) {
				throw new BuilderPerformerException(
						"Chain with the same ID already exist: "
								+ architecture.getChainId());
			}
			chain = new ChainImpl(architecture.getChainId(), null, null, null);
		} else {
			if (container.getChain(architecture.getChainId()) == null) {
				throw new BuilderPerformerException("Chain does not exist: "
						+ architecture.getChainId());
			}
			chain = (ChainImpl)container.getChain(architecture.getChainId());
		}
		return chain;
	}



	private void doCreate() throws BuilderPerformerException {
		List created = architecture.getCreated();
		Iterator it = created.iterator();
		while (it.hasNext()) {
			CreatorImpl creat = (CreatorImpl) it.next();
			switch (creat.getInstanceType()) {
			case Architecture.ADAPTER:
				createAdapter(creat);
				break;
			case Architecture.MEDIATOR:
				createMediator(creat);
				break;
			}
		}
	}

	private void createMediator(CreatorImpl creator) {
		MediatorImpl mediator = new MediatorImpl(creator.getId(), creator.getType(),
				creator.getNamespace(), creator.getCategory(),
				creator.getVersion(), creator.getConfiguration(), chain);
		chain.add(mediator);
	}

	private void createAdapter(CreatorImpl creator) {
		AdapterImpl adapter = new AdapterImpl(creator.getId(), creator.getType(),
				creator.getNamespace(), creator.getVersion(),
				creator.getConfiguration(), chain, PatternType.UNASSIGNED);
		chain.add(adapter);
	}

	private void doRemove() throws BuilderPerformerException {
		Iterator it = architecture.getRemoved().iterator();
		while (it.hasNext()) {
			RemoverImpl toRemove = (RemoverImpl) it.next();
			String id = toRemove.getId();
			switch (toRemove.getType()) {
			case Architecture.ADAPTER:
				chain.removeAdapter(id);
				break;
			case Architecture.MEDIATOR:
				chain.removeMediator(id);
				break;
			}
		}
	}

	private void doModify() throws BuilderPerformerException {
		Iterator it = architecture.getModified().iterator();
		while (it.hasNext()) {
			InstanceModifierImpl toModify = (InstanceModifierImpl) it.next();
			String id = toModify.getId();
			MediatorComponentImpl comp = null;
			switch (toModify.getType()) {
			case Architecture.ADAPTER:
				comp = (MediatorComponentImpl)chain.getAdapter(id);
				break;
			case Architecture.MEDIATOR:
				comp = (MediatorComponentImpl)chain.getMediator(id);
				break;
			}
			if (comp == null) {
				throw new BuilderPerformerException(
						"Unable to modify inexistent component:" + id);
			}
			comp.setProperties(toModify.getConfiguration());
		}
	}

	private void doBind() throws BuilderPerformerException {
		Iterator it = architecture.getBindings().iterator();
		while (it.hasNext()) {
			BinderImpl bi = (BinderImpl) it.next();
			MediatorComponent from = getMediatorComponent(bi.getFromMediator());
			MediatorComponent to = getMediatorComponent(bi.getToMediator());
			Binding binding = new BindingImpl(bi.getUsing(),
					bi.getConfiguration());
			chain.bind(from.getOutPort(bi.getFromPort()),
					to.getInPort(bi.getToPort()), binding);
		}
	}

	private void doUnbind() throws BuilderPerformerException {
		Iterator it = architecture.getUnbindings().iterator();
		while (it.hasNext()) {
			BinderImpl bi = (BinderImpl) it.next();
			MediatorComponent from = getMediatorComponent(bi.getFromMediator());
			MediatorComponent to = getMediatorComponent(bi.getToMediator());
			Binding[] bindings = chain.getBindings(from, to);
			if (bindings != null) {
				for (int i = 0; i < bindings.length; i++)
					chain.unbind(bindings[i]);
			}
		}
	}

	private void doReplace() throws BuilderPerformerException {
		Iterator it = architecture.getReplaced().iterator();
		while (it.hasNext()){
			ReplacerImpl rep = (ReplacerImpl)it.next();
			replaceMediator(rep);
		}
	}

	private void replaceMediator(ReplacerImpl rep) throws BuilderPerformerException{
		MediatorComponent from = getMediatorComponent(rep.id);
		MediatorComponent to = getMediatorComponent(rep.to);
		//we lock mediator execution.
		((MediatorComponentImpl)from).lockRuntime();
		((MediatorComponentImpl)to).lockRuntime();
		//replace out bindings.
		Binding outbindings[] = from.getOutBindings();
		for (int i = 0; outbindings != null &&  i < outbindings.length ; i ++) {
			replaceOutBinding(to, rep, outbindings[i]);
		}
		//replace in bindings.
		Binding inbindings[] = from.getInBindings();
		for (int i = 0; inbindings != null &&  i < inbindings.length ; i ++) {
			replaceInBinding(to, rep, inbindings[i]);
		}
		/* Now data is injected */
		try {
			architecture.ccontext.getApplicationRuntime().copyData(from, to);
		} catch (CiliaIllegalParameterException e) {
			throw new BuilderPerformerException(e.getMessage());
		}
		//we unlock mediation execution
		((MediatorComponentImpl)from).unLockRuntime();
		((MediatorComponentImpl)to).unLockRuntime();
	}

	private void replaceOutBinding(MediatorComponent mediator, ReplacerImpl rep, Binding binding) throws BuilderPerformerException {
		String portname = null;
		String oldPortname = binding.getSourcePort().getName();
		if (rep.outports.containsKey(oldPortname)){ // If new mediator has another portname we switch.
			portname = (String)rep.outports.get(oldPortname);
		} else { //if not, we use the same port name as the previous mediator.
			portname = oldPortname;
		}
		chain.bind(mediator.getOutPort(portname), binding.getTargetMediator().getInPort(binding.getTargetPort().getName()));
		chain.unbind(binding);
	}

	private void replaceInBinding(MediatorComponent mediator, ReplacerImpl rep, Binding binding) throws BuilderPerformerException {
		String portname = null;
		String oldPortname = binding.getTargetPort().getName();
		if (rep.inports.containsKey(oldPortname)){ // If new mediator has another portname we switch.
			portname = (String)rep.inports.get(oldPortname);
		} else { //if not, we use the same port name as the previous mediator.
			portname = oldPortname;
		}
		chain.bind(binding.getSourceMediator().getOutPort(binding.getSourcePort().getName()), mediator.getInPort(portname)); 
		chain.unbind(binding);
	}


	private MediatorComponent getMediatorComponent(String id)
			throws BuilderPerformerException {
		MediatorComponent medComponent = chain.getMediator(id);
		if (medComponent == null) {
			medComponent = chain.getAdapter(id);
		}
		if (medComponent == null) {
			throw new BuilderPerformerException(
					"Unable to retrieve to perform bin; ID: " + id);
		}
		return medComponent;
	}

	private void verifyOperations() throws BuilderPerformerException {
		verifyNewInstances();
		verifyRemoveInstances();
		verifyConfiguration();
		verifyBindings();
		verifyUnbindings();
		verifyReplaced();
	}


	private void verifyNewInstances() throws BuilderPerformerException {
		List created = architecture.getCreated();
		Iterator it = created.iterator();
		while (it.hasNext()) {
			CreatorImpl creat = (CreatorImpl) it.next();
			String id = creat.getId();
			if (id == null) {
				throw new BuilderPerformerException(
						"Impossible to create a component without ID");
			}
			if ((chain.getMediator(id) != null) || chain.getAdapter(id) != null) { //
				switch (creat.getInstanceType()) {
				case Architecture.ADAPTER:
					throw new BuilderPerformerException(
							"Impossible to create an adapter with ID "
									+ id
									+ "; Another instance with the same ID exists in chain "
									+ chain.getId());
				case Architecture.MEDIATOR:
					throw new BuilderPerformerException(
							"Impossible to create a mediator with ID "
									+ id
									+ "; Another instance with the same ID exists in chain "
									+ chain.getId());
				}
			}
		}
	}

	private void verifyRemoveInstances() throws BuilderPerformerException {
		List removed = architecture.getRemoved();
		Iterator it = removed.iterator();
		while (it.hasNext()) {
			RemoverImpl toRemove = (RemoverImpl) it.next();
			String id = toRemove.getId();
			if (!isRealComponent(id)) { //
				switch (toRemove.getType()) {
				case Architecture.ADAPTER:
					throw new BuilderPerformerException(
							"Impossible to remove an adapter with ID " + id
							+ "; Unexistant adapterin chain "
							+ chain.getId());
				case Architecture.MEDIATOR:
					throw new BuilderPerformerException(
							"Impossible to remove a mediator with ID " + id
							+ "; Unexistant mediator in chain "
							+ chain.getId());
				}
			}
		}
	}

	private void verifyConfiguration() throws BuilderPerformerException {
		Iterator it = architecture.getModified().iterator();
		while (it.hasNext()) {
			InstanceModifierImpl toModify = (InstanceModifierImpl) it.next();
			String id = toModify.getId();
			if (!isRealComponent(id) ) { //
				switch (toModify.getType()) {
				case Architecture.ADAPTER:
					throw new BuilderPerformerException(
							"Impossible to modify an adapter with ID" + id
							+ "; Unexistant adapterin chain "
							+ chain.getId());
				case Architecture.MEDIATOR:
					throw new BuilderPerformerException(
							"Impossible to modify a mediator with ID" + id
							+ "; Unexistant mediator in chain "
							+ chain.getId());
				}
			}
		}
	}

	private void verifyBindings() throws BuilderPerformerException {
		Iterator it = architecture.getBindings().iterator();
		while (it.hasNext()) {
			BinderImpl bi = (BinderImpl) it.next();
			String id = bi.getFromMediator();
			if (!isRealComponent(id)) {
				throw new BuilderPerformerException(
						"Unable to bind from an inexistent component:" + id);
			}
			id = bi.getToMediator();
			if (!isRealComponent(id)) {
				throw new BuilderPerformerException(
						"Unable to bind to an inexistant component:" + id);
			}
		}
	}

	private void verifyUnbindings() throws BuilderPerformerException {
		Iterator it = architecture.getBindings().iterator();
		while (it.hasNext()) {
			BinderImpl bi = (BinderImpl) it.next();
			String id = bi.getFromMediator();
			if (!isRealComponent(id)) {
				throw new BuilderPerformerException(
						"Unable to unbind from an inexistent component:" + id);
			}
			id = bi.getToMediator();
			if (!isRealComponent(id)) {
				throw new BuilderPerformerException(
						"Unable to bind to an inexistant component:" + id);
			}
		}
	}

	private void verifyReplaced() throws BuilderPerformerException {
		Iterator it = architecture.getReplaced().iterator();
		while (it.hasNext()) {
			ReplacerImpl rep = (ReplacerImpl) it.next();
			String id = rep.getFromMediator();
			if (!isRealComponent(id)) {
				throw new BuilderPerformerException(
						"Unable to replace an inexistent component:" + id);
			}
			id = rep.getToMediator();
			if (!isRealComponent(id)) {
				throw new BuilderPerformerException(
						"Unable to replace to an inexistant component:" + id);
			}
		}
	}

	private boolean isRealComponent(String id) {
		if (chain.getAdapter(id) != null || chain.getMediator(id) != null) {
			return true;
		}
		List newerComponents = architecture.getCreated();
		Iterator it = newerComponents.iterator();
		while (it.hasNext()) {
			CreatorImpl toCreate = (CreatorImpl) it.next();
			String mid = toCreate.getId();
			if (id.equalsIgnoreCase(mid)) {
				return true;
			}
		}
		return false;
	}
}
