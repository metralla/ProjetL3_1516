package interfacegraphique;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import client.StrategieAssassin;
import client.StrategieBarde;
import client.StrategieBerserker;
import client.StrategieGuerrier;
import client.StrategieIvrogne;
import client.StrategieMageTemps;
import client.StrategiePaladin;
import client.StrategieVoleur;

import interfacegraphique.interfacesimple.AreneJPanel;
import interfacegraphique.interfacesimple.ElementsJPanel;
import interfacegraphique.interfacesimple.FenetreClassement;
import interfacegraphique.interfacesimple.FenetreDetail;
import interfacegraphique.interfacesimple.components.VictoryScreen;
import lanceur.ErreurLancement;
import logger.LoggerProjet;
import serveur.IArene;
import serveur.IAreneIHM;
import serveur.element.Caracteristique;
import serveur.element.Potion;
import serveur.vuelement.VueElement;
import serveur.vuelement.VuePersonnage;
import serveur.vuelement.VuePotion;
import utilitaires.Calculs;
import utilitaires.Constantes;

import interfacegraphique.interfacesimple.FenetreNouveauPersonnage;
import interfacegraphique.interfacesimple.FenetreNouvellePotion;

/**
 * Interface graphique.
 */
public class IHM extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	/**
	 * Adresse IP du serveur. 
	 */
	private String ipArene;

	/**
	 * Port de communication avec l'arene. 
	 */
	private int port;
	
	/**
	 * Etats de l'interface : initialisation ou en jeu.
	 * 
	 */
	private enum State {
		INIT, PLAYING
	};

	/**
	 * Etat de l'interface.
	 */
	private State state = State.INIT;
	
	/**
	 * Serveur.
	 */
	protected IAreneIHM arene;
	
	/**
	 * Thread de connexion au serveur.
	 */
	private Thread connexion = null;
	
	/**
	 * Vrai s'il y a eu une erreur de connexion.
	 */
	private boolean erreurConnexion = false;
	
	/**
	 * Gestionnaire de log.
	 */
	private LoggerProjet logger;
	
	/**
	 * VueElement correspondant a l'element actuellement selectionnee dans le 
	 * tableau.
	 */
	protected VueElement<?> elementSelectionne;
	
	/**
	 * JLabel affichant le timer. 
	 */
	private class TimerLabel extends JLabel {
		private static final long serialVersionUID = 1L;
	
		public TimerLabel() {
			super(" ");
			this.setHorizontalAlignment(JLabel.CENTER);
			this.setVerticalAlignment(JLabel.CENTER);
			this.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
			this.setForeground(grisFonce);
			this.setBackground(grisClair);
			this.setOpaque(true);
			this.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
					grisFonce));
			this.setPreferredSize(new Dimension(0, 50));
		}
	}

	/**
	 * Panel affichant l'arene.
	 */
	protected AreneJPanel arenePanel;
	
	/**
	 * Panel affichant les tableaux des elements participants a la partie :
	 * personnages et potions.
	 */
	private ElementsJPanel infosPanel;
	
	/**
	 * Panel affichant le timer et le panel de l'arene.
	 */
	protected JPanel gauchePanel;

	/**
	 * Label affichant le timer.
	 */
	private JLabel timerLabel;
	
	/**
	 * Couleurs predefinies.
	 */
	public static Color grisFonce = new Color(115, 115, 115);
	public static Color noir = new Color(33, 33, 33);
	public static Color grisClair = new Color(200, 200, 200);

	/**
	 * Fenetre Nouveau Personnage
	 */
	private FenetreNouveauPersonnage fenetrePersonnage;

	/**
	 * Fenetre Nouvelle potion.
	 */
	private FenetreNouvellePotion fenetrePotion;
	
	/**
	 * Initialise l'IHM.
	 * @param port port de communication avec l'arene
	 * @param ipArene IP de communication avec l'arene
	 * @param logger gestionnaire de log
	 */
	public IHM(int port, String ipArene, LoggerProjet logger) 
	{
		this.logger = logger;
		this.port = port;
		this.ipArene = ipArene;
		initComposants();
		
		/**
		 *  Ajout panneau cr�ation personnage et potion
		 * 
		 */
		fenetrePersonnage = new FenetreNouveauPersonnage(this);
		fenetrePotion = new FenetreNouvellePotion(this);

		// ajout d'un listener de clic sur l'arene permettant l'envoi de personnage et de potion dynamiquement
		arenePanel.addMouseListener
		(
			new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent e) 
				{
					if (fenetrePersonnage != null && fenetrePersonnage.isVisible()) 
					{
						// affichage 
					}
					else if (fenetrePotion != null && fenetrePotion.isVisible())
					{
						// 
					}
				}
			}
		);
	}

	/**
	 * Initialise les composants de l'IHM. 
	 */
	private void initComposants() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();

		int fenHeight = 2 * screenSize.height / 3;
		int fenWidth = 3 * screenSize.width / 4;

		// personnalise et positionne la fenetre par rapport a l'ecran
		setPreferredSize(new Dimension(fenWidth, fenHeight));
		setLocation(screenSize.width / 10, screenSize.height / 10);

		// cree un titre de la fenetre
		String titre = "Arene";
		setTitle(titre);

		// ajoute une operation si le bouton de fermeture de la fenetre est cliquee
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		initMenuBar();

		timerLabel = new TimerLabel();
		arenePanel = new AreneJPanel();

		gauchePanel = new JPanel(new BorderLayout());
		gauchePanel.add(timerLabel, BorderLayout.NORTH);
		gauchePanel.add(arenePanel, BorderLayout.CENTER);

		infosPanel = new ElementsJPanel(this);

		JSplitPane jSplitPane = new JSplitPane();
		int dividerLocation = fenWidth / 2;
		jSplitPane.setDividerLocation(dividerLocation);
		jSplitPane.setLeftComponent(gauchePanel);
		jSplitPane.setRightComponent(infosPanel);

		setVisible(true);

		getContentPane().add(jSplitPane);

		pack();
	}

	/**
	 * Initialise la JMenuBar. 
	 */
	private void initMenuBar() {
		// creation d'un menu "Fichier" avec deux options : "Quitter" et "A propos"
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Fichier");
		
		// ajout d'une action pour afficher la fenetre "A propos"
		Action aboutAction = new AbstractAction("A propos") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(null,
						"Projet de programmation L3 - Université Paul Sabatier\n" + 
						"Repris par Clement Chaumel, Valentin Chevalier, Christophe Claustre\n" +
						"lors du TER L3 de 2014/2015\n" +
						"Modifie pour le projet de programmation 2015/2016", "A propos",
						JOptionPane.PLAIN_MESSAGE);
			}
		};
		
		fileMenu.add(aboutAction);

		// ajout d'une action pour arreter l'execution de l'interface graphique
		Action exitAction = new AbstractAction("Quitter") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				System.exit(0);
			}
		};
		
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);

		JMenu affichageMenu = new JMenu("Affichage");

		JCheckBoxMenuItem affichageJauge = new JCheckBoxMenuItem("Jauge de vie");
		affichageJauge.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
				arenePanel.setAffichageJauge(cb.isSelected());
			}
		});
		/**
		 * Ajout Menu Personnage
		 */
		JMenu menuNouveauPersonnage = new JMenu("Personnage");
		menuNouveauPersonnage.getAccessibleContext().setAccessibleDescription(
				"Op�ration sur les personnages.");
		JMenuItem nouveauPersonnage = new JMenuItem("New");
		nouveauPersonnage.getAccessibleContext().setAccessibleDescription(
				"Permet d'ajouter un personnage.");
		nouveauPersonnage.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				affichageFenetrePersonnage();
			}			
		});
		/**
		 * Ajout Menu Potion
		 */
		JMenu menuNouveauPotion = new JMenu("Potion");
		menuNouveauPotion.getAccessibleContext().setAccessibleDescription(
				"Op�ration sur les potions.");
		JMenuItem nouvellePotion = new JMenuItem("New");
		nouvellePotion.getAccessibleContext().setAccessibleDescription(
				"Permet d'ajouter une potion.");
		nouvellePotion.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				affichageFenetrePotion();
			}			
		});
		
		affichageMenu.add(affichageJauge);
		menuBar.add(affichageMenu);
		menuNouveauPersonnage.add(nouveauPersonnage);
		menuBar.add(menuNouveauPersonnage);
		menuNouveauPotion.add(nouvellePotion);
		menuBar.add(menuNouveauPotion);
		setJMenuBar(menuBar);
	}

	
	/**
	 * Affiche la fenetre de creation de personnage.
	 */
	public void affichageFenetrePersonnage() 
	{
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int x = ((int) screenSize.getWidth() / 2 ) - (fenetrePersonnage.getWidth() / 2);
		int y = ((int) screenSize.getHeight() / 2 ) - (fenetrePersonnage.getHeight() / 2);
		Point point = new Point(x,y);
		fenetrePersonnage.setLocation(point);
		fenetrePersonnage.setVisible(true);	
	}
	/**
	 * Affiche la fenetre de creation de potion.
	 */
	public void affichageFenetrePotion() 
	{
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int x = ((int) screenSize.getWidth() / 2 ) - (fenetrePotion.getWidth() / 2);
		int y = ((int) screenSize.getHeight() / 2 ) - (fenetrePotion.getHeight() / 2);
		Point point = new Point(x,y);
		fenetrePotion.setLocation(point);
		fenetrePotion.setVisible(true);	
	}
	
	/**
	 * Methode de rafraichissement appelee a tous les tours de jeu.
	 */
	public void repaint() {
		// erreur ou en initialisation
		if ((state == State.INIT) || (erreurConnexion)) {
			// affiche le message correspondant
			if (!erreurConnexion) {
				arenePanel.afficheMessage("Connexion en cours sur le serveur Arene...");
			} else {
				arenePanel.afficheMessage("Erreur de connexion !");
			}
			
			// verifie si la connexion a ete realisee
			// un thread est "alive" si on est en cours de connexion
			if ((connexion != null) && (!connexion.isAlive())) {
				
				// met a jour l'etat de l'arene
				state = State.PLAYING;
				
				// remet la connexion a null pour une autre execution
				connexion = null;
			}
		} else {
			try {
				// met a jour la liste des elements de l'arene
				List<VuePersonnage> personnages = arene.getPersonnages();
				List<VuePersonnage> personnagesMorts = arene.getPersonnagesMorts();
				List<VuePotion> potions = arene.getPotions();

				infosPanel.setElements(personnages, personnagesMorts, potions);
				arenePanel.setVues(personnages, potions);

				// MAJ du timer
				int tempsRestant = arene.getNbToursRestants();
				int nbTour = arene.getTour();
				
				timerLabel.setText("Duree de la partie : "
						+ Calculs.timerToString(nbTour)
						+ "   -   Temps restant : "
						+ Calculs.timerToString(tempsRestant));

				if (!estPartieCommencee())
					arenePanel
							.afficheMessage("La partie n'a pas encore commence");

			} catch (RemoteException e) {
				erreurConnexion(e);
			}
		}

		super.repaint();
	}

	/**
	 * Teste si la partie a commence sur le serveur.
	 * @return vrai si la partie a commence
	 */
	private boolean estPartieCommencee() {
		boolean res = false;
		
		try {
			res = arene.estPartieCommencee();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	/**
	 * Traite une erreur de connexion.
	 * @param e exception ayant entraine l'erreur de connexion
	 */
	protected void erreurConnexion(Exception e) {
		// en cas de deconnexion ou erreur du serveur
		// remet l'etat de l'arene a jour
		state = State.INIT;
		String message = "Impossible de se connecter au serveur sur le port "
				+ port + " !\n(le serveur ne doit pas etre actif)";
		
		// affiche un dialog avec le message d'erreur
		JOptionPane.showMessageDialog(this,
				message + "\n\nRaison : " + e.getMessage(),
				"Erreur de connexion au serveur", JOptionPane.ERROR_MESSAGE);
		erreurConnexion = true;
		e.printStackTrace();
		logger.info("IHM", "Erreur de connexion : " + e.getMessage());
	}

	/**
	 * Renvoie la vue correspondant a l'element selectionne dans l'IHM.
	 * @return vue selectionnee
	 */
	public VueElement<?> getElementSelectionne() {
		return elementSelectionne;
	}

	/**
	 * Definit la vue correspondant a l'element selectionne dans l'IHM.
	 * @param vue vue a selectionner
	 */
	public void setElementSelectionne(VueElement<?> vue) {
		this.elementSelectionne = vue;
	}

	/**
	 * Affiche la fenetre de details de l'element selectionne. Positionne la
	 * fenetre au point donne.
	 * @param point point ou afficher la fenetre
	 */
	public void detailleSelectionne(Point point) {
		if (getElementSelectionne() != null) {
			FenetreDetail fenetre = new FenetreDetail(getElementSelectionne());
			
			if (point == null) {
				// Centrage de la fenetre
				Toolkit kit = Toolkit.getDefaultToolkit();
				Dimension screenSize = kit.getScreenSize();
				int x = ((int) screenSize.getWidth() / 2)
						- (fenetre.getWidth() / 2);
				int y = ((int) screenSize.getHeight() / 2)
						- (fenetre.getHeight() / 2);
				point = new Point(x, y);
			}
			
			fenetre.setLocation(point);
			fenetre.setVisible(true);
			fenetre.lanceChargementJauges();
			fenetre.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
	}

	/**
	 * Lance une connexion au serveur dans un thread separe.
	 */
	public void connecte() {
		connexion = new Thread() {
			public void run() {
				try {
					arene = (IAreneIHM) Naming.lookup(Constantes.nomRMI(ipArene, port, "Arene"));
				} catch (Exception e) {
					erreurConnexion(e);
				}
			}
		};
		
		connexion.start();
	}

	/**
	 * Recharge l'IHM toutes les 0,5 secondes.
	 */
	@Override
	public void run() {
		try {
			while (state == State.INIT || !arene.estPartieFinie()) {
				repaint();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (arene.estPartieFinie()) {
				finDePartie();
			}
		} catch (RemoteException e) {
			erreurConnexion(e);
		}
	}

	public void lancePersonnage(String type,Point position)
	{
		String groupe = "G10";
		String nom = type;
		
		// creation du logger
		LoggerProjet logger = null;
		try 
		{
			logger = new LoggerProjet(true, "personnage_" + nom + groupe);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(ErreurLancement.suivant);
		}
		// lancement du serveur
		try
		{
			String ipConsole = InetAddress.getLocalHost().getHostAddress();
			HashMap<Caracteristique, Integer> caracts = new HashMap<Caracteristique, Integer>();
			int nbTours = Constantes.NB_TOURS_PERSONNAGE_DEFAUT;
			logger.info("Lanceur", "Creation du personnage...");
			switch(type)
			{
				case "Assassin":
					caracts.put(Caracteristique.FORCE, 80);
					caracts.put(Caracteristique.VIE, 20);
					caracts.put(Caracteristique.INITIATIVE, 80);
					new StrategieAssassin(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
				case "Barde":
					caracts.put(Caracteristique.FORCE, 20);
					caracts.put(Caracteristique.VIE, 65);
					caracts.put(Caracteristique.INITIATIVE, 75);
					new StrategieBarde(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
					break;
				case "Berserker":
					caracts.put(Caracteristique.FORCE, 35);
					caracts.put(Caracteristique.VIE, 100);
					caracts.put(Caracteristique.INITIATIVE, 50);
					new StrategieBerserker(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
				case "Guerrier":
					caracts.put(Caracteristique.FORCE, 80);
					caracts.put(Caracteristique.VIE, 80);
					caracts.put(Caracteristique.INITIATIVE, 20);
					new StrategieGuerrier(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
				case "Ivrogne":
					int temp = (int)(Math.random() * 100.);
					caracts.put(Caracteristique.FORCE, temp);
					temp = (int)(Math.random() * 100.);
					caracts.put(Caracteristique.VIE, temp);
					temp = (int)(Math.random() * 100.);
					caracts.put(Caracteristique.INITIATIVE, temp);
					new StrategieIvrogne(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
				case "MageTemps":
					caracts.put(Caracteristique.FORCE, 40);
					caracts.put(Caracteristique.VIE, 30);
					caracts.put(Caracteristique.INITIATIVE, 70);
					new StrategieMageTemps(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
				case "Paladin":
					caracts.put(Caracteristique.FORCE, 50);
					caracts.put(Caracteristique.VIE, 50);
					new StrategiePaladin(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
				case "Voleur":
					caracts.put(Caracteristique.FORCE, 30);
					caracts.put(Caracteristique.VIE, 30);
					caracts.put(Caracteristique.INITIATIVE, 100);
					new StrategieVoleur(ipArene, port, ipConsole, nom, groupe, caracts, nbTours, position, logger);
				break;
			}
			logger.info("Lanceur", "Creation du personnage reussie");	
		}
	catch (Exception e)
	{
		logger.severe("Lanceur", "Erreur lancement :\n" + e.getCause());
		e.printStackTrace();
		System.exit(ErreurLancement.suivant);
		}
	}
	
	public void lancePotion(String type,Point position)
	{
		String groupe = "G10";
		String nom = type;
		
		// creation du logger
		LoggerProjet logger = null;
		try 
		{
			logger = new LoggerProjet(true, "potion_" + nom + groupe);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(ErreurLancement.suivant);
		}
	// lancement de la potion
		try 
		{
			IArene arene = (IArene) java.rmi.Naming.lookup(Constantes.nomRMI(ipArene, port, "Arene"));
			logger.info("Lanceur", "Lancement de la potion sur le serveur...");
			
			// caracteristiques de la potion
			HashMap<Caracteristique, Integer> caractsPotion = new HashMap<Caracteristique, Integer>();
			switch(type)
			{
			case "BonusForce":
				caractsPotion.put(Caracteristique.VIE,0);
				caractsPotion.put(Caracteristique.FORCE,10);
				caractsPotion.put(Caracteristique.INITIATIVE,0);
				break;
			case "BonusIni":
				caractsPotion.put(Caracteristique.VIE,0);
				caractsPotion.put(Caracteristique.FORCE,0);
				caractsPotion.put(Caracteristique.INITIATIVE,10);
				break;
			case "EtoileDeBowser":
				caractsPotion.put(Caracteristique.VIE,-20);
				caractsPotion.put(Caracteristique.FORCE,-20);
				caractsPotion.put(Caracteristique.INITIATIVE,-20);
				break;
			case "EtoileDeMario":
				caractsPotion.put(Caracteristique.VIE,20);
				caractsPotion.put(Caracteristique.FORCE,20);
				caractsPotion.put(Caracteristique.INITIATIVE,20);
				break;
			case "MalusForce":
				caractsPotion.put(Caracteristique.VIE,0);
				caractsPotion.put(Caracteristique.FORCE,-10);
				caractsPotion.put(Caracteristique.INITIATIVE,0);
				break;
			case "MalusIni":
				caractsPotion.put(Caracteristique.VIE,0);
				caractsPotion.put(Caracteristique.FORCE,0);
				caractsPotion.put(Caracteristique.INITIATIVE,-10);
				break;
			case "PotDegats":
				caractsPotion.put(Caracteristique.VIE,-10);
				caractsPotion.put(Caracteristique.FORCE,0);
				caractsPotion.put(Caracteristique.INITIATIVE,0);
				break;
			case "PotHeal":
				caractsPotion.put(Caracteristique.VIE,10);
				caractsPotion.put(Caracteristique.FORCE,0);
				caractsPotion.put(Caracteristique.INITIATIVE,0);
				break;
			case "Potion":
				caractsPotion.put(Caracteristique.VIE, Calculs.valeurCaracAleatoirePosNeg(Caracteristique.VIE));
				caractsPotion.put(Caracteristique.FORCE, Calculs.valeurCaracAleatoirePosNeg(Caracteristique.FORCE));
				caractsPotion.put(Caracteristique.INITIATIVE, Calculs.valeurCaracAleatoirePosNeg(Caracteristique.INITIATIVE));
				break;
			}
			
			// ajout de la potion
			arene.ajoutePotion(new Potion(nom, groupe, caractsPotion),position);
			logger.info("Lanceur", "Lancement de la potion reussi");
			
		} 
		catch (Exception e) 
		{
			logger.severe("Lanceur", "Erreur lancement :\n" + e.getCause());
			e.printStackTrace();
			System.exit(ErreurLancement.suivant);
		}
	}
	
	private void finDePartie() {
		try {
			List<VuePersonnage> classement = arene.getClassement();
			new FenetreClassement(classement);

			VuePersonnage gagnant = arene.getGagnant();
			
			if(gagnant != null) {
				this.setGlassPane(new VictoryScreen(arene.getGagnant()));
				((JPanel) this.getGlassPane()).setOpaque(false);
				((JPanel) this.getGlassPane()).setVisible(true);
			}
			
		} catch (RemoteException e) {
			erreurConnexion(e);
		}
	}
	
	/**
	 * Lance l'actualisation automatique de l'IHM
	 */
	public void start() {
		new Thread(this).start();
	}

}
