package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.AbstractSplitter;
import fr.liglab.adele.cilia.framework.data.XmlSplitter;

import java.util.List;

/**
 * This class is for test purpose.
 *
 * @author torito
 */
public class XpathSplitter extends AbstractSplitter {

    /**
     * Expression used to split with xpath.
     * injected by iPOJO
     *
     * @property expression.
     */
    private String expression;

    private boolean addSplitInfo = false;


    public List process(List data) {
        return super.process(data);
    }

    /**
     * Split one data.
     *
     * @param receiveData Data to be splited.
     * @return the data set containing the splited messages.
     */
    public List split(Data receiveData) throws CiliaException {
        try {
            return XmlSplitter.split(receiveData, expression, addSplitInfo);
        } catch (CiliaException e) {
            throw new CiliaException(e.getMessage());
        }
    }
}
