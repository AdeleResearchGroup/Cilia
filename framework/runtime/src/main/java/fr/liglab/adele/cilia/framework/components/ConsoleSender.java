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


package fr.liglab.adele.cilia.framework.components;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ISender;


public class ConsoleSender implements ISender {

	boolean detail;
	
	String header ;
	
	private static Logger log =LoggerFactory.getLogger("cilia.ipojo.compendium");
	public ConsoleSender(BundleContext context) {
	}
	
	public boolean send(Data data) {
		if (data != null) {
			if (detail == true) {
				System.out.println(header + "\n" + data.toString());
			} else {
                System.out.println(header + "\n" + String.valueOf(data.getContent()));
			}
		} else  {
			log.warn(header + "\n" + "[INFO] ConsoleSender : data=null");
		}
		return false;
	}
}
