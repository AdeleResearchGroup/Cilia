package fr.liglab.adele.cilia.specification;

/**
 * This interface is the api to specify new adapters specifications.
 */
public interface InAdapterSpecification {
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
     * Assign the collector information.
     *
     * @param collectorName      the chosen collector name.
     * @param collectorNamespace the chosen collector namespace.
     * @return the current InAdapterSpecification object.
     */
    InAdapterSpecification setCollector(String collectorName, String collectorNamespace);

    /**
     * Retrieve the collector assigned name.
     *
     * @return the collector name.
     */
    String getCollectorName();

    /**
     * Retrieve the chosen collector namespace.
     *
     * @return the collector namespace.
     */
    String getCollectorNamespace();

    /**
     * Assign the chosen dispatcher info.
     *
     * @param dispatcherName      the chosen dispatcher name.
     * @param dispatcherNamespace the chosen dispatcher namespace.
     */
    InAdapterSpecification setDispatcher(String dispatcherName, String dispatcherNamespace);

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
     * Initialize the adapter specification type.
     */
    InAdapterSpecification initializeSpecification();

    /**
     * Stop the adapter specification type.
     */
    InAdapterSpecification stopSpecification();

}
