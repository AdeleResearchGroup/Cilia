package fr.liglab.adele.cilia.framework.components;

import java.util.ArrayList;
import java.util.List;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.AbstractSplitter;
import fr.liglab.adele.cilia.framework.data.DataEnrichment;


public class SplitterProcessor extends AbstractSplitter {

    private String expression; 


    public List process(List data) {
        return super.process(data);
    }

    @Override
    public List split(Data dataToSplit) throws CiliaException {
        List dataList = new ArrayList();
        String content;
        try {
            content = String.valueOf(dataToSplit.getContent());
        } catch (Exception ex) {
            throw new CiliaException(ex.getMessage());
        }
        String[] contents = content.split(expression);
        for (int i = 0; contents != null && i < contents.length ; i++) {
            Data ndata = (Data)dataToSplit.clone();
            ndata.setContent(contents[i]);
            ndata = DataEnrichment.addCorrelationInfo(ndata, contents.length, i, String.valueOf(dataToSplit.hashCode()));
            dataList.add(ndata);
        }
        return dataList;
    }

}
