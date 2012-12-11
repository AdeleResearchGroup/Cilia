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

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;

/**
 * thresolds on runtime data
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface Thresholds extends Node {

	/**
	 * 
	 * @return Categories of state variables variables
	 */
	String[] getCategories() ;

	/**
	 * 
	 * @param category
	 *          
	 * @return list of variable per category of all state variable name if
	 *         category is null
	 */
	String[] getVariableNameByCategory(String category) ;
	
	/**

	 *          
	 * @return all variable name  
	 */
	String[] getAllVariablesName()  ;
	/** 
	 * @param variableId 
	 * @return true if state enable , false disable
	 * @throws CiliaIllegalStateException
	 * @throws CiliaIllegalParameterException
	 */
	boolean getStateVariableState(String variableId) throws CiliaIllegalStateException,
			CiliaIllegalParameterException;
	
	/**
	 * Set threshols low on numerical variable only
	 * 
	 * @param variableId
	 *            variable name
	 * @param low
	 * @param veryLow
	 *            Double.Na if for no veryLow threshold
	 * @throws CiliaIllegalParameterException
	 *             if variable name is null
	 */
	void setLow(String variableId, double low) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * Set thresholds high
	 * 
	 * @param variableId
	 *            variable name
	 * @param high
	 * @param veryhigh
	 *            Double.Na if for no threshold very high
	 * @throws CiliaIllegalParameterException
	 * @throws CiliaIllegalStateException
	 */
	void setHigh(String variableId, double high) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * Set threshols low on numerical variable only
	 * 
	 * @param variableId
	 *            variable name
	 * @param low
	 * @param veryLow
	 *            Double.Na if for no veryLow threshold
	 * @throws CiliaIllegalParameterException
	 *             if variable name is null
	 */
	void setVeryLow(String variableId, double verylow)
			throws CiliaIllegalParameterException, CiliaIllegalStateException;

	/**
	 * Set thresholds high
	 * 
	 * @param variableId
	 *            variable name
	 * @param high
	 * @param veryhigh
	 *            Double.Na if for no threshold very high
	 * @throws CiliaIllegalParameterException
	 * @throws CiliaIllegalStateException
	 */
	void setVeryHigh(String variableId, double veryhigh)
			throws CiliaIllegalParameterException, CiliaIllegalStateException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the low threshold , Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 *             if variable name is null
	 * @throws CiliaIllegalStateException
	 */
	double getLow(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the very low threshold, Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 * @throws CiliaIllegalStateException
	 */
	double getVeryLow(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the high threshold, Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 */
	double getHigh(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the very high threshold, Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 */
	double getVeryHigh(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;
}
