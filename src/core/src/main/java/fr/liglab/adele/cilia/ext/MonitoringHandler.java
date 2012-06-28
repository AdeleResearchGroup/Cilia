package fr.liglab.adele.cilia.ext;

import java.util.HashMap;
import java.util.Map;

import fr.liglab.adele.cilia.model.impl.MediatorComponentImpl;

public class MonitoringHandler {
	private final static String MONITORING = "configuration.state.variable";
    
	private Map stateVarConfig   ;
	private MediatorComponentImpl mediator;
	private Object _lock = new Object() ;
		
	public MonitoringHandler(MediatorComponentImpl mediator) {
		this.mediator = mediator;
		this.stateVarConfig = new HashMap();
	}
	
	public void done() {
		mediator.setProperty(MONITORING, new HashMap(stateVarConfig)) ;
		System.out.println(">>>> "+mediator.getId()+"  MONITORING "+stateVarConfig.toString()) ;
	}
	
	public StateVarConfigurationImpl addId(String id,boolean enable) {
		StateVarConfigurationImpl config = new StateVarConfigurationImpl(enable) ;
		stateVarConfig.put(id, config) ;
		return config ;
 	}
		
	public Map configurations(){
		Map configurations = null;
		synchronized (_lock) {
			Object prop = mediator.getProperty(MONITORING);
			if (prop != null && prop instanceof Map) {
				configurations = (Map)prop;
			} else {
				configurations = new HashMap();
			}
		}
		return configurations;
	}
}
