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
/**
 * 
 */
package fr.liglab.adele.cilia.helper.impl;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.AbstractDispatcher;
import fr.liglab.adele.cilia.helper.DispatcherProcessorHelper;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class DispatcherProcessorHelperImpl extends AbstractDispatcher implements DispatcherProcessorHelper {

	List<Data> receivedData = new ArrayList<Data>();
	/**
	 * @param context
	 */
	public DispatcherProcessorHelperImpl(BundleContext context) {
		super(context);
	}

	@Override
	public void dispatch(Data data) throws CiliaException {
		receivedData.add(data);
	}

	
	public void clear() {
		receivedData.clear();
	}

	public List<Data> getData() {
		List<Data> data = new ArrayList<Data>(receivedData);
		clear();
		return data;
	}

	
	public int getAmountData() {
		return receivedData.size();
	}


	public Data getLastData() {
		if (getAmountData() == 0) {
			return null;
		}
		return receivedData.get(getAmountData()-1);
	}

}
