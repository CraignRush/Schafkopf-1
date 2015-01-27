/**
 * Repräsentiert serverseitig einen menschlichen Spieler. Übernimmt einen Port und horcht diesen ab.
 * 
 */

package server;

import java.net.Socket;

import lib.Karte;
import lib.Model;
import lib.Model.modus;
import server.Netzwerk;

public class Mensch implements Spieler {
	
	private Netzwerk netzwerk;
	
	//Hält eine Referenz auf den Server, um diesen zu benachrichtigen, wenn
	//die Verbindung abgebrochen wurde
	private Server server;
	
	private String name;
	
	//speichert neueste antworten
	private String antwort;
	private Model model;
	private Karte karte;

	private boolean modelupdate = false;
	
	private boolean beenden;
	
	public Mensch(Socket client, Server server) throws Exception {
		
		beenden = false;
		
		antwort = "";
		
		this.server = server;
		
		//Errichtet neue Verbindung
		netzwerk = new Netzwerk(client);
		
		new Thread() {
			public void run() {
				listen();
			}
		}.start();
	}
	
	/**
	 * Horcht auf Befehle vom Client
	 */
	public void listen() {
		
		while(!beenden) {
			try {
				Object[] data = netzwerk.read();
				
				switch(data[0].toString()) {
				case "!NAME":
					name = data[1].toString();
					continue;
				case "!ERSTE3":
					antwort = data[1].toString();
					continue;
				case "!SPIEL":
					model = (Model) data[1];
					modelupdate = true;
					continue;
				case "!SPIELSTDU":
					antwort = data[1].toString();
					continue;
				case "!KONTRA":
					antwort = data[1].toString();
					continue;
				case "!HOCHZEIT":
					//Antwort: JA + Karte oder NEIN
					antwort = data[1].toString();
					if(antwort.equals("JA")) {
						model = (Model) data[2];
						modelupdate = true;
					}
					continue;
				case "!KARTE":	
					karte = new Karte(
							Karte.farbe.valueOf(data[1].toString()),
							Karte.wert.valueOf(data[2].toString()));
					continue;
				case "!BEENDEN":
					beenden();
					continue;
				default: 
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
				beenden = true;
				//Benachrichtige Server, dass ein Spieler entfernt wurde
				abmelden();
			}
		}
	}
	
	public synchronized String gibAntwort() {
		//Solange keine Antwort da ist...
		while(antwort == null) {
			//vllt. Observer-Pattern?
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		String a = antwort;
		//Eingelesene Antwort löschen
		antwort = null;
		return a;
	}
	
	public Model gibModel() {
		while(!modelupdate) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		modelupdate = false;
		return model; 
	}
	
	public synchronized void setzeModel(Model m) {
		this.model = m;
	}

	public boolean erste3(Model model) throws Exception {
		//sendet das Model und erwartet antwort
		setzeModel(model);
		netzwerk.printModel("!ERSTE3", model);			
		//horcht nach der Antwort	
		if(gibAntwort().equals("JA")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sendet die Namen der Mitspieler
	 */
	private void mitspielerNamen() {
		try {
			String[] namen = model.gibNamen();
			netzwerk.print("!MITSPIELER", namen);
		} catch (Exception e) {
			//Keine Fehlermeldung, da unbedeutend
			e.printStackTrace();
		}
	}

	public void spielen(Model model) throws Exception {
		netzwerk.printModel("!SPIEL", model);
	}

	public String spielstDu(Model model) throws Exception {
		netzwerk.printModel("!SPIELSTDU", model);
		return gibAntwort();
	} 
	
	public void spieler(int spielt, int mitspieler) throws Exception {
		String[] data = new String[] {
				String.valueOf(spielt),
				String.valueOf(mitspieler)
		};
		netzwerk.print("!SPIEL", data);
	}

	public boolean modus(lib.Model.modus m) throws Exception {
		netzwerk.print("!MODUS", m.toString());
		
		//gibt zurück, ob Kontra gegeben wurde
		if(gibAntwort().equals("JA")) {
			return true;
		} else {
			return false;
		}
	}

	public void sieger(int s1, int s2) throws Exception {
		String[] data = new String[] {
			String.valueOf(s1),
			String.valueOf(s2)
		};
		netzwerk.print("!SIEGER", data);
	}
 
	public String gibIP() {
		String ip = netzwerk.gibIP();
		return ip;
	}
	
	public String gibName() {
		while(name == null) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		return name;
	}

	public void name() throws Exception {
		netzwerk.print("!NAME", "");
	}

	public void setzeID(int ID) throws Exception {
		netzwerk.setID(ID);
		netzwerk.print("!ID", String.valueOf(ID));
		
		//Sendet die Namen der Mitspieler
		mitspielerNamen();
	}

	public Karte gibKarte() throws InterruptedException {	
		while(karte == null) {
			//vlt. Observer-Pattern?
			Thread.sleep(100);
		}
		Karte k = karte;
		//löscht die eingelesene Karte, um nicht zweimal die gleiche zurückzugeben
		karte = null;
		return k;
	}

	public boolean hochzeit() throws Exception {
		netzwerk.print("!HOCHZEIT", "");
		if(gibAntwort().equals("JA")) {
			return true;
		} else {
			return false;
		}
	}

	public void geklopft(boolean[] geklopft) throws Exception {
		String[] data = new String[4];
		for(int i = 0; i < 4; i++) {
			if(geklopft[i]) {
				data[i] = "true";
			} else {
				data[i] = "false";
			}
		}
		netzwerk.print("!GEKLOPFT", data);
	}

	public synchronized void kontra(boolean[] kontra) throws Exception {
		String[] data = new String[4];
		for(int i = 0; i < 4; i++) {
			if(kontra[i]) {
				data[i] = "true";
			} else {
				data[i] = "false";
			}
		}
		netzwerk.print("!KONTRA", data);
	}

	public void beenden() {
		beenden = true;
		netzwerk.beenden();
		server.entferneSpieler(this);
	}
	
	public void abmelden() {
		try {
			netzwerk.print("!BEENDEN", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		beenden();
	}

}
