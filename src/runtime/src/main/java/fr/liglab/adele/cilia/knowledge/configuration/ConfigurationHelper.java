/* Copyright Adele Team LIG
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

package fr.liglab.adele.cilia.knowledge.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.knowledge.MediatorMonitoring;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.util.FrameworkUtils;

/**
 * Configuration utilities
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConfigurationHelper {
	/* Liste of state var */
	private static final Set setStateVar, setDependencyCall, setEventing, setSystemCall,
			setFunctionnalCall, dataflowKeys;
	static {
		/* State var Event fired by dependency Manager */
		setDependencyCall = new HashSet();
		setDependencyCall.add("service.arrival");
		setDependencyCall.add("service.departure");
		setDependencyCall.add("service.arrival.count");
		setDependencyCall.add("service.departure.count");

		/* state var type eventing */
		setEventing = new HashSet();
		setEventing.add("fire.event");
		setEventing.add("fire.event.count");

		/* State var type SystemCall */
		setSystemCall = new HashSet();
		/* Phase Collect */
		setSystemCall.add("scheduler.count");
		setSystemCall.add("scheduler.data");
		/* Phase Processing */
		setSystemCall.add("process.entry.count");
		setSystemCall.add("process.entry.data");
		setSystemCall.add("process.exit.count");
		setSystemCall.add("process.exit.data");
		setSystemCall.add("process.err.count");
		setSystemCall.add("process.err.data");
		setSystemCall.add("process.msg.treated");
		/* number of ticks for the phase Process */
		setSystemCall.add("processing.delay");
		/* Phase dispatching */
		setSystemCall.add("dispatch.count");
		setSystemCall.add("dispatch.data");
		setSystemCall.add("dispatch.msg.treated");
		/* Message history */
		setSystemCall.add("message.history");
		/* time between dispatch and collect */
		setSystemCall.add("transmission.delay");

		setFunctionnalCall = new HashSet();
		setFunctionnalCall.add("field.set");
		setFunctionnalCall.add("field.set.count");
		setFunctionnalCall.add("field.get");
		setFunctionnalCall.add("field.get.count");

		/* All state variables */
		setStateVar = new HashSet(setSystemCall);
		setStateVar.addAll(setDependencyCall);
		setStateVar.addAll(setEventing);
		setStateVar.addAll(setFunctionnalCall);

		Set set = new HashSet();
		set.add(FrameworkUtils.VALUE_CURRENT);
		set.add(FrameworkUtils.VALUE_PREVIOUS);
		set.add(FrameworkUtils.DELTA_ABSOLUTE);
		set.add(FrameworkUtils.DELTA_RELATIVE);
		set.add(FrameworkUtils.TIME_ELAPSED);
		set.add(FrameworkUtils.TIME_ELAPSED);
		set.add(FrameworkUtils.TIME_CURRENT);
		set.add(FrameworkUtils.TIME_PREVIOUS);
		dataflowKeys = Collections.unmodifiableSet(set);
	}

	public static String[] getCategories() {
		String[] array = { "SystemCall", "DependencyCall", "EventingCall",
				"FunctionnalCall" };
		return array;
	}

	public static String[] variablesByCategory(String category) {
		String[] array;
		if (category == null) {
			array = (String[]) setStateVar.toArray(new String[setStateVar.size()]);
		} else {
			if (category.equalsIgnoreCase("SystemCall"))
				array = (String[]) setSystemCall
						.toArray(new String[setSystemCall.size()]);
			else if (category.equalsIgnoreCase("DependencyCall")) {
				array = (String[]) setDependencyCall.toArray(new String[setDependencyCall
						.size()]);
			} else if (category.equalsIgnoreCase("EventingCall")) {
				array = (String[]) setEventing.toArray(new String[setEventing.size()]);
			} else if (category.equalsIgnoreCase("FunctionnalCall")) {
				array = (String[]) setEventing.toArray(new String[setFunctionnalCall
						.size()]);
			} else
				array = new String[0];
		}
		return array;
	}

	/* verify varaible name */
	public static void checkStateVarId(String variableId)
			throws CiliaIllegalParameterException {
		if (variableId == null)
			throw new CiliaIllegalParameterException("variable id must not be null");
		if (!setStateVar.contains(variableId))
			throw new CiliaIllegalParameterException("unknown state variable name :'"
					+ variableId + "'");
	}

	public static void checkQueueSize(int queue) throws CiliaIllegalParameterException {
		if (queue < 1)
			throw new CiliaIllegalParameterException("queue size must be >1");
	}

	/* build the ldap filter */
	public synchronized static final void checkDataFlowFilter(String filter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
		if ((filter == null) || (filter.length() == 0))
			return;
		boolean found = false;
		/* at least one keyword is required */
		Iterator it = dataflowKeys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (filter.contains(key)) {
				found = true;
				break;
			}
		}
		if (found == false)
			throw new CiliaIllegalParameterException("missing ldap filter keyword "
					+ dataflowKeys.toString() + "!" + filter);
		try {
			FrameworkUtil.createFilter(filter);
		} catch (InvalidSyntaxException e) {
			throw new CiliaInvalidSyntaxException(e.getMessage(), e.getFilter());
		}
	}

	public static void storeEnable(Map props, String stateVarId, boolean enable) {
		Set setEnable = (Set) props.get("enable");
		if (setEnable == null) {
			setEnable = new HashSet();
			props.put("enable", setEnable);
		}
		if (enable) {
			setEnable.add(stateVarId);
		} else
			setEnable.remove(stateVarId);
	}

	public static Set getEnabledVariable(Map props) {
		Set set = (Set) props.get("enabled");
		if (set == null)
			set = new HashSet();
		return set;
	}

	public static String getFlowControl(Map props, String variableId) {
		String flowControl = (String) props.get(variableId);
		if (flowControl == null)
			return "";
		else
			return flowControl;
	}

	public static void storeDataFlowControl(Map props, String stateVarId,
			String ldapfilter) {
		if (ldapfilter == null)
			props.put(stateVarId, "");
		else
			props.put(stateVarId, ldapfilter);
	}

	public static Map getRootConfig(MediatorComponent model) {

		MediatorMonitoring monitoring = getModelMonitoring(model);
		if (monitoring == null) {
			monitoring = new MediatorMonitoring();
			monitoring.setModel(model);
			model.addModel(MediatorMonitoring.NAME, monitoring);
		}
		Map config = (Map) model.getProperties().get(
				ConstRuntime.MONITORING_CONFIGURATION);
		if (config == null)
			config = new HashMap();
		return config;
	}

	public static void storeRootConfig(MediatorComponent model, Map config) {
		if (!config.isEmpty())
			model.setProperty(ConstRuntime.MONITORING_CONFIGURATION, config);
	}

	public static final MediatorMonitoring getModelMonitoring(MediatorComponent model) {
		return (MediatorMonitoring) model.getModel(MediatorMonitoring.NAME);
	}
}
