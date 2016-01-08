package client;


import java.awt.Point;
import java.rmi.RemoteException;
import java.util.HashMap;

import client.controle.Console;
import logger.LoggerProjet;
import serveur.IArene;
import serveur.element.Caracteristique;
import serveur.element.Personnage;
import utilitaires.Calculs;
import utilitaires.Constantes;

/**
 * Strategie d'un personnage. 
 */
public class StrategiePersonnage {
	
	/**
	 * Console permettant d'ajouter une phrase et de recuperer le serveur 
	 * (l'arene).
	 */
	protected Console console;
	private int checked = 0;
	protected StrategiePersonnage(LoggerProjet logger){
		logger.info("Lanceur", "Creation de la console...");
	}

	/**
	 * Cree un personnage, la console associe et sa strategie.
	 * @param ipArene ip de communication avec l'arene
	 * @param port port de communication avec l'arene
	 * @param ipConsole ip de la console du personnage
	 * @param nom nom du personnage
	 * @param groupe groupe d'etudiants du personnage
	 * @param nbTours nombre de tours pour ce personnage (si negatif, illimite)
	 * @param position position initiale du personnage dans l'arene
	 * @param logger gestionnaire de log
	 */
	public StrategiePersonnage(String ipArene, int port, String ipConsole, 
			String nom, String groupe, HashMap<Caracteristique, Integer> caracts,
			int nbTours, Point position, LoggerProjet logger) {
		this(logger);
		
		try {
			console = new Console(ipArene, port, ipConsole, this, 
					new Personnage(nom, groupe, caracts), 
					nbTours, position, logger);
			logger.info("Lanceur", "Creation de la console reussie");
			
		} catch (Exception e) {
			logger.info("Personnage", "Erreur lors de la creation de la console : \n" + e.toString());
			e.printStackTrace();
		}
	}

	/** 
	 * Decrit la strategie.
	 * Les methodes pour evoluer dans le jeu doivent etre les methodes RMI
	 * de Arene et de ConsolePersonnage. 
	 * @param voisins element voisins de cet element (elements qu'il voit)
	 * @throws RemoteException
	 */
	public void executeStrategie(HashMap<Integer, Point> voisins) throws RemoteException {
		// arene
		IArene arene = console.getArene();
		// reference RMI de l'element courant
		int refRMI = 0;
		int offset = Calculs.getOffset();
		int milieu = (int)((Constantes.XMAX_ARENE - offset)/2);
		// position de l'element courant
		Point position = null;
		
		try {
			
			refRMI = console.getRefRMI();
			position = arene.getPosition(refRMI);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (voisins.isEmpty()) { 
			if((console.getPersonnage().getCaract(Caracteristique.VIE)<100)){
				arene.lanceAutoSoin(refRMI);
			}
			else{
				if (position.x != milieu || position.y != milieu) {
					console.setPhrase("Wolf is comming");
					arene.deplace(refRMI, new Point(milieu,milieu));
				} else {
					console.setPhrase("Je reste posey");
				}			
			}	
		} else {
			int refCible = Calculs.chercheElementProche(position, voisins);
			int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

			String elemPlusProche = arene.nomFromRef(refCible);

			if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si suffisamment proches
				// j'interagis directement
				if(arene.estPotionFromRef(refCible)){ // potion
					int effpoponear= arene.caractFromRef(refCible, Caracteristique.VIE)+arene.caractFromRef(refCible, Caracteristique.FORCE)+arene.caractFromRef(refCible, Caracteristique.DEFENSE);
					if((effpoponear>0)&&((console.getPersonnage().getCaract(Caracteristique.VIE)+arene.caractFromRef(refCible,Caracteristique.VIE))>20)){
						console.setPhrase("Je ramasse une potion sympathique");
						arene.ramassePotion(refRMI, refCible);		
					} else {
						console.setPhrase("Je me tire, ne me demande pas pourquoi ");
						arene.deplace(refRMI, 0);
					}
				} else if (arene.estPersonnageFromRef(refCible)){ // personnage
					// duel
					console.setPhrase("Je fais un duel avec " + elemPlusProche);
					arene.deplace(refRMI, refCible);
					arene.lanceAttaque(refRMI, refCible);
					if(arene.caractFromRef(refCible, Caracteristique.VIE)<=0){
						checked=0;
					}
				} else if(arene.estMonstreFromRef(refCible)){
					console.setPhrase("Je tape le monstre " + elemPlusProche);
					arene.deplace(refRMI, refCible);
					arene.lanceAttaque(refRMI, refCible);
				}
			} else { 
				
				if(arene.estPotionFromRef(refCible)){
					
					int effpopo= arene.caractFromRef(refCible, Caracteristique.VIE)+arene.caractFromRef(refCible, Caracteristique.FORCE)+arene.caractFromRef(refCible, Caracteristique.DEFENSE);
					if((effpopo>0)&&((console.getPersonnage().getCaract(Caracteristique.VIE)+arene.caractFromRef(refCible,Caracteristique.VIE))>20)){
						console.setPhrase("Je vais vers cette gouleyante potion " + elemPlusProche);
						arene.deplace(refRMI, refCible);	
					} else {
						console.setPhrase("Je ne vais pas vers cette gouleyante potion " + elemPlusProche);
						arene.deplace(refRMI,0);
					}
				} else if(arene.estMonstreFromRef(refCible)){	
					if (distPlusProche == 4){
						arene.lanceAutoSoin(refRMI);
					} else if (distPlusProche < 4){		
						console.setPhrase("Je vais vers mon voisin " + elemPlusProche);
						arene.deplace(refRMI, refCible);
						arene.lanceAttaque(refRMI, refCible);
					} else {
						console.setPhrase("Je vais vers mon voisin " + elemPlusProche);
						arene.deplace(refRMI, refCible);
				    }
				} else {
					if (checked==0){
						checked=1;
					}
					else{
						if (distPlusProche == 4){
							
							int choix= utilitaires.Calculs.nombreAleatoire(1, 10);
							switch(choix)
							{
							case 1: console.setPhrase("To be Or not to be ?");
							break;
							case 2: console.setPhrase("Allez PARIS !!!!");
							break;
							case 3: console.setPhrase("Mais elle est ou ma caisse :(");
							break;
							case 4: console.setPhrase("Isidore de Casse, enchanté");
							break;
							case 5: console.setPhrase("I'll kill you");
							break;
							case 6: console.setPhrase("Say my name !");
							break;
							case 7: console.setPhrase("I'll be back");
							break;
							case 8: console.setPhrase("TERMINAAAAAATE");
							break;
							case 9: console.setPhrase("Zumbaaaaa je danseeeeeee la zumba");
							break;
							case 10: console.setPhrase("Je suis une PATATE");
							break;
							}
							
							arene.lanceAutoSoin(refRMI);
						} else if (distPlusProche < 4){		
							console.setPhrase("Je vais vers mon voisin " + elemPlusProche);
							arene.deplace(refRMI, refCible);
							arene.lanceAttaque(refRMI, refCible);
						} else {
							console.setPhrase("Je vais vers mon voisin " + elemPlusProche);
							arene.deplace(refRMI, refCible);
					    }
					}
				}
			}
		}
	}
}

	

