package serveur.element;

import java.util.HashMap;

public class BonusForce extends Potion {
	
	private static final long serialVersionUID = 1L;

	public BonusForce(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
