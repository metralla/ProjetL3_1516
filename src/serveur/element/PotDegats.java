package serveur.element;

import java.util.HashMap;

public class PotDegats extends Potion {

	private static final long serialVersionUID = 1L;
	
	public PotDegats(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
