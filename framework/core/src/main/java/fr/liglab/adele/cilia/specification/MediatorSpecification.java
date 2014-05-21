package fr.liglab.adele.cilia.specification;

/**
 * This interface is the api to specify new mediators specifications.
 */
public interface MediatorSpecification {
    /**
     * Get the mediator category.
     *
     * @return the mediator category.
     */
    String getCategory();

    /**
     * Get the mediator namespace.
     *
     * @return the mediator namespace.
     */
    String getNamespace();

    /**
     * Retrieve the mediator specification name.
     *
     * @return
     */
    String getName();

    /**
     * Assign the scheduler information.
     *
     * @param schedulareName     the chosen scheduler name.
     * @param schedulerNamespace the chosen scheduler namespace.
     * @return the current MediatorSpecification object.
     */
    MediatorSpecification setScheduler(String schedulareName, String schedulerNamespace);

    /**
     * Retrieve the scheduler assigned name.
     *
     * @return the scheduler name.
     */
    String getSchedulerName();

    /**
     * Retrieve the chosen scheduler namespace.
     *
     * @return the scheduler namespace.
     */
    String getSchedulerNamespace();

    /**
     * Set the chosen processor info.
     *
     * @param processorName      the processor name.
     * @param processorNamespace the processor namespace.
     */
    MediatorSpecification setProcessor(String processorName, String processorNamespace);

    /**
     * Retrieve the chosen processor name.
     *
     * @return the processor name.
     */
    String getProcessorName();

    /**
     * Retrieve the processor namespace.
     *
     * @return the processor namespace.
     */
    String getProcessorNamespace();

    /**
     * Assign the chosen dispatcher info.
     *
     * @param dispatcherName      the chosen dispatcher name.
     * @param dispatcherNamespace the chosen dispatcher namespace.
     */
    MediatorSpecification setDispatcher(String dispatcherName, String dispatcherNamespace);

    /**
     * Retrieve the chosen dispatcher name.
     *
     * @return the dispatcher name.
     */
    String getDispatcherName();

    /**
     * Retrieve the dispatcher namespace.
     *
     * @return the dispatcher namespace.
     */
    String getDispatcherNamespace();

    /**
     * Initialize the mediator specification type.
     */
    MediatorSpecification initializeSpecification();

    /**
     * Stop the mediator specification type.
     */
    MediatorSpecification stopSpecification();

}
