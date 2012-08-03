package fr.liglab.adele.cilia.builder.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import fr.liglab.adele.cilia.builder.PortReplacerConfigurator;
import fr.liglab.adele.cilia.builder.Replacer;
import fr.liglab.adele.cilia.builder.ReplacerConfigurator;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;

public class ReplacerImpl implements Replacer, ReplacerConfigurator, PortReplacerConfigurator {

	private static int INPORT = 1;

	private static int OUTPORT = 2;

	private static int ANY = 0;

	ArchitectureImpl architecture;

	String id;

	String to;

	Map inports = new HashMap();

	Map outports = new HashMap();

	int actualPort = ANY;

	Hashtable properties;

	String oldPort = null;


	protected ReplacerImpl(ArchitectureImpl arch) {
		architecture = arch;
	}

	public ReplacerConfigurator id(String id) {
		this.id = id;
		return this;
	}

	public ReplacerConfigurator to(String id) {
		this.to = id;  
		return this;
	}

	public PortReplacerConfigurator inport(String oldPort) {
		actualPort = INPORT;
		this.oldPort = oldPort;
		return this;
	}

	public PortReplacerConfigurator outport(String oldPort) {
		actualPort = OUTPORT;
		this.oldPort = oldPort;
		return this;
	}

	public ReplacerConfigurator configure(Hashtable props) {
		properties = new Hashtable(props);
		return this;
	}

	public ReplacerConfigurator toPort(String newPort) throws BuilderConfigurationException{
		try{
			if (oldPort != null && newPort != null ) {
				if (actualPort == INPORT){
					inports.put(oldPort, newPort);
				} else if (actualPort == OUTPORT){
					outports.put(oldPort, newPort);
				} else {
					throw new BuilderConfigurationException("There is any previous port configured");
				}
			} else {
				throw new BuilderConfigurationException("'port name' can't be null when replacing mediator");
			}
			return this;
		}
		finally{
			oldPort = null;
			actualPort = ANY;
		}
	}
	
	protected String getFromMediator() {
		return id;
	}

	protected String getToMediator() {
		return id;
	}
}
