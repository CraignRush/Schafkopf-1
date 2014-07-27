package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import regeln.Hochzeit;

import regeln.Controll;
import regeln.Regelwahl;
import lib.Karte;
import lib.Model;
import lib.Model.modus;

public class Server implements Runnable{
	
		private static final int PORT = 15555;
	
		//Server, der die Verbindungen verwaltet
		private ServerSocket server;
		private Thread listener;
		
		//Speichert den Spielstand
		private Model model;
        
        //hält alle 4 Spieler, ob Bot oder Mensch
        private ArrayList<Spieler> spieler;
        private int spielerzahl;
        
        private boolean[] geklopft;
        
        private boolean[] kontra;
        
        //Speichert die Höhe des Stocks
        private int stock;
        
        //speichert den Spielmodus
        private modus mod;
        
        private Controll regeln;
        private Regelwahl regelwahl;
        
        private int spielt;
        private int mitspieler;
        
        //fragt ab, ob gerade kein Spiel läuft
        private boolean nocheins;
        
        private final Graphik graphik;
                
        /**
         * Erstellt einen neuen Server
         **/
        public Server(Graphik graphik) {
        	
        	model = new Model();
              
        	spieler = new ArrayList<Spieler>();
        	spielerzahl = 4;
        	
        	geklopft = new boolean[4];
        	for(int i = 0; i < 4; i++) {
        		geklopft[i] = false;
        	}
        	
        	kontra = new boolean[4];
        	
        	regelwahl = new Regelwahl();
        	
        	stock = 0;
        	
        	nocheins = true;
        	
        	this.graphik = graphik;
        	
        	try {
        		//Server für jeden Port
				server = new ServerSocket(PORT);
				listener = new Thread(this);
				
				listener.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        } 
        
        /**
         * Nimmt die Verbindungen auf
         */
        public void run() {
        	try {
        		while(true) {
        			//Akzeptiert die Verbindung
	        		Socket client = server.accept();
	        		
	        		spieler.add(new Mensch(client, this));
	        		graphik.textSetzen(spieler);
	        		
	        		if(spieler.size() == spielerzahl && nocheins) {
	        			nocheins = false;
	        			neuesSpiel();
	        		}
	        		
	        		//Drossel
	        		Thread.sleep(100);
        		}
        	} catch(Exception e) {
        		e.printStackTrace();
        		graphik.textSetzen(spieler);
        	}
        }
        
        /**
         * Erstellt ein neues Spiel
         * @throws Exception 
         */
        private void neuesSpiel() throws Exception {
        	
        	//Spiel wurde gestartet
        	while(!nocheins) {
        		
        		//Am Anfang jeder Runde ein neues Model erzeugen
        		model = new Model();
        		
        		//Anzeigen der Spieler
	        	for(int i = 0; i < 4; i++) {
	        		model.setzeName(i, spieler.get(i).gibName());
	        	}
        		graphik.textSetzen(spieler);
        		
        		//gibt jedem Spieler seine ID
        		for(int i = 0; i < 4; i++) {
        			try {
						spieler.get(i).setzeID(i);
					} catch (Exception e) {
						e.printStackTrace();
						//Bei Fehler abbrechen
						break;
					}
        		}

	        	model.mischen();
	        	model.ersteKartenGeben();
	        	
	        	for(int i = 0; i < 4; i++) {
	        		//Speichert, ob ein Spieler geklopft hat etc.
	        		spieler.get(i).erste3(model);
	        		if(spieler.get(i).gibAntwort().equals("JA"))
	        			geklopft[i] = true;
	        	}
	        	
	        	//Spieler benachrichtigen, wer geklopft hat
	        	for(int i = 0; i < 4; i++) {
	        		spieler.get(i).geklopft(geklopft);
	        	}
	        	
	        	model.zweiteKartenGeben();
	        	
	        	for(int i = 0; i < 4; i++) {
	        		//speichert, was der Spieler spielen will
	        		spieler.get(i).spielstDu(model);
	        		
	        		modus m = null;
	        		while(m == null) {
	        			try {
	        				m = Model.modus.valueOf(spieler.get(i).gibAntwort());
	        			} catch(Exception e) {
	        				nocheins = true;
	        				continue;
	        			}
	        			Thread.sleep(100);
	        		}
	        		
	        		//speichert, was gespielt wird
	        		mod = model.werSpielt(m, mod);
	        		
	        		//Wenn das Spiel des aktuellen Spielers über ein anderes geht
	        		if(mod.equals(m)) spielt = i;
	        	}
	        	
	        	//will niemand spielen geht es zur nächsten Runde
	        	if(mod.equals(null)) {
	        		stock();
	        		continue;
	        	}
	        	//legt die Regeln fest
	        	regeln = regelwahl.wahl(mod, model, spielt);
	        	if(regeln == null) {
	        		stock();
	        		break;
	        	}
	        	
	        	//Wenn eine Hochzeit gespielt wird
	        	if(mod.equals(modus.HOCHZEIT)) {
	        		Hochzeit h = (Hochzeit) regeln;
	        		
	        		Karte angebot = spieler.get(spielt).gibKarte();
	        		
	        		if(h.hochzeitMoeglich(model, spielt, angebot)) {
		        		for(int i = 0; i < 4; i++) {
		        			
		        			if(i != spielt) {
		        				spieler.get(i).hochzeit();
		        				
		        				if(spieler.get(i).gibAntwort().equals("JA")) {
		        					//Wenn die Hochzeit angenommen wird
		        					Karte k = spieler.get(i).gibKarte();
		        					
		        					//Wenn die Karte ein Trumpf ist
			        				if(!h.istTrumpf(k.gibWert(), k.gibFarbe())) {
			        					model.hochzeit(spielt, i, angebot, k);
			        					mitspieler = i;
			        				}
		        				}
		        			}
		        		}
	        		}
	        	} else 
	        		//bestimmt einen eventuellen Mitspieler
		        	mitspieler = regeln.mitspieler(model);   
	        	
	        	//Sendet den Modus an alle Spieler und empfängt, ob kontra gegeben wurde
	        	for(int i = 0; i < 4; i++) {
	        		String k;
					try {
						k = spieler.get(i).modus(mod);
						//Wenn eine Hochzeit gespielt wird, werden beide spielenden gesendet
						int mit = 4;
						if(mod.equals(modus.HOCHZEIT))
							mit = mitspieler;
						spieler.get(i).spieler(spielt, mit);
					} catch (Exception e) {
						e.printStackTrace(); 
						k = null;
					}
	        		
	        		if(k == null || k == "") kontra[i] = false;
	        		else kontra[i] = true;
	        	}	
	        	
	        	for(int i = 0; i < 4; i++) {
	        		spieler.get(i).kontra(kontra); 
	        	}
	        	
	        	//Wenn ein Si gespielt wird die Runde gar nicht ers spielen
	        	if(mod.equals(modus.SI)) {
	        		rundeBeenden();
	        		continue;
	        	}
	        	
	        	//Spielen
	        	for(int i = 0; i < 6; i++) {
	        		for(int j = 0; j < 4; j++) {
	        			//Übergibt dem Spieler das aktuelle Model und...
	        			spieler.get(i).spielen(model);
	        			//...empfängt das aktualisierte
	        			model = spieler.get(i).gibModel();
	        			
	        			//Wenn ein Fehler auftritt beenden
	        			if(model == null) {
	        				nocheins = true;
	        				break;
	        			}
	        		}
	        		//Wenn ein Fehler aufgetreten ist
	        		if(nocheins) break;
	        		
	        		//einen Stich zuteilen
	        		int sieger = regeln.sieger(model);
	        		model.Stich(sieger);
	        	}

	        	rundeBeenden();
	        	
	        	//neu Runde
	        	naechster();
        	}
        }
        
        /**
         * Der nächste spieler ist an der Reihe
         */
        private void naechster() {
        	//Den ersten Spieler auf die letzte Position
        	Spieler s = spieler.get(0);
        	spieler.remove(s);
        	spieler.add(s);
        }
        
        /**
         * Beendet die Runde
         */
        private void rundeBeenden() {
        	int pSpielt;
        	if(mod.equals(modus.SI)) {
        		//Der Spieler hat alle Punkte
        		pSpielt = 120;
        	}
        	
        	ArrayList<Integer> punkte = model.gibPunkte();
        	//Die Punkte des Spielers
        	pSpielt = punkte.get(spielt);
        	//und vielleicht des Mitspielers
        	if(mitspieler != 4)
        		pSpielt += punkte.get(mitspieler);
        	
        	//Wenn ein Du gespielt wurde
        	String modString = mod.toString();
        	//Die letzten zwei Buchstaben werden verglichen
        	if(modString.substring(modString.length() - 3, modString.length() - 1)
        			.toLowerCase()
        			.equals("du")) {
        		if(pSpielt != 120)
        			//Wenn der Du verloren wurde
        			spielt += 10;
        	} else 
        	//Ansonsten wird normal verrechnet
        	if(pSpielt <= 60) {
        		//Anzeigen, dass er verloren hat
        		spielt += 10;
        		if(mitspieler != 4)
        			mitspieler += 10;
        	}
        	
        	for(int i = 0; i < 4; i++) {
        		try {
        			spieler.get(i).sieger(spielt, mitspieler);
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        	
        	//Den Spielern Geld abziehen oder hinzufügen
        	for(int i = 0; i < 4; i++) {
        		if(i == spielt || i == mitspieler) {
        			
        		}
        		if(i == spielt + 10 || i == mitspieler + 10) {
        			
        		}
        	}
        }
        
        /**
         * Füllt den Stock auf
         */
        private void stock() {
        	
        }
        
        /**
         * Gibt alle Spieler zurück, damit diese angezeigt werden können
         * @return spieler
         */
        public synchronized ArrayList<Spieler> gibSpieler() {
        	return spieler;
        }
        
        /**
         * Entfernt einen Spieler und sorgt dafür, dass das Spiel unterbrochen wird
         * @param s
         */
        public synchronized void entferneSpieler(Spieler s) {
        	spieler.remove(s);
        	nocheins = true;
        }
        
        /**
         * Setzt die Spielerzahl
         * @param spielerzahl
         */
        public void setSpielerzahl(int spielerzahl) {
        	this.spielerzahl = spielerzahl;
        }
        
        /**
         * Gibt die Spielerzahl zurück
         * @return
         */
        public int gibSpielerzahl() {
        	return spielerzahl;
        }
        
        /**
         * Beendet den Server
         */
        public void beenden() {
        	try {
        		listener.stop();
				server.close();
				spieler = null;
			} catch (IOException e) {
				e.printStackTrace();
				//Programm beenden
				System.exit(0);
			}
        }

}
