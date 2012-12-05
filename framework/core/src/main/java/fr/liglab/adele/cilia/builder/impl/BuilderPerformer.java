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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.AdapterImpl;
import fr.liglab.adele.cilia.model.impl.BindingImpl;
import fr.liglab.adele.cilia.model.impl.ChainImpl;
import fr.liglab.adele.cilia.model.impl.MediatorComponentImpl;
import fr.liglab.adele.cilia.model.impl.MediatorImpl;
import fr.liglab.adele.cilia.model.impl.PatternType;
import fr.liglab.adele.cilia.util.Const;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class BuilderPerformer {

	private CiliaContainer container;
	private CiliaContext ccontext;
	private ArchitectureImpl architecture;
	private ChainImpl chain;

	private static Logger log = LoggerFactory.getLogger(Const.LOGGER_CORE);

	/**
	 * @param architectureImpl
	 */
	protected BuilderPerformer(ArchitectureImpl arch, CiliaContainer cont, CiliaContext context) {
		container = cont;
		this.ccontext= context; 
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
		doReplace();
		try {
			doModify();
		} catch (CiliaException e) {
			e.printStackTrace();
			throw new BuilderPerformerException(e.getMessage());
		} 
		doCopy();
		doRemove();
		doBind();
		doUnbind();
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
			if (architecture.getChainId().compareToIgnoreCase("cilia")==0){
				throw new BuilderPerformerException(
						"Chain with the 'cilia' as ID is forbiden");
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
		try {
			chain.add(mediator);
		} catch (CiliaException e) {
			e.printStackTrace();
		}
	}

	private void createAdapter(CreatorImpl creator) {
		AdapterImpl adapter = new AdapterImpl(creator.getId(), creator.getType(),
				creator.getNamespace(), creator.getVersion(),
				creator.getConfiguration(), chain, PatternType.UNASSIGNED);
		try {
			chain.add(adapter);
		} catch (CiliaException e) {
			e.printStackTrace();
		}
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

	private void doModify() throws BuilderPerformerException, CiliaIllegalParameterException, CiliaInvalidSyntaxException {
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
				String filter = "(&(node="+id+")(chain="+chain.getId()+"))";
				ccontext.getApplicationRuntime().addListener(filter,  new NodeNotifier(toModify.getConfiguration()));
			} else {
				comp.setProperties(toModify.getConfiguration());
			}
		}
	}

	private void doBind() throws BuilderPerformerException {
		Iterator it = architecture.getBindings().iterator();
		while (it.hasNext()) {
			BinderImpl bi = (BinderImpl) it.next();
			MediatorComponent from = getMediatorComponent(bi.getFromMediator());
			MediatorComponent to = getMediatorComponent(bi.getToMediator());
			Binding binding = new BindingImpl(bi.getUsing(),bi.getConfiguration());
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

	private void doCopy() throws BuilderPerformerException {
		Iterator it = architecture.getCopied().iterator();
		while (it.hasNext()){
			ReplacerImpl rep = (ReplacerImpl)it.next();
			copyMediator(rep);
		}
	}

	private void copyMediator(ReplacerImpl rep) throws BuilderPerformerException{
		MediatorComponent from = getMediatorComponent(rep.getFromMediator());
		Dictionary properties = from.getProperties();

		//why Denis?
		if (properties != null) {
			properties.remove(Const.PROPERTY_COMPONENT_ID);
			properties.remove(Const.PROPERTY_CHAIN_ID);
			properties.remove(Const.PROPERTY_LOCK_UNLOCK);
		}
		if (from instanceof Adapter){
			Adapter ad = (Adapter)from;
			AdapterImpl nadapter = new AdapterImpl(rep.getToMediator(), ad.getType(),
					ad.getNamespace(), 
					ad.getVersion(), properties, chain, ad.getPattern());

			try {
				chain.add(nadapter);
			} catch (CiliaException e) {
				e.printStackTrace();
			}
		} else {
			MediatorImpl mediator = new MediatorImpl(rep.getToMediator(), from.getType(),
					from.getNamespace(), from.getCategory(),
					from.getVersion(), properties, chain);
			try {
				chain.add(mediator);
			} catch (CiliaException e) {
				e.printStackTrace();
			}
		}
		//
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
			ccontext.getApplicationRuntime().copyData(from, to);
		} catch (CiliaIllegalParameterException e) {
			throw new BuilderPerformerException(e.getMessage());
		}
		//we unlock mediation execution
		finally {
			((MediatorComponentImpl)from).unLockRuntime();
			((MediatorComponentImpl)to).unLockRuntime();
		}
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
		if (id == null){
			throw new BuilderPerformerException(
					"Unable to retrieve ID: " + id);
		}
		MediatorComponent medComponent = chain.getMediator(id);
		if (medComponent == null) {
			medComponent = chain.getAdapter(id);
		}
		if (medComponent == null) {
			throw new BuilderPerformerException(
					"Unable to retrieve ID: " + id);
		}
		return medComponent;
	}

	private void verifyOperations() throws BuilderPerformerException {
		verifyNewInstances();
		verifyRemoveInstances();
		//verifyConfiguration();
		verifyBindings();
		verifyUnbindings();
		verifyReplaced();
		verifyCopied();
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
			if (creat.getType() == null || creat.getType().length()<1) {
				throw new BuilderPerformerException("Components must have a valid type");
			}
			if ((chain.getMediator(id) != null) || chain.getAdapter(id) != null) { //
				switch (creat.getInstanceType()) {
				case Architecture.ADAPTER:
					throw new BuilderPerformerException(
							"Impossible to create an adapter with ID "
									+ creat.getType() + ":" + id
									+ "; Another instance with the same ID exists in chain "
									+ chain.getId());
				case Architecture.MEDIATOR:
					throw new BuilderPerformerException(
							"Impossible to create a mediator with ID "
									+ creat.getType() + ":" +id
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
			if (!itExist(id)) { //
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
			if (!itExist(id) ) { //
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
			if (!itExist(id)) {
				throw new BuilderPerformerException(
						"Unable to bind from an inexistent component:" + id);
			}
			id = bi.getToMediator();
			if (!itExist(id)) {
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
			if (!itExist(id)) {
				throw new BuilderPerformerException(
						"Unable to unbind from an inexistent component:" + id);
			}
			id = bi.getToMediator();
			if (!itExist(id)) {
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
			if (!itExist(id)) {
				throw new BuilderPerformerException(
						"Unable to replace an inexistent component:" + id);
			}
			id = rep.getToMediator();
			if (!itExist(id)) {
				throw new BuilderPerformerException(
						"Unable to replace to an inexistant component:" + id);
			}
		}
	}

	private void verifyCopied() throws BuilderPerformerException {
		Iterator it = architecture.getCopied().iterator();
		while (it.hasNext()) {
			ReplacerImpl rep = (ReplacerImpl) it.next();
			String id = rep.getFromMediator();
			if (!itExist(id)) {
				throw new BuilderPerformerException(
						"Unable to copy information of an inexistent mediator:" + id);
			}
			id = rep.getToMediator();
			if (isAlreadyInstantiated(id)) {
				throw new BuilderPerformerException(
						"Alredy exist a component with the id:" + id + " , Unable to copy");
			}
		}
	}

	private boolean itExist(String id) {
		if (chain.getAdapter(id) != null || chain.getMediator(id) != null) {
			return true;
		}
		//See if mediator will be created using the creation operation
		List newerComponents = architecture.getCreated();
		Iterator it = newerComponents.iterator();
		while (it.hasNext()) {
			CreatorImpl toCreate = (CreatorImpl) it.next();
			String mid = toCreate.getId();
			if (id.equalsIgnoreCase(mid)) {
				return true;
			}
		}
		//See if mediator will be created using the copy operation.
		List copiedComponents = architecture.getCopied();
		Iterator cit = copiedComponents.iterator();
		while (cit.hasNext()) {
			ReplacerImpl toCreate = (ReplacerImpl) cit.next();
			String mid = toCreate.to; // is the new mediator id.
			if (id.equalsIgnoreCase(mid)) {
				return true;
			}
		}
		return false;
	}
	

	private boolean isAlreadyInstantiated(String id) {
		if (chain.getAdapter(id) != null || chain.getMediator(id) != null) {
			return true;
		}
		return false;
	}
	
	private class NodeNotifier implements NodeCallback {

		private Hashtable properties = new Hashtable();
		protected NodeNotifier(Hashtable props){
			if(props != null) {
				properties.putAll(props);
			}
		}

		public void onArrival(Node node) {
			log.warn("[Builder Performer] Node arrival, it will be modified" + node.nodeId());
			try {
				MediatorComponent mc = ccontext.getApplicationRuntime().getModel(node);
				mc.setProperties(properties);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
			try {
				ccontext.getApplicationRuntime().removeListener(this);
			} catch (CiliaIllegalParameterException e) {
				e.printStackTrace();
			}
		}


		public void onDeparture(Node node) {
		}


		public void onModified(Node node) {
		}


		public void onBind(Node from, Node to) {
		}

		public void onUnBind(Node from, Node to) {
		}

		public void onStateChange(Node node, boolean isValid) {
		}
		
	}
}
