package serveur.element;

import java.util.HashMap;

public class Voleur extends Personnage {
	
	private static final long serialVersionUID = 1L;

	public Voleur(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
