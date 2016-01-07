package serveur.element;

import java.util.HashMap;

public class Ivrogne extends Personnage {
	
	private static final long serialVersionUID = 1L;

	public Ivrogne(String nom, String groupe, HashMap<Caracteristique, Integer> carac) {
		super(nom, groupe, carac);
	}
}
