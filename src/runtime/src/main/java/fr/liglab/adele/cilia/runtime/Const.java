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

package fr.liglab.adele.cilia.runtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.framework.IDispatcher;
import fr.liglab.adele.cilia.framework.IProcessor;
import fr.liglab.adele.cilia.framework.IScheduler;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.model.impl.ConstModel;

public class Const implements ConstModel {

	/** tags **/
	public static final String FROM = "from";

	public static final String TO = "to";

	public static final String NAME = "name";

	public static final String ID = "id";

	public static final String PROTOCOL = "protocol";

	// public static final String PROTOCOL_DEFAULT="ea";

	public static final String PROPERTY = "property";

	public static final String PROPERTIES = "properties";

	public static final String VALUE = "value";

	public static final String TOPIC = "topic";

	public static final String COLLECTORS = "collectors";

	public static final String SCHEDULER = INSTANCE_TYPE_SCHEDULER;

	public static final String SENDERS = "senders";

	public static final String SENDER = "sender";

	public static final String SEND = INSTANCE_TYPE_SENDER;

	public static final String DISPATCH = "dispatcher";

	public static final String IN_PROCESS_METHOD = "in-method";

	public static final String OUT_PROCESS_METHOD = "out-method";

	public static final String DEFAULT_PROCESS_METHOD = "process";

	/** Handler */
	public static final String HANDLER_NAME = "mediator";

	public static final String HANDLER_NAMESPACE = "org.ciliamediation.handler";

	public static final String LOG_PREFIX = "[collector-handler]";

	/** commands **/
	public static final String COMMAND_TYPE_SCHEDULER = "scheduler";
	public static final String COMMAND_TYPE_DISPATCHER = DISPATCH;

	/** Default Components */
	public static final String DEFAULT_SCHEDULER = "DefaultScheduler";

	public static final String DEFAULT_DISPATCHER = "DefaultDispatcher";

	public static final String COLLECTOR_TYPE = ICollector.class.getName();// "org.ciliamediation.framework.Collector";

	public static final String SCHEDULER_TYPE = IScheduler.class.getName();// "org.ciliamediation.framework.Scheduler";

	public static final String DISPATCHER_TYPE = IDispatcher.class.getName();// "org.ciliamediation.framework.Dispatcher";

	public static final String SENDER_TYPE = ISender.class.getName();// "org.ciliamediation.framework.Sender";

	public static final String MEDIATOR_TYPE = IProcessor.class.getName();// "org.ciliamediation.framework.Sender";

	public final static String CILIA_LOG = "cilia.log.level";

	/*
	 * Trace runtime INFO = chain Start/Stop , mediator start/Stop DEBUG =
	 * incoming messages on each mediator /adapter
	 */
	public final static String LOGGER_CORE = "cilia.runtime.core";

	public final static String LOGGER_ADAPTATION = "cilia.runtime.adaptation";

	public static final String LOGGER_KNOWLEDGE = "cilia.runtime.knowledge"; 
	
	/*
	 * Build a default Cilia Qualified Name String
	 */
	public static final String ciliaQualifiedName(String name) {
		StringBuffer sb = new StringBuffer().append(Const.CILIA_NAMESPACE);
		sb.append(":").append(name);
		return sb.toString();
	}

	/* ---- Runtime ---- */
	/*
	 * Unique ID identifier
	 */
	public static final String UUID = "uuid";
	/*
	 * chain
	 */
	public static final String CHAIN_ID = "chain";
	/*
	 * Cilia components (adapters, mediators)
	 */
	public static final String NODE_ID = "node";

	/*
	 * Node creation timeStamp
	 */
	public static final String TIMESTAMP = "timestamp";
	/*
	 * Variable
	 */
	public static final String VARIABLE_ID = "variable";
	/*
	 * logger name
	 */

	/*
	 * #items stored per state variables
	 */
	public static final int DEFAULT_QUEUE_SIZE = 1;


	/* Topic Event Admin between base level and monitoring model */
	public static final String TOPIC_HEADER = "cilia/runtime/statevariable/";

	public static final String EVENT_TYPE ="type" ;
	public static final int TYPE_DATA = 0 ;
	public static final int TYPE_STATUS_NODE = 1 ;
	public static final int TYPE_STATUS_VARIABLE = 2 ;

	
	/**
	 * concat 2 arrays
	 * 
	 * @param first
	 * @param second
	 * @return first+second
	 */
	public static final Node[] concat(Node[] first, Node[] second) {
		Node[] result = (Node[]) Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static final Set ldapKeys;
	static {
		Set set = new HashSet();
		set.add(UUID);
		set.add(CHAIN_ID);
		set.add(NODE_ID);
		set.add(VARIABLE_ID);
		ldapKeys = Collections.unmodifiableSet(set);
	}

	public synchronized static final Filter createFilter(String filter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		if (filter == null)
			throw new CiliaIllegalParameterException("filter is null !");
		boolean found = false;
		/* at least one keyword is required */
		Iterator it = ldapKeys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (filter.contains(key)) {
				found = true;
				break;
			}
		}
		if (found == false)
			throw new CiliaIllegalParameterException("missing ldap filter keyword "
					+ ldapKeys.toString() + "!" + filter);
		try {
			return FrameworkUtil.createFilter(filter);
		} catch (InvalidSyntaxException e) {
			throw new CiliaInvalidSyntaxException(e.getMessage(), e.getFilter());
		}
	}

	public static final boolean isFilterMatching(Filter filter, Node node) {
		Dictionary dico = new Hashtable(4);

		dico.put(UUID, node.uuid());
		dico.put(CHAIN_ID, node.chainId());
		dico.put(NODE_ID, node.nodeId());
		dico.put(TIMESTAMP, new Long(node.timeStamp()));
		return filter.match(dico);

	}

	public static final boolean isFilterMatching(Filter filter, Node node, String variable) {
		Dictionary dico = new Hashtable(5);

		dico.put(UUID, node.uuid());
		dico.put(CHAIN_ID, node.chainId());
		dico.put(NODE_ID, node.nodeId());
		dico.put(TIMESTAMP, new Long(node.timeStamp()));
		dico.put(VARIABLE_ID, variable);
		return filter.match(dico);

	}

}
