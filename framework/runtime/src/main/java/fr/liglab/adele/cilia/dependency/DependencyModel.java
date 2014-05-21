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

import fr.liglab.adele.cilia.util.Const;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.IPOJOServiceFactory;
import org.apache.felix.ipojo.context.ServiceReferenceImpl;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.ServiceReferenceRankingComparator;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Abstract dependency model. This class is the parent class of every service
 * dependency. It manages the most part of dependency management. This class
 * creates an interface between the service tracker and the concrete dependency.
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public abstract class DependencyModel implements TrackerCustomizer {
    protected static Logger logger = LoggerFactory
            .getLogger(Const.LOGGER_RUNTIME);

    /**
     * Dependency state : BROKEN. A broken dependency cannot be fulfilled
     * anymore. The dependency becomes broken when a used service disappears in
     * the static binding policy.
     */
    public static final int BROKEN = -1;

    /**
     * Dependency state : UNRESOLVED. A dependency is unresolved if the
     * dependency is not valid and no service providers are available.
     */
    public static final int UNRESOLVED = 0;

    /**
     * Dependency state : RESOLVED. A dependency is resolved if the dependency
     * is optional or at least one provider is available.
     */
    public static final int RESOLVED = 1;

    /**
     * Binding policy : Dynamic. In this policy, services can appears and
     * departs without special treatment.
     */
    public static final int DYNAMIC_BINDING_POLICY = 0;

    /**
     * Binding policy : Static. Once a service is used, if this service
     * disappears the dependency becomes {@link DependencyModel#BROKEN}. The
     * instance needs to be recreated.
     */
    public static final int STATIC_BINDING_POLICY = 1;

    /**
     * Binding policy : Dynamic-Priority. In this policy, services can appears
     * and departs. However, once a service with a highest ranking (according to
     * the used comparator) appears, this new service is re-injected.
     */
    public static final int DYNAMIC_PRIORITY_BINDING_POLICY = 2;

    /**
     * Does the dependency bind several providers ?
     */
    private boolean m_aggregate;

    /**
     * Is the dependency optional ?
     */
    private boolean m_optional;

    /**
     * The required specification. Cannot change once set.
     */
    private Class m_specification;

    /**
     * The comparator to sort service references.
     */
    private Comparator m_comparator;

    /**
     * The LDAP filter object selecting service references from the set of
     * providers providing the required specification.
     */
    private Filter m_filter;

    /**
     * Bundle context used by the dependency. (may be a {@link ServiceContext}).
     */
    private BundleContext m_context;

    /**
     * Listener object on which invoking the
     * {@link DependencyStateListener#validate(DependencyModel)} and
     * {@link DependencyStateListener#invalidate(DependencyModel)} methods.
     */
    private final DependencyStateListener m_listener;

    /**
     * The actual state of the dependency. {@link DependencyModel#UNRESOLVED} at
     * the beginning.
     */
    private int m_state;

    /**
     * The Binding policy of the dependency.
     */
    private int m_policy = DYNAMIC_BINDING_POLICY;

    /**
     * The tracker used by this dependency to track providers.
     */
    private Tracker m_tracker;

    /**
     * The list of matching service references. This list is a subset of tracked
     * references. This set is computed according to the filter and the
     * {@link DependencyModel#match(ServiceReference)} method.
     */
    private final List m_matchingRefs = new ArrayList();
    private final List m_matchingRefs_InWait = new ArrayList();

    /**
     * The instance requiring the service.
     */
    private final ComponentInstance m_instance;

    /**
     * Map {@link ServiceReference} -> Service Object. This map stores service
     * object, and so is able to handle iPOJO custom policies.
     */
    private Map/* <ServiceReference, Object> */m_serviceObjects = new HashMap();

    /**
     * Creates a DependencyModel. If the dependency has no comparator and
     * follows the {@link DependencyModel#DYNAMIC_PRIORITY_BINDING_POLICY}
     * policy the OSGi Service Reference Comparator is used.
     *
     * @param specification the required specification
     * @param aggregate     is the dependency aggregate ?
     * @param optional      is the dependency optional ?
     * @param filter        the LDAP filter
     * @param comparator    the comparator object to sort references
     * @param policy        the binding policy
     * @param context       the bundle context (or service context)
     * @param listener      the dependency lifecycle listener to notify from dependency
     * @param ci            instance managing the dependency state changes.
     */
    public DependencyModel(Class specification, boolean aggregate, boolean optional,
                           Filter filter, Comparator comparator, int policy, BundleContext context,
                           DependencyStateListener listener, ComponentInstance ci) {
        m_specification = specification;
        m_aggregate = aggregate;
        m_optional = optional;
        m_filter = filter;
        m_comparator = comparator;
        m_context = context;
        m_policy = policy;
        // If the dynamic priority policy is chosen, and we have no comparator,
        // fix it to OSGi standard service reference comparator.
        if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY && m_comparator == null) {
            m_comparator = new ServiceReferenceRankingComparator();
        }
        m_state = UNRESOLVED;
        m_listener = listener;
        m_instance = ci;
    }

    /**
     * Opens the tracking. This method computes the dependency state
     *
     * @see DependencyModel#computeDependencyState()
     */
    public void start() {
        m_state = UNRESOLVED;
        m_tracker = new Tracker(m_context, m_specification.getName(), this);
        m_tracker.open();
        computeDependencyState();
    }

    /**
     * Closes the tracking. The dependency becomes
     * {@link DependencyModel#UNRESOLVED} at the end of this method.
     */
    public void stop() {
        if (m_tracker != null) {
            m_tracker.close();
            m_tracker = null;
        }
        m_matchingRefs.clear();
        m_matchingRefs_InWait.clear();
        ungetAllServices();
        m_state = UNRESOLVED;
    }

    /**
     * Ungets all 'get' service references. This also clears the service object
     * map.
     */
    private void ungetAllServices() {
        Set entries = m_serviceObjects.entrySet();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            ServiceReference ref = (ServiceReference) entry.getKey();
            Object svc = entry.getValue();
            if (m_tracker != null) {
                m_tracker.ungetService(ref);
            }
            if (svc instanceof IPOJOServiceFactory) {
                ((IPOJOServiceFactory) svc).ungetService(m_instance, svc);
            }
        }
        m_serviceObjects.clear();
    }

    /**
     * Is the reference set frozen (cannot change anymore)? This method must be
     * override by concrete dependency to support the static binding policy. In
     * fact, this method allows optimizing the static dependencies to become
     * frozen only when needed. This method returns <code>false</code> by
     * default. The method must always return <code>false</code> for non-static
     * dependencies.
     *
     * @return <code>true</code> if the reference set is frozen.
     */
    public boolean isFrozen() {
        return false;
    }

    public boolean isImmediate() {
        return true;
    }

    public int getMaxServiceInjected() {
        return -1;
    }


    public boolean isServicesInWaitToInject() {
        return false;
    }

    /**
     * Unfreezes the dependency. This method must be overide by concrete
     * dependency to support the static binding policy. This method is called
     * after tracking restarting.
     */
    public void unfreeze() {
        // nothing to do
    }

    /**
     * Does the service reference match ? This method must be override by
     * concrete dependencies if they need advanced testing on service reference
     * (that cannot be expressed in the LDAP filter). By default this method
     * returns <code>true</code>.
     *
     * @param ref the tested reference.
     * @return <code>true</code> if the service reference matches.
     */
    public boolean match(ServiceReference ref) {
        return true;
    }

    /**
     * Computes the actual dependency state. This methods invokes the
     * {@link DependencyStateListener}.
     */
    private void computeDependencyState() {
        if (m_state == BROKEN) {
            return;
        } // The dependency is broken ...

        boolean mustCallValidate = false;
        boolean mustCallInvalidate = false;
        synchronized (this) {
            if (m_optional || !m_matchingRefs.isEmpty()) {
                // The dependency is valid
                if (m_state == UNRESOLVED) {
                    m_state = RESOLVED;
                    mustCallValidate = true;
                }
            } else {
                // The dependency is invalid
                if (m_state == RESOLVED) {
                    m_state = UNRESOLVED;
                    mustCallInvalidate = true;
                }
            }
        }

        // Invoke callback in a non-synchronized region
        if (mustCallInvalidate) {
            invalidate();
        } else if (mustCallValidate) {
            validate();
        }

    }

    /**
     * Service tracker adding service callback. It accepts the service only if
     * the dependency isn't broken or frozen.
     *
     * @param ref the arriving service reference.
     * @return <code>true</code> if the reference must be tracked.
     * @see org.apache.felix.ipojo.util.TrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public boolean addingService(ServiceReference ref) {
        return !((m_state == BROKEN) || isFrozen());
    }

    /**
     * Service Tracker added service callback. If the service matches (against
     * the filter and the {@link DependencyModel#match(ServiceReference)},
     * manages the provider arrival.
     *
     * @param ref : new references.
     * @see org.apache.felix.ipojo.util.TrackerCustomizer#addedService(org.osgi.framework.ServiceReference)
     */
    public void addedService(ServiceReference ref) {
        if (matchAgainstFilter(ref) && match(ref)) {
            manageArrival(ref);
        }
        // Do not store the service if it doesn't match.
    }

    /**
     * Checks if the given service reference match the current filter. This
     * method aims to avoid calling {@link Filter#match(ServiceReference)}
     * method when manipulating a composite reference. In fact, this method
     * thrown a {@link ClassCastException} on Equinox.
     *
     * @param ref the service reference to check.
     * @return <code>true</code> if the service reference matches.
     */
    private boolean matchAgainstFilter(ServiceReference ref) {
        boolean match = true;
        if (m_filter != null) {
            if (ref instanceof ServiceReferenceImpl) {
                // Can't use the match(ref) as it throw a class cast exception
                // on Equinox.
                match = m_filter.match(((ServiceReferenceImpl) ref).getProperties());
            } else { // Non composite reference.
                match = m_filter.match(ref);
            }
        }
        return match;
    }

    /**
     * Manages the arrival of a new service reference. The reference is valid
     * and matches the filter and the
     * {@link DependencyModel#match(ServiceReference)} method. This method has
     * different behavior according to the binding policy.
     *
     * @param ref the new reference
     */
    private void manageArrival(ServiceReference ref) {
        // Create a local copy of the state and of the list size.
        int state = m_state;
        int size;

        synchronized (this) {
            m_matchingRefs.add(ref);

            // Sort the collection if needed, if not sort, services are append
            // to the list.
            if (m_comparator != null) {
                // The collection must be sort only if:
                // The policy is dynamic-priority
                // No services are already used
                // If so, sorting can imply a re-binding, and so don't follow
                // the Dynamic Binding policy
                if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY
                        || m_tracker.getUsedServiceReferences() == null
                        || m_tracker.getUsedServiceReferences().isEmpty()) {
                    Collections.sort(m_matchingRefs, m_comparator);
                }
            }

            size = m_matchingRefs.size();
        }

        if (m_aggregate) {
            if (state == UNRESOLVED) { // If we was unresolved, try to validate
                // the dependency.
                computeDependencyState();
            }
            /*
             * immediate = false -> do not inject on service arrival
			 * Injection delayed on service departure  
			 */
            if (!isImmediate()) {
                if (size > 1) {
                    m_matchingRefs.remove(ref);
                    m_matchingRefs_InWait.add(ref);
                    size = m_matchingRefs.size();
                    logger.debug("References InWait size =" + m_matchingRefs_InWait);
                } else {
                    /* first reference */
                    onServiceArrival(ref);
                }
            }
            if ((getMaxServiceInjected() > 0) && (size > getMaxServiceInjected())) {
                synchronized (m_matchingRefs) {
                    ServiceReference reflowest;
                    if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY) {
						/*ref
						 * unbind the used one and bind the new highest ranked
						 * reference
						 */
                        reflowest = (ServiceReference) m_matchingRefs.get(size - 1);
                        onServiceDeparture(reflowest);
                        onServiceArrival(ref);
                    } else {
						/* Remove the current reference newly added */
                        reflowest = ref;
                    }
                    m_matchingRefs.remove(reflowest);
                    m_matchingRefs_InWait.add(reflowest);
                    logger.debug("References InWait size =" + m_matchingRefs_InWait);
                    if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY) {
                        Collections.sort(m_matchingRefs_InWait, m_comparator);
                    }
                }
            } else {
                onServiceArrival(ref);
            }

        } else { // We are not aggregate.
            if (size == 1) {
                onServiceArrival(ref); // It is the first service, so notify.
                computeDependencyState();
            } else {
                // In the case of a dynamic priority binding, we have to test if
                // we have to update the bound reference
                if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY
                        && m_matchingRefs.get(0) == ref) {
                    // We are sure that we have at least two references, so if
                    // the highest ranked references (first one) is the new
                    // received
                    // references,
                    // we have to unbind the used one and to bind the the new
                    // one.
                    onServiceDeparture((ServiceReference) m_matchingRefs.get(1));
                    onServiceArrival(ref);
                }
            }
        }
        // Ignore others cases
    }

    /**
     * Service tracker removed service callback. A service provider goes away.
     * The depart needs to be managed only if the reference was used.
     *
     * @param ref  the leaving service reference
     * @param arg1 the service object if the service was already get
     * @see org.apache.felix.ipojo.util.TrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     * java.lang.Object)
     */
    public void removedService(ServiceReference ref, Object arg1) {
        m_matchingRefs_InWait.remove(ref);
        if (m_matchingRefs.contains(ref)) {
            manageDeparture(ref, arg1);
        }
    }

    /**
     * Manages the departure of a used service.
     *
     * @param ref the leaving service reference
     * @param obj the service object if the service was get
     */
    private void manageDeparture(ServiceReference ref, Object obj) {
        // Unget the service reference
        ungetService(ref);

        // If we already get this service and the binding policy is static, the
        // dependency becomes broken
        if (isFrozen() && obj != null) {
            if (m_state != BROKEN) {
                m_state = BROKEN;
                invalidate(); // This will invalidate the instance.
                // Reinitialize the dependency tracking
                ComponentInstance instance = null;
                synchronized (this) {
                    instance = m_instance;
                }
                instance.stop(); // Stop the instance
                unfreeze();
                instance.start();
            }
        } else {
            synchronized (this) {
                m_matchingRefs.remove(ref);
                logger.debug("References InWait size =" + m_matchingRefs_InWait.size());
            }
            if (obj == null) {
                computeDependencyState(); // check if the dependency stills
                // valid.
            } else {
                // A used service disappears, we have to sort the available
                // providers to choose the best one.
                // However, the sort has to be done only for scalar dependencies
                // following the dynamic binding
                // policy. Static dependencies will be broken, DP dependencies
                // are always sorted.
                // Aggregate dependencies does not need to be sort, as it will
                // change the array
                // order.
                if (m_comparator != null && m_policy == DYNAMIC_BINDING_POLICY
                        && !m_aggregate) {
                    Collections.sort(m_matchingRefs, m_comparator);
                }
                onServiceDeparture(ref);
                ServiceReference newRef = getServiceReference();
                if (newRef == null) { // Check if there is another provider.
                    computeDependencyState(); // no more references.
                } else {
                    if (isServicesInWaitToInject()) {
                        onServiceArrival(newRef);
                    } else {
                        if (!m_aggregate) {
                            onServiceArrival(newRef); // Injecting the new
                            // service
                            // reference for non
                            // aggregate dependencies.
                        }
                    }
                }
            }
        }

    }

    /**
     * Service tracker modified service callback. This method must handle if the
     * modified service should be considered as a depart or an arrival.
     * According to the dependency filter, a service can now match or can no
     * match anymore.
     *
     * @param ref  the modified reference
     * @param arg1 the service object if already get.
     * @see org.apache.felix.ipojo.util.TrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     * java.lang.Object)
     */
    public void modifiedService(ServiceReference ref, Object arg1) {
        if (m_matchingRefs.contains(ref)) {
            // It's a used service. Check if the service always match.
            if (!matchAgainstFilter(ref) && match(ref)) {
                // The service does not match anymore. Call removedService.
                manageDeparture(ref, arg1);
            } else {
                manageModification(ref);
            }
        } else {
            // The service was not used. Check if it matches.
            if (matchAgainstFilter(ref) && match(ref)) {
                manageArrival(ref);
            }
            // Else, the service does not match.
        }
    }

    /**
     * Gets the next matching service reference.
     *
     * @return <code>null</code> if no more provider is available, else returns
     * the first reference from the matching set.
     */
    public ServiceReference getServiceReference() {
        List references = new ArrayList(m_matchingRefs);
        ServiceReference aRef;
        synchronized (this) {
            if (isServicesInWaitToInject()) {
				/* Concat 2 lists */
                references.addAll(m_matchingRefs_InWait);
                if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY) {
                    Collections.sort(references, m_comparator);
                }
            }
            if (references.isEmpty()) {
                return null;
            } else {
                aRef = (ServiceReference) references.get(0);
                if (m_matchingRefs_InWait.remove(aRef)) {
                    m_matchingRefs.add(aRef);
                    logger.debug("Extract reference from ref in wait, inject in Matching refs");
                    logger.debug("References InWait size =" + m_matchingRefs_InWait);
                }
                return (ServiceReference) aRef;
            }
        }
    }

    /**
     * Gets matching service references.
     *
     * @return the sorted (if a comparator is used) array of matching service
     * references, <code>null</code> if no references are available.
     */
    public ServiceReference[] getServiceReferences() {
        synchronized (this) {
            if (m_matchingRefs.isEmpty()) {
                return null;
            }
            // TODO Consider sorting the array (on a copy of matching ref) if
            // dynamic priority used.
            return (ServiceReference[]) m_matchingRefs
                    .toArray(new ServiceReference[m_matchingRefs.size()]);
        }
    }

    /**
     * Gets the list of currently used service references. If no service
     * references, returns <code>null</code>
     *
     * @return the list of used reference (according to the service tracker).
     */
    public List getUsedServiceReferences() {
        synchronized (this) {
            // The list must confront actual matching services with already get
            // services from the tracker.

            int size = m_matchingRefs.size();
            List usedByTracker = null;
            if (m_tracker != null) {
                usedByTracker = m_tracker.getUsedServiceReferences();
            }
            if (size == 0 || usedByTracker == null) {
                return null;
            }

            List list = new ArrayList(1);
            for (int i = 0; i < size; i++) {
                if (usedByTracker.contains(m_matchingRefs.get(i))) {
                    list.add(m_matchingRefs.get(i)); // Add the service in the
                    // list.
                    if (!isAggregate()) { // IF we are not multiple, return the
                        // list when the first element is
                        // found.
                        return list;
                    }
                }
            }

            return list;
        }
    }

    /**
     * Gets the number of actual matching references.
     *
     * @return the number of matching references
     */
    public int getSize() {
        return m_matchingRefs.size();
    }

    /**
     * Concrete dependency callback. This method is called when a new service
     * needs to be re-injected in the underlying concrete dependency.
     *
     * @param ref the service reference to inject.
     */
    public abstract void onServiceArrival(ServiceReference ref);

    /**
     * Concrete dependency callback. This method is called when a used service
     * (already injected) is leaving.
     *
     * @param ref the leaving service reference.
     */
    public abstract void onServiceDeparture(ServiceReference ref);

    /**
     * Concrete dependency callback. This method is called when a used service
     * (already injected) is modified.
     *
     * @param ref the modified service reference.
     */
    public abstract void onServiceModification(ServiceReference ref);

    /**
     * This method can be override by the concrete dependency to be notified of
     * service modification. This modification is not an arrival or a departure.
     *
     * @param ref the modified service reference.
     */
    public void manageModification(ServiceReference ref) {
        if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY) {
            // Check that the order has changed or not.
            int indexBefore = m_matchingRefs.indexOf(ref);
            Collections.sort(m_matchingRefs, m_comparator);
            if (indexBefore != m_matchingRefs.indexOf(ref) && !m_aggregate) {
                // The order has changed during the sort.
                onServiceDeparture((ServiceReference) m_matchingRefs.get(1));
                onServiceArrival(ref);
            }

        } else {
            // It's a modification...
            onServiceModification(ref);
        }
    }

    /**
     * Concrete dependency callback. This method is called when the dependency
     * is reconfigured and when this reconfiguration implies changes on the
     * matching service set ( and by the way on the injected service).
     *
     * @param departs  the service leaving the matching set.
     * @param arrivals the service arriving in the matching set.
     */
    public abstract void onDependencyReconfiguration(ServiceReference[] departs,
                                                     ServiceReference[] arrivals);

    /**
     * Calls the listener callback to notify the new state of the current
     * dependency.
     */
    private void invalidate() {
        m_listener.invalidate(this);
    }

    /**
     * Calls the listener callback to notify the new state of the current
     * dependency.
     */
    private void validate() {
        m_listener.validate(this);
    }

    /**
     * Gets the actual state of the dependency.
     *
     * @return the state of the dependency.
     */
    public int getState() {
        return m_state;
    }

    /**
     * Gets the tracked specification.
     *
     * @return the Class object tracked by the dependency.
     */
    public Class getSpecification() {
        return m_specification;
    }

    /**
     * Sets the required specification of this service dependency. This
     * operation is not supported if the dependency tracking has already begun.
     *
     * @param specification the required specification.
     */
    public void setSpecification(Class specification) {
        if (m_tracker == null) {
            m_specification = specification;
        } else {
            throw new UnsupportedOperationException(
                    "Dynamic specification change is not yet supported");
        }
    }

    /**
     * Sets the filter of the dependency. This method recomputes the matching
     * set and call the onDependencyReconfiguration callback.
     *
     * @param filter the new LDAP filter.
     */
    public void setFilter(Filter filter) { // NOPMD
        m_filter = filter;
        if (m_tracker != null) { // Tracking started ...
            List toRemove = new ArrayList();
            List toAdd = new ArrayList();
            ServiceReference usedRef = null;
            synchronized (this) {

                // Store the used service references.
                if (!m_aggregate && !m_matchingRefs.isEmpty()) {
                    usedRef = (ServiceReference) m_matchingRefs.get(0);
                }

                // Get actually all tracked references.
                ServiceReference[] refs = m_tracker.getServiceReferences();

                if (refs == null) {
                    for (int j = 0; j < m_matchingRefs.size(); j++) {
                        // All references need to be removed.
                        toRemove.add(m_matchingRefs.get(j));
                    }
                    // No more matching dependency. Clear the matching reference
                    // set.
                    m_matchingRefs.clear();
                } else {
                    // Compute matching services.
                    List matching = new ArrayList();
                    for (int i = 0; i < refs.length; i++) {
                        if (matchAgainstFilter(refs[i]) && match(refs[i])) {
                            matching.add(refs[i]);
                        }
                    }
                    // Now compare with used services.
                    for (int j = 0; j < m_matchingRefs.size(); j++) {
                        ServiceReference ref = (ServiceReference) m_matchingRefs.get(j);
                        // Check if the reference is inside the matching list:
                        if (!matching.contains(ref)) {
                            // The reference should be removed
                            toRemove.add(ref);
                        }
                    }

                    // Then remove services which do no more match.
                    m_matchingRefs.removeAll(toRemove);

                    // Then, add new matching services.

                    for (int k = 0; k < matching.size(); k++) {
                        if (!m_matchingRefs.contains(matching.get(k))) {
                            m_matchingRefs.add(matching.get(k));
                            toAdd.add(matching.get(k));
                        }
                    }

                    // Sort the collections if needed.
                    if (m_comparator != null) {
                        Collections.sort(m_matchingRefs, m_comparator);
                        Collections.sort(toAdd, m_comparator);
                        Collections.sort(toRemove, m_comparator);
                    }

                }
            }

            // Call the callback outside the sync bloc.
            if (m_aggregate) {
                ServiceReference[] rem = null;
                ServiceReference[] add = null;
                if (!toAdd.isEmpty()) {
                    add = (ServiceReference[]) toAdd.toArray(new ServiceReference[toAdd
                            .size()]);
                }
                if (!toRemove.isEmpty()) {
                    rem = (ServiceReference[]) toRemove
                            .toArray(new ServiceReference[toRemove.size()]);
                }
                if (rem != null || add != null) { // Notify the change only when
                    // a change is made on the
                    // matching reference list.
                    onDependencyReconfiguration(rem, add);
                }
            } else {
                // Create a local copy to avoid un-sync reference list access.
                int size;
                ServiceReference newRef = null;
                synchronized (m_matchingRefs) {
                    size = m_matchingRefs.size();
                    if (size > 0) {
                        newRef = (ServiceReference) m_matchingRefs.get(0);
                    }
                }
                // Non aggregate case.
                // If the used reference was not null
                if (usedRef == null) {
                    // The used ref was null,
                    if (size > 0) {
                        onDependencyReconfiguration(null,
                                new ServiceReference[]{newRef});
                    } // Don't notify the change, if the set is not touched by
                    // the reconfiguration.
                } else {
                    // If the used ref disappears, inject a new service if
                    // available, else reinject null.
                    if (toRemove.contains(usedRef)) {
                        // We have to replace the service.
                        if (size > 0) {
                            onDependencyReconfiguration(
                                    new ServiceReference[]{usedRef},
                                    new ServiceReference[]{newRef});
                        } else {
                            onDependencyReconfiguration(
                                    new ServiceReference[]{usedRef}, null);
                        }
                    } else if (m_policy == DYNAMIC_PRIORITY_BINDING_POLICY
                            && newRef != usedRef) { // NOPMD
                        // In the case of dynamic-priority, check if the used
                        // ref is no more the highest reference
                        onDependencyReconfiguration(new ServiceReference[]{usedRef},
                                new ServiceReference[]{newRef});
                    }
                }
            }
            // Now, compute the new dependency state.
            computeDependencyState();
        }
    }

    /**
     * Returns the dependency filter (String form).
     *
     * @return the String form of the LDAP filter used by this dependency,
     * <code>null</code> if not set.
     */
    public String getFilter() {
        if (m_filter == null) {
            return null;
        } else {
            return m_filter.toString();
        }
    }

    /**
     * Sets the aggregate attribute of the current dependency. If the tracking
     * is opened, it will call arrival and departure callbacks.
     *
     * @param isAggregate the new aggregate attribute value.
     */
    public synchronized void setAggregate(boolean isAggregate) {
        if (m_tracker == null) { // Not started ...
            m_aggregate = isAggregate;
        } else {
            // We become aggregate.
            if (!m_aggregate && isAggregate) {
                m_aggregate = true;
                // Call the callback on all non already injected service.
                if (m_state == RESOLVED) {

                    for (int i = 1; i < m_matchingRefs.size(); i++) { // The
                        // loop
                        // begin
                        // at 1,
                        // as
                        // the 0
                        // is
                        // already
                        // injected.
                        onServiceArrival((ServiceReference) m_matchingRefs.get(i));
                    }
                }
            } else if (m_aggregate && !isAggregate) {
                m_aggregate = false;
                // We become non-aggregate.
                if (m_state == RESOLVED) {
                    for (int i = 1; i < m_matchingRefs.size(); i++) { // The
                        // loop
                        // begin
                        // at 1,
                        // as
                        // the 0
                        // stills
                        // injected.
                        onServiceDeparture((ServiceReference) m_matchingRefs.get(i));
                    }
                }
            }
            // Else, do nothing.
        }
    }

    public synchronized boolean isAggregate() {
        return m_aggregate;
    }

    /**
     * Sets the optionality attribute of the current dependency.
     *
     * @param isOptional the new optional attribute value.
     */
    public void setOptionality(boolean isOptional) {
        if (m_tracker == null) { // Not started ...
            m_optional = isOptional;
        } else {
            computeDependencyState();
        }
    }

    public boolean isOptional() {
        return m_optional;
    }

    /**
     * Gets the used binding policy.
     *
     * @return the current binding policy.
     */
    public int getBindingPolicy() {
        return m_policy;
    }

    /**
     * Sets the binding policy. Not yet supported.
     */
    public void setBindingPolicy() {
        throw new UnsupportedOperationException(
                "Binding Policy change is not yet supported");
        // TODO supporting dynamic policy change.
    }

    public void setComparator(Comparator cmp) {
        m_comparator = cmp;
        // NOTE: the array will be sorted at the next get.
    }

    /**
     * Gets the used comparator name. <code>Null</code> if no comparator (i.e.
     * the OSGi one is used).
     *
     * @return the comparator class name or <code>null</code> if the dependency
     * doesn't use a comparator.
     */
    public synchronized String getComparator() {
        if (m_comparator != null) {
            return m_comparator.getClass().getName();
        } else {
            return null;
        }
    }

    /**
     * Sets the bundle context used by this dependency. This operation is not
     * supported if the tracker is already opened.
     *
     * @param context the bundle context or service context to use
     */
    public void setBundleContext(BundleContext context) {
        if (m_tracker == null) { // Not started ...
            m_context = context;
        } else {
            throw new UnsupportedOperationException(
                    "Dynamic bundle (i.e. service) context change is not supported");
        }
    }

    /**
     * Gets a service object for the given reference.
     *
     * @param ref the wanted service reference
     * @return the service object attached to the given reference
     */
    public Object getService(ServiceReference ref) {
        Object svc = m_tracker.getService(ref);
        if (svc instanceof IPOJOServiceFactory) {
            Object obj = ((IPOJOServiceFactory) svc).getService(m_instance);
            m_serviceObjects.put(ref, svc); // We store the factory !
            return obj;
        } else {
            m_serviceObjects.put(ref, svc);
            return svc;
        }
    }

    /**
     * Ungets a used service reference.
     *
     * @param ref the reference to unget.
     */
    public void ungetService(ServiceReference ref) {
        m_tracker.ungetService(ref);
        Object obj = m_serviceObjects.remove(ref); // Remove the service object
        if (obj != null && obj instanceof IPOJOServiceFactory) {
            ((IPOJOServiceFactory) obj).ungetService(m_instance, obj);
        }
    }

    /**
     * Helper method parsing the comparator attribute and returning the
     * comparator object. If the 'comparator' attribute is not set, this method
     * returns null. If the 'comparator' attribute is set to 'osgi', this method
     * returns the normal OSGi comparator. In other case, it tries to create an
     * instance of the declared comparator class.
     *
     * @param dep     the Element describing the dependency
     * @param context the bundle context (to load the comparator class)
     * @return the comparator object, <code>null</code> if not set.
     * @throws ConfigurationException the comparator class cannot be load or the comparator cannot
     *                                be instantiated correctly.
     */
    public static Comparator getComparator(Element dep, BundleContext context)
            throws ConfigurationException {
        Comparator cmp = null;
        String comp = dep.getAttribute("comparator");
        if (comp != null) {
            if (comp.equalsIgnoreCase("osgi")) {
                cmp = new ServiceReferenceRankingComparator();
            } else {
                try {
                    Class cla = context.getBundle().loadClass(comp);
                    cmp = (Comparator) cla.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException(
                            "Cannot load a customized comparator : " + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(
                            "Cannot create a customized comparator : " + e.getMessage());
                } catch (InstantiationException e) {
                    throw new ConfigurationException(
                            "Cannot create a customized comparator : " + e.getMessage());
                }
            }
        }
        return cmp;
    }

    /**
     * Loads the given specification class.
     *
     * @param specification the specification class name to load
     * @param context       the bundle context
     * @return the class object for the given specification
     * @throws ConfigurationException if the class cannot be loaded correctly.
     */
    public static Class loadSpecification(String specification, BundleContext context)
            throws ConfigurationException {
        Class spec = null;
        try {
            spec = context.getBundle().loadClass(specification);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                    "A required specification cannot be loaded : " + specification);
        }
        return spec;
    }

    /**
     * Helper method parsing the binding policy. If the 'policy' attribute is
     * not set in the dependency, the method returns the 'DYNAMIC BINDING
     * POLICY'. Accepted policy values are : dynamic, dynamic-priority and
     * static.
     *
     * @param dep the Element describing the dependency
     * @return the policy attached to this dependency
     * @throws ConfigurationException if an unknown binding policy was described.
     */
    public static int getPolicy(Element dep) throws ConfigurationException {
        String policy = dep.getAttribute("policy");
        if (policy == null || policy.equalsIgnoreCase("dynamic")) {
            return DYNAMIC_BINDING_POLICY;
        } else if (policy.equalsIgnoreCase("dynamic-priority")) {
            return DYNAMIC_PRIORITY_BINDING_POLICY;
        } else if (policy.equalsIgnoreCase("static")) {
            return STATIC_BINDING_POLICY;
        } else {
            throw new ConfigurationException("Binding policy unknown : " + policy);
        }
    }

    /**
     * Helper method parsing the binding policy. If the 'policy' attribute is
     * not set in the dependency, the method returns the 'DYNAMIC BINDING
     * POLICY'. Accepted policy values are : dynamic, dynamic-priority and
     * static.
     *
     * @param String policy the Element describing the dependency
     * @return the policy attached to this dependency
     * @throws ConfigurationException if an unknown binding policy was described.
     */
    public static int getPolicy(String policy) throws ConfigurationException {
        if (policy == null || policy.equalsIgnoreCase("dynamic")) {
            return DYNAMIC_BINDING_POLICY;
        } else if (policy.equalsIgnoreCase("dynamic-priority")) {
            return DYNAMIC_PRIORITY_BINDING_POLICY;
        } else if (policy.equalsIgnoreCase("static")) {
            return STATIC_BINDING_POLICY;
        } else {
            throw new ConfigurationException("Binding policy unknown : " + policy);
        }
    }

}
