package cilia.runtime.dynamic.test;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.monitor.ProcessorNotifier;

/**
 * The Hello World Processor Class
 * 
 */

public class ImmediateProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger("test.runtime");
	/**
	 * Method modifying the received data
	 * 
	 * @param data_int
	 *            The processor received data
	 * @return The data with "Hello, " prefix
	 */
	String header ;
	int debug;

	public ProcessorNotifier notify ;

	public void setHeader(String str) {
		header = str + "[tag-" + new Random().nextInt(100) + "-]";
	}
	
	
	/* Entry point  */
	public Data processor(Data data) {
		if (data != null) {
			data.setContent(header+data.getContent()) ;
			System.out.println(header+data.getContent()) ;
		}
		// Fire event to MonitorHandler
		if (notify !=null) {
			notify.fireEvent(null) ;
		}
		return data;
	}
}
