package equipment;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

import network.Accept_clients;



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
	
	public PublicKey getMaCle(){
		return maCle.getPublic(); // Retourne la clé publique de l'équipement
	}
	
	
	public void setServeur() throws IOException{
		ServerSocket serverSocket = new ServerSocket(monPort); // Creation de socket (TCP)
		Thread t = new Thread(new Accept_clients(serverSocket)); // Gestion des connexions par un thread
		t.start();
		System.out.println(monNom+" est serveur");
	}
	
	public void sendCertificat() throws UnknownHostException, IOException, ClassNotFoundException{
		Socket clientSocket = null; 
		InputStream NativeIn = null; 
		ObjectInputStream ois = null; 
		OutputStream NativeOut = null; 
		ObjectOutputStream oos = null;
		
		clientSocket = new Socket("localHost",monPort); 

		// Creation des flux natifs et evolues
		NativeOut = clientSocket.getOutputStream(); 
		oos = new ObjectOutputStream(NativeOut); 
		NativeIn = clientSocket.getInputStream(); 
		ois = new ObjectInputStream(NativeIn);

		// Emission d’un String
		oos.writeObject("Bonjour"); 
		oos.flush();

		// Reception d’un String
		String res = (String) ois.readObject(); 
		System.out.println(res);

		// Fermeture des flux evolues et natifs
		ois.close();
		oos.close(); 
		NativeIn.close(); 
		NativeOut.close();

		// Fermeture de la connexion
		clientSocket.close(); 
	}

}