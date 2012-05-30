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

/**
 * thresolds on runtime data
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface Thresholds extends Node {

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
	void setLow(String variableId, double low, double veryLow)
			throws CiliaIllegalParameterException;

	/**
	 * Set thresholds high
	 * 
	 * @param variableId
	 *            variable name
	 * @param high
	 * @param veryhigh
	 *            Double.Na if for no threshold very high
	 * @throws CiliaIllegalParameterException
	 */
	void setHigh(String variableId, double high, double veryhigh)
			throws CiliaIllegalParameterException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the low threshold , Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 *             if variable name is null
	 */
	double getLow(String variableId) throws CiliaIllegalParameterException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the very low threshold, Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 */
	double getVeryLow(String variableId) throws CiliaIllegalParameterException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the high threshold, Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 */
	double getHigh(String variableId) throws CiliaIllegalParameterException;

	/**
	 * 
	 * @param variableId
	 *            variable name
	 * @return double value setted for the very high threshold, Double.Na if no
	 *         thresolhold
	 * @throws CiliaIllegalParameterException
	 */
	double getVeryHigh(String variableId) throws CiliaIllegalParameterException;
}
