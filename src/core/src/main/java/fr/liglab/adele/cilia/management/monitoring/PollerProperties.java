package fr.liglab.adele.cilia.management.monitoring;

import fr.liglab.adele.cilia.management.Configurable;

/**
 * This interface provides methods for setting the poller <br>
 * some framework state variable must be polled 
 * 
 * @author denismorand
 * 
 */
public interface PollerProperties extends Configurable {
	/** 
	 * Period is given in ms 
	 * string representation of integer 
	 */
	static final String DEFAULT_PERIOD = "1000" ; // 1second
	/** 
	 * Value = must be string integer
	 * example : setPropery(PROPERTY_POLLER_PERIOD,"10000")  
	 */	
	String PROPERTY_POLLER_PERIOD  = "poller.period" ;
	
}
