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

package fr.liglab.adele.cilia.runtime.knowledge;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.EventsConfiguration;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.RawData;
import fr.liglab.adele.cilia.SetUp;
import fr.liglab.adele.cilia.Thresholds;
import fr.liglab.adele.cilia.ThresholdsCallback;
import fr.liglab.adele.cilia.VariableCallback;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.ChainRuntime;
import fr.liglab.adele.cilia.util.UnModifiableDictionary;

/**
 * Knowledge engine implementation
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
public class KEngineImpl extends TopologyImpl implements ApplicationRuntime {

	private EventsConfiguration eventsConfig;
	private ListNodes registry;
	private BaseLevelListener baseLevelListener;

	public KEngineImpl(BundleContext bc, CiliaContainer cc, EventsConfiguration evtManager) {
		super(cc);
		eventsConfig = evtManager;
		registry = new ListNodes(this);
		baseLevelListener = new BaseLevelListener(bc, registry);
	}

	public void start() {
		baseLevelListener.start();
	}

	public void stop() {
		baseLevelListener.stop();
		registry.clearCache();
	}

	/*
	 * retrieve properties ( Read only access ) (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.cilia.knowledge.core.specification.Application#properties
	 * (fr.liglab.adele.cilia.knowledge.core.Node)
	 */
	public Dictionary getProperties(Node node) throws CiliaIllegalStateException,
			CiliaIllegalParameterException {
		MediatorComponent mc = getModel(node);
		return new UnModifiableDictionary(mc.getProperties());
	}

	public Object getProperty(Node node, String key) throws CiliaIllegalStateException,
			CiliaIllegalParameterException {
		if ((key == null) || (key.length() == 0))
			throw new CiliaIllegalParameterException("key parameter is null");
		MediatorComponent mc = getModel(node);
		return mc.getProperty(key);
	}

	public void addListener(String ldapFilter, NodeCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapFilter, listener);
	}

	public void removeListener(NodeCallback listener)
			throws CiliaIllegalParameterException {
		eventsConfig.removeListener(listener);
	}

	public void addListener(String ldapFilter, ChainCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapFilter, listener);
	}

	public void removeListener(ChainCallback listener)
			throws CiliaIllegalParameterException {
		eventsConfig.removeListener(listener);
	}

	public void addListener(String ldapfilter, ThresholdsCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapfilter, listener);

	}

	public void addListener(String ldapfilter, VariableCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapfilter, listener);
	}

	public void removeListener(ThresholdsCallback listener)
			throws CiliaIllegalParameterException {
		eventsConfig.removeListener(listener);
	}

	public void removeListener(VariableCallback listener)
			throws CiliaIllegalParameterException {
		eventsConfig.removeListener(listener);
	}

	public void startChain(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null) {
			throw new CiliaIllegalParameterException("Chain Id is null");
		}
		if (ciliaContext.getChain(chainId) == null) {
			throw new CiliaIllegalStateException("chain " + chainId + " not found");
		}
		ciliaContext.startChain(chainId);
	}

	public void stopChain(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null) {
			throw new CiliaIllegalParameterException("Chain Id is null");
		}
		if (ciliaContext.getChain(chainId) == null) {
			throw new CiliaIllegalStateException("chain " + chainId + " not found");
		}
		ciliaContext.stopChain(chainId);
	}

	public SetUp nodeSetup(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		return new SetUpImpl(registry, node);
	}

	/* -- */
	public int getChainState(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null)
			throw new CiliaIllegalParameterException("chain id is null");
		ChainRuntime chain = ciliaContext.getChainRuntime(chainId);
		if (chain == null)
			throw new CiliaIllegalStateException("'" + chainId + "' not found");
		return chain.getState();
	}

	public Date lastCommand(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null)
			throw new CiliaIllegalParameterException("chain id is null");
		ChainRuntime chain = ciliaContext.getChainRuntime(chainId);
		if (chain == null)
			throw new CiliaIllegalStateException("'" + chainId + "' not found");
		return chain.lastCommand();
	}

	public SetUp[] nodeSetup(String ldapFilter) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException {
		Node[] nodes = findNodeByFilter(ldapFilter, false);
		Set set = new HashSet();
		for (int i = 0; i < nodes.length; i++) {
			try {
				set.add(nodeSetup(nodes[i]));
			} catch (CiliaIllegalStateException e) {
			}
		}
		return (SetUp[]) set.toArray(new SetUp[set.size()]);
	}


	public RawData[] nodeRawData(String ldapFilter) throws CiliaInvalidSyntaxException,
			CiliaIllegalParameterException {
		Node[] nodes = findNodeByFilter(ldapFilter, false);
		Set set = new HashSet();
		for (int i = 0; i < nodes.length; i++) {
			try {
				set.add(nodeRawData(nodes[i]));
			} catch (CiliaIllegalStateException e) {
			}
		}
		return (RawData[]) set.toArray(new RawData[set.size()]);
	}

	public RawData nodeRawData(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		return new RawDataImpl(registry, new NodeImpl(node));
	}

	public Thresholds[] nodeMonitoring(String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	public Thresholds nodeMonitoring(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		// TODO Auto-generated method stub

		return null;
	}

}
