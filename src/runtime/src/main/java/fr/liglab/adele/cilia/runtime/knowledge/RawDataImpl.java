package fr.liglab.adele.cilia.runtime.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.RawData;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.runtime.Const;

public class RawDataImpl extends NodeImpl implements RawData {

	private final Logger logger = LoggerFactory.getLogger(Const.LOGGER_KNOWLEDGE);
	
	private final ListNodes registry;

	public RawDataImpl(ListNodes registry, Node node) throws CiliaIllegalStateException {
		super(node);
		this.registry = registry;
	}

	private MediatorMonitoring getModel() throws CiliaIllegalStateException {
		MediatorMonitoring model = registry.get(uuid) ;
		if (model ==null) {
			throw new CiliaIllegalStateException("Node " + super.toString()
					+ "no longer exist");
		}
		return model ;
	}

	public boolean isValid() throws CiliaIllegalStateException {
		return getModel().getState();
	}

	public Measure[] measures(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		return getModel().measures(variableId);
	}

	public String[] getEnabledVariable() throws CiliaIllegalStateException {
		return getModel().enabledVariable();
	}

}
