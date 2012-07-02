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
import fr.liglab.adele.cilia.model.MediatorComponent;

public class ParserConfiguration {
	MediatorComponent mc;
	Map config;

	public ParserConfiguration(MediatorComponent mc) {
		this.mc = mc;
		config = ConfigurationHelper.getRootConfig(mc);
	}

	public boolean addVariable(String variableId, boolean enable) {
		boolean isInserted  ;
		try {
			ConfigurationHelper.checkStateVarId(variableId);
			ConfigurationHelper.storeEnable(config, variableId, enable);
			isInserted = true ;
		} catch (CiliaIllegalParameterException e) {
			isInserted=false ;
		}
		return isInserted;
	}

	public void addSetUp(String variableId, String queue, String dataFlow) {
		try {
			int q = Integer.parseInt(queue);
			ConfigurationHelper.checkQueueSize(q);
			ConfigurationHelper.checkDataFlowFilter(dataFlow);
			/* store if no error */
			ConfigurationHelper.getModelMonitoring(mc).setQueueSize(variableId, q) ;
			ConfigurationHelper.storeDataFlowControl(config, variableId, dataFlow);
		} catch (Exception e) {	
		}
	}

	public void addThreshold(String variableId, String low, String veryLow, String high,
			String veryHigh) {
		double dlow, dhigh, dverylow, dveryhigh;
		try {
			if (low != null) {
				dlow = Integer.parseInt(low);
			} else dlow = Double.NaN ;
			
			if (veryLow != null) {
				dverylow = Integer.parseInt(veryLow);
			}else dverylow = Double.NaN ;
			
			if (high != null) {
				dhigh = Integer.parseInt(high);
			}else dhigh = Double.NaN ;
			
			if (veryHigh != null) {
				dveryhigh = Integer.parseInt(veryHigh);
			}else dveryhigh = Double.NaN ;

			/* Store if no error  */
		
			if (dlow !=Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setLow(variableId, dlow) ;	
			}
			if (dverylow !=Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setVeryLow(variableId, dverylow) ;	
			}
			if (dhigh !=Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setHigh(variableId, dhigh) ;	
			}
			if (dveryhigh !=Double.NaN) {
				ConfigurationHelper.getModelMonitoring(mc).setVeryHigh(variableId, dveryhigh) ;	
			}
		} catch (NumberFormatException e) {
		}
	}
	
	public void configure() {
		ConfigurationHelper.storeRootConfig(mc, config) ;
	}
}
