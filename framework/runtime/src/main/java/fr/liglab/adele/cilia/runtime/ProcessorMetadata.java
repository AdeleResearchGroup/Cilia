package fr.liglab.adele.cilia.runtime;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;

import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class ProcessorMetadata {
    /**
     * method attribute to configure handler.
     */
    private final static String METHOD = "method";
    /**
     * method attribute to configure handler.
     */
    private final static String DATA_TYPE = "data.type";
    /**
     * callback attribute to configure handler.
     */
    private final static String CALLBACK = "callback";

    private final static String NAME = "name";
    /**
     * Default Method Name.
     */
    private final static String DEFAULT_METHOD = "process";
    /**
     * Parametred and returned data type.
     */
    private final String RETURN_PARAMETER_TYPE = List.class.getName();
    /**
     * process Method.
     */
    private String method;

    private String[] returnDataType = new String[1];

    private String[] paramDataType = new String[1];

    /**
     * @param element
     * @throws ConfigurationException
     */
    public ProcessorMetadata(Element element) throws ConfigurationException {
        if (element == null) {
            method = DEFAULT_METHOD;
        } else if (element.containsAttribute(CALLBACK)) {
            method = element.getAttribute(CALLBACK);
        } else if (element.containsAttribute(METHOD)) {
            method = element.getAttribute(METHOD);
        } else if (element.containsAttribute(NAME)) {
            method = element.getAttribute(NAME);
        } else {
            method = DEFAULT_METHOD;
        }
        //get dataType it allows Data and List


        if (element != null && element.containsAttribute(DATA_TYPE)) {
            returnDataType[0] = element.getAttribute(DATA_TYPE);
            paramDataType[0] = element.getAttribute(DATA_TYPE);
        } else {
            returnDataType[0] = RETURN_PARAMETER_TYPE;
            paramDataType[0] = RETURN_PARAMETER_TYPE;
        }

        if (element != null && element.containsAttribute("in." + DATA_TYPE)) {
            paramDataType[0] = element.getAttribute("in." + DATA_TYPE);
        }

        if (element != null && element.containsAttribute("out." + DATA_TYPE)) {
            returnDataType[0] = element.getAttribute("out." + DATA_TYPE);
        }

    }

    /**
     * get Method Name.
     *
     * @return
     */
    public String getMethod() {
        return method;
    }

    public String[] getReturnedDataType() {
        return returnDataType;
    }

    public String[] getParameterDataType() {
        return paramDataType;
    }

}
