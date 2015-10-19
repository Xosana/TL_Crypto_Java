package equipment;
import java.security.*;

public class PaireClesRSA {

	private KeyPair key;

	// Constructeur : génération d’une paire de clé RSA
	PaireClesRSA() {
				
		// Initialisation de la structure pour la generation de clé :
		KeyPairGenerator kpg = null;
		
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace();	}
		
		// Définition de la taille de cle 512 bits:
		if(kpg != null){
			kpg.initialize(512, new SecureRandom());
			// On genere la paire de cle :
			key = kpg.generateKeyPair();
		}
	}
	
	
	public PublicKey Publique() {
		return key.getPublic(); // Récupération de la clé publique
	}

	public PrivateKey Privee() {
		return key.getPrivate(); // Récupération de la clé privée
	}
}