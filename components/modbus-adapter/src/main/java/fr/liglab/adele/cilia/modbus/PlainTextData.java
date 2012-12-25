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

package fr.liglab.adele.cilia.modbus;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.util.Const;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class PlainTextData implements DataFormater {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

	public PlainTextData() {
	}

	public Data data(String key, int ref, Integer[] values) {
		Data data;
		Map response = new HashMap();
		int offset = ref;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				response.put(Integer.toString(offset++), values[i]);
			}
		}
		data = new Data(response, "plain-text");
		logger.debug(key + " :" + data.toString());
		return data;
	}

	public Data data(String key, int ref, BitSet values) {
		Data data;
		Map response = new HashMap();
		int offset = ref;
		if (values != null) {
			for (int i = 0; i < values.length(); i++) {
				response.put(Integer.toString(offset++), Boolean.toString(values.get(i)));
			}
		}
		data = new Data(response, "plain-text");
		logger.debug(key + " :" + data.toString());
		return data;
	}

}
