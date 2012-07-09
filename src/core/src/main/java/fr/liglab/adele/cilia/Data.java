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

package fr.liglab.adele.cilia;

import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * Data Used to encapsulate the data to be processed on the MediatorImpl. It has a
 * 'content' of type Object, and a dictionary of extra informations (metadata).
 * 
 * The values of the metadata cannot be <code>null</code>.
 * 
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */

//@SuppressWarnings({"rawtypes", "unchecked"})
public class Data {

	/**
	 * All the data will be stored in this Data dictionary.
	 */
	private Hashtable/* <String, Object> */data = new Hashtable/* <String, Object> */();

	/**
	 * The lock used to keep data integrity.
	 */
	private ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();

	/**
	 * This name is not used as a key to obtain data content. The only purpose
	 * is to have a data identifier.
	 * 
	 * Once set, the data.name property cannot be changed. If no data name is
	 * specified at creation time, the default "data" name is used.
	 * 
	 * <p>
	 * Type : <code>String</code>.
	 */
	public static final String DATA_NAME = "data.name";

	/**
	 * Key to be used to obtain the data content.
	 * 
	 * <p>
	 * Type : <code>Object</code>.
	 */
	public static final String DATA_CONTENT = "data.content";

	/**
	 * Key to obtain the data type.
	 * 
	 * <p>
	 * Type : <code>String</code>.
	 */
	public static final String DATA_TYPE = "data.type";

	/**
	 * Key to obtain the data source.
	 * 
	 * <p>
	 * Type : <code>String</code>.
	 */
	public static final String DATA_SOURCE = "data.source";
	/**
	 * Date timespamp.
	 * 
	 * The data.timestamp property is automatically set when the data is created
	 * and cannot be changed.
	 * 
	 * <p>
	 * Type : <code>java.util.Date</code>.
	 */
	public static final String DATA_TIMESTAMP = "data.timestamp";
	/**
	 * Key used to add or get the identifier unique of this data.
	 * 
	 * <p>
	 * Type : <code>String</code>.
	 */
	public static final String DATA_ID = "data.id";
	/**
	 * Data Types. // Not used actually.
	 */
	public static final String OBJECT_DATA = "object";
	/**
	 * Data Types. // Not used actually
	 */
	public static final String TEXT_DATA = "text";
	/**
	 * Data Types. // Not used actually
	 */
	public static final String BYTE_DATA = "byte";

	/**
	 * Return address.
	 */
	public static final String RETURN_ADDRESS = "data.return.address";

	private static final String DATA_TARGET = "data.source";

	/**
	 * Private verbatim copy constructor.
	 * 
	 * @param fromData
	 */
	private Data(final Data fromData) {
		try {
			fromData.lock.readLock().acquire();
			try {
				data = new Hashtable(fromData.data);
			} finally {
				fromData.lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param content
	 * @param name
	 * @param metadata
	 */
	public Data(Object content, String name, Dictionary metadata) {

		String finalName;
		Date timestamp = new Date();
		Date timestampF = null;
		if (content != null) {
			data.put(DATA_CONTENT, content);
		}

		if (name == null) {
			finalName = "unnamed";
		} else {
			finalName = name;
		}

		data.put(DATA_NAME, finalName);

		data.put(DATA_TIMESTAMP, timestamp);// we use the dico because there is
											// not possible to add timestamp
											// using setProperty.
		if (metadata != null) {
			Enumeration e = metadata.keys();
			while (e.hasMoreElements()) {
				String key = e.nextElement().toString();
				if (!key.equals(DATA_CONTENT) && !key.equals(DATA_NAME)
						&& !key.equals(DATA_TIMESTAMP)) {
					setProperty(key, metadata.get(key));
				}
			}
			Object ts = metadata.get(DATA_TIMESTAMP);
			if (ts != null) {
				try {
					timestampF = (Date) ts;
				} catch (Exception ex) { // is time stamp could be converted to
											// Time.
					timestampF = timestamp;
				}
			} else {
				timestampF = timestamp;
			}
			data.put(DATA_TIMESTAMP, timestampF);// we use the dico because
													// there is not possible to
													// add timestamp using
													// setProperty.
		}

	}

	/**
	 * Constructor
	 * 
	 * @param content
	 */
	public Data(final Object content) {
		this(content, "data", null);
	}

	/**
	 * Constructor
	 * 
	 * @param content
	 * @param name
	 */
	public Data(final Object content, final String name) {
		this(content, name, null);
	}

	/**
	 * Get a metadata
	 * 
	 * @param key
	 * @return
	 */
	public final Object getProperty(final String key) {
		try {
			lock.readLock().acquire();
			try {
				return this.data.get(key);
			} finally {
				lock.readLock().release();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Add a metadata
	 * 
	 * @param key
	 * @param value
	 */
	public final void setProperty(final String key, final Object value) {
		// Protect the data.name and data.timestamp properties from changes.
		if (!DATA_NAME.equals(key) && !DATA_TIMESTAMP.equals(key)) {

			// Check that special properties type.
			if (DATA_ID.equals(key) && !(value instanceof String)) {
				throw new IllegalArgumentException(
						"The data.id property must be a String.");
			} else if (DATA_TYPE.equals(key) && !(value instanceof String)) {
				throw new IllegalArgumentException(
						"The data.type property must be a String.");
			} else if (DATA_SOURCE.equals(key) && !(value instanceof String)) {
				throw new IllegalArgumentException(
						"The data.source property must be a String.");
			}

			try {
				lock.writeLock().acquire();
				try {
					this.data.put(key, value);
				} finally {
					lock.writeLock().release();
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new UnsupportedOperationException();
			}

		} else {
			throw new IllegalArgumentException(
					"The data.name and data.timestamp properties cannot be changed.");
		}
	}

	/**
	 * Remove a metadata
	 * 
	 * @param key
	 */
	public final void removeProperty(final String key) {
		// Protect the data.name and data.timestamp properties from removal.
		if (!DATA_NAME.equals(key) && !DATA_TIMESTAMP.equals(key)) {
			try {
				lock.writeLock().acquire();
				try {
					this.data.remove(key);
				} finally {
					lock.writeLock().release();
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Get data content plus all the metadata associated to it.
	 * 
	 * Changes to the data are not propagated to the returned dictionary.
	 * 
	 * @return
	 */
	public final Dictionary getAllData() {
		try {
			lock.readLock().acquire();
				try {
			return new Hashtable(data);
		} finally {
			lock.readLock().release();
		}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new UnsupportedOperationException();
		}


	}

	/**
	 * Get the content
	 * 
	 * @return
	 */
	public final Object getContent() {
		return getProperty(DATA_CONTENT);
	}

	public final boolean hasContent() {
		if (getProperty(DATA_CONTENT) == null) {
			return false;
		}
		return true;
	}

	/**
	 * Set the data content
	 * 
	 * @param content
	 */
	public final void setContent(final Object content) {
		setProperty(DATA_CONTENT, content);
	}

	/**
	 * Get the data name
	 * 
	 * @return
	 */
	public final String getName() {
		return (String) getProperty(DATA_NAME);
	}

	/**
	 * Get the data source
	 * 
	 * @return
	 */
	public final String getLastReceivingPort() {
		return (String) getProperty(DATA_SOURCE);
	}

	/**
	 * Set the data source
	 * 
	 * @param port
	 */
	public final void setLastReceivingPort(final String port) {
		setProperty(DATA_SOURCE, port);
	}
	
	/**
	 * Set the data source
	 * 
	 * @param source
	 */
	public final void setLastDeliveryPort(final String port) {
		setProperty(DATA_TARGET, port);
	}

	/**
	 * Get the data source
	 * 
	 * @return
	 */
	public final String getLastDeliveryPort() {
		return (String) getProperty(DATA_TARGET);
	}
	
	/**
	 * Get data timestamp
	 * 
	 * @return
	 */
	public final Object getTimestamp() {
		return getProperty(DATA_TIMESTAMP);
	}

	/**
	 * Get data type
	 * 
	 * @return
	 */
	public final String getType() {
		return (String) getProperty(DATA_TYPE);
	}

	/**
	 * Set the data type
	 * 
	 * @param type
	 */
	public final void setType(final String type) {
		setProperty(DATA_TYPE, type);
	}

	/**
	 * Set the identificator used to route message.
	 * 
	 * @param id
	 *            identificator to set.
	 */
	public final void setId(final String id) {
		setProperty(DATA_ID, id);
	}

	/**
	 * 
	 * @return ID setted before.
	 */
	public final String getId() {
		return (String) getProperty(DATA_ID);
	}

	/**
	 * @deprecated
	 * @param key
	 * @return
	 */
	public final Object get(final Object key) {
		return getProperty(key.toString());
	}

	/**
	 * Return an exact copy of the given data.
	 * 
	 * Changes to the given data are not propagated to the returned copy.
	 * 
	 */
	public final Object clone() {
		return new Data(this);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Dictionary allData = this.getAllData();
		sb.append(allData.toString());
		return sb.toString();
	}

}
