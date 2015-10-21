package equipment;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import network.Accept_clients;



public class Equipement {

	private KeyPair maCle; // Paire de clés de l’equipement.
	private X509Certificate monCert; // Certificat auto-signé.
	private String monNom; // Identité de l’equipement.
	private int monPort; // Numéro de port d’écoute.
	private static  BigInteger id = BigInteger.ZERO;

	
	public Equipement (String nom, int port, boolean b) throws Exception {
		// Constructeur de l’equipement identifié par nom
		// et qui « écoutera » sur le port port.
		monNom = nom;
		monPort = port;
		id = id.add(BigInteger.ONE); // Numéro de série du certificat


		// Initialisation de la structure pour la generation de clé
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

		// Définition de la taille de cle 512 bits
		if(kpg != null){
			kpg.initialize(512, new SecureRandom());
			maCle = kpg.generateKeyPair(); // Génération de la paire de clés
		}

		monCert = Certificat.buildSelfCert(nom, maCle, 10);
		Certificat.verifX509(monCert, maCle.getPublic());

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
		System.out.println(id);
		System.out.println(monPort);
		System.out.println(maCle.getPublic());
		System.out.println(monCert);
	}
	
	public X500Principal getSubject(){
		return monCert.getIssuerX500Principal();
	}
	
	public X509Certificate getCert(){
		return monCert;
	}
	
	public PublicKey getKPub(){
		return maCle.getPublic(); // Retourne la clé publique de l'équipement
	}
	
	public KeyPair getKP(){
		return maCle;
	}
	
	
	public void setServeur() throws IOException{
		ServerSocket serverSocket = new ServerSocket(monPort); // Creation de socket (TCP)
		Thread t = new Thread(new Accept_clients(serverSocket, maCle.getPrivate())); // Gestion des connexions par un thread
		t.start();
		System.out.println(monNom+" est serveur");
	}
	
	public void sendCSR() throws Exception{
		Socket clientSocket = null; 
		InputStream NativeIn = null; 
		ObjectInputStream ois = null; 
		OutputStream NativeOut = null; 
		ObjectOutputStream oos = null;
		
		clientSocket = new Socket("localHost",monPort); 

		// Création des flux natifs et evolues
		NativeOut = clientSocket.getOutputStream(); 
		oos = new ObjectOutputStream(NativeOut); 
		NativeIn = clientSocket.getInputStream(); 
		ois = new ObjectInputStream(NativeIn);

		// Création du CSR
		String strCSR = Certificat.cSRtoPEM(Certificat.buildCSR(monCert.getSubjectX500Principal(), maCle));
		
		// Emission d’un String
		oos.writeObject(strCSR); 
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