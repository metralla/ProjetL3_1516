package serveur.element;

import java.util.HashMap;

public class Berserker extends Personnage {
	
	private static final long serialVersionUID = 1L;

	public Berserker(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
