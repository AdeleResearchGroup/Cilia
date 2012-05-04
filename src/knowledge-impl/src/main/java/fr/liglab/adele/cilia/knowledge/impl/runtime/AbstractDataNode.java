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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.dynamic.Measure;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.knowledge.eventbus.EventProperties;
import fr.liglab.adele.cilia.knowledge.impl.DataNode;
import fr.liglab.adele.cilia.knowledge.impl.Knowledge;
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
public abstract class AbstractDataNode implements DataNode {

	protected final Logger logger = LoggerFactory.getLogger(Knowledge.LOG_NAME);

	private static final int NB_THRESHOLD = 4;

	/* unique identifier */
	protected String uuid;
	protected String chainId; 
	protected String nodeId;
	
	/* list of variables managed by this component */
	protected Map stateVariables = new ConcurrentReaderHashMap();


	public String uuid() {
		return uuid;
	}

	public String chainId() {
		return chainId ;
	}

	public String nodeId() {
		return nodeId;
	}

	public void setLow(String variableId, double low, double verylow) throws CiliaIllegalParameterException {
		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id must not be null !");
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new CiliaIllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		measures.setLow(low);
		measures.setVeryLow(verylow);
	}

	public void setHigh(String variableId, double high, double veryhigh) throws CiliaIllegalParameterException {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new CiliaIllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		measures.setHigh(high);
		measures.setVeryHigh(veryhigh);
	}

	public double getLow(String variableId) throws CiliaIllegalParameterException {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new CiliaIllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getLow();
	}

	public double getVeryLow(String variableId) throws CiliaIllegalParameterException  {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new CiliaIllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getVeryLow();
	}

	public double getHigh(String variableId) throws CiliaIllegalParameterException  {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new CiliaIllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getHigh();
	}

	public double getVeryHigh(String variableId) throws CiliaIllegalParameterException   {
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures == null)
			throw new CiliaIllegalParameterException(
					"Monitoring has not been set for this variable " + variableId);
		return measures.getVeryHigh();
	}

	public Measure[] measures(String variableId) throws CiliaIllegalParameterException   {
		Measure[] m;
		if (variableId == null)
			throw new CiliaIllegalParameterException("Variable id must not be null !");
		/* retreive the component */
		Observations measures = (Observations) stateVariables.get(variableId);
		if (measures != null) {
			m = measures.getMeasure();
		} else
			m = new Measure[0];
		return m;
	}

	public int addMeasure(String variableId, Measure obj) {
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

		public int addMeasure(Measure m) {
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

		public Measure[] getMeasure() {
			try {
				measures.readerSync().acquire();
				try {
					ArrayList m=new ArrayList(measures) ;
					System.out.println(">>>>>>MEASURES"+m) ;
					return (Measure[]) m.toArray(new Measure[m.size()]);
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

		public int viability(Measure m) {
			if ((m != null) && (m.value() instanceof Long)) {
				Long l=(Long)m.value() ;
				if ((threshold[0] != Double.NaN) && (l.longValue() < threshold[0]))
					return EventProperties.DATA_VERY_LOW;
				if ((threshold[1] != Double.NaN) && (l.longValue() < threshold[1]))
					return EventProperties.DATA_LOW;
				if ((threshold[3] != Double.NaN) && (l.longValue() > threshold[3]))
					return EventProperties.DATA_VERY_HIGH;
				if ((threshold[2] != Double.NaN) && (l.longValue() > threshold[2]))
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
