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
package fr.liglab.adele.cilia.administration.processors;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContainer;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.model.AdapterImpl;
import fr.liglab.adele.cilia.model.BindingImpl;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.MediatorImpl;
import fr.liglab.adele.cilia.model.MediatorComponentImpl;

/**
 * CiliaShowProcessor: The processor class. Shows cilia chain instances, chains,
 * mediators, adapters.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaShowProcessor {
	/**
	 * The Cilia Context service, injected by iPOJO
	 */
	CiliaContainer ccontext;

	/**
	 * The main process method, this method is called by the cilia framework.
	 * 
	 * @param data
	 *            contains the parameters to show a cilia chain element
	 *            instance.
	 * @return the same unchanged data.
	 */
	public Data show(Data data) {
		try {
			ccontext.getMutex().readLock().acquire();
		} catch (InterruptedException e) {
		}
		try {
			if ("chain".compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
				showChainInfo(String.valueOf(data.getProperty("id")));
			} else if ("mediator".compareToIgnoreCase(String.valueOf(data
					.getProperty("element"))) == 0) {
				showMediatorInfo(data);
			} else if ("adapter".compareToIgnoreCase(String.valueOf(data
					.getProperty("element"))) == 0) {
				showAdapterInfo(data);
			} else {
				showChains();
			}
		} finally {
			ccontext.getMutex().readLock().release();
		}
		return data;
	}

	/**
	 * Shows all the mediation chains.
	 */
	private void showChains() {
		StringBuffer toShow = new StringBuffer("Chains:\n");
		Set chains = ccontext.getAllChains();
		if (chains == null || chains.size() < 1) {
			return;
		}
		Iterator it = chains.iterator();
		while (it.hasNext()) {
			ChainImpl ch = (ChainImpl) it.next();
			toShow.append(ch.getId());
			toShow.append("\n");
		}
		System.out.println(toShow.toString());
	}

	/**
	 * Show to the console, the information of the given chain.
	 * 
	 * @param chainId
	 *            The chain id to see.
	 */
	private void showChainInfo(String chainId) {
		Chain ch = ccontext.getChain(chainId);
		StringBuffer toShow = new StringBuffer("ChainImpl: ");
		if (ch == null) {
			toShow.append(chainId);
			toShow.append(" not found\n");
		}
		if (ch != null) {
			toShow.append(ch.getId());
			toShow.append("\n");
			Set mediators = ch.getMediators();
			Set adapters = ch.getAdapters();
			Set bindings = ch.getBindings();
			Iterator itm = mediators.iterator();
			Iterator ita = adapters.iterator();
			Iterator itb = bindings.iterator();
			// Add the mediator id's to the StringBuffer.
			if (itm.hasNext()) {
				toShow.append("[Mediators]\n");
			}
			while (itm.hasNext()) {
				MediatorImpl mediator = (MediatorImpl) itm.next();
				toShow.append(mediator.getId());
				toShow.append("\n");
			}
			// Add the AdapterImpl id's to the StringBuffer.
			if (ita.hasNext()) {
				toShow.append("[Adapters]\n");
			}
			while (ita.hasNext()) {
				AdapterImpl adapter = (AdapterImpl) ita.next();
				toShow.append(adapter.getId());
				toShow.append("\n");
			}
			if (itb.hasNext()) {
				toShow.append("[Bindings]\n");
			}
			while (itb.hasNext()) {
				BindingImpl binding = (BindingImpl) itb.next();
				toShow.append(getBindingInfo(binding));
			}
		}
		System.err.println(toShow.toString());
	}

	/**
	 * Shows to the console the mediator instance information.
	 * 
	 * @param data
	 *            the given data must contain the mediator information (id,
	 *            chain). The property "element" in data must be mediator.
	 */
	private void showMediatorInfo(Data data) {
		Chain ch = null;
		Mediator med = null;
		StringBuffer toShow = new StringBuffer();
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		ch = ccontext.getChain(chainId);
		if (ch == null) {
			System.out.println("ChainImpl " + chainId + " Not found.");
			return;
		}
		med = ch.getMediator(mediatorId);
		if (med == null) {
			System.out.println("MediatorImpl " + mediatorId + " in ChainImpl " + chainId
					+ " Not found.");
			showChainInfo(chainId);
			return;
		}
		toShow.append("MediatorImpl Id: ");
		toShow.append(getMediatorInfo(med));
		System.err.println(toShow.toString());
	}

	/**
	 * Shows to the console the adapter instance information.
	 * 
	 * @param data
	 *            the given data must contain the adapter information (id,
	 *            chain). The property "element" in data must be adapter.
	 */
	private void showAdapterInfo(Data data) {
		Chain ch = null;
		Adapter med = null;
		StringBuffer toShow = new StringBuffer();
		String mediatorId = String.valueOf(data.getProperty("id"));
		String chainId = String.valueOf(data.getProperty("chain"));
		ch = ccontext.getChain(chainId);
		if (ch == null) {
			System.err.println("ChainImpl " + chainId + " Not found.");
			return;
		}
		med = ch.getAdapter(mediatorId);
		if (med == null) {
			System.err.println("AdapterImpl " + mediatorId + " in ChainImpl " + chainId
					+ " Not found.");
			showChainInfo(chainId);
			return;
		}
		toShow.append("AdapterImpl Id: ");
		toShow.append(getMediatorInfo(med));
		System.err.println(toShow.toString());
	}

	/**
	 * Get the mediator instance information.
	 * 
	 * @param mediator
	 *            the mediator to obtain its information.
	 * @return the mediator info as a String.
	 */
	private String getMediatorInfo(MediatorComponent mediator) {
		StringBuffer toShow = new StringBuffer();
		toShow.append(mediator.getId());
		toShow.append("\nType: ");
		toShow.append(mediator.getType());
		toShow.append("\nChain Id: ");
		toShow.append(mediator.getChain().getId());
		Binding[] bis = mediator.getInBindings();
		if (bis != null && bis.length > 0) {
			toShow.append("\n[In Bindings]\n");
			for (int i = 0; i < bis.length; i++) {
				Binding b = bis[i];
				toShow.append(getBindingInfo(b));
			}
		}
		Binding[] bos = mediator.getOutBindings();
		if (bos != null && bos.length > 0) {
			toShow.append("\n[Out Bindings]\n");
			for (int i = 0; i < bos.length; i++) {
				Binding b = bos[i];
				toShow.append(getBindingInfo(b));
			}
		}
		Dictionary props = mediator.getProperties();
		if (props != null) {
			toShow.append("\n[Properties]\n");
			Enumeration enume = props.keys();
			while (enume.hasMoreElements()) {
				Object key = enume.nextElement();
				Object value = props.get(key);
				toShow.append("Key = ");
				toShow.append(key);
				toShow.append(" Value = ");
				toShow.append(value);
				toShow.append("\n");
			}
		}
		return toShow.toString();
	}

	/**
	 * Get the binding instance information.
	 * 
	 * @param binding
	 *            the binding to obtain its information.
	 * @return the binding info as a String.
	 */
	private String getBindingInfo(Binding binding) {
		StringBuffer toShow = new StringBuffer();
		if (binding.getSourcePort() != null) {
			toShow.append("FROM ");
			toShow.append(binding.getSourceMediator().getId());
			toShow.append(":");
			toShow.append(binding.getSourcePort().getName());
		}
		if (binding.getSourcePort() != null) {
			toShow.append(" TO ");
			toShow.append(binding.getTargetMediator().getId());
			toShow.append(":");
			toShow.append(binding.getTargetPort().getName());
		}
		toShow.append("\n");
		return toShow.toString();
	}
}
