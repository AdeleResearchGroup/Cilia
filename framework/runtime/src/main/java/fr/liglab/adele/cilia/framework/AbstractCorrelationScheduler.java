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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.data.CiliaExpression;
import fr.liglab.adele.cilia.framework.data.ExpressionFactory;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public abstract class AbstractCorrelationScheduler extends AbstractScheduler {

    /**
     * Correlation expression to says which incoming messages belongs together.
     */
    protected String correlation;

    /**
     * Correlation expression by default.
     */
    protected static final String CORRELATION_DEFAULT = "";

    /**
     *
     */
    protected CiliaExpression expreParser;

    public AbstractCorrelationScheduler(BundleContext bcontext) {
        ExpressionFactory ef = new ExpressionFactory(bcontext);
        try {
            expreParser = ef.getExpressionParser("ldap");
        } catch (CiliaException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void setCorrelation(String corre) {
        if (corre == null) {
            corre = CORRELATION_DEFAULT;
        }
        correlation = corre;
    }

    public void notifyData(Data data) {
        String corre = getCorrelationKey(data);
        List<Data> dataset = null;
        boolean complet = false;
        if (getData() == null) {
            appLogger.warn("data received map is null");
        }
        synchronized (getData()) {
            dataset = (List<Data>) getData().get(corre);
            if (appLogger.isDebugEnabled())
                appLogger.debug("[AbstractCorrelationScheduler] correlation id" + corre);
            if (dataset == null) {
                dataset = new ArrayList();
                getData().put(corre, dataset);
                appLogger.debug("[AbstractCorrelationScheduler] new data set");
            }

            dataset.add(data);

            complet = checkCompletness(dataset);

            if (complet) {
                getData().remove(corre);
            }

        }

        // If the dataset has been completed, it is no more present on the
        // DataSets structure and so, the process action is thread safe.
        //
        if (complet) {
            process(dataset);
        }

    }

    /**
     * @param data
     * @return
     */
    public String getCorrelationKey(Data data) {
        String corre = "";
        corre = expreParser.resolveVariables(correlation, data);
        return corre;
    }

    /**
     * @param dataset
     * @return
     */
    public abstract boolean checkCompletness(List dataset);

}
