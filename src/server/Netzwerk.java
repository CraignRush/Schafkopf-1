package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lib.Karte;

public class Netzwerk extends lib.Netzwerk {
	
	private Socket client;
	
	public Netzwerk(Socket client) throws Exception {
		super();
		
		try {			
			//Erstellen eines Clients -> Output
			this.client = client;
			
			out = new PrintWriter(client.getOutputStream());
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch(Exception e) {
			//Verbindung beenden
			client.close();
			throw e;
		}
	}
	
	/**
	 * Gibt die IP des Clients zurück
	 * @return IP
	 */
	public String gibIP() {
		return client.getInetAddress().getHostAddress();
	}

	public Karte getKarte() throws Exception {
		Karte karte;
		try {
			karte = new Karte(Karte.farbe.valueOf(einlesen()),
							Karte.wert.valueOf(einlesen())); 
		} catch(Exception e) {
			throw e;
		}
		return karte;
	}
}

