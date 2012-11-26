/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.liglab.adele.cilia.dependency;

import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.IPojoContext;
import org.apache.felix.ipojo.PolicyServiceContext;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.util.Const;

/**
 * The dependency handler manages a list of service dependencies.
 * 
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class DependencyHandler extends PrimitiveHandler implements
		DependencyStateListener {
	public static Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_RUNTIME);

	/**
	 * This filter Id is used when no id has been defined
	 */
	public static final String DEFAULT_FILTER_NAME = "fr.liglab.adele.cilia.default-filter-id";
	/**
	 * Proxy settings property.
	 */
	public static final String PROXY_SETTINGS_PROPERTY = "ipojo.proxy";

	/**
	 * Proxy type property.
	 */
	public static final String PROXY_TYPE_PROPERTY = "ipojo.proxy.type";

	/**
	 * Proxy type value: smart.
	 */
	public static final String SMART_PROXY = "smart";

	/**
	 * Proxy type value: dynamic-proxy.
	 */
	public static final String DYNAMIC_PROXY = "dynamic-proxy";

	/**
	 * Proxy settings value: enabled.
	 */
	public static final String PROXY_ENABLED = "enabled";

	/**
	 * Proxy settings value: disabled.
	 */
	public static final String PROXY_DISABLED = "disabled";

	/**
	 * Dependency field type : Vector The dependency will be injected as a
	 * vector.
	 */
	protected static final int VECTOR = 2;

	/**
	 * Dependency Field Type : List. The dependency will be injected as a list.
	 */
	protected static final int LIST = 1;

	/**
	 * Dependency Field Type : Set. The dependency will be injected as a set.
	 */
	protected static final int SET = 3;

	/**
	 * List of dependencies of the component.
	 */
	private Dependency[] m_dependencies = new Dependency[0];

	/**
	 * Is the handler started.
	 */
	private boolean m_started;

	/**
	 * The handler description.
	 */
	private DependencyHandlerDescription m_description;

	/**
	 * Add a dependency.
	 * 
	 * @param dep
	 *            : the dependency to add
	 */
	private void addDependency(Dependency dep) {
		for (int i = 0; m_dependencies != null && i < m_dependencies.length; i++) {
			if (m_dependencies[i] == dep) {
				return;
			}
		}
		if (m_dependencies == null) {
			m_dependencies = new Dependency[] { dep };
		} else {
			Dependency[] newDep = new Dependency[m_dependencies.length + 1];
			System.arraycopy(m_dependencies, 0, newDep, 0, m_dependencies.length);
			newDep[m_dependencies.length] = dep;
			m_dependencies = newDep;
		}
	}

	/**
	 * Get the list of managed dependency.
	 * 
	 * @return the dependency list
	 */
	public Dependency[] getDependencies() {
		return m_dependencies;
	}

	/**
	 * Validate method. This method is invoked by an AbstractServiceDependency
	 * when this dependency becomes RESOLVED.
	 * 
	 * @param dep
	 *            : the dependency becoming RESOLVED.
	 * @see org.apache.felix.ipojo.util.DependencyStateListener#validate(org.apache.felix.ipojo.util.DependencyModel)
	 */
	public void validate(DependencyModel dep) {
		checkContext();
	}

	/**
	 * Invalidate method. This method is invoked by an AbstractServiceDependency
	 * when this dependency becomes UNRESOLVED or BROKEN.
	 * 
	 * @param dep
	 *            : the dependency becoming UNRESOLVED or BROKEN.
	 * @see org.apache.felix.ipojo.util.DependencyStateListener#invalidate(org.apache.felix.ipojo.util.DependencyModel)
	 */
	public void invalidate(DependencyModel dep) {
		setValidity(false);
	}

	/**
	 * Check the validity of the dependencies.
	 */
	protected void checkContext() {
		if (!m_started) {
			return;
		}
		synchronized (m_dependencies) {
			// Store the initial state
			boolean initialState = getValidity();

			boolean valid = true;
			for (int i = 0; i < m_dependencies.length; i++) {
				Dependency dep = m_dependencies[i];
				if (dep.getState() != Dependency.RESOLVED) {
					valid = false;
					break;
				}
			}

			// Check the component dependencies
			if (valid) {
				// The dependencies are valid
				if (!initialState) {
					// There is a state change
					setValidity(true);
				}
				// Else do nothing, the component state stay VALID
			} else {
				// The dependencies are not valid
				if (initialState) {
					// There is a state change
					setValidity(false);
				}
				// Else do nothing, the component state stay UNRESOLVED
			}

		}
	}

	/**
	 * Check if the dependency given is valid in the sense that metadata are
	 * consistent.
	 * 
	 * @param dep
	 *            : the dependency to check
	 * @param manipulation
	 *            : the component-type manipulation metadata
	 * @return true if the dependency is valid
	 * @throws ConfigurationException
	 *             : the checked dependency is not correct
	 */
	private boolean checkDependency(Dependency dep, PojoMetadata manipulation)
			throws ConfigurationException {
		// Check the internal type of dependency
		String field = dep.getField();
		DependencyCallback[] callbacks = dep.getCallbacks();
		int index = dep.getConstructorParameterIndex();

		if (callbacks == null && field == null && index == -1) {
			throw new ConfigurationException(
					"A service requirement requires at least binding methods, "
							+ "a field or a constructor parameter");
		}

		for (int i = 0; callbacks != null && i < callbacks.length; i++) {
			MethodMetadata[] mets = manipulation.getMethods(callbacks[i].getMethodName());
			if (mets.length == 0) {
				debug("A requirement callback "
						+ callbacks[i].getMethodName()
						+ " does not exist in the implementation class, will try the super classes");
			} else {
				if (mets[0].getMethodArguments().length > 2) {
					throw new ConfigurationException(
							"Requirement Callback : A requirement callback "
									+ callbacks[i].getMethodName()
									+ " must have 0, 1 or 2 arguments");
				}

				callbacks[i].setArgument(mets[0].getMethodArguments());

				if (mets[0].getMethodArguments().length == 1) {
					if (!mets[0].getMethodArguments()[0].equals(ServiceReference.class
							.getName())) {
						// The callback receives the service object.
						setSpecification(dep, mets[0].getMethodArguments()[0], false); // Just
																						// warn
																						// if
																						// a
																						// mismatch
																						// is
																						// discovered.
					}
				} else if (mets[0].getMethodArguments().length == 2) {
					// The callback receives service object, service reference.
					// Check that the second argument is a service reference
					if (!(mets[0].getMethodArguments()[1].equals(ServiceReference.class
							.getName()) // callback with (service object,
										// service reference)
							|| mets[0].getMethodArguments()[1].equals(Dictionary.class
									.getName()) // callback with (service
												// object, service properties in
												// a dictionary)
					|| mets[0].getMethodArguments()[1].equals(Map.class.getName()))) { // callback
																						// with
																						// (service
																						// object,
																						// service
																						// properties
																						// in
																						// a
																						// map)
						String message = "The requirement callback "
								+ callbacks[i].getMethodName()
								+ " must have a ServiceReference, a Dictionary or a Map as the second argument";
						throw new ConfigurationException(message);
					}
					setSpecification(dep, mets[0].getMethodArguments()[0], false); // Just
																					// warn
																					// if
																					// a
																					// mismatch
																					// is
																					// discovered.
				}
			}

		}

		if (field != null) {
			FieldMetadata meta = manipulation.getField(field);
			if (meta == null) {
				throw new ConfigurationException(
						"Requirement Callback : A requirement field " + field
								+ " does not exist in the implementation class");
			}
			String type = meta.getFieldType();
			if (type.endsWith("[]")) {
				if (dep.isProxy()) {
					info("Arrays cannot be used for proxied dependencies - Disabling the proxy mode");
					dep.setProxy(false);
				}
				// Set the dependency to multiple
				dep.setAggregate(true);
				type = type.substring(0, type.length() - 2);
			} else if (type.equals(List.class.getName())
					|| type.equals(Collection.class.getName())) {
				dep.setType(LIST);
				type = null;
			} else if (type.equals(Vector.class.getName())) {
				dep.setType(VECTOR);
				if (dep.isProxy()) {
					warn("Vectors cannot be used for proxied dependencies - Disabling the proxy mode");
					dep.setProxy(false);
				}
				type = null;
			} else if (type.equals(Set.class.getName())) {
				dep.setType(SET);
				type = null;
			} else {
				if (dep.isAggregate()) {
					throw new ConfigurationException(
							"A required service is not correct : the field "
									+ meta.getFieldName()
									+ " must be an array to support aggregate injections");
				}
			}
			setSpecification(dep, type, true); // Throws an exception if the
												// field type mismatch.
		}

		// Constructor parameter
		if (index != -1) {
			if (!dep.isProxy()) {
				throw new ConfigurationException(
						"Services injected into constructor must be proxied");
			}

			MethodMetadata[] cts = manipulation.getConstructors();
			// If we don't have a type, try to get the first constructor and get
			// the type of the parameter
			// we the index 'index'.
			if (cts.length > 0 && cts[0].getMethodArguments().length > index) {
				String type = cts[0].getMethodArguments()[index];
				if (type.endsWith("[]")) {
					throw new ConfigurationException(
							"Services injected into constructor cannot be arrays");
				} else if (type.equals(List.class.getName())
						|| type.equals(Collection.class.getName())) {
					dep.setType(LIST);
					type = null;
				} else if (type.equals(Vector.class.getName())) {
					throw new ConfigurationException(
							"Services injected into constructor cannot be Vectors");
				} else if (type.equals(Set.class.getName())) {
					dep.setType(SET);
					type = null;
				} else {
					if (dep.isAggregate()) {
						throw new ConfigurationException(
								"A required service is not correct : the constructor parameter "
										+ index
										+ " must be an aggregate type to support aggregate injections");
					}
				}
				setSpecification(dep, type, true); // Throws an exception if the
													// field type mismatch.
			} else {
				throw new ConfigurationException(
						"Cannot determine the specification of the dependency " + index
								+ ", please use the specification attribute");
			}
		}

		// Disable proxy on scalar dependency targeting non-interface
		// specification
		if (!dep.isAggregate() && dep.isProxy()) {
			if (!dep.getSpecification().isInterface()) {
				warn("Proxies cannot be used on service dependency targetting non interface "
						+ "service specification " + dep.getSpecification().getName());
				dep.setProxy(false);
			}
		}

		// Disables proxy on null (nullable=false)
		// if (dep.isProxy() && dep.isOptional() && ! dep.supportsNullable()) {
		// dep.setProxy(false);
		// warn("Optional Null Dependencies do not support proxying - Disable the proxy mode");
		// }

		// Check that all required info are set
		return dep.getSpecification() != null;
	}

	/**
	 * Check if we have to set the dependency specification with the given class
	 * name.
	 * 
	 * @param dep
	 *            : dependency to check
	 * @param className
	 *            : class name
	 * @param error
	 *            : set to true to throw an error if the set dependency
	 *            specification and the given specification are different.
	 * @throws ConfigurationException
	 *             : the specification class cannot be loaded correctly
	 */
	private void setSpecification(Dependency dep, String className, boolean error)
			throws ConfigurationException {
		if (className == null) {
			// No found type (list and vector)
			if (dep.getSpecification() == null) {
				if (error) {
					String id = dep.getId();
					if (id == null) {
						id = dep.getField();
						if (id == null) {
							id = Integer.toString(dep.getConstructorParameterIndex());
						}
					}
					throw new ConfigurationException(
							"Cannot discover the required specification for " + id);
				} else {
					// If the specification is different, warn that we will
					// override it.
					info("Cannot discover the required specification for "
							+ dep.getField());
				}
			}
		} else { // In all other case, className is not null.
			if (dep.getSpecification() == null
					|| !dep.getSpecification().getName().equals(className)) {
				if (dep.getSpecification() != null) {
					if (error) {
						throw new ConfigurationException(
								"A required service is not correct : the discovered type ["
										+ className
										+ "] and the specified (or already discovered)  service interface ["
										+ dep.getSpecification().getName()
										+ "] are not the same");
					} else {
						// If the specification is different, warn that we will
						// override it.
						warn("[" + getInstanceManager().getInstanceName()
								+ "] The field type [" + className
								+ "] and the required service interface ["
								+ dep.getSpecification() + "] are not the same");
					}
				}

				try {
					dep.setSpecification(getInstanceManager().getContext().getBundle()
							.loadClass(className));
				} catch (ClassNotFoundException e) {
					throw new ConfigurationException(
							"The required service interface cannot be loaded : "
									+ e.getMessage());
				}
			}
		}
	}

	/**
	 * Configure the handler.
	 * 
	 * @param componentMetadata
	 *            : the component type metadata
	 * @param configuration
	 *            : the instance configuration
	 * @throws ConfigurationException
	 *             : one dependency metadata is not correct.
	 * @see org.apache.felix.ipojo.Handler#configure(org.apache.felix.ipojo.InstanceManager,
	 *      org.apache.felix.ipojo.metadata.Element, java.util.Dictionary)
	 */
	public void configure(Element componentMetadata, Dictionary configuration)
			throws ConfigurationException {
		// getPojoMetadata();
		PojoMetadata manipulation = getFactory().getPojoMetadata();
		boolean atLeastOneField = false;

		// Create the dependency according to the component metadata
		Element[] deps = componentMetadata.getElements("dependency",
				Const.CILIA_NAMESPACE);
		// Get instance filters.
		Dictionary filtersConfiguration = getRequiresFilters(configuration
				.get("requires.filters"));

		Dictionary fromConfiguration = (Dictionary) configuration.get("requires.from");

		String m_cardinality = (String) configuration.get("cardinality");

		/* the policy is now set by the component configuration instance */
		int policy = Dependency.getPolicy((String) configuration.get("policy"));
		String m_immediate = (String) configuration.get("immediate");
		boolean isImmediate;
		/* 
		 * True = default value, injection is done on Service Arrival
		 * false = injection is done on Service Departure , when a service is leaving
		 */
		if ((m_immediate != null) && (m_immediate.equalsIgnoreCase("false"))) {
			isImmediate = false;
		} else {
			/* default value = true */
			isImmediate = true;
		}
		
		for (int i = 0; deps != null && i < deps.length; i++) {
			// Create the dependency metadata
			String field = deps[i].getAttribute("field");

			String serviceSpecification = deps[i].getAttribute("interface");
			// the 'interface' attribute is deprecated
			if (serviceSpecification != null) {
				warn("The 'interface' attribute is deprecated, use the 'specification' attribute instead");
			} else {
				serviceSpecification = deps[i].getAttribute("specification");
			}

			String filter = deps[i].getAttribute("filter");
			String opt = deps[i].getAttribute("optional");
			boolean optional = opt != null && opt.equalsIgnoreCase("true");
			String defaultImplem = deps[i].getAttribute("default-implementation");

			String agg = deps[i].getAttribute("aggregate");
			boolean aggregate = agg != null && agg.equalsIgnoreCase("true");
			String identitity = deps[i].getAttribute("id");
			if (identitity == null) {
				/* set default value */
				identitity = DEFAULT_FILTER_NAME;
			}

			String nul = deps[i].getAttribute("nullable");
			boolean nullable = nul == null || nul.equalsIgnoreCase("true");

			boolean isProxy = true;
			// Detect proxy default value.
			String setting = getInstanceManager().getContext().getProperty(
					PROXY_SETTINGS_PROPERTY);
			if (setting == null || PROXY_ENABLED.equals(setting)) { // If not
																	// set =>
																	// Enabled
				isProxy = true;
			} else if (setting != null && PROXY_DISABLED.equals(setting)) {
				isProxy = false;
			}

			String proxy = deps[i].getAttribute("proxy");
			// If proxy == null, use default value
			if (proxy != null) {
				if (proxy.equals("false")) {
					isProxy = false;
				} else if (proxy.equals("true")) {
					if (!isProxy) { // The configuration overrides the system
									// setting
						warn("The configuration of a service dependency overrides the proxy mode");
					}
					isProxy = true;
				}
			}

			String scope = deps[i].getAttribute("scope");
			BundleContext context = getInstanceManager().getContext(); // Get
																		// the
																		// default
																		// bundle
																		// context.
			if (scope != null) {
				// If we are not in a composite, the policy is set to global.
				if (scope.equalsIgnoreCase("global")
						|| ((((IPojoContext) getInstanceManager().getContext())
								.getServiceContext()) == null)) {
					context = new PolicyServiceContext(getInstanceManager()
							.getGlobalContext(), getInstanceManager()
							.getLocalServiceContext(), PolicyServiceContext.GLOBAL);
				} else if (scope.equalsIgnoreCase("composite")) {
					context = new PolicyServiceContext(getInstanceManager()
							.getGlobalContext(), getInstanceManager()
							.getLocalServiceContext(), PolicyServiceContext.LOCAL);
				} else if (scope.equalsIgnoreCase("composite+global")) {
					context = new PolicyServiceContext(getInstanceManager()
							.getGlobalContext(), getInstanceManager()
							.getLocalServiceContext(),
							PolicyServiceContext.LOCAL_AND_GLOBAL);
				}
			}

			// Get instance filter if available
			if (filtersConfiguration != null && identitity != null
					&& filtersConfiguration.get(identitity) != null) {
				filter = (String) filtersConfiguration.get(identitity);
			}

			// Compute the 'from' attribute
			String from = deps[i].getAttribute("from");
			if (fromConfiguration != null && identitity != null
					&& fromConfiguration.get(identitity) != null) {
				from = (String) fromConfiguration.get(identitity);
			}
			if (from != null) {
				String fromFilter = "(|(instance.name=" + from + ")(service.pid=" + from
						+ "))";
				if (aggregate) {
					warn("The 'from' attribute is incompatible with aggregate requirements: only one provider will match : "
							+ fromFilter);
				}
				if (filter != null) {
					filter = "(&" + fromFilter + filter + ")"; // Append the two
																// filters
				} else {
					filter = fromFilter;
				}
			}

			Filter fil = null;
			if (filter != null) {
				try {
					fil = getInstanceManager().getContext().createFilter(filter);
				} catch (InvalidSyntaxException e) {
					throw new ConfigurationException("A requirement filter is invalid : "
							+ filter + " - " + e.getMessage());
				}
			}

			Class spec = null;
			if (serviceSpecification != null) {
				spec = DependencyModel.loadSpecification(serviceSpecification,
						getInstanceManager().getContext());
			}

			// int policy = DependencyModel.getPolicy(deps[i]);
			Comparator cmp = DependencyModel.getComparator(deps[i], getInstanceManager()
					.getGlobalContext());
			Dependency dep;
			int cardinalityMax =getCardinalityMaximum(m_cardinality);
			if ((cardinalityMax <=0) && (isImmediate)) {
				/* Default implementation */
				dep = new Dependency(this, field, spec, fil, optional, aggregate,
						nullable, isProxy, identitity, context, policy, cmp,
						defaultImplem);
			} else {
				/* Force Flag aggregation */
				aggregate = true;
				/* Force flag optional if min cardinality is 0 */
				checkCardinality(m_cardinality) ;
				if (isMinCardinalityOptionnal(m_cardinality)) {
					optional = true;
				}
				dep = new Dependency(this, field, spec, fil, optional, aggregate,
						nullable, isProxy, identitity, context, policy, cmp,
						defaultImplem, cardinalityMax,isImmediate);
			}

			// Look for dependency callback :
			Element[] cbs = deps[i].getElements("Callback");
			for (int j = 0; cbs != null && j < cbs.length; j++) {
				if (!cbs[j].containsAttribute("method")
						&& cbs[j].containsAttribute("type")) {
					throw new ConfigurationException(
							"Requirement Callback : a dependency callback must contain a method and a type (bind or unbind) attribute");
				}
				String method = cbs[j].getAttribute("method");
				String type = cbs[j].getAttribute("type");
				int methodType = 0;
				if ("bind".equalsIgnoreCase(type)) {
					methodType = DependencyCallback.BIND;
				} else if ("modified".equalsIgnoreCase(type)) {
					methodType = DependencyCallback.MODIFIED;
				} else {
					methodType = DependencyCallback.UNBIND;
				}

				DependencyCallback callback = new DependencyCallback(dep, method,
						methodType);
				dep.addDependencyCallback(callback);
			}

			// Add the constructor parameter if needed
			String paramIndex = deps[i].getAttribute("constructor-parameter");
			if (paramIndex != null) {
				int index = Integer.parseInt(paramIndex);
				dep.addConstructorInjection(index);
			}

			// Check the dependency :
			if (checkDependency(dep, manipulation)) {
				addDependency(dep);
				if (dep.getField() != null) {
					getInstanceManager().register(manipulation.getField(dep.getField()),
							dep);
					atLeastOneField = true;
				}
			}
		}

		if (atLeastOneField) { // Does register only if we have fields
			MethodMetadata[] methods = manipulation.getMethods();
			for (int i = 0; i < methods.length; i++) {
				for (int j = 0; j < m_dependencies.length; j++) {
					getInstanceManager().register(methods[i], m_dependencies[j]);
				}
			}
		}

		m_description = new DependencyHandlerDescription(this, m_dependencies); // Initialize
																				// the
																				// description.
	}

	/**
	 * Gets the requires filter configuration from the given object. The given
	 * object must come from the instance configuration. This method was made to
	 * fix FELIX-2688. It supports filter configuration using an array:
	 * <code>{"myFirstDep", "(property1=value1)", "mySecondDep", "(property2=value2)"});</code>
	 * 
	 * @param requiresFiltersValue
	 *            the value contained in the instance configuration.
	 * @return the dictionary. If the object in already a dictionary, just
	 *         returns it, if it's an array, builds the dictionary.
	 * @throws ConfigurationException
	 *             the dictionary cannot be built
	 */
	private Dictionary getRequiresFilters(Object requiresFiltersValue)
			throws ConfigurationException {
		if (requiresFiltersValue != null && requiresFiltersValue.getClass().isArray()) {
			String[] filtersArray = (String[]) requiresFiltersValue;
			if (filtersArray.length % 2 != 0) {
				throw new ConfigurationException("A requirement filter is invalid : "
						+ requiresFiltersValue);
			}
			Dictionary requiresFilters = new Hashtable();
			for (int i = 0; i < filtersArray.length; i += 2) {
				requiresFilters.put(filtersArray[i], filtersArray[i + 1]);
			}
			return requiresFilters;
		}

		return (Dictionary) requiresFiltersValue;
	}

	/**
	 * Handler start method.
	 * 
	 * @see org.apache.felix.ipojo.Handler#start()
	 */
	public void start() {
		// Start the dependencies
		for (int i = 0; i < m_dependencies.length; i++) {
			Dependency dep = m_dependencies[i];

			dep.start();
		}
		// Check the state
		m_started = true;
		setValidity(false);
		checkContext();
	}

	/**
	 * Handler stop method.
	 * 
	 * @see org.apache.felix.ipojo.Handler#stop()
	 */
	public void stop() {
		m_started = false;
		for (int i = 0; i < m_dependencies.length; i++) {
			m_dependencies[i].stop();
		}
	}

	/**
	 * Handler createInstance method. This method is override to allow delayed
	 * callback invocation.
	 * 
	 * @param instance
	 *            : the created object
	 * @see org.apache.felix.ipojo.Handler#onCreation(java.lang.Object)
	 */
	public void onCreation(Object instance) {
		for (int i = 0; i < m_dependencies.length; i++) {
			m_dependencies[i].onObjectCreation(instance);
		}
	}

	/**
	 * Get the dependency handler description.
	 * 
	 * @return the dependency handler description.
	 * @see org.apache.felix.ipojo.Handler#getDescription()
	 */
	public HandlerDescription getDescription() {
		return m_description;
	}


	/**
	 * Extract the cardinality minimum
	 * 
	 * @param aCardinality
	 * @return true/false 
	 * @throws ConfigurationException
	 */
	public static boolean isMinCardinalityOptionnal(String aCardinality)
			throws ConfigurationException {
		boolean optional = false;
		if ((aCardinality == null) || (aCardinality.length() == 0))
			return optional;
		int idx = aCardinality.indexOf(".");
		if (idx > 0) {
			try {
				if (Math.abs(Integer.parseInt(aCardinality.substring(0, idx))) == 0)
					optional = true;
			} catch (NumberFormatException e) {
				throw new ConfigurationException(
						"Cardinality bound min is not an integer :" + aCardinality);
			}
		}
		return optional;
	}

	/**
	 * Extract the cardinality max a cardinality max ="*" means no bound
	 * 
	 * @param aCardinality
	 * @return -1 no cardinality
	 * @throws ConfigurationException
	 */
	public static int getCardinalityMaximum(String aCardinality)
			throws ConfigurationException {
		int max =-1 ;
		String cardinality;

		if ((aCardinality == null) || (aCardinality.length() == 0))
			return max;

		int idx = aCardinality.indexOf(".");
		if (idx > 0) {
			cardinality = aCardinality.substring(idx + 2);
		} else {
			cardinality = aCardinality;
		}
		try {
			if (!cardinality.equals("*"))
				max = Integer.parseInt(cardinality);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Cardinality bound max is not an integer :"
					+ aCardinality);
		}
		return max;
	}
	
	/* check the syntax '0|1 ..b' or '0|1..*' */
	public static void checkCardinality(String aCardinality) throws ConfigurationException {
		if ((aCardinality ==null) || (aCardinality.length()==0) )return ;
		Pattern p = Pattern.compile("(0|1)..(\\d|\\*)");
		Matcher m = p.matcher(aCardinality);
		if (!m.matches()) throw new ConfigurationException("Cardinality syntax error "+aCardinality);
	}

}
