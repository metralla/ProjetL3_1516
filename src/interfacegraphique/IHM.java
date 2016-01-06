package interfacegraphique;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import interfacegraphique.interfacesimple.AreneJPanel;
import interfacegraphique.interfacesimple.ElementsJPanel;
import interfacegraphique.interfacesimple.FenetreClassement;
import interfacegraphique.interfacesimple.FenetreDetail;
import interfacegraphique.interfacesimple.components.VictoryScreen;
import logger.LoggerProjet;
import serveur.IAreneIHM;
import serveur.vuelement.VueElement;
import serveur.vuelement.VuePersonnage;
import serveur.vuelement.VuePotion;
import utilitaires.Calculs;
import utilitaires.Constantes;

/**
 * Interface graphique.
 */
public class IHM extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	/**
	 * Adresse IP du serveur. 
	 */
	protected String ipArene;

	/**
	 * Port de communication avec l'arene. 
	 */
	protected int port;
	
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
	 * Initialise l'IHM.
	 * @param port port de communication avec l'arene
	 * @param ipArene IP de communication avec l'arene
	 * @param logger gestionnaire de log
	 */
	public IHM(int port, String ipArene, LoggerProjet logger) {
		this.logger = logger;
		this.port = port;
		this.ipArene = ipArene;
		initComposants();
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
						"Projet de programmation L3 - Universite Paul Sabatier\n" + 
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


		affichageMenu.add(affichageJauge);
		// affichageMenu.add(controleAction);
		menuBar.add(affichageMenu);
		setJMenuBar(menuBar);
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
				arenePanel.setVues(personnages, potions, arene.getOffset());

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
