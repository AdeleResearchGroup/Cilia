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

package fr.liglab.adele.cilia.knowledge.configuration;

import java.util.Map;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.model.MediatorComponent;

public class ParserConfiguration {
	MediatorComponent mc;
	@SuppressWarnings("rawtypes")
	Map config;

	public ParserConfiguration(MediatorComponent mc) {
		this.mc = mc;
		config = ConfigurationHelper.getRootConfig(mc);
	}

	public void addVariable(String variableId, boolean enable)
			throws CiliaParserException {
		try {
			ConfigurationHelper.checkStateVarId(variableId);
			ConfigurationHelper.storeEnable(config, variableId, enable);
		} catch (CiliaIllegalParameterException e) {
			throw new CiliaParserException("Variable [" + variableId + "] is undefined, "+e.getMessage());
		}
	}

	public void addSetUp(String variableId, String queue, String dataFlow)
			throws CiliaParserException {
		try {
			int q = Integer.parseInt(queue);
			ConfigurationHelper.checkQueueSize(q);
			ConfigurationHelper.checkDataFlowFilter(dataFlow);
			/* store if no error */
			ConfigurationHelper.getModelMonitoring(mc).setQueueSize(variableId, q);
			ConfigurationHelper.storeDataFlowControl(config, variableId, dataFlow);
		} catch (CiliaIllegalParameterException e) {
			throw new CiliaParserException("Variable " + variableId
					+ " Setup is not valid ,"+e.getMessage());

		} catch (CiliaInvalidSyntaxException e) {
			throw new CiliaParserException("Variable " + variableId
					+ " Setup is not valid ,"+e.getMessage());
		}
	}

	public void addThreshold(String variableId, String low, String veryLow, String high,
			String veryHigh) throws CiliaParserException {
		double dlow, dhigh, dverylow, dveryhigh;
		try {
			if (low != null) {
				dlow = Integer.parseInt(low);
			} else
				dlow = Double.NaN;

			if (veryLow != null) {
				dverylow = Integer.parseInt(veryLow);
			} else
				dverylow = Double.NaN;

			if (high != null) {
				dhigh = Integer.parseInt(high);
			} else
				dhigh = Double.NaN;

			if (veryHigh != null) {
				dveryhigh = Integer.parseInt(veryHigh);
			} else
				dveryhigh = Double.NaN;

			/* Store if no error */

			if (dlow != Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setLow(variableId, dlow);
			}
			if (dverylow != Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setVeryLow(variableId,
						dverylow);
			}
			if (dhigh != Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setHigh(variableId, dhigh);
			}
			if (dveryhigh != Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setVeryHigh(variableId,
						dveryhigh);
			}
		} catch (NumberFormatException e) {
			throw new CiliaParserException("Variable " + variableId
					+ " threshold is not valid ,"+e.getMessage());
		}
	}

	public void configure() {
		ConfigurationHelper.storeRootConfig(mc, config);
	}
}
