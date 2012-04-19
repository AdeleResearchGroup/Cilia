package fr.liglab.adele.cilia.framework.components;

import java.util.List;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.data.DataEnrichment;

public class CorrelationSchedulerImpl extends AbstractCorrelationScheduler {

	public CorrelationSchedulerImpl(BundleContext bcontext) {
		super(bcontext);
	}

	/**
	 * List of dataSets.
	 */
	//private Map/* <String, DataSet> */mapListData = new HashMap/*
	//															 * <String,
	//															 * List<Data>>
	//															 */();

	public boolean checkCompletness(List dataset) {
		logger.debug("will check completness");
		boolean completness = false;
		int count = 0;
		int splitTotal = 0;

		count = dataset.size();

		if (count > 0) {
			splitTotal = DataEnrichment.getSplitTotal((Data) dataset.get(0));
		}
		if (splitTotal == count) {
			completness = true;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("count = " + count);
			logger.debug("split Total = " + splitTotal);
		}
		return completness;
	}
}
