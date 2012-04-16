package fr.liglab.adele.cilia.knowledge.specification;

/**
 * callback  events level chain 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public interface ChainCallback {

	/**
	 * Callback upon new chain arrival
	 * 
	 * @param node
	 */
	void arrival(String chainId);

	/**
	 * Callback upon chain departure
	 * 
	 * @param node
	 */
	void departure(String chainId);
	
	/**
	 * Chain started
	 * @param chainId
	 */
	void started(String chainId) ;
	
	/**
	 * Chain stopped
	 * @param chainId
	 */
	void stopped(String chainId) ;
	
}
