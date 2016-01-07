package serveur.element;

import java.util.HashMap;

public class EtoileDeBowser extends Potion {
	
	private static final long serialVersionUID = 1L;

	public EtoileDeBowser(String nom, String groupe, HashMap<Caracteristique, Integer> caracts) {
		super(nom, groupe, caracts);
	}
}
