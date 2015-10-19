package equipment;
import java.security.PublicKey;

import network.Client;
import network.Server;


public class Equipement {

	private PaireClesRSA maCle; // Paire de clés de l’equipement.
	private Certificat monCert; // Certificat auto-signé.
	private String monNom; // Identité de l’equipement.
	private int monPort; // Numéro de port d’écoute.
	public Equipement (String nom, int port, boolean b) throws Exception {
		// Constructeur de l’equipement identifié par nom
		// et qui « écoutera » sur le port port.
		monNom = nom;
		monPort = port;
		maCle = new PaireClesRSA();
		monCert = new Certificat(nom, maCle, 10);
		
		/*if(b){
			new Server(monPort);
		} else {
			new Client("localHost", monPort);
		}*/
	}

	public void affichage_da() {
		// Affichage de la liste des équipements de DA.
	}

	public void affichage_ca() {
		// Affichage de la liste des équipements de CA.
	}

	public void affichage() throws Exception {
		// Ensemble des info de l’équipement.
		System.out.println(monNom);
		System.out.println(monPort);
		System.out.println(maCle);
		System.out.println(Certificat.decodePEM(Certificat.encodePEM(monCert)));
	}

	public String monNom (){
		return monNom; // Recuperation de l’identite de l’équipement.
	}

	public PublicKey maClePub() {
		return maCle.Publique(); // Recuperation de la clé publique de l’équipement.
	}

	public Certificat monCertif() {
		return monCert; // Recuperation du certificat auto-signé.
	}

}