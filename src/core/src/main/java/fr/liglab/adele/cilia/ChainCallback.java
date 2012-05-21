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
	void onArrival(String chainId);

	/**
	 * Callback upon chain departure
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onDeparture(String chainId);

	/**
	 * Chain started
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onStarted(String chainId);

	/**
	 * Chain stopped
	 * 
	 * @param chaindId
	 *            chain identificator
	 */
	void onStopped(String chainId);

}
