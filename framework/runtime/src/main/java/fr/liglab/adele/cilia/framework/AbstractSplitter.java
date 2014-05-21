package fr.liglab.adele.cilia.framework;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractSplitter implements IProcessor {

    Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

    public List process(List receivedDataSet) {
        List splittedDataSet = null;
        List returnedDataSet = null;
        if (receivedDataSet != null && receivedDataSet.size() > 0) {
            splittedDataSet = new ArrayList();
            for (int i = 0; i < receivedDataSet.size(); i++) {
                Data dataToSplit = (Data) receivedDataSet.get(i);
                if (logger.isDebugEnabled())
                    logger.debug("processing " + dataToSplit.toString());
                List msplittedDataSet = null;
                try {
                    msplittedDataSet = split(dataToSplit);
                } catch (Exception ex) {
                    logger.error("Error when splitting data" + dataToSplit);
                }
                for (int j = 0; msplittedDataSet != null && j < msplittedDataSet.size(); j++) {
                    splittedDataSet.add(msplittedDataSet.get(j));
                }
            }
        }

        if (splittedDataSet != null) {
            returnedDataSet = splittedDataSet;

        } else {
            returnedDataSet = receivedDataSet;
            logger.warn("splited returned the same dataset.");
        }
        return returnedDataSet;
    }

    public abstract List split(Data dataToSplit) throws CiliaException;

}
