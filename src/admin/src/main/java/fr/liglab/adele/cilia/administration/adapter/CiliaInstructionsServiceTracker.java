/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.liglab.adele.cilia.administration.adapter;

import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import fr.liglab.adele.cilia.CiliaContainer;


public class CiliaInstructionsServiceTracker  extends ServiceTracker implements ServiceTrackerCustomizer {
	
	ServiceTracker tracker;
	
	Map trackedServices = new Hashtable(); 
	
	CiliaContainer ccontext;
	
	CiliaInstructionsAdapter adapter;
	
	private final Object lockObject = new Object(); 
	
	private BundleContext bcontext;
	
	public CiliaInstructionsServiceTracker(BundleContext bc, CiliaInstructionsAdapter cia) throws InvalidSyntaxException {
		super(bc, bc.createFilter("(cilia.tracked.chain=*"), null);
		bcontext = bc;
		adapter = cia;
	}
	
	
	public Object addingService(ServiceReference reference) {
		return bcontext.getService(reference);		
	}


	public void modifiedService(ServiceReference reference, Object service) {
		String chainToHandle = (String)reference.getProperty("cilia.tracked.chain");
		ChainTracker cc = new ChainTracker(adapter, null, null);
		ccontext.addChainListener(chainToHandle, cc);
		trackedServices.put(reference, cc);
		// TODO Auto-generated method stub
	}


	public void removedService(ServiceReference reference, Object service) {
		// TODO Auto-generated method stub
	}
	
}
