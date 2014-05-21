package fr.liglab.adele.cilia.runtime;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.util.Const;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import java.util.*;

public class ConstRuntime extends Const {

    /* ---- Runtime ---- */
    /*
     * Unique ID identifier
	 */
    public static final String UUID = "uuid";
    /*
     * chain
     */
    public static final String CHAIN_ID = "chain";
    /*
     * Cilia components (adapters, mediators)
     */
    public static final String NODE_ID = "node";

    /*
     * Node creation timeStamp
     */
    public static final String TIMESTAMP = "timestamp";
    /*
     * Variable
     */
    public static final String VARIABLE_ID = "variable";

    /*
     * Value published
     */
    public static final String VALUE = "value";
    /*
	 * logger name
	 */

    /* Properties providing the base level monitoring configuration */
    public static final String MONITORING_CONFIGURATION = "monitoring.base.level.config";

    /* Topic Event Admin between base level and monitoring model */
    public static final String TOPIC_HEADER = "cilia/runtime/statevariable/";

    public static final String EVENT_TYPE = "type";
    public static final int TYPE_DATA = 0;
    public static final int TYPE_STATUS_VARIABLE = 2;


    /**
     * concat 2 arrays
     *
     * @param first
     * @param second
     * @return first+second
     */
    public static final Node[] concat(Node[] first, Node[] second) {
        Node[] result = (Node[]) Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static final Set ldapKeys;

    static {
        Set set = new HashSet();
        set.add(UUID);
        set.add(CHAIN_ID);
        set.add(NODE_ID);
        set.add(VARIABLE_ID);
        ldapKeys = Collections.unmodifiableSet(set);
    }

    public synchronized static final Filter createFilter(String filter)
            throws CiliaIllegalParameterException, CiliaInvalidSyntaxException {
        if (filter == null)
            throw new CiliaIllegalParameterException("filter is null !");
        boolean found = false;
		/* at least one keyword is required */
        Iterator it = ldapKeys.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (filter.contains(key)) {
                found = true;
                break;
            }
        }
        if (found == false)
            throw new CiliaIllegalParameterException("missing ldap filter keyword "
                    + ldapKeys.toString() + "!" + filter);
        try {
            return FrameworkUtil.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            throw new CiliaInvalidSyntaxException(e.getMessage(), e.getFilter());
        }
    }

    public static final boolean isFilterMatching(Filter filter, Node node) {
        Dictionary dico = new Hashtable(4);

        dico.put(UUID, node.uuid());
        dico.put(CHAIN_ID, node.chainId());
        dico.put(NODE_ID, node.nodeId());
        dico.put(TIMESTAMP, new Long(node.timeStamp()));
        return filter.match(dico);

    }

    public static final boolean isFilterMatching(Filter filter, Node node, String variable) {
        Dictionary dico = new Hashtable(5);

        dico.put(UUID, node.uuid());
        dico.put(CHAIN_ID, node.chainId());
        dico.put(NODE_ID, node.nodeId());
        dico.put(TIMESTAMP, new Long(node.timeStamp()));
        dico.put(VARIABLE_ID, variable);
        return filter.match(dico);

    }

}
