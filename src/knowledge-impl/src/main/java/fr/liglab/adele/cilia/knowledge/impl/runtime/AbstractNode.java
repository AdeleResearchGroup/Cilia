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

package fr.liglab.adele.cilia.knowledge.impl.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.util.measurement.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.exception.IllegalParameterException;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
import fr.liglab.adele.cilia.knowledge.runtime.RawData;
import fr.liglab.adele.cilia.knowledge.runtime.SetUp;
import fr.liglab.adele.cilia.knowledge.runtime.Thresholds;
import fr.liglab.adele.cilia.util.concurrent.ConcurrentReaderHashMap;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.SyncList;

/**
 * Base class for monitored mediator/adapter
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public abstract class AbstractNode implements SetUp, RawData, Thresholds {

	protected final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private static final int NB_THRESHOLD = 4;

	/* unique identifier */
	protected final String uuid;
	protected final String chain;
	protected final String node;

	/* list of variables managed by this component */
	protected Map stateVariables = new ConcurrentReaderHashMap();

	public AbstractNode(String uuid, String chain, String node) {
		this.uuid = uuid;
		this.chain = chain;
		this.node = node;
	}

	public String uuid() {
		return uuid;
	}

	public String chainId() {
		return chain;
	}

	public String nodeId() {
		return node;
	}

	public void setLow(String variableId, double low, double verylow) throws IllegalParameterException {
		if (variableId == null)
			throw new IllegalParameterException("Variable id must not be null !");
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new IllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		measures.setLow(low);
		measures.setVeryLow(verylow);
	}

	public void setHigh(String variableId, double high, double veryhigh) throws IllegalParameterException {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new IllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		measures.setHigh(high);
		measures.setVeryHigh(veryhigh);
	}

	public double getLow(String variableId) throws IllegalParameterException {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new IllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getLow();
	}

	public double getVeryLow(String variableId) throws IllegalParameterException  {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new IllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getVeryLow();
	}

	public double getHigh(String variableId) throws IllegalParameterException  {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new IllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getHigh();
	}

	public double getVeryHigh(String variableId) throws IllegalParameterException   {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new IllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getVeryHigh();
	}

	public List measures(String variableId)   {
		List m;
		if (variableId == null)
			throw new RuntimeException("Variable id must not be null !");
		/* retreive the component */
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures != null) {
			m = measures.getMeasure();
		} else
			m = Collections.EMPTY_LIST;
		return m;
	}

	public int addMeasure(String variableId, Object obj) {
		int evt = -1;
		/* retreive the component */
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures != null) {
			/* insert a new measure */
			evt = measures.addMeasure(obj);
			logger.info("Received variable [{}], value [{}]", this.toString(),
					obj.toString());
		}
		return evt;
	}

	protected class Observations {

		public int queueSize;
		public SyncList measures;
		public double[] threshold; /* list of threshold */

		public Observations(int size) {
			queueSize = size;
			measures = new SyncList(new ArrayList(size),
					new ReentrantWriterPreferenceReadWriteLock());
			threshold = new double[NB_THRESHOLD];
			for (int i = 0; i < NB_THRESHOLD; i++) {
				threshold[i] = Double.NaN;
			}
		}

		public int addMeasure(Object m) {
			try {
				measures.writerSync().acquire();
				try {
					measures.add(0, m);
					if (measures.size() > queueSize)
						measures.remove(queueSize - 1);
					return viability(m);
				} finally {
					measures.writerSync().release();
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex.getMessage());
			}
		}

		public List getMeasure() {
			try {
				measures.readerSync().acquire();
				try {
					return new ArrayList(measures);
				} finally {
					measures.readerSync().release();
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex.getMessage());
			}
		}

		public void setQueueSize(int queue) {
			if (queue < 1)
				return;
			try {
				measures.writerSync().acquire();
				try {
					for (int i = queueSize; i < queue; i++)
						measures.remove(i - 1);
					queueSize = queue;
				} finally {
					measures.writerSync().release();
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex.getMessage());
			}
		}

		public void setVeryLow(double d) {
			threshold[0] = d;
		}

		public void setLow(double d) {
			threshold[1] = d;
		}

		public void setHigh(double d) {
			threshold[2] = d;
		}

		public void setVeryHigh(double d) {
			threshold[3] = d;
		}

		public double getLow() {
			return threshold[1];
		}

		public double getVeryLow() {
			return threshold[0];
		}

		public double getHigh() {
			return threshold[2];
		}

		public double getVeryHigh() {
			return threshold[3];
		}

		public int viability(Object m) {
			if ((m != null) && (m instanceof Measurement)) {
				Measurement measure = (Measurement) m;
				if ((threshold[0] != Double.NaN) && (measure.getValue() < threshold[0]))
					return EventProperties.DATA_VERY_LOW;
				if ((threshold[1] != Double.NaN) && (measure.getValue() < threshold[1]))
					return EventProperties.DATA_LOW;
				if ((threshold[3] != Double.NaN) && (measure.getValue() > threshold[3]))
					return EventProperties.DATA_VERY_HIGH;
				if ((threshold[2] != Double.NaN) && (measure.getValue() > threshold[2]))
					return EventProperties.DATA_VERY_HIGH;
			}
			return EventProperties.DATA_UPDATE;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("uuid [").append(uuid()).append("] ");
		sb.append("qualified name [").append(chainId()).append("/").append(nodeId())
				.append("]");
		return sb.toString();
	}
}
