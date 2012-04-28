package fr.liglab.adele.cilia.framework.monitor;

import java.util.Map;

public class CiliaNotify implements CiliaNotification {
	
	private IProcessorMonitor monitor;

	public void ciliaEvent(Map data) {
		monitor.fireEvent(data);	
	}

}
