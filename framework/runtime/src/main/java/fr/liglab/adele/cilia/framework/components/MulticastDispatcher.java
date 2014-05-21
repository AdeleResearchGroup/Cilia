package fr.liglab.adele.cilia.framework.components;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.AbstractDispatcher;
import fr.liglab.adele.cilia.framework.IDispatcher;
import org.osgi.framework.BundleContext;

import java.util.List;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class MulticastDispatcher extends AbstractDispatcher implements IDispatcher {


    public MulticastDispatcher(BundleContext context) {
        super(context);
    }

    public void dispatch(Data data) throws CiliaException {
        List sendersNames = getSendersIds();
        int senderListSize = sendersNames.size();
        for (int j = 0; j < senderListSize; j++) {
            String senderName = (String) sendersNames.get(j);
            try {
                send(senderName, data);
            } catch (CiliaException ex) {
                log.error("send exception " + ex.getStackTrace().toString());
                throw new CiliaException(ex.getMessage());
            }
        }
    }


}
