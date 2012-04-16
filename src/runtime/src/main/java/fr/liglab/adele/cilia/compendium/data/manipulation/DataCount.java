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

package fr.liglab.adele.cilia.compendium.data.manipulation;

import java.util.List;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.compendium.expressioncommands.CiliaExpression;
import fr.liglab.adele.cilia.compendium.expressioncommands.ExpressionFactory;

/**
 * This class is used to count Data
 * in a DataSet
 * @author torito 
 *
 */
public class DataCount {
    /**
     * CiliaExpression used to count the number of data
     * in a DataSet that match the given expression. 
     */
    private CiliaExpression expreParser;
    /**
     * The default language to count Data in DataSet
     * that match the given expression.
     */
    public static String LANGUAGE_DEFAULT = "ldap";
    /**
     * the choosen language expression.
     */
    private String language = LANGUAGE_DEFAULT;
    /**
     * OSGi BundleContext is used to use the
     * ldap filter provided in the OSGi framework.
     */
    private BundleContext bcontext = null; 
    /**
     * Constructor 
     * @param context OSGi BundleContext
     */
    public DataCount (BundleContext context) {
        bcontext = context;
    }
    /**
     * Constructor
     */
    public DataCount () {}
    
    /**
     * Return the number of Data in the DataSet
     * (SimpleCount)
     * @param ds
     * @return
     */
    public int count (List/*<Data>*/ ds) {
        return ds.size();
    }
    /**
     * Create the Expression Parser used to count.
     * @throws CiliaException When ExpressionParser could not be created.
     */
    private void createExpressionParser() throws CiliaException {
        if (expreParser == null) {
            ExpressionFactory ef = new ExpressionFactory(bcontext);
            expreParser = ef.getExpressionParser(language);
        }
        return;
    }
    /**
     * Count number of Data in DataSet that match the given expression.
     * This method should be called in a synchronized block.
     * @param dataset DataSet where it will search matching.
     * @param expression expression used to search.
     * @return number of Data in DataSet that match expression.
     * @throws CiliaException When ExpressionParser could not be created.
     */
    public int count(List/*<Data>*/ dataset, String expression) throws CiliaException {
        createExpressionParser();
        
        int count = 0;
        boolean matched = false;
        
        int size = dataset.size();
        if (expression.compareTo("") != 0) {
            for (int i = 0; i < size; i++) {
                Data mdata = (Data)dataset.get(i);
                matched = expreParser.evaluateBooleanExpression(expression, mdata);
                if (matched) {
                    count++;
                }
            }
        } else {
            //if expression is empty it will return DataSet.getSize() 
            count = dataset.size();
        }
        return count;
    }
}
