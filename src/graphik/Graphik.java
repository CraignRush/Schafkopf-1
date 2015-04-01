package graphik;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import regeln.Control;
import regeln.Hochzeit;
import regeln.Regelwahl;
import lib.Karte;
import lib.Model;  
import lib.Model.modus;
import client.Client; 

import javax.swing.JPanel;

public class Graphik extends JFrame implements View{	
	
	private String logo = "graphik/karten/Logo.gif";
	private String hintergrundbild = "graphik/karten/hintergrund1.jpg";
	
	private Model model;
	
	//Enthält die Controll -> kontrolliert einen Spielzug
	private Control control;
	
	private int ID;
	private String[] namen;
	
	private boolean keinKontra;
	
	//GUI
	//Karten des Spielers
	private Spieler spielerKarten;
	//Nachrichten des Clients
	private Meldungen spielerMeldungen;
	
	//Karten der Gegenspieler
	private Gegenspieler[] gegenspielerKarten;
	
	//Tisch
	private Tisch tisch;
	
	//Dialog, der abfrägt, welches Spiel der Spieler spielen will
	private SpielmodusDialog dialog;
	
	private JButton letzterStichButton;
	private LetzterStich letzterStichAnzeige;
	
	private Konto konto;
	
	//Hintergrund
	private JLabel hintergrund;	
	
	public Graphik(Model model, final Client client) {
		super();
		//Vorerst keine ID setzen
		ID = -1;
		
		keinKontra = false;
		
		this.model = model;
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setTitle("Schafkopf-App");
		//Icon der Anwendung setzen
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(logo));
		this.setIconImage(icon.getImage()); 
		
		this.setSize(1130, 740);
		//arrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrg
		this.setResizable(false);
		//Äußeres Layout nicht vorhanden
		this.setLayout(null);
		this.setLocationRelativeTo(null);
		this.initGUI(); 
		this.setVisible(true);
		this.pack();
		this.setSize(1130, 740);
		
		this.addWindowListener(new WindowListener() {

			public void windowClosing(WindowEvent arg0) {
			}
			public void windowActivated(WindowEvent e) {
			}
			public void windowClosed(WindowEvent e) {
				client.abmelden(); 
			}
			public void windowDeactivated(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowOpened(WindowEvent e) {
			}
		});
	}	
	
	/**
	 * Erstellt eine neue GUI
	 */
	private void initGUI() {
		try {
		
			hintergrund = new JLabel();
			getContentPane().add(hintergrund);
			//Das letzte fünftel des Fensters ist für Spielermeldungen
			hintergrund.setBounds(0, 0, this.getWidth(), this.getHeight());
			hintergrund.setIcon(new ImageIcon(getClass().getClassLoader().getResource(hintergrundbild)));
			//hintergrund.setIcon(new ImageIcon(hintergrundbild));
			hintergrund.setVisible(true);
			hintergrund.setLayout(null);
			
			//-------------------------------------------------------------hintergrund
			
			//Breite der Felder 
			int breite = hintergrund.getWidth() / 3;
			//Höhe der Felder
			int hoehe = hintergrund.getHeight() / 3;
			
			//Karten auf dem Tisch
			tisch = new Tisch();
			hintergrund.add(tisch);
			//Der Hintergrund wird + -förmig aufgeteilt
			tisch.setBounds(breite, hoehe, breite, hoehe);
			tisch.kartenPosition();
			tisch.setVisible(true);
			
			gegenspielerKarten = new Gegenspieler[3];
			
			for(int i = 0; i < gegenspielerKarten.length; i++) {
				//Name muss noch gesetzt werden
				gegenspielerKarten[i] = new Gegenspieler();
				gegenspielerKarten[i].setVisible(true);
			}
			
			hintergrund.add(gegenspielerKarten[0]);
			gegenspielerKarten[0].setBounds(0, hoehe, breite, hoehe);
			
			gegenspielerKarten[0].nachricht("Spieler 1");
			
			hintergrund.add(gegenspielerKarten[1]);
			gegenspielerKarten[1].setBounds(breite, 0, breite, hoehe);
			
			gegenspielerKarten[1].nachricht("Spieler 2");
			
			hintergrund.add(gegenspielerKarten[2]);
			gegenspielerKarten[2].setBounds(2 * breite, hoehe, breite, hoehe);
			
			gegenspielerKarten[2].nachricht("Spieler 3");
			
			//Anzeige der Karten der Spieler
			spielerKarten = new Spieler(440, 120);
			hintergrund.add(spielerKarten);
			//Unterhalb der eigenen Meldungen platziert
			spielerKarten.setLocation(this.getWidth() / 2 - 220, hoehe*2 + 100);
			spielerKarten.setVisible(true);
			
			//Meldungen des Spielers (4 Meldungen werden angezeigt)
			spielerMeldungen = new Meldungen(5);
			hintergrund.add(spielerMeldungen);
			//Die Meldungen laufen im letzten Fünftel des Fensters
			spielerMeldungen.setBounds(breite, hoehe*2, breite, hoehe);
			spielerMeldungen.setVisible(true);
			//erste Ausgabe
			spielerMeldungen.nachricht("Mit Server verbunden");
			
			dialog = new SpielmodusDialog();
			hintergrund.add(dialog);
			dialog.setBounds(10, 10, 300, 300);
			dialog.setVisible(false);
			
			letzterStichButton = new JButton();
			hintergrund.add(letzterStichButton);
			letzterStichButton.setText("Letzter Stich");
			letzterStichButton.setBounds(this.getWidth() - 150, 10, 140, 30);
			letzterStichButton.setVisible(true);
			letzterStichButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if(letzterStichAnzeige.isVisible()) {
						letzterStichAnzeige.setVisible(false);
						letzterStichButton.setText("Letzter Stich");
					} else {
						letzterStichAnzeige.setzeModel(model, namen);
						letzterStichAnzeige.setVisible(true);
						letzterStichButton.setText("Ausblenden");
					}
				}
			});
			
			letzterStichAnzeige = new LetzterStich();
			hintergrund.add(letzterStichAnzeige);
			letzterStichAnzeige.setLocation(this.getWidth() - 350, 40);
			letzterStichAnzeige.setVisible(false);
			
			konto = new Konto();
			hintergrund.add(konto);
			konto.setLocation(10, this.getHeight() - 210);
			konto.setVisible(true);
			
			//-------------------------------------------------------------hintergrund
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Zeichnet das Fenster neu
	 */
	public void update() {
		setModel(model);
	}
	
	/**
	 * Aktualisiert das Model
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
		//aktualiseren der Anzeige
		spielerKarten.update(this.model.gibSpielerKarten(ID));
		
		//Passt die Karten auf dem Tisch an die Spieler an
		Karte[] gespielt = new Karte[4];
		gespielt[3] = this.model.gibTisch()[ID]; 
		for(int i = 0; i < 3; i++) {
			gespielt[i] = this.model.gibTisch()[(ID + 1 + i)%4];
		}
		
		tisch.setzeKarten(gespielt);
				
		for(int i = 0; i < 3; i++) {
			int angezeigteKarten = this.model.gibSpielerKarten(ID).size();
			//Prüft, ob der Spieler schon eine Karte gespielt hat 
			if(gespielt[3] == null) {
				if(gespielt[i] != null) {
					//Wenn der Spieler noch nicht gespielt hat, aber der Gegner
					angezeigteKarten--;
				}
			} else {
				if(gespielt[i] == null) {
					//Wenn der Spieler schon gespielt hat, aber der Gegner noch nicht
					angezeigteKarten++;
				}
			}
			gegenspielerKarten[i].update(angezeigteKarten);
		}
		//Aktualisiert den letzten Stich
		letzterStichAnzeige.setzeModel(model, namen);
		
		this.repaint();
	}
	
	public void weristdran(int ID) {
		if(ID == this.ID){
			spielerMeldungen.dran(true);
		} else {
			spielerMeldungen.dran(false);
		}
		
		int dran = ID - this.ID;
		if(dran < 0) {
			//Eine Runde rundum
			dran += 4;
		}
		dran -= 1;
			
		for(int i = 0; i < 3; i++) {
			if(i == dran) {
				gegenspielerKarten[i].dran(true);
			} else {
				gegenspielerKarten[i].dran(false);
			}
		}
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public void setzeNamen(String[] namen) {
		//speichert die Namen der Mitspieler, die angezeigt werden, sobald die ID verfügbar ist
		this.namen = namen;
		//Wenn die Namen bekannt sind, werden sie angezeigt
		mitspieler();
	}
	
	/**
	 * Zeigt die Namen der Mitspieler an
	 */
	private void mitspieler() {
		//setzt alle Meldungen des Spielers zurück
		spielerMeldungen.reset();
		spielerMeldungen.festeAnzeige(namen[ID]);
		
		int start = ID;
		
		for(int i = 0; i < 3; i++) {
			//Der Spieler links vom vorherigen Spieler
			start++;
			//Nach Nr. 3 wird neu begonnen
			start %= 4;
			
			//Gegenspieler den richtigen Namen zuweisen
			gegenspielerKarten[i].name(namen[start]);
		}
	}
	
	/**
	 * Gibt eine Nachricht an/bei einen Spieler aus
	 * @param spielerID
	 * @param text
	 */
	private void nachricht(int spielerID, String text) {
		if(spielerID == ID) {
			spielerMeldungen.nachricht(text);
		} else {
			//Differenz zwischen eigener ID und der des anderen Spielers
			//alias wie weit sitzt dieser Spieler entfernt
			int nr = spielerID - ID;
			
			//Wenn nr negativ ist, "4 mal in die richtige Richtung" - "Schritte in die falsche Richtung"
			if(nr < 0) {
				nr += 4;
			}
			
			gegenspielerKarten[nr - 1].nachricht(text);
		}
	}

	public Model spiel() throws Exception {
		this.toFront();
		//Nochmal Anzeige aktualisieren
		this.update();
		Karte gespielt = spielerKarten.spiel();
		boolean ok = false;
		
		do {
			spielerKarten.update(model.setTisch(ID, gespielt));
			if(control.erlaubt(model, ID)) {
				ok = true;
			} else {
				model.undo(ID);
				this.update();
				JOptionPane.showMessageDialog(this, "Diese Karte ist nicht erlaubt");
				gespielt = spielerKarten.spiel();
			}
		} while(!ok);
		
		return model;
	}
	
	public void setzeModus(modus mod) {
		control = new Regelwahl().wahl(mod, model);
		nachricht(ID, "Es wird " + dialog.modusZuSprache(mod) + " gespielt");
	}
	
	public void bestesspiel(modus mod) {
		nachricht(ID, "Bisher höchstes Spiel ist " + dialog.modusZuSprache(mod));
	}
	
	public modus spielstDu() {
		boolean fertig = false;
		this.toFront();
		dialog.setVisible(true);
		while(!fertig) {			
			modus m = dialog.modusWahl();
			
			//Prüft, ob die Karte erlaubt ist
			if(!new Regelwahl().darfGespieltWerden(m, model, ID, dialog.farbe(m))) {
				JOptionPane.showMessageDialog(this, "Das geht nicht!");
				dialog.reset();
				continue;
			}
			dialog.setVisible(false);
			dialog.reset();
			fertig = true;
			return m;
		}
		return modus.NICHTS;
	}
	
	public String klopfstDu() {
		if(javax.swing.JOptionPane.showConfirmDialog(this, "Willst du klopfen?") == 0) {
			return "JA";
		} else {
			return "NEIN";	
		}
	}
	
	public String kontra() {
		//Wenn der Spieler nicht spielt
		if(!keinKontra) {
			if(javax.swing.JOptionPane.showConfirmDialog(this, "Kontra?") == 0) {
				return "JA";
			} else {
				return "NEIN";	
			}
		} else {
			return "NEIN";
		}
	}
	
	public void sieger(int s1, int s2) {
		String text = "";
		//Wenn die spielenden verloren haben
		if(s1 > 9) {
			for(int i = 0; i < 4; i++) {
				if(i == s1 - 10) {
					text += namen[i] + ": Verdammt! Verloren.\n";
				} else if(i == s2 - 10) { //s2 kann 4 sein und wird damit nie i
					text += namen[i] + ": Oh nööö! Verloren.\n";
				} else {
					text += namen[i] + ": Haha! Gewonnen.\n";
				}
			}
		} else { //Ansonsten
			for(int i = 0; i < 4; i++) {
				if(i == s1) {
					text += namen[i] + ": Juhu! Gewonnen.\n";
				} else if(i == s2) { //s2 kann 4 sein und damit nie gleich i werden
					text += namen[i] + ": Na geht doch! Gewonnen.\n";
				} else {
					text += namen[i] + ": Na super. Verloren.\n";
				}
			}
		}
		
		JOptionPane.showMessageDialog(this, text);
	}

	public String hochzeit() {
		if(JOptionPane.showConfirmDialog(this, "Willst du heiraten?") == JOptionPane.OK_OPTION) {
			return "JA";
		}
		return "NEIN";
	}

	public Karte hochzeitKarte() {
		this.toFront();
		JOptionPane.showMessageDialog(this, "Welche gibst du her?");
		return spielerKarten.spiel();
	}

	public void spielt(int spielt, int mitspieler) { 
		nachricht(spielt, "Ich spiel");
		if(mitspieler != 4) {
			nachricht(mitspieler, "Und ich hab die Hochzeit angenommen");
		}
		
		if(ID == spielt || ID == mitspieler) {
			keinKontra = true;
		} else {
			keinKontra = false;
		}
	}

	public void kontra(boolean[] kontra) {
		for(int i = 0; i < 4; i++) {
			if(kontra[i]) {
				nachricht(i, "Kontra!");
			}
		}
	}

	public void geklopft(boolean[] geklopft) {
		for(int i = 0; i < 4; i++) {
			if(geklopft[i]) {
				nachricht(i, "[Klopf] [Klopf]");
			}
		}
	}
	
	public void konto(int kontostand, int stock) {
		konto.setzeKontostand(kontostand);
		konto.setzetStock(stock);
	}
	
	public void beenden() {
		JOptionPane.showMessageDialog(null, "Der Server hat das Spiel beendet");
		this.dispose();
	}
}

