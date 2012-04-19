package fr.liglab.adele.cilia.framework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;

public abstract class AbstractIOAdapter {
	/**
	 * OSGi Bundle Context.
	 */
	BundleContext bcontext;

	Map locks = Collections.synchronizedMap(new HashMap());;

	Map waitingData = Collections.synchronizedMap(new HashMap());

	long timeout = 3000;

	String CILIA_KEY = "cilia.data.key";
	/**
	 * The Cilia logger.
	 */
	protected static Logger logger = LoggerFactory
			.getLogger("cilia.ipojo.runtime");

	public AbstractIOAdapter(BundleContext context) {
		bcontext = context;
	}

	/**
	 * Invoke a Chain with the data as parammeter.
	 * 
	 * @param data
	 *            Data to be passed to the chain.
	 * @return the processed data by the chain.
	 */
	public Data invokeChain(Data data) {
		return callAndWait(data);
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public Object invokeChain(Object content) {
		Data ndata = new Data(content);
		Data rs = callAndWait(ndata);
		if (rs != null) {
			return rs.getContent();
		}
		return null;
	}

	/**
	 * Send the data to the first mediator and wait for the reponse.
	 * 
	 * @param data
	 *            Data to send to the first mediator.
	 * @return the processed data.
	 */
	protected Data callAndWait(Data data) {
		String key = generateKey(data);
		data.setProperty(CILIA_KEY, key);
		Object lock = new Object();
		dispatchData(data);
		synchronized (locks) {
			locks.put(key, lock);
		}
		try {
			long initialTime = System.currentTimeMillis();
			long currentTime = initialTime;
			long waitingTime = timeout;
			synchronized (lock) {

				while (((currentTime < (initialTime + timeout)) && waitingTime > 1)
						&& (!waitingData.containsKey(key))) {
					logger.debug("Adapter will wait" + waitingTime);
					lock.wait(waitingTime);
					currentTime = System.currentTimeMillis();
					waitingTime = waitingTime - (currentTime - initialTime);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Data returnedData = (Data) waitingData.remove(key);
		if (logger.isDebugEnabled()) {
			logger.debug("Message obtained" + key);
			logger.debug("Data obtained" + returnedData);
		}
		return returnedData;
	}

	/**
	 * Generate a key to track response.
	 * 
	 * @param Data
	 *            the data use to generate the key, this key will be associated
	 *            to the data.
	 * @return the generated key.
	 */
	protected String generateKey(Data data) {
		return data.hashCode() + "";
	}

	/**
	 * 
	 * @param data
	 */
	public void receiveData(Data data) {
		String key = (String) data.getProperty(CILIA_KEY);
		Object lock;
		logger.debug("Notify Data obtained" + key);

		synchronized (locks) {
			if (locks.containsKey(key)) {
				logger.debug("Locks contain the key:" + key);
			}
			lock = locks.remove(key);
		}
		waitingData.put(key, data);
		if (lock != null) {
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public Data dispatchData(Data data) {
		return data;
	}

}
