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
package fr.liglab.adele.cilia.helper;

import java.util.List;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractAsyncIOAdapter;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class AdapterHelper extends AbstractAsyncIOAdapter implements MediatorTestHelper {

	/**
	 * Get last the port name where the mediator send data.
	 */
	public synchronized String lastExitPort() {
		return getLastData().getLastReceivingPort();
	}

	/**
	 * See if mediator has finish to process data.
	 */
	public synchronized boolean hasFinishToProcess() {
		return super.hasData();
	}

	/**
	 * Inject a single Data to the mediator.
	 * @param the data to inject.
	 * @return true if is injected, false if not.
	 */
	public boolean injectData(Data data) {
		try{
			dispatchData(data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Inject an array of data to the mediator.
	 * @param data, the array of data to inject.
	 * @return true if the array is injected, false if some data is not well injected.
	 */
	public boolean injectData(Data[] data) {
		boolean injected = false;
		for (int i = 0; i < data.length; i ++){
			injected = injectData(data[i]);
			if (!injected) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the last received Data.
	 */
	public synchronized Data getLastData() {
		return super.currentData.get(currentData.size());
	}

	/**
	 * Get the amount of data treated and delivered by the mediator.
	 */
	public synchronized int amountReceivedData() {
		return super.currentData.size();
		
	}

	/**
	 * Get and cleat the received data.
	 */
	public Data[] getReceivedData() {
		List<Data> data = super.getData();
		return (Data[]) data.toArray();
	}
	
	
	public void receiveData(Data data) {
		super.receiveData(data);
	}

	public  Data dispatchData(Data data) {
		return super.dispatchData(data);
	}
	
}
