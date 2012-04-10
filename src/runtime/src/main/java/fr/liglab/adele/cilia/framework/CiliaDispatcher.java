package fr.liglab.adele.cilia.framework;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaException;
import fr.liglab.adele.cilia.Data;

public class CiliaDispatcher implements IDispatcher {

	IDispatcher dispatcher;

	protected BundleContext bcontext;

	protected static Logger log= LoggerFactory.getLogger("cilia.ipojo.runtime");

	public CiliaDispatcher(BundleContext context) {
		bcontext = context;
	}

	public void setDispatcher(IDispatcher hdispatcher) {
		dispatcher = hdispatcher;
	}

	public void addSender(String senderName, String senderId, Dictionary props) {
		if (log.isTraceEnabled()) {
			log.trace("Add sender '" + senderName + "'");
		}
		dispatcher.addSender(senderName, senderId, props);
	}

	public void dispatch(List dataSet) throws CiliaException {
		int dataCount = 0;

		if (dataSet != null) {
			dataCount = dataSet.size();
		}

		for (int i = 0; i < dataCount; i++) {
			Data data = (Data) dataSet.get(i);
			List sendersNames = getSendersIds();
			int senderListSize = sendersNames.size();
			for (int j = 0; j < senderListSize; j++) {
				String senderName = (String) sendersNames.get(j);
				try {
					send(senderName, data);
				} catch (CiliaException ex) {
					log.error("send exception " + ex.getStackTrace().toString());
					throw new CiliaException(ex.getMessage());
				}
			}
		}

	}

	public List getSendersIds() {
		return dispatcher.getSendersIds();
	}

	public void removeSender(String portname, String senderName) {
		log.debug("remove sender '" + senderName + "'");
		dispatcher.removeSender(portname, senderName);
	}

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
