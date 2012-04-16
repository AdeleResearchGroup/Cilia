package fr.liglab.adele.cilia.framework.components;

import java.util.List;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.CiliaDispatcher;
import fr.liglab.adele.cilia.framework.data.DataEnrichment;

public class CorrelationMulticastDispatcherImpl extends CiliaDispatcher {

	public CorrelationMulticastDispatcherImpl(BundleContext context) {
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
