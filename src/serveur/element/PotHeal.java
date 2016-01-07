package serveur.element;

import java.util.HashMap;

public class PotHeal extends Potion {

	private static final long serialVersionUID = 1L;
	
	public PotHeal(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
