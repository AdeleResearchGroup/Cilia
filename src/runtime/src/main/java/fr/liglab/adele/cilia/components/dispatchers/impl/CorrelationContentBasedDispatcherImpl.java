package fr.liglab.adele.cilia.components.dispatchers.impl;

import java.util.List;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.compendium.data.manipulation.DataEnrichment;



public class CorrelationContentBasedDispatcherImpl extends ContentBasedDispatcherImpl {

	public CorrelationContentBasedDispatcherImpl(BundleContext context) {
		super(context);
	}

	public void dispatch(List dataSet) throws CiliaException {
		dataSet = addCorrelationInfo(dataSet);
		super.dispatch(dataSet);
	}
	
	private List addCorrelationInfo(List dataList) {
		int code = dataList.hashCode();
		for (int i = 0 ; dataList != null && i < dataList.size(); i++) {
			Data data = (Data) dataList.get(i);
			data = DataEnrichment.addSplitInfo(data, dataList.size(), i, code);
		}
		return dataList;
	}
	
}
