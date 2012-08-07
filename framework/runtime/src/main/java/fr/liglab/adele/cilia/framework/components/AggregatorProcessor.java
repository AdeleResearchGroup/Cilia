package fr.liglab.adele.cilia.framework.components;

import java.util.ArrayList;
import java.util.List;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractAggregator;

public class AggregatorProcessor extends AbstractAggregator {
	
	public String getName() {
		return "BasicAgregator";
	}
	
	public List process(List dataSet) {
		return super.process(dataSet);
	}

	public List aggregate(List dataSet){
		List nDataSet=new ArrayList();
		Data data = new Data(dataSet,"aggregated-data");
		nDataSet.add(data);
		return nDataSet;
	}
}
