package equipment;
import java.security.*;



public class Equipement {

	private KeyPair maCle; // Paire de clés de l’equipement.
	private Certificat monCert; // Certificat auto-signé.
	private String monNom; // Identité de l’equipement.
	private int monPort; // Numéro de port d’écoute.

	public Equipement (String nom, int port, boolean b) throws Exception {
		// Constructeur de l’equipement identifié par nom
		// et qui « écoutera » sur le port port.
		monNom = nom;
		monPort = port;

		// Initialisation de la structure pour la generation de clé
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

		// Définition de la taille de cle 512 bits
		if(kpg != null){
			kpg.initialize(512, new SecureRandom());
			maCle = kpg.generateKeyPair(); // Génération de la paire de clés
		}

		monCert = new Certificat(nom, maCle, 10);

	}

	public void affichage_da() {
		// Affichage de la liste des équipements de DA
	}

	public void affichage_ca() {
		// Affichage de la liste des équipements de CA
	}

	public void affichage() throws Exception {
		// Ensemble des info de l’équipement
		System.out.println(monNom);
		System.out.println(monPort);
		System.out.println(maCle);
		System.out.println(monCert.getX509());
	}

	public String monNom (){
		return monNom; // Recuperation de l’identite de l’équipement
	}

	public Certificat monCertif() {
		return monCert; // Recuperation du certificat auto-signé
	}

}