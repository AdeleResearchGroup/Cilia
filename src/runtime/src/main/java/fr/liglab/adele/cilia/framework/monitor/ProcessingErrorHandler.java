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
package fr.liglab.adele.cilia.framework.monitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class ProcessingErrorHandler extends AbstractMonitor {
	/**
	 * Rules in the form <Exception, <list of ports to send>>.
	 */
	private Map rules;
	/**
	 * OSGi Bundle Context.
	 */
	private BundleContext bcontext;


	protected static Logger log = LoggerFactory.getLogger("cilia.ipojo.compendium.error-handling");

	/**
	 * Constructor
	 * @param bc OSGi Bundle Context
	 */
	public ProcessingErrorHandler (BundleContext bc) {
		bcontext = bc;
	}

	/**
	 * 
	 * @param ex
	 * @return
	 */
	private List getPorts(String ex){
		List inports = null;
		String rawports = null;
		if (rules ==null || rules.isEmpty()) {
			inports = Collections.singletonList("error");
		} else if (rules.containsKey(ex)) {
			rawports = String.class.cast((rules.get(ex)));
			inports = Arrays.asList(rawports.split(","));
		}
		log.debug("chosen ports " + inports.toString());
		return inports;
	}


	/**
	 * Method called when an exception occurs on the processing.
	 */
	public void onProcessError(List data, Exception ex) {
		log.error("Error Handler Monitor:" + data.size());
		//iterate, get data causing exception
		for (int i = 0; i < data.size(); i ++) {
			Data d = Data.class.cast(data.get(i));
			d.setProperty("error.handler.exception", ex);
			List ports = getPorts(ex.getClass().getName());
			sendException(ports, d);
		}
	}
	/**
	 * Send the data causing the exception to the list of specified ports.
	 * @param ports the list of ports.
	 * @param data The data causing the exception.
	 */
	private void sendException(List ports, Data data) {
		for (int i = 0; i < ports.size(); i++) {
			String port = String.valueOf(ports.get(i));
			try {
				this.getDispatcher().send(port, data);
			} catch (CiliaException e) {
				log.error("Unable to send via port: " + port, e);
			}
		}
	}


}
