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

package fr.liglab.adele.cilia.framework.data;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.exceptions.CiliaException;

/**
 * This class is used to create an instance
 * of a Cilia Expression object.
 * Is intended to extend this class
 * to later use different comands
 * provided as a services.
 * @author torito
 *
 */
public class ExpressionFactory {
    /**
     * Constant to create an ldap CiliaExpression object
     */
    public static final  String LDAP = "ldap";
    /**
     * Constant to create an xpath CiliaExpression object
     */
    public static final  String XPATH = "xpath";
    /**
     * OSGi BundleContext
     */
    private BundleContext bcontext = null;
    /**
     * Constructor.
     * @param context OSGi BundleContext.
     */
    public ExpressionFactory (BundleContext context) {
        bcontext = context;
    }
    /**
     * create an instance of a CiliaExpression.
     * @param expression
     * @return
     * @throws CiliaException
     */
    public CiliaExpression getExpressionParser(String expression) throws CiliaException{
        CiliaExpression ciliaExpression = null;
        if (expression.compareTo(LDAP) == 0) {
            if (bcontext == null) {
                throw new CiliaException ("LDAP expression require BundleContext to be created");
            }
            ciliaExpression = new ExpressionLDAP(bcontext);
        } else if (expression.compareTo(XPATH) == 0) {
            ciliaExpression = new ExpressionXPATH();
        }
        return ciliaExpression;
    }
}
