package serveur.element;

import java.util.HashMap;

public class Paladin extends Personnage {
	
	private static final long serialVersionUID = 1L;

	public Paladin(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
