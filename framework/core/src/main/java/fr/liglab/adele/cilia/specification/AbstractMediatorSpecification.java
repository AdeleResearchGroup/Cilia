package fr.liglab.adele.cilia.specification;


import java.util.HashMap;

import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.model.impl.MediatorImpl;
import fr.liglab.adele.cilia.model.impl.PortImpl;
import fr.liglab.adele.cilia.model.impl.PortType;

public abstract class AbstractMediatorSpecification implements MediatorSpecification {

	private MediatorImpl mediatorSpec;
	
	protected HashMap inports = new HashMap();
	
	protected HashMap outports = new HashMap();
	
	private ComponentImpl dispatcherDef;
	
	private ComponentImpl processorDef;
	
	private ComponentImpl schedulerDef;
	
	
	
	private static final String DEFAULT_CAT = "generic";
	
	private static final String DEFAULT_NS = "fr.liglab.adele.cilia";
	
	public AbstractMediatorSpecification(String name, String namespace, String category){
		mediatorSpec = new MediatorImpl(name, name, namespace, category, null, null, null);
	}
	
	/**
	 * Set the mediator category.
	 * @param category the given category.
	 */
	public MediatorSpecification setCategory( String category){
		mediatorSpec.setCategory(category);
		return this;
	}
	/**
	 * Get the mediator category.
	 * @return the mediator category.
	 */
	public String getCategory(){
		if (mediatorSpec.getCategory() == null) {
			return DEFAULT_CAT;
		}
		return mediatorSpec.getCategory();
	}
	
	/**
	 * Set the mediator namespace.
	 * @param namespace the mediator namespace.
	 */
	public MediatorSpecification setNamespace(String namespace){
		mediatorSpec.setNamespace(namespace);
		return this;
	}
	/**
	 * Get the mediator namespace.
	 * @return the mediator namespace.
	 */
	public String getNamespace(){
		if (mediatorSpec.getNamespace() == null) {
			return DEFAULT_NS;
		}
		return mediatorSpec.getNamespace();
	}
	
	
	/**
	 * Retrieve the mediator specification name.
	 * @return
	 */
	public String getName(){
		return mediatorSpec.getType();
	}
	
	/**
	 * Assign the scheduler info to the mediator.
	 * @param schedulareName
	 * @param schedulareNamespace
	 */
	public MediatorSpecification setScheduler(String schedulareName, String schedulerNamespace){
		schedulerDef = new ComponentImpl(schedulareName, schedulareName, schedulerNamespace, null);
		return this;	
	}
	/**
	 * Retrieve the scheduler assigned name.
	 * @return the scheduler name.
	 */
	public String getSchedulerName(){
		if (schedulerDef == null) {
			return null;
		}
		return schedulerDef.getType();
	}
	/**
	 * Retrieve the chosen scheduler namespace.
	 * @return the scheduler namespace. NULL when there is not scheduler assigned.
	 */
	public String getSchedulerNamespace(){
		if (schedulerDef == null) {
			return null;
		}
		if (schedulerDef.getNamespace() == null) {
			return DEFAULT_NS;
		}
		return schedulerDef.getNamespace();
	}
	
	
	/**
	 * Set the chosen processor info.
	 * @param processorName the processor name.
	 * @param processorNamespace the processor namesâce.
	 */
	public MediatorSpecification setProcessor(String processorName, String processorNamespace){
		processorDef = new ComponentImpl(processorName, processorName, processorNamespace,null);
		return this;	
	}
	/**
	 * Retrieve the chosen processor name. 
	 * @return the processor name.
	 */
	public String getProcessorName(){
		if (processorDef == null) {
			return null;
		}
		return processorDef.getType();
	}
	/**
	 * Retrieve the processor namespace.
	 * @return the processor namespace.
	 */
	public String getProcessorNamespace(){
		if (processorDef == null) {
			return null;
		}
		if (processorDef.getNamespace() == null) {
			return DEFAULT_NS;
		}
		return processorDef.getNamespace();
	}
	
	
	/**
	 * Assign the chosen dispatcher name.
	 * @param dispatcherName the chosen dispatcher name.
	 * @param dispatcherNamespace the chosen dispatcher namespace.
	 */
	public MediatorSpecification setDispatcher(String dispatcherName, String dispatcherNamespace){
		dispatcherDef = new ComponentImpl(dispatcherName,dispatcherName, dispatcherNamespace, null);
		return this;	
	}
	/**
	 * Retrieve the chosen dispatcher name.
	 * @return the dispatcher name.
	 */
	public String getDispatcherName(){
		if (dispatcherDef == null) {
			return null;
		}
		return dispatcherDef.getType();
	}
	/**
	 * Retrieve the dispatcher namespace.
	 * @return the dispatcher namespace.
	 */
	public String getDispatcherNamespace(){
		if (dispatcherDef == null) {
			return null;
		}
		if (dispatcherDef.getNamespace() == null) {
			return DEFAULT_NS;
		}
		return dispatcherDef.getNamespace();
	}
	

	public void setInPort(String name, String type){
		inports.put(name, new PortImpl(name, type, PortType.INPUT,  null));
	}
	
	public void setOutPort(String name, String type){
		outports.put(name, new PortImpl(name, type, PortType.OUTPUT,  null));
	}
	
	/**
	 *Initialize the mediator specification type. 
	 */
	public abstract MediatorSpecification initializeSpecification() ;
	/**
	 * Stop the mediator specification type.
	 */
	public abstract MediatorSpecification stopSpecification();
	
	
}
