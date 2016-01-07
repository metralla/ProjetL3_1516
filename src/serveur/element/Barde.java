package serveur.element;

import java.util.HashMap;

public class Barde extends Personnage {
	
	private static final long serialVersionUID = 1L;

	public Barde(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
