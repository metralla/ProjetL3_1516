package serveur.element;

import java.util.HashMap;

public class MalusIni extends Potion {
	
	private static final long serialVersionUID = 1L;

	public MalusIni(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
