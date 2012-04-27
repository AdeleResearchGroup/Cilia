package fr.liglab.adele.cilia.demo.endpoint;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractIOAdapter;


public class DemoEndpointImpl extends AbstractIOAdapter implements DemoEndpointI {


	public DemoEndpointImpl(BundleContext context) {
		super(context);
	}

	
    public String getPrice(String data) {
        System.out.println("toto has been called " + data);
        String result = String.valueOf(invokeChain(data));
        System.out.println("Resultado: " + result);
        return result;
    }
    
    public void receiveData(Data data){
    	super.receiveData(data);
    }
    
    public Data dispatchData(Data data) {
    	return super.dispatchData(data);
    }
    
}
