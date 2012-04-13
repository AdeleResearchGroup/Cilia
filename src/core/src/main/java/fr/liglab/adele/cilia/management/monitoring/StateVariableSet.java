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

package fr.liglab.adele.cilia.management.monitoring;

import fr.liglab.adele.cilia.event.ChangeStateListener;

/**
 * Runtime value updated by ComponentImpl ( mediator / adapter) <br>
 * ChangeStateListener :callcack on events , see list of events see ComponentEvent <br>
 * event fired is a Map ( key = state variable id, value = events ->see ComponentEvent.java) <br>
 * 
 * @author denismorand
 *
 */
public interface StateVariableSet extends ChangeStateListener {

	/* Default measures stored */
	static final int DEFAULT_WINDOW = 10;

	/**
	 * Return the windows size for a given state var
	 * 
	 * @param stateVarId
	 * @return window size or -1 if error
	 */
	int getWindow(String stateVarId);

	/**
	 * set window size for a given state var
	 * 
	 * @param stateVarId
	 * @param window
	 * @return true if success , false otherwhise
	 */
	boolean setWindow(String stateVarId, int window);

	/**
	 * The ComponentImpl is now publishing data
	 * 
	 * @param stateVarId
	 */
	void enable(String stateVarId);

	/**
	 * The mediator stop publish data
	 * 
	 * @param stateVarId
	 */
	void disable(String stateVarId);

	/** 
	 * 
	 * @return list of state variable Id enabled of null if none
	 */
	String[] getEnabledId() ;

	/**
	 * Return the status of s state var 
	 * 
	 * @param stateVarId
	 * @return true if the StateVar is enable, false otherwhise.
	 */
	boolean isEnabled(String stateVarId);

	/**
	 * 
	 * @return array of state variable id discovered
	 */
	String[] getStateVariableId();

	/**
	 * Condition
	 * 
	 * @param stateVarId
	 *            , state var id name
	 * @param ldapFilter
	 *            , condition ( LDAP filter) or null ( no condition)
	 * @return true if no error
	 */
	boolean setCondition(String stateVarId, String ldapFilter);

	/**
	 * retreive the condition associated to the state variable
	 * 
	 * @param stateVarId
	 * @return String ( or "" )
	 */
	String getCondition(String stateVarId);

	/**
	 * Retreives all measures
	 * 
	 * @param stateVarId
	 * @return array of measures ( maximum = getWindow() ) or null if no measures
	 */
	Object[] getMeasures(String stateVarId);
	
	/** 
	 * Retrieve the latest measures 
	 * 
	 */
	Object getMeasure(String stateVarId) ;

	/**
	 * clear all measures stored
	 * 
	 * @param stateVarId
	 */
	void clearMeasures(String stateVarId);
	
	/* 
	 * Verylow/veryhigh could be configured only id low/high threshold is defined  
	 * Double.Na is a equivalent to null 
	 * 
	 * example one threshold low :
	 * 
	 * setLowThreshold("my-statevar",23,Double.Na) 
	 * 2 thresholds low 
	 * setLowThreshold(""my-second-statevar",10,5) 
	 */
	void setLowThreshold(String stateVarId,double low,double veryLow) ;
	void setHighThreshold(String stateVarId,double high, double veryHigh) ;
	
	double getLowThreshold(String stateVarId) ;
	double getVeryLowThreshold(String stateVarId) ;	
	double getHighThreshold(String stateVarId) ;
	double getVeryHighThreshold(String stateVarId) ;
	
}