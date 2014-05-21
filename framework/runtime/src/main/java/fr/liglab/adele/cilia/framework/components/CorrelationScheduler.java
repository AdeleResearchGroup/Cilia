package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCorrelationScheduler;
import fr.liglab.adele.cilia.framework.data.DataEnrichment;
import org.osgi.framework.BundleContext;

import java.util.List;

public class CorrelationScheduler extends AbstractCorrelationScheduler {

    public CorrelationScheduler(BundleContext bcontext) {
        super(bcontext);
    }


    public boolean checkCompletness(List dataset) {
        appLogger.debug("[CorrelationScheduler] checking data set completness");
        boolean completness = false;
        int count = 0;
        int splitTotal = 0;

        count = dataset.size();

        if (count > 0) {
            splitTotal = DataEnrichment.getCorrelatedTotal((Data) dataset.get(0));
        }
        if (splitTotal == count) {
            completness = true;
        }
        appLogger.debug("[CorrelationScheduler] count = {}", count);
        appLogger.debug("[CorrelationScheduler] Split Total = {}", splitTotal);
        return completness;
    }
}
