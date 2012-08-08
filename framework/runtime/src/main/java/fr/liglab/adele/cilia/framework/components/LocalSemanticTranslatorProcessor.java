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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class LocalSemanticTranslatorProcessor {
	/**
	 * The dictionary containing words to be changed.
	 */
	private Map<String, String> dictionary;
	
	private static final Logger logger = LoggerFactory.getLogger("cilia.components");
	/**
	 * Will translate words on the content of a data.
	 * @param dataSet The data to be modified.
	 * @return The same data with the modified content.
	 */
	public Data process(Data dataSet) {
		String translatedContent;
		if (dictionary == null) { // If any configuration has been given, it returns the same content.
			logger.warn("Translator will return the same received Data. Dictionary is null");
			return dataSet;
		}
		translatedContent=(String) dataSet.getContent();
		Iterator<Entry<String, String>> it = dictionary.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> pairs = it.next();
			translatedContent = translatedContent.replaceAll(pairs.getKey(),pairs.getValue());
		}
		dataSet.setContent(translatedContent);
		
		return dataSet;
	}
}
