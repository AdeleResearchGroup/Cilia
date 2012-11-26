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

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.util.Const;

/**
 * LDAP Cilia expression parser to be used in data
 * 
 * @author torito
 * 
 */
public class ExpressionLDAP implements CiliaExpression {
	/**
	 * Due to OSGi create a filter using the ldap expression this field is used
	 * to stock the last expression used.
	 */
	private String m_expression;
	/**
	 * OSGi ldap filter.
	 */
	private Filter filter;
	/**
	 * OSGi BundleContext
	 */
	private BundleContext bcontext;
	/**
	 * Token used to resolve variables.
	 */
	private static String VARIABLE_TOKEN = "\\$";
	/**
	 * Constructor
	 * 
	 * @param bc
	 *            OSGi BundleContext.
	 */
	private static Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_APPLICATION);

	public ExpressionLDAP(BundleContext bc) {
		this.bcontext = bc;
	}

	/**
	 * Evaluate the given expression and see if it match with the given data.
	 * 
	 * @param expression
	 *            ldap expression.
	 * @param data
	 *            Data to analize
	 * @return true if the expression match with the Data. False if not.
	 */
	public boolean evaluateBooleanExpression(String expression, Data data) {
		boolean result = false;
		if (data == null) {
			return false;
		}
		String newExpression = resolveVariables(expression, data);

		if (filter == null) {
			try {
				m_expression = newExpression;
				filter = bcontext.createFilter(m_expression);
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
				filter = null;
				return false;
			}
		}

		if ((m_expression.compareTo(newExpression) != 0)) {

			m_expression = newExpression;
			filter = createFilter();
			try {
				filter = null;
				filter = bcontext.createFilter(m_expression);
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			result = filter.match(data.getAllData());
		} catch (NullPointerException ex) {
			filter = createFilter();
			result = filter.match(data.getAllData());
		}

		if (!result) {
			Dictionary contentDictionary = getContentDictionary(data.getContent());
			if (contentDictionary != null) {
				try {
					result = filter.match(contentDictionary);
				} catch (NullPointerException ex) {
					filter = createFilter();
					result = (filter.match(contentDictionary));
				}
			}
		}
		return result;
	}

	/**
	 * Evaluate the given expression and see if it match with the given data.
	 * 
	 * @param expression
	 *            ldap expression.
	 * @param data
	 *            Data to analize
	 * @return result evaluation.
	 */
	public List evaluateExpression(String expre, Data data) {
		return Collections.singletonList(new Boolean(evaluateBooleanExpression(expre,
				data)));
	}

	/**
	 * 
	 * TODO: move the next code to another class.
	 */
	public String resolveVariables(String expression, Data data) {
		if (logger.isDebugEnabled()) {
			logger.debug("[ExpressionLDAP] Expression:" + expression);
			logger.debug("[ExpressionLDAP] Dictionary to use:" + data);
		}
		Vector variables = getVariables(expression);
		String resultExpression = replaceVariables(variables, expression, data);
		if (logger.isDebugEnabled()) {
			logger.debug("[ExpressionLDAP] Result:" + resultExpression);
		}
		return resultExpression;
	}

	/**
	 * TODO: move the next code to another class.
	 * 
	 * @param variable
	 * @param data
	 * @return
	 */
	private String resolveVariable(String variable, Data data) {
		// TODO:variable in header should be $header.variable
		// For instance is $variable
		String result = null;
		Object value = null;
		Dictionary dico = data.getAllData();
		value = dico.get(variable);
		if (value != null) {
			result = value.toString();
		}

		if (result == null) {
			// TODO:value in content should be $content.variable
			Dictionary contentDictionary = getContentDictionary(data.getContent());
			if (contentDictionary != null) {
				result = (String) contentDictionary.get(variable);
			}
		}
		if (result == null) {
			result = "";
		}
		return result;
	}

	/**
	 * TODO: move the next code to another class.
	 * 
	 * @param expression
	 * @return
	 */
	private Vector getVariables(String expression) {
		Vector variables = null;
		Pattern regex = Pattern.compile("\\((.*?\\$.*?\\))");
		Matcher m = regex.matcher(expression);
		variables = new Vector();
		if (m.find()) {
			String resu = m.group();
			resu = resu
					.substring(resu.indexOf('$'), resu.indexOf(')', resu.indexOf('$')));
			resu = resu.trim();
			String[] vars = resu.split("\\$");
			for (int i = 0; vars != null && i < vars.length; i++) {
				if (vars[i].indexOf(' ') > 0) {
					vars[i] = vars[i].substring(0, vars[i].indexOf(' '));
				}
				vars[i] = vars[i].trim();
				if (vars[i].length() > 0) {
					variables.add(vars[i]);
				}
			}
		}
		return variables;
	}

	/**
	 * TODO: move the next code to another class.
	 * 
	 * @param variables
	 * @param expre
	 * @param data
	 * @return
	 */
	private String replaceVariables(Vector variables, String expre, Data data) {
		if (variables == null) {
			return expre;
		}
		String replacedVariables = expre;
		for (int i = 0; i < variables.size(); i++) {
			String replacing = VARIABLE_TOKEN + variables.get(i);
			String value = resolveVariable((String) variables.get(i), data);
			replacedVariables = replacedVariables.replaceAll(replacing, value);
		}
		return replacedVariables;
	}

	/**
	 * TODO: move the next code to another class.
	 * 
	 * @param content
	 * @return
	 */
	private Dictionary getContentDictionary(Object content) {
		Dictionary dictionaryContent = null;
		if (content instanceof Dictionary) {
			dictionaryContent = (Dictionary) content;
		} else if (content instanceof Map) {
			Map dico = (Map) content;
			Properties propContent = new Properties();
			propContent.putAll(dico);
			dictionaryContent = propContent;
		}
		return dictionaryContent;
	}

	private Filter createFilter() {
		Filter lfilter;
		try {
			lfilter = bcontext.createFilter(m_expression);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			return null;
		}
		return lfilter;
	}

}
