package fr.liglab.adele.cilia.components.mediators;

import fr.liglab.adele.cilia.Data;

/**
 * The Hello World Processor Class
 */
public class HelloProcessor {

    /**
     * Method modifying the received data
     *
     * @param data The processor received data
     * @return The data with "Hello, " prefix
     */
    public Data sayHello(Data data) {
        if (data != null) {
            data.setContent("Hello, " + data.getContent().toString());
        }
        return data;
    }
}
