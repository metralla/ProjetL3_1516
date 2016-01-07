package serveur.element;

import java.util.HashMap;

public class BonusIni extends Potion {
	
	private static final long serialVersionUID = 1L;
	
	public BonusIni(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
