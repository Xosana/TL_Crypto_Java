package com.equipment;
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

import com.network.Accept_clients;



public class Equipement {

	private KeyPair maCle; // Paire de clés de l’équipement.
	private X509Certificate monCert; // Certificat auto-signé.
	private String monNom; // Identité de l’équipement.
	private int monPort; // Numéro de port d’écoute.
	private static  BigInteger bi = BigInteger.ZERO; // Compteur d'Id
	private BigInteger id; // Id de l'équipement

	
	public Equipement (String nom, int port) throws Exception {
		// Constructeur de l’equipement identifié par nom
		// et qui « écoutera » sur le port port.
		monNom = nom;
		monPort = port;
		
		bi = bi.add(BigInteger.ONE); 
		id = bi;

		// Initialisation de la structure pour la generation de clé
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

		if(kpg != null){
			kpg.initialize(512, new SecureRandom()); // Définition de la taille de cle 512 bits
			maCle = kpg.generateKeyPair(); // Génération de la paire de clés
		}

		// Auto-certification de la clé publique
		monCert = Certificat.buildSelfCert(id+" "+monNom, maCle, 10);
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
	
	public static BigInteger getBi(){
		return bi;
	}
	
	public BigInteger getId(){
		return id;
	}
	
	public String getNom(){
		return monNom;
	}
	
	public X509Certificate getX509(){
		return monCert;
	}
	
	public void setServeur() throws IOException{
		ServerSocket serverSocket = new ServerSocket(monPort); // Creation de socket (TCP)
		Thread t = new Thread(new Accept_clients(serverSocket, maCle.getPrivate(), this)); // Gestion des connexions par un thread
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
		System.out.println("CSR est envoyé");


		// Reception d’un String
		String pemcert = (String) ois.readObject(); 
		X509Certificate intermX509 = Certificat.pEMtoX509(pemcert);
		System.out.println("Réception du certificat de la Kpub");
		System.out.println(intermX509);

		// Fermeture des flux evolues et natifs
		ois.close();
		oos.close(); 
		NativeIn.close(); 
		NativeOut.close();

		// Fermeture de la connexion
		clientSocket.close(); 
	}

}