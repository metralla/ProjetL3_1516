package interfacegraphique.interfacesimple;

import interfacegraphique.IHM;
import interfacegraphique.interfacetournoi.components.SaisiePosition;
import interfacegraphique.interfacetournoi.exceptionSaisie.PositionNonValideException;
import utilitaires.Constantes;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JPanel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;

/**
 * 
 * @author Barber Valerian
 * 
 */
public class FenetreNouvellePotion extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Champ de saisi Position
	 */
	private SaisiePosition champPosition;
	/**
	 * JList permettant de choisir un type.
	 */
	private JList<String> listeType;
	/**
	 * JLabel Etiquette d'information sur le type d'une potion.
	 */
	private JLabel labelInformationType = new JLabel("");
	/**
	 * Bouton pour le lancement du personnage.
	 */
	private JButton boutonLancementPotion;
	/**
	 * IHM
	 */
	private IHM ihm;
	
	public FenetreNouvellePotion(IHM ihm) 
	{
		this.ihm = ihm;
		// Titre Fenetre
		this.setTitle("Création Potion");
		// Option fermeture
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// Taille de base 
		this.setSize(500, 300);
	    // Fenetre toujours au premier plan
	    this.setAlwaysOnTop(true);
	    // Taille non modifiable
	    this.setResizable(false);
		// gridbaglayout
		getContentPane().setLayout(new GridBagLayout());
		//-----------------------Type---------------------------------	
		JPanel panelType = new JPanel();
		panelType.setLayout(new GridBagLayout());
		JLabel labelType = new JLabel("Type de Potion");
		labelType.setHorizontalAlignment(JLabel.CENTER);
		
		String[] ar = {"BonusForce","BonusIni","EtoileDeBowser",
				"EtoileDeMario","MalusForce","MalusIni",
				"PotDegats","PotHeal","Potion"};
		
		listeType = new JList<String>(ar);
		listeType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listeType.setLayoutOrientation(JList.VERTICAL);
		listeType.setVisibleRowCount(-1);
		listeType.setSelectedIndex(0);
		
		DefaultListCellRenderer centrerListe = new DefaultListCellRenderer();
		centrerListe.setHorizontalAlignment(JLabel.CENTER);
		listeType.setCellRenderer(centrerListe);
		
		labelInformationType.setText("+10 force");
		labelInformationType.setHorizontalAlignment(JLabel.CENTER);
		labelInformationType.setFont(new Font(labelInformationType.getFont().getName(),Font.ITALIC, labelInformationType.getFont().getSize()));
		
		listeType.addListSelectionListener
		(
				new ListSelectionListener() 
				{
				public void valueChanged(ListSelectionEvent e) 
					{
						if (e.getValueIsAdjusting() == false) 
						{							
							switch(listeType.getSelectedValue())
							{
								case "BonusForce":
									labelInformationType.setText("+10 force");
									break;
								case "BonusIni":
									labelInformationType.setText("+10 initiative");
								break;
								case "EtoileDeBowser":
									labelInformationType.setText("-20 force,-20 vie,-20 initiative");
									break;
								case "EtoileDeMario":
									labelInformationType.setText("+20 force,+20 vie,+20 initiative");
									break;
								case "MalusForce":
									labelInformationType.setText("-10 force");
									break;
								case "MalusIni":
									labelInformationType.setText("-10 initiative");
									break;
								case "PotDegats":
									labelInformationType.setText("-10 Vie");
									break;
								case "PotHeal":
									labelInformationType.setText("+10 Vie");
									break;
								case "Potion":
									labelInformationType.setText("Une Potion aléatoire :)");
									break;
							}
						}
					}
				}
		);
		
		GridBagConstraints gbc_label_type = new GridBagConstraints();
		gbc_label_type.gridy = 0;
		gbc_label_type.gridwidth = 1;
		GridBagConstraints gbc_liste_type = new GridBagConstraints();
		gbc_liste_type.gridy = 1;
		gbc_liste_type.gridwidth = 1;
		GridBagConstraints gbc_label_info = new GridBagConstraints();
		gbc_label_info.gridy = 2;
		gbc_label_info.gridwidth = 1;
		
		panelType.add(labelType,gbc_label_type);
		panelType.add(listeType,gbc_liste_type);
		panelType.add(labelInformationType,gbc_label_info);				
		//-----------------------Position------------------------------
		JPanel panelPosition = new JPanel();
		panelPosition.setLayout(new GridBagLayout());
		
		champPosition = new SaisiePosition();
		
		panelPosition.add(champPosition);
		//-----------------------Bouton Envoi--------------------------
		JPanel panelBouton = new JPanel();
		panelBouton.setLayout(new GridBagLayout());
		
		boutonLancementPotion = new JButton();
		boutonLancementPotion.setText("Ajouter Potion");
		boutonLancementPotion.addActionListener
		(
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent event) 
				{
					lancePersonnage();
				}
			}
		);
		panelBouton.add(boutonLancementPotion);
		//---------------Positionnement des panels----------------------------
		GridBagConstraints gbc_type = new GridBagConstraints();
		gbc_type.gridy = 0;
		
		GridBagConstraints gbc_position = new GridBagConstraints();
		gbc_position.gridy = 1;
		
		GridBagConstraints gbc_bouton = new GridBagConstraints();
		gbc_bouton.gridy = 2;
		//-------------------------------------------------------------
		getContentPane().add(panelType,gbc_type);
		getContentPane().add(panelPosition,gbc_position);
		getContentPane().add(panelBouton,gbc_bouton);
	}
	
	public void lancePersonnage() 
	{
		Point position = null;
		boolean formulaireValide = true;
		List<String> erreurMessage = new ArrayList<String>();
		
		try 
		{
			position = champPosition.getPosition();
		} 
		catch (PositionNonValideException e) 
		{
			formulaireValide= false;
			erreurMessage.add("La position saisie est invalide. (" + 
					Constantes.XMIN_ARENE + "-" + Constantes.XMAX_ARENE + "),(" +
					Constantes.YMIN_ARENE + "-" + Constantes.YMAX_ARENE+ ")");
		}

		if (formulaireValide) 
		{
			ihm.lancePotion(listeType.getSelectedValue(),position);
		} 
		else 
		{
			String s = "<html><body><div width='300px' align='center'>";
			for (String msg : erreurMessage) 
			{
				s += "<p>" + msg + "</p><br>";
			}
			JOptionPane.showMessageDialog(this, s, "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
}