package fr.liglab.adele.cilia.framework.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.data.XsltTransformator;
import fr.liglab.adele.cilia.util.Const;

public class XsltTransformerProcessor {

    //String filepath = "http://localhost:8080/addAcountRes/xslt.xslt";
    String filepath ;
   
    Logger log = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);
    
    public Data process(Data data) {
        if (filepath == null) {
            log.error( "There is any xslt file set");
            return null;
        }
        Data newData = null;
        try {
            newData = XsltTransformator.dataTransformFromPathFile(data, filepath);
        } catch (CiliaException e2) {
           log.error( "Error when transforming data");
        }
        return newData;
    }
	
}
