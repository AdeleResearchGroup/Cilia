package fr.liglab.adele.cilia.framework.components;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.IProcessor;

public class LocalSemanticTranslatorProcessor implements IProcessor{

	private Map dictionary;

	public List process(List dataSet) {
		String translatedContent;
		Iterator it;
		/**
		 * pour toute les donn�es
		 */
		for(int i=0;i<dataSet.size();i++){
			translatedContent=(String) ((Data)dataSet.get(i)).getContent();
			/**
			 * Remplacement des occurences des entr�e du dictionaire;
			 */
			 it = dictionary.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        translatedContent.replaceAll((String)pairs.getKey(),(String)pairs.getValue());
		    }
		    ((Data)dataSet.get(i)).setContent(translatedContent);
		}
		
		return dataSet;
	}
}
