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

package fr.liglab.adele.cilia.demo.components;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.util.Const;

/*
 * Add an offset
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NormalizerProcessor {
	private static final Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_APPLICATION);

	int m_scale;

	public Data process(Data data) {

		Map content = (Map) data.getContent();
		logger.debug("Data before being processed :{}", data.getContent()
				.toString());
		/* Retrive the name plain-text or XML */
		String name = data.getName();
		if (name != null) {
			/* treats only plain-text */
			if (name.compareToIgnoreCase("plain-text") == 0) {
				/* Multiply only if holding reagister */
				String type = (String) content.get("Modbus.type");
				if (type.compareToIgnoreCase("read.holding.registers") == 0) {
					Iterator it = content.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pairs = (Map.Entry) it.next();
						String key = (String) pairs.getKey();
						/* Skip the type and add m_scale to values */
						if (key.compareToIgnoreCase("Modbus.type") != 0) {
							Integer i = (Integer) pairs.getValue();
							i += m_scale;
							content.put(pairs.getKey(), i);
						}
					}
				}
			}
		}
		logger.debug("Data processed :{}", data.getContent().toString());
		return data;
	}

}
