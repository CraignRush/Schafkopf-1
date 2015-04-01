package graphik;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Konto extends JLabel {
	
	private JLabel kontostand;
	private JLabel stock;
	
	public Konto() {
		super();
		this.setSize(200, 200);
		this.setIcon(new ImageIcon(getClass().getClassLoader().getResource("graphik/karten/holzschale.png")));
		this.setLayout(null);
		
		kontostand = new JLabel();
		this.add(kontostand);
		kontostand.setBounds(50, 65, 100, 30);
		kontostand.setText("Konto: ");
		kontostand.setOpaque(false);
		kontostand.setForeground(Color.white);
		
		Font schrift = kontostand.getFont();
		schrift = schrift.deriveFont(schrift.BOLD);
		kontostand.setFont(schrift);
		
		stock = new JLabel();
		this.add(stock);
		stock.setBounds(50, 105, 100, 30);
		stock.setText("Stock: ");
		stock.setOpaque(false);
		stock.setForeground(Color.white);
		
		stock.setFont(schrift);		
	}
	
	public void setzeKontostand(int kontostand) {
		this.kontostand.setText("Konto: " + String.valueOf(kontostand));
	}
	
	public void setzetStock(int stock) {
		this.stock.setText("Stock: " + String.valueOf(stock));
	}

}
