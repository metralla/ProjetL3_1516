package serveur.element;

import java.util.HashMap;

public class EtoileDeMario extends Potion {
	
	private static final long serialVersionUID = 1L;

	public EtoileDeMario(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
