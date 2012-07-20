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
package fr.liglab.adele.cilia.framework;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public abstract class AbstractDispatcher implements IDispatcher{

	IDispatcher dispatcher;

	protected BundleContext bcontext;

	protected static Logger log= LoggerFactory.getLogger("cilia.ipojo.runtime");

	public AbstractDispatcher(BundleContext context) {
		bcontext = context;
	}

	public void setDispatcher(IDispatcher hdispatcher) {
		dispatcher = hdispatcher;
	}

//	public void addSender(String senderName, String senderId, Dictionary props) {
//		if (log.isTraceEnabled()) {
//			log.trace("Add sender '" + senderName + "'");
//		}
//		dispatcher.addSender(senderName, senderId, props);
//	}

	public abstract void dispatch(Data data) throws CiliaException  ;

	public List getSendersIds() {
		return dispatcher.getSendersIds();
	}

//	public void removeSender(String portname, String senderName) {
//		log.debug("remove sender '" + senderName + "'");
//		dispatcher.removeSender(portname, senderName);
//	}

	public void send(String portname, Data data) throws CiliaException {
		if (log.isTraceEnabled())
			log.trace("send " + data);
		dispatcher.send(portname, data);
	}

	public void send(String portname, Properties properties, Data data)
			throws CiliaException {
		if (log.isTraceEnabled())
			log.trace("send " + data);
		dispatcher.send(portname, properties, data);
	}

	public void fireEvent(Map info) {
		log.info("fireEvent " + info);
		if (dispatcher != null)
			dispatcher.fireEvent(info);
	}
	
}
