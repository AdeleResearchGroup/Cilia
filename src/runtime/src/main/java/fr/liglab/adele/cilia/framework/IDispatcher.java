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

package fr.liglab.adele.cilia.framework;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;


/**
 * Dispatcher interface.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public interface IDispatcher {
	
	void dispatch(Data dataSet) throws CiliaException;
	//ISender getSender(String name);
	//void addSender(String senderName, String senderId, Dictionary props);
	List getSendersIds();
	//void removeSender(String portname, String senderName);
	void send(String senderName, Data data ) throws CiliaException;
	void send(String senderName, Properties properties,  Data data ) throws CiliaException;
	public void fireEvent(Map map);
	//String getName();
}
