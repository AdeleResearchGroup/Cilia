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

package fr.liglab.adele.cilia.framework.monitor.statevariable;

import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.measurement.Measurement;

import java.util.Dictionary;
import java.util.Hashtable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Condition {


    private Filter filter;
    private final Dictionary dico = new Hashtable(7);
    private final Object synchro = new Object();

    /**
     * Contructor
     *
     * @param bc         bunde context
     * @param expression LDAP expression or null
     * @throws InvalidSyntaxException
     */
    public Condition(BundleContext bc, String ldapfilter)
            throws CiliaInvalidSyntaxException {
        dico.put(FrameworkUtils.VALUE_CURRENT, new Double(Double.NaN));
        dico.put(FrameworkUtils.TIME_CURRENT, new Long(0));
        setCondition(bc, ldapfilter);
    }

    public Condition(Filter filter) {
        dico.put(FrameworkUtils.VALUE_CURRENT, new Double(Double.NaN));
        dico.put(FrameworkUtils.TIME_CURRENT, new Long(0));
        setCondition(filter);
    }

    public String getCondition() {
        String strFilter;
        synchronized (synchro) {
            if (filter == null) {
                strFilter = "";
            } else
                strFilter = filter.toString();
        }
        return new String(strFilter);
    }

    public void setCondition(BundleContext bc, String expression)
            throws CiliaInvalidSyntaxException {
        filter = null;
        if ((expression != null) && (expression.length() != 0)) {
            synchronized (synchro) {
                try {
                    filter = bc.createFilter(expression);
                } catch (InvalidSyntaxException e) {
                    throw new CiliaInvalidSyntaxException(e.getMessage(), e.getFilter());
                }
            }
        }
    }

    public void setCondition(Filter filter) {
        synchronized (synchro) {
            this.filter = filter;
        }
    }

    public boolean match(Double d, long timeCurrent, long timeElapsed) {
        boolean result;
        Long previousTime;
        Double previousValue;

        if (filter != null) {
            synchronized (synchro) {
                /* Current value */
                previousValue = (Double) dico.get(FrameworkUtils.VALUE_CURRENT);
                dico.put(FrameworkUtils.VALUE_PREVIOUS, previousValue);
                dico.put(FrameworkUtils.VALUE_CURRENT, new Double(d));

				/* Current time */
                previousTime = (Long) dico.get(FrameworkUtils.TIME_CURRENT);
                dico.put(FrameworkUtils.TIME_PREVIOUS, previousTime);
                dico.put(FrameworkUtils.TIME_CURRENT, new Long(timeCurrent));

				/* computes delta Absolute and delta relative */
                if ((previousValue.doubleValue() != Double.NaN)
                        && (d.doubleValue() != Double.NaN)) {
                    double dd = Math.abs(previousValue.doubleValue() - d.doubleValue());
                    dico.put(FrameworkUtils.DELTA_ABSOLUTE, new Double(dd));
                    dico.put(FrameworkUtils.DELTA_RELATIVE,
                            new Double(dd / Math.abs(d.doubleValue())));
                }
                /* computes time elapsed */
                dico.put(FrameworkUtils.TIME_ELAPSED, new Long(timeElapsed));
                result = filter.matchCase(dico);
            }
        } else
            result = false;
        return result;
    }

    public boolean match(Measurement m, long timeElapsed) {
        boolean result;
        Long previousTime;
        Double previousValue;

        if (filter != null) {
            synchronized (synchro) {
                /* Current value */
                previousValue = (Double) dico.get(FrameworkUtils.VALUE_CURRENT);
                dico.put(FrameworkUtils.VALUE_PREVIOUS, previousValue);
                dico.put(FrameworkUtils.VALUE_CURRENT, new Double(m.getValue()));

				/* Current time */
                previousTime = (Long) dico.get(FrameworkUtils.TIME_CURRENT);
                dico.put(FrameworkUtils.TIME_PREVIOUS, previousTime);
                dico.put(FrameworkUtils.TIME_CURRENT, new Long(m.getTime()));

				/* computes delta Absolute and delta relative */
                if ((previousValue.doubleValue() != Double.NaN)
                        && (m.getValue() != Double.NaN)) {
                    double d = Math.abs(previousValue.doubleValue() - m.getValue());
                    dico.put(FrameworkUtils.DELTA_ABSOLUTE, new Double(d));
                    dico.put(FrameworkUtils.DELTA_RELATIVE,
                            new Double(d / Math.abs(m.getValue())));
                }
				/* computes time elapsed */
                dico.put(FrameworkUtils.TIME_ELAPSED, new Long(timeElapsed));
                result = filter.matchCase(dico);

            }
        } else
            result = false;
        return result;
    }

    public boolean match(Measurement m) {
        return match(m, 0);
    }

    public boolean match(long timestamp, long timeElapsed) {
        boolean result;
        Long previousTime;

        if (filter != null) {
            synchronized (synchro) {
                previousTime = (Long) dico.get(FrameworkUtils.TIME_CURRENT);
                dico.put(FrameworkUtils.TIME_PREVIOUS, previousTime);
                dico.put(FrameworkUtils.TIME_CURRENT, new Long(timestamp));
                dico.put(FrameworkUtils.TIME_ELAPSED, new Long(timeElapsed));
                result = filter.matchCase(dico);
            }
        } else
            result = false;
        return result;
    }

    public void clear() {
        synchronized (synchro) {
            ((Hashtable) dico).clear();
            filter = null;
        }
    }
}
