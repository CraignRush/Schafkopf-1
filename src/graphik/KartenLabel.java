package graphik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

import lib.Karte;

public class KartenLabel extends JLabel {
	
	//Speichert den Pfad zum angezeigten bild
	private String bild;
	//Speichert das Bild
	private BufferedImage icon;
	
	//Speichert Höhe und Weite des Panels
	private int width;
	private int height;
	
	
	public KartenLabel(Karte bild, int width, int height) {
		super();
		
		this.setLayout(null);
		
		//Anzeige der Begrenzungen des Panels
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		
		//Setzt das Bild der Karte
		setBild(bild);
		try {
			//Liest das Bild ein
			KartenLabel.class.getResource(this.bild);
			icon = ImageIO.read(new File(this.bild));
			
			this.setText("Karte");
			
		} catch (IOException e) {
			System.out.println("Bild wurde nicht gefunden");
			e.printStackTrace();
		}
		this.width = width;
		this.height = height;
		
		this.setSize(width, height);
	}
	
	/**
	 * Legt ein Bild auf die Karte
	 * Wird null übergeben, wird das Bild rueckseite.jpg angezeigt
	 * @param karte
	 */
	public void setBild(Karte karte) {
		if(karte == null) {
			bild =  "karten/rueckseite.jpg";
		} else {
			//Zeigt das zur Karte gehörende Bild
			bild =  "karten/" + 
					karte.gibFarbe().toString().toLowerCase() + 
					karte.gibWert().toString().toLowerCase() + 
					".jpg";
		}
	}
	
	/**
	 * Passt die Größe des Bildes auf eine veränderte Fenstergröße an
	 * @param width
	 * @param height
	 */	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Dimension windowSize = this.getSize();
        this.height = (int) windowSize.getHeight();
        this.width = (int) windowSize.getWidth();
		
		Image img = icon.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		
		g.drawImage(img, 0, this.getInsets().top, this);
	}
}
