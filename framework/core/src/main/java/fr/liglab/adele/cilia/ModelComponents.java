package fr.liglab.adele.cilia;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;

import java.util.Dictionary;

/**
 * Gives the Component model
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
@SuppressWarnings("rawtypes")
public interface ModelComponents {
    /**
     * Return all properties for a node
     *
     * @param node
     * @return unmodifiable Dictionary
     * @throws CiliaIllegalStateException
     * @throws CiliaIllegalParameterException
     */
    Dictionary getProperties(Node node) throws CiliaIllegalStateException,
            CiliaIllegalParameterException;

    /**
     * Return a specified property
     *
     * @param node
     * @param key
     * @return null if the kety is not a node propertie
     * @throws CiliaIllegalStateException
     * @throws CiliaIllegalParameterException
     */
    Object getProperty(Node node, String key) throws CiliaIllegalStateException,
            CiliaIllegalParameterException;

    /**
     * @param node
     * @return Mediator component model
     * @throws CiliaIllegalParameterException , wrong parameter
     * @throws CiliaIllegalStateException     , the node object doesn't
     */
    MediatorComponent getModel(Node node) throws CiliaIllegalParameterException,
            CiliaIllegalStateException;

    /**
     * @param chainId
     * @return Chain model or null if the chainIs doesn't exist
     * @throws CiliaIllegalParameterException
     */
    Chain getChain(String chainId) throws CiliaIllegalParameterException;

}
