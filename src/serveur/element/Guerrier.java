package serveur.element;

import java.util.HashMap;

public class Guerrier extends Personnage {

	private static final long serialVersionUID = 1L;
	
	public Guerrier(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
