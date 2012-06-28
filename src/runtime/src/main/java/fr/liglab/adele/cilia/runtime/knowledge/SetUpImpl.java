package fr.liglab.adele.cilia.runtime.knowledge;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.SetUp;
import fr.liglab.adele.cilia.Thresholds;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SetUpImpl extends NodeImpl implements SetUp, Thresholds {

	private final ListNodes registry;

	public SetUpImpl(final ListNodes registry, Node node) {
		super(node);
		this.registry = registry;
	}

	public String[] getCategories() {
		return MonitoringConfHelper.getCategories();
	}

	public String[] variablesByCategory(String category) {
		return MonitoringConfHelper.variablesByCategory(category);
	}

	public void setMonitoring(String variableId, int queueSize, String LdapFilter,
			boolean enable) throws CiliaIllegalParameterException,
			CiliaInvalidSyntaxException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.get(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		MonitoringConfHelper.checkQueueSize(queueSize);
		MonitoringConfHelper.createFilterDataFlow(LdapFilter);

	}

	public void setMonitoring(String variableId, int queueSize)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		MonitoringConfHelper.checkQueueSize(queueSize);
		MediatorMonitoring monitoring = registry.getAndStore(uuid);
		monitoring.setQueueSize(variableId, queueSize);
	}

	public void setMonitoring(String variableId, String ldapFilter)
			throws CiliaIllegalParameterException, CiliaInvalidSyntaxException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkDataFlowFilter(ldapFilter);
		Dictionary props = m.getModel().getProperties() ;
		Map configBase = (Map)props.get("monitoring.base") ;
		MonitoringConfHelper.storeDataFlowControl(configBase, variableId, ldapFilter);
		m.getModel().setProperty("monitoring.base", configBase) ;
	}

	public void setMonitoring(String variableId, boolean enable)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		Dictionary props = m.getModel().getProperties() ;
		
		Map configBase = (Map)props.get("monitoring.base") ;
		MonitoringConfHelper.storeEnable(configBase, variableId, enable) ;
		m.getModel().setProperty("monitoring.base", configBase) ;
	}

	public String[] getEnabledVariable() throws CiliaIllegalStateException {
		Set listEnabled = new HashSet() ;
		MediatorMonitoring m = registry.getAndStore(uuid);
		Map props = m.getModel().getProperties();
		Map baseLevelConfig = (Map)props.get("monitoring.base") ;
		if (baseLevelConfig !=null) {
			Set enabled = (Set)baseLevelConfig.get("enable");
			if (enabled !=null) {
				listEnabled.addAll(enabled) ;
			}
		}
		return (String[]) listEnabled.toArray(new String[listEnabled.size()]);
	}

	public int getQueueSize(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);

		return m.getQueueSize(variableId);
	}

	public String getFlowControl(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
	    
		return null;
	}

	public void setLow(String variableId, double low)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		m.setLow(variableId, low);

	}

	public void setVeryLow(String variableId, double veryLow)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		m.setVeryLow(variableId, veryLow);
	}

	public void setHigh(String variableId, double high)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		m.setHigh(variableId, high);
	}

	public void setVeryHigh(String variableId, double veryHigh)
			throws CiliaIllegalParameterException, CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		m.setVeryHigh(variableId, veryHigh);
	}

	public double getLow(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		return m.getLow(variableId);
	}

	public double getVeryLow(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		return m.getVeryLow(variableId);
	}

	public double getHigh(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		return m.getHigh(variableId);
	}

	public double getVeryHigh(String variableId) throws CiliaIllegalParameterException,
			CiliaIllegalStateException {
		MediatorMonitoring m = registry.getAndStore(uuid);
		MonitoringConfHelper.checkStateVarId(variableId);
		return m.getVeryHigh(variableId);
	}

}
