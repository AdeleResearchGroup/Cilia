package fr.liglab.adele.cilia.specification;

/**
 * This interface is the api to specify new adapters specifications.
 */
public interface OutAdapterSpecification {
    /**
     * Get the adapter category.
     *
     * @return the adapter category.
     */
    String getCategory();

    /**
     * Get the adapter namespace.
     *
     * @return the adapter namespace.
     */
    String getNamespace();

    /**
     * Retrieve the adapter specification name.
     *
     * @return
     */
    String getName();

    /**
     * Assign the scheduler information.
     *
     * @param schedulerName      the chosen scheduler name.
     * @param schedulerNamespace the chosen scheduler namespace.
     * @return the current AdapterSpecification object.
     */
    OutAdapterSpecification setScheduler(String schedulerName, String schedulerNamespace);

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
     * Assign the chosen sender info.
     *
     * @param senderName      the chosen sender name.
     * @param senderNamespace the chosen sender namespace.
     */
    OutAdapterSpecification setSender(String senderName, String senderNamespace);

    /**
     * Retrieve the chosen sender name.
     *
     * @return the sender name.
     */
    String getSenderName();

    /**
     * Retrieve the sender namespace.
     *
     * @return the sender namespace.
     */
    String getSenderNamespace();

    /**
     * Initialize the adapter specification type.
     */
    OutAdapterSpecification initializeSpecification();

    /**
     * Stop the adapter specification type.
     */
    OutAdapterSpecification stopSpecification();

}
