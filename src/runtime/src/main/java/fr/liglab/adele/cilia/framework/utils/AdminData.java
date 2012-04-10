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

package fr.liglab.adele.cilia.framework.utils;

import java.util.Map;

public interface AdminData {

	/**
	 * retreive data buffer map related to the mediator.
	 * 
	 * @param mediatorId mediator reference id 
	 * @param isRegular
	 *            true selects regular message flow, false selects messages
	 *            bufferized
	 * @return Map containing buffer Data
	 */
	public Map getData(String mediatorId, boolean isRegular);

	/**
	 * clear all messages (regular and bufferized messages)
	 * 
	 * @param mediatorId
	 *            mediator unique id
	 */
	public void clearData(String mediatorId);

	/**
	 * copy messages ( regular and stored)
	 * @param mediatorfrom_Id
	 * @param mediatorTo_Id 
	 */
	public void copyData(String mediatorFrom_Id, String mediatorTo_Id) ;
	
	public void start();

	public void stop();

}
