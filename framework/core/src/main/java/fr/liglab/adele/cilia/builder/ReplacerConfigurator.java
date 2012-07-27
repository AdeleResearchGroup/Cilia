package fr.liglab.adele.cilia.builder;

import java.util.Hashtable;

public interface ReplacerConfigurator {

	ReplacerConfigurator to(String id);
	
	PortReplacerConfigurator inport(String oldPort);
	
	PortReplacerConfigurator outport(String oldPort);
	
	ReplacerConfigurator configure(Hashtable props);
	
}
