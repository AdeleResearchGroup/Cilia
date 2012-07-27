package fr.liglab.adele.cilia.builder;

import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;

public interface PortReplacerConfigurator {
	ReplacerConfigurator toPort(String newPort) throws BuilderConfigurationException;
}
