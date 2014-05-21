package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractAggregator;

import java.util.ArrayList;
import java.util.List;

public class AggregatorProcessor extends AbstractAggregator {

    public String getName() {
        return "BasicAgregator";
    }

    public List process(List dataSet) {
        return super.process(dataSet);
    }

    public List aggregate(List dataSet) {
        List nDataSet = new ArrayList();
        Data data = new Data(dataSet, "aggregated-data");
        nDataSet.add(data);
        return nDataSet;
    }
}
