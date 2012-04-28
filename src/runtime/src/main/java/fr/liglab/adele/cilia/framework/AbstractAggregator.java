package fr.liglab.adele.cilia.framework;

import java.util.List;


public abstract class AbstractAggregator implements IProcessor {
	
	
	public List process(List dataSet) {
		return aggregate(dataSet);
	}

	public abstract List aggregate(List dataSet);
}
