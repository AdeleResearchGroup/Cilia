package fr.liglab.adele.cilia.runtime.impl;

import java.util.List;

import fr.liglab.adele.cilia.framework.IProcessor;

public class SimpleProcessor implements IProcessor {

	public List process(List dataSet) {
		return dataSet;
	}

}
