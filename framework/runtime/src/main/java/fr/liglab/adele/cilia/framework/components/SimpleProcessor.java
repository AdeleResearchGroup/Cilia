package fr.liglab.adele.cilia.framework.components;

import java.util.List;

import fr.liglab.adele.cilia.framework.IProcessor;

public class SimpleProcessor implements IProcessor {

	public List process(List dataSet) {
		System.out.println("Calling Simple Processor");
		return dataSet;
	}

}
