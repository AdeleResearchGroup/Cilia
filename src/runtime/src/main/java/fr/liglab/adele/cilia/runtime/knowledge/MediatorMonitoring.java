/* Copyright Adele Team LIG
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

package fr.liglab.adele.cilia.runtime.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.ThresholdsCallback;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.ModelExtension;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.runtime.FirerEvents;
import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.SyncList;
import fr.liglab.adele.cilia.util.concurrent.SyncMap;

/**
 * Extended Model "type Monitoring"
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MediatorMonitoring implements ModelExtension {

	private static final int DEFAULT_QUEUE_SIZE = 1 ;
	private final Logger logger = LoggerFactory.getLogger(ConstRuntime.LOGGER_KNOWLEDGE);

	public static final String NAME = "monitoring";

	private static final int NB_THRESHOLD = 4;

	/* mediator component valid/invalid */
	private boolean isValid;
	private MediatorComponent model;
	private FirerEvents firerEvents;

	/* list of variables managed by this component */
	private SyncMap variablesId = new SyncMap(new HashMap(),
			new ReentrantWriterPreferenceReadWriteLock());

	public MediatorMonitoring() {
	}

	public MediatorComponent getModel() {
		return model;
	}

	public void setModel(MediatorComponent model) {
		this.model = model;
	}

	/* Return the mediator State */
	public boolean getState() {
		return isValid;
	}

	public void setFirerEvent(FirerEvents firer) {
		firerEvents = firer;
	}

	/* store the mediatorState */
	public void setMediatorStatus(boolean valid) {
		isValid = valid;
		if (valid)
			firerEvents.fireEventNode(EventsManagerImpl.EVT_VALID, model);
		else
			firerEvents.fireEventNode(EventsManagerImpl.EVT_INVALID, model);
	}

	public void setVariableStatus(String variableId, boolean enable) {
		firerEvents.fireEventVariableStatus(model, variableId, enable);
	}

	private Observations getObservations(String variableId) {
		Observations observations  ;
		if (!variablesId.containsKey(variableId)) {
			/* a variable with defaults configuration is created */
			observations = new Observations() ;
			variablesId.put(variableId, observations);
		} else {
			 observations = (Observations) variablesId.get(variableId);
		}
		return observations;
	}
	
	public Measure[] measures(String variableId) {	
		return getObservations(variableId).getMeasure();
	}

	public void addMeasure(String variableId, Measure measure) {
		/* retreive the component */
		int thresoldEvent =getObservations(variableId).addMeasure(measure);
		firerEvents.fireEventMeasure(model, variableId, measure);
		
		if (thresoldEvent != ThresholdsCallback.NONE) {
			/* fire event threshold reached */
			firerEvents.fireThresholdEvent(model, variableId, measure, thresoldEvent);
		}
		
		logger.info("Received variable [{},{}]", variableId, measure.toString());
	}

	
	public void setQueueSize(String variableId, int queueSize) {
		try {
			variablesId.writerSync().acquire();
			try {
				if (!variablesId.containsKey(variableId)) {
					/* a variable with defaults configuration is created */
					variablesId.put(variableId, new Observations(queueSize));
				} else {
					Observations observation = (Observations) variablesId.get(variableId);
					observation.setQueueSize(queueSize);
				}
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public int getQueueSize(String variableId) {
		try {
			variablesId.writerSync().acquire();
			try {
				return getObservations(variableId).getQueueSize();
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void setLow(String variableId, double low) {
		try {
			variablesId.writerSync().acquire();
			try {
				getObservations(variableId).setLow(low);
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void setVeryLow(String variableId, double verylow) {
		try {
			variablesId.writerSync().acquire();
			try {
				getObservations(variableId).setVeryLow(verylow);
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void setHigh(String variableId, double high) {
		try {
			variablesId.writerSync().acquire();
			try {
				getObservations(variableId).setHigh(high);
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}

	}

	public void setVeryHigh(String variableId, double veryhigh) {
		try {
			variablesId.writerSync().acquire();
			try {
				getObservations(variableId).setVeryHigh(veryhigh);
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public double getLow(String variableId) {
		try {
			variablesId.writerSync().acquire();
			try {
				return getObservations(variableId).getLow() ;
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public double getVeryLow(String variableId) {
		try {
			variablesId.writerSync().acquire();
			try {
				return getObservations(variableId).getVeryLow() ;
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public double getHigh(String variableId) throws CiliaIllegalParameterException {
		try {
			variablesId.writerSync().acquire();
			try {
				return getObservations(variableId).getHigh() ;
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public double getVeryHigh(String variableId) {
		try {
			variablesId.writerSync().acquire();
			try {
				return getObservations(variableId).getVeryHigh() ;
			} finally {
				variablesId.writerSync().release();
			}

		} catch (InterruptedException e) {
			logger.error("Interruped thread ", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String[] getEnabledVariable() {
		Hashtable props = model.getProperties();
		Map config = (Map)props.get("monitoring.base") ;
		Set enabled = (Set)config.get("enable") ;
		if (enabled==null) enabled = new HashSet();
		return (String[]) enabled.toArray(new String[enabled.size()]); 
	}
	
	/**
	 * Observation store measures from runtime
	 * 
	 */
	private class Observations {

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

		public Observations() {
			this(DEFAULT_QUEUE_SIZE);
		}

		/* circular fifo management */
		public int addMeasure(Measure m) {
			try {
				measures.writerSync().acquire();
				try {
					if (measures.size() >= queueSize)
						measures.remove(queueSize - 1);
					measures.add(0, m);
					return viability(m);
				} finally {
					measures.writerSync().release();
				}
			} catch (InterruptedException ex) {
				logger.error("Interruped thread ", ex);
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex.getMessage());
			}
		}

		public Measure[] getMeasure() {
			try {
				measures.readerSync().acquire();
				try {
					ArrayList m = new ArrayList(measures);
					return (Measure[]) m.toArray(new Measure[m.size()]);
				} finally {
					measures.readerSync().release();
				}
			} catch (InterruptedException ex) {
				logger.error("Interruped thread ", ex);
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex.getMessage());
			}
		}


		public void setQueueSize(int queue) {
			try {
				System.out.println("New queue Size "+queue+ "current size = "+measures.size()) ;
				measures.writerSync().acquire();
				try {
					if (measures.size() > queue) {
						/* Remove oldest measures */
						int over = measures.size() - queue ;
						for (int i=0 ; i< over ; i++) {
							measures.remove((measures.size()-1)) ;	
						}
					}
					queueSize = queue;
				} finally {
					measures.writerSync().release();
				}
			} catch (InterruptedException ex) {
				logger.error("Interruped thread ", ex);
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex.getMessage());
			}
		}

		public int getQueueSize() {
			return queueSize;
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
				Long l = (Long) m.value();
				if ((threshold[0] != Double.NaN) && (l.longValue() < threshold[0]))
					return ThresholdsCallback.VERY_LOW;
				if ((threshold[1] != Double.NaN) && (l.longValue() < threshold[1]))
					return ThresholdsCallback.LOW;
				if ((threshold[3] != Double.NaN) && (l.longValue() > threshold[3]))
					return ThresholdsCallback.VERY_HIGH;
				if ((threshold[2] != Double.NaN) && (l.longValue() > threshold[2]))
					return ThresholdsCallback.VERY_HIGH;
			}
			return ThresholdsCallback.NONE;
		}
	}

}
