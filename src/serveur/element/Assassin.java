package serveur.element;

import java.util.HashMap;

public class Assassin extends Personnage {
	
	private static final long serialVersionUID = 1L;
	
	public Assassin(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
