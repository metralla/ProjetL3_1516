package serveur.element;

import java.util.HashMap;

public class MageTemps extends Personnage {
	
	private static final long serialVersionUID = 1L;

	public MageTemps(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
