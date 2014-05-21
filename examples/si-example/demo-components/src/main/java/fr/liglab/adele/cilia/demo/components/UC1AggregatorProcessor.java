package fr.liglab.adele.cilia.demo.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.IProcessor;
import fr.liglab.adele.cilia.framework.data.XmlTools;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;

public class UC1AggregatorProcessor implements IProcessor {

    private static String resultEmptyContent = "<suiviconso-reponse xmlns=\"http://www.example.org/SuiviConso/\">\n"
            + "    <fixe></fixe>\n"
            + "    <mobile></mobile>\n"
            + "    <internet></internet>\n"
            + "    <total></total>\n"
            + "</suiviconso-reponse>\n";

    public List process(List dataSet) {
        System.out.println("To Aggregate");
        String fixe = "0", mobile = "0", internet = "0";
        int total = 0;
        Document document = null; // document xml a decouper
        /**
         * Extraction des info des sous messages
         */
        for (int i = 0; i < dataSet.size(); i++) {
            document = getDocumentFromData((Data) dataSet.get(i));
            if (isDocumentFromMobile(document)) {
                mobile = getConsommation(document);
            } else if (isDocumentFromInternet(document)) {
                internet = getConsommation(document);
            } else if (isDocumentFromFixe(document)) {
                fixe = getConsommation(document);
            }
        }
        /**
         * calcul du total
         */
        total = Integer.valueOf(mobile) + Integer.valueOf(fixe) + Integer.valueOf(internet);
        /**
         * composistion de la r�ponse
         */
        Data data = (Data) ((Data) dataSet.get(0)).clone();
        data.setContent(buildReponse(fixe, mobile, internet, total));
        List returnList = Collections.singletonList(data);
        return returnList;
    }

    private Document getDocumentFromData(Data data) {
        try {
            return (Document) XmlTools.stringToNode((String) (data.getContent()));
        } catch (CiliaException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isDocumentFromMobile(Document doc) {
        return isDocumentFrom("suiviconso-reponseMobile", doc);
    }

    private boolean isDocumentFromInternet(Document doc) {
        return isDocumentFrom("suiviconso-reponseInternet", doc);
    }

    private boolean isDocumentFromFixe(Document doc) {
        return isDocumentFrom("suiviconso-reponseFixe", doc);
    }

    private boolean isDocumentFrom(String tagName, Document doc) {
        return (doc != null && doc.getElementsByTagName(tagName).getLength() > 0);
    }

    private String getConsommation(Document doc) {
        return doc.getElementsByTagName("conso").item(0).getTextContent();
    }

    private String buildReponse(String fixe, String mobile, String internet, int total) {
        Document document = null;
        try {
            document = (Document) XmlTools.stringToNode(resultEmptyContent);
            document.getElementsByTagName("internet").item(0).setTextContent(internet);
            document.getElementsByTagName("mobile").item(0).setTextContent(mobile);
            document.getElementsByTagName("fixe").item(0).setTextContent(fixe);
            document.getElementsByTagName("total").item(0).setTextContent(String.valueOf(total));
            return XmlTools.nodeToString(document);
        } catch (CiliaException e) {
            return resultEmptyContent;
        }
    }

}
