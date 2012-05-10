package fr.liglab.adele.cilia;

/**
 * callback events level chain
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ChainCallback {

	/**
	 * Callback upon new chain arrival
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void arrival(String chainId);

	/**
	 * Callback upon chain departure
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void departure(String chainId);

	/**
	 * Chain started
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void started(String chainId);

	/**
	 * Chain stopped
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void stopped(String chainId);

}
