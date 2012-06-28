package fr.liglab.adele.cilia;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;

/**
 * Gives the Component model 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ModelComponents {
	/**
	 * 
	 * @param node
	 * @return Mediator component model
	 * @throws CiliaIllegalParameterException
	 *             , wrong parameter
	 * @throws CiliaIllegalStateException
	 *             , the node object doesn't
	 */
	MediatorComponent getModel(Node node) throws CiliaIllegalParameterException,
			CiliaIllegalStateException;

	/**
	 * 
	 * @param chainId
	 * @return Chain model or null if the chainIs doesn't exist
	 * @throws CiliaIllegalParameterException
	 */
	Chain getChain(String chainId) throws CiliaIllegalParameterException;

}
