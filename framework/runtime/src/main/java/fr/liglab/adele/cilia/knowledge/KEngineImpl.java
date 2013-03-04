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

package fr.liglab.adele.cilia.knowledge;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.reflect.ComponentMetadata;

import fr.liglab.adele.cilia.AdminData;
import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.EventsConfiguration;
import fr.liglab.adele.cilia.Measure;
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
import fr.liglab.adele.cilia.knowledge.configuration.RawDataImpl;
import fr.liglab.adele.cilia.knowledge.configuration.SetUpImpl;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.ChainRuntime;
import fr.liglab.adele.cilia.runtime.FirerEvents;
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

	private Eventing eventsConfig;
	private ListNodes registry;
	private BaseLevelListener baseLevelListener;
	private BundleContext bcontext;

	public KEngineImpl(BundleContext bc, CiliaContainer cc, Eventing evtManager) {
		super(cc);
		bcontext = bc;
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
		Node[] node = findNodeByFilter("(&(chain=*)(node=*))", false);
		for (int i = 0; i < node.length; i++) {
			/*
			 * Event arrival for all node present , and the state valid or
			 * invalid
			 */
			eventsConfig.fireEventNode(eventsConfig.EVT_ARRIVAL, node[i]);
			try {
				if (getModel(node[i]).isRunning())
					eventsConfig.fireEventNode(eventsConfig.EVT_VALID, node[i]);
				else
					eventsConfig.fireEventNode(eventsConfig.EVT_INVALID, node[i]);
			} catch (CiliaIllegalStateException e) {
			}
		}
	}

	public void removeListener(NodeCallback listener)
			throws CiliaIllegalParameterException {
		eventsConfig.removeListener(listener);
	}

	public void addListener(String ldapFilter, ChainCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapFilter, listener);
		Set chainSet = ciliaContainer.getAllChains();
		/* Iterate over all chains and fire event arrival and state */
		Iterator it = chainSet.iterator();
		while (it.hasNext()) {
			String chain = (String) (it.next());
			eventsConfig.fireEventChain(eventsConfig.EVT_ARRIVAL, chain);
			if (ciliaContainer.getChainRuntime(chain).getState() == CHAIN_STATE_STARTED)
				eventsConfig.fireEventChain(eventsConfig.EVT_STARTED, chain);
			else
				eventsConfig.fireEventChain(eventsConfig.EVT_STOPPED, chain);
		}
	}

	public void removeListener(ChainCallback listener)
			throws CiliaIllegalParameterException {
		eventsConfig.removeListener(listener);
	}

	public void addListener(String ldapfilter, ThresholdsCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapfilter, listener);
		RawData rawdata;
		Node[] node = findNodeByFilter("(&(chain=*)(node=*))", false);
		for (int i = 0; i < node.length; i++) {
			try {
				rawdata = nodeRawData(node[i]);
				String[] variable = rawdata.getAllEnabledVariable();
				Measure[] measure;
				int thresold;
				/* fire events for all enabled variables */
				for (int j = 0; j < variable.length; j++) {
					/* checks all measures out of bound */
					measure = rawdata.measures(variable[j]);
					for (int k = 0; i < measure.length; k++) {
						thresold = rawdata.viability(variable[i], measure[k]);
						if (thresold != ThresholdsCallback.NONE) {
							eventsConfig.fireThresholdEvent(node[i], variable[j],
									measure[k], thresold);
						}
					}
				}
			} catch (CiliaIllegalStateException e) {
			}
		}
	}

	public void addListener(String ldapfilter, VariableCallback listener)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		eventsConfig.addListener(ldapfilter, listener);
		/* Fire current state for all configured variables */
		RawData rawdata;
		Node[] node = findNodeByFilter("(&(chain=*)(node=*))", false);
		for (int i = 0; i < node.length; i++) {
			try {
				rawdata = nodeRawData(node[i]);
				String[] variable = rawdata.getAllEnabledVariable();
				Measure[] measure;
				/* fire events for all enabled variables */
				for (int j = 0; j < variable.length; j++) {
					/* fire the state */
					eventsConfig.fireEventVariableStatus(node[i], variable[j], true);
					/* fire the latest measure */
					measure = rawdata.measures(variable[j]);
					if (measure.length > 0) {
						eventsConfig.fireEventMeasure(node[i], variable[j], measure[0]);
					}
				}
			} catch (CiliaIllegalStateException e) {

			}
		}
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
		if (ciliaContainer.getChain(chainId) == null) {
			throw new CiliaIllegalStateException("chain " + chainId + " not found");
		}
		ciliaContainer.startChain(chainId);
	}

	public void stopChain(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null) {
			throw new CiliaIllegalParameterException("Chain Id is null");
		}
		if (ciliaContainer.getChain(chainId) == null) {
			throw new CiliaIllegalStateException("chain " + chainId + " not found");
		}
		ciliaContainer.stopChain(chainId);
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
		ChainRuntime chain = ciliaContainer.getChainRuntime(chainId);
		if (chain == null)
			throw new CiliaIllegalStateException("'" + chainId + "' not found");
		return chain.getState();
	}

	public Date lastCommand(String chainId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		if (chainId == null)
			throw new CiliaIllegalParameterException("chain id is null");
		ChainRuntime chain = ciliaContainer.getChainRuntime(chainId);
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
		Node[] nodes = findNodeByFilter(ldapFilter, false);
		Set set = new HashSet();
		for (int i = 0; i < nodes.length; i++) {
			try {
				set.add(nodeSetup(nodes[i]));
			} catch (CiliaIllegalStateException e) {
			}
		}
		return (Thresholds[]) set.toArray(new Thresholds[set.size()]);
	}

	public Thresholds nodeMonitoring(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		return new SetUpImpl(registry, new NodeImpl(node));
	}

	public Map getBufferedData(Node node) throws CiliaIllegalParameterException {
		ServiceReference refData = getAdminDataReference(node);
		Map returnedMap = null;
		if (refData == null) {
			return null;
		}
		AdminData admin = (AdminData) bcontext.getService(refData);
		returnedMap = new HashMap(admin.getData(node.nodeId(), true));
		bcontext.ungetService(refData);
		return returnedMap;
	}

	public boolean copyData(Node from, Node to) throws CiliaIllegalParameterException {
		if (from.chainId().compareTo(to.chainId()) != 0) {
			throw new CiliaIllegalParameterException(
					"both nodes must be on the same chain");
		}
		ServiceReference refData = getAdminDataReference(from);
		if (refData == null) {
			return false;
		}
		AdminData admin = (AdminData) bcontext.getService(refData);
		admin.copyData(from.nodeId(), to.nodeId());
		return true;
	}

	private ServiceReference getAdminDataReference(Node node)
			throws CiliaIllegalParameterException {
		ServiceReference[] refs = null;
		ServiceReference refData = null;
		Map returnedMap = null;
		if (node == null) {
			throw new CiliaIllegalParameterException("Node is empty");
		}
		try {
			refs = bcontext.getServiceReferences(AdminData.class.getName(),
					"(chain.name=" + node.chainId() + ")");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Admin data service lookup unrecoverable error");
		}
		if (refs != null)
			refData = refs[0];
		else {
			return null;
		}
		return refData;
	}

}
