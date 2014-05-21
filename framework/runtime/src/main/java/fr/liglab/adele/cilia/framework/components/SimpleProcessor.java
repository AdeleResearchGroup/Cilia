package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.framework.IProcessor;

import java.util.List;

public class SimpleProcessor implements IProcessor {

    public List process(List dataSet) {
        return dataSet;
    }

}
