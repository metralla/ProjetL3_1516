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

	// TODO etablir une strategie afin d'evoluer dans l'arene de combat
	// une proposition de strategie (simple) est donnee ci-dessous
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
		HashMap<Caracteristique,Integer> cv =new HashMap<Caracteristique, Integer>();
		int checked=0;
		
		// reference RMI de l'element courant
		int refRMI = 0;
		int initmstr=0;
		
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
			console.setPhrase("J'erre...");
			arene.deplace(refRMI, 0); 
			}
			
		} else {
			int refCible = Calculs.chercheElementProche(position, voisins);
			int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

			String elemPlusProche = arene.nomFromRef(refCible);

			if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si suffisamment proches
				// j'interagis directement
				if(arene.estPotionFromRef(refCible)){ // potion
					int effpoponear= arene.caractFromRef(refCible, Caracteristique.VIE)+arene.caractFromRef(refCible, Caracteristique.FORCE)+arene.caractFromRef(refCible, Caracteristique.INITIATIVE)+arene.caractFromRef(refCible, Caracteristique.DEFENSE);
					if((effpoponear>0)&&((console.getPersonnage().getCaract(Caracteristique.VIE)-arene.caractFromRef(refCible,Caracteristique.VIE))>30)){
					console.setPhrase("Je ramasse une potion sympathique");
					arene.ramassePotion(refRMI, refCible);		
					}
							
				} else if (arene.estPersonnageFromRef(refCible)){ // personnage
					// duel
					console.setPhrase("Je fais un duel avec " + elemPlusProche);
					if(arene.estMonstreFromRef(refRMI)){
						initmstr=0;
					}
					arene.lanceAttaque(refRMI, refCible);
					arene.deplace(refRMI, refCible);
					if(arene.caractFromRef(refCible, Caracteristique.VIE)<=0){
						checked=0;
					}
					else{ //Potion negative qui est apparue directement en face -> d�placement al�atoire
						arene.lanceAttaque(refRMI, refCible);
						arene.deplace(refRMI, 0);
					}
					
				}
				
			} else { 
				
				if(arene.estPotionFromRef(refCible)){
					
					int effpopo= arene.caractFromRef(refCible, Caracteristique.VIE)+arene.caractFromRef(refCible, Caracteristique.FORCE)+arene.caractFromRef(refCible, Caracteristique.INITIATIVE)+arene.caractFromRef(refCible, Caracteristique.DEFENSE);
					if((effpopo>0)&&((console.getPersonnage().getCaract(Caracteristique.VIE)-arene.caractFromRef(refCible,Caracteristique.VIE))>30)){
						console.setPhrase("Je vais vers cette gouleyante potion " + elemPlusProche);
						arene.deplace(refRMI, refCible);	
					}
				}
				if(arene.estMonstreFromRef(refCible)){
					if (initmstr==0){
						cv=arene.lanceClairvoyance(refRMI, refCible);
						initmstr= cv.get(Caracteristique.INITIATIVE);
					}
					else if (initmstr<console.getPersonnage().getCaract(Caracteristique.INITIATIVE)){
						
						if (distPlusProche==4){
							arene.lanceAttaque(refRMI, refCible);
						}
						else { arene.deplace(refRMI, refCible);
						arene.lanceAttaque(refRMI, refCible);
						}
					}
					else{
						arene.deplace(refRMI,0);
						arene.lanceAttaque(refRMI, 0);
					}
					
				}
				else{
					if (checked==0){
						cv=arene.lanceClairvoyance(refRMI, refCible);
						checked=1;
					}
					else{
						if (distPlusProche==4){
							arene.lanceAttaque(refRMI, refCible);
						}
						else{		
						if((console.getPersonnage().getCaract(Caracteristique.INITIATIVE)> cv.get(Caracteristique.INITIATIVE))&&(cv.get(Caracteristique.VIE)-console.getPersonnage().getCaract(Caracteristique.FORCE)*cv.get(Caracteristique.DEFENSE)/100<=0)){
						arene.deplace(refRMI, refCible);
						arene.lanceAttaque(refRMI, refCible);
						}
						
					    else{
						checked=0;
						arene.deplace(refRMI, 0);
						arene.lanceAttaque(refRMI, 0);	
					    }
					}
					
				}
				// si voisins, mais plus eloignes
				// je vais vers le plus proche
				console.setPhrase("Je vais vers mon voisin " + elemPlusProche);
				arene.deplace(refRMI, refCible);
				arene.lanceAttaque(refRMI, refCible);
			}
		}
	}	}

	
}
