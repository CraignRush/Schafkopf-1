package regeln;

import lib.Model;

public interface Control {
	
	/**
	 * Bestimmt den Sieger eines Spiels
	 * @param model
	 * @return SpielerID des Siegers
	 */
	public int sieger(Model m, int erster);
	
	/**
	 * Bestimmt, ob ein Spielzug erlaubt ist und gibt das Ergebnis zurück
	 * @param model
	 * @param ID
	 * @return erlaubt
	 */
	public boolean erlaubt(Model m, int ID);
	
	/**
	 * Bestimmt einen eventuellen Mitspieler
	 * @param model
	 * @return mitspieler oder null
	 */
	public int mitspieler(Model m);

}
