package com.equipment;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;


public class Equipement {

	private String monNom; // Identité de l’équipement
	private int monPort; // Numéro de port d’écoute
	private KeyPair maCle; // Paire de clés de l’équipement
	private X509Certificate monCert; // Certificat auto-signé

	private HashMap<String, X509Certificate> ca;
	private HashMap<String, X509Certificate> da;

	private ServerSocket serverSocket; // Serveur d'écoute de l'équipement
	private Socket socket;
	private InputStream NativeIn; // Flux natif entrant
	private ObjectInputStream ois; // Flux évolué entrant
	private OutputStream NativeOut; // Flux natif sortant
	private ObjectOutputStream oos; // Flux évolué sortant


	public Equipement (String nom, int port) throws Exception {
		// Constructeur de l’equipement identifié par nom
		// et qui « écoutera » sur le port port.
		monNom = nom;
		monPort = port;
		serverSocket = new ServerSocket(monPort); // Creation de socket (TCP)

		// Initialisation de la structure pour la generation de clé
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		if(kpg != null){
			kpg.initialize(512, new SecureRandom()); // Définition de la taille de cle 512 bits
			maCle = kpg.generateKeyPair(); // Génération de la paire de clés
		}

		// Auto-certification de la clé publique
		monCert = Certificat.buildSelfCert(monNom, maCle, 10);
		Certificat.verifX509(monCert, maCle.getPublic());

		// Initialisation de CA et DA
		ca = new HashMap<String, X509Certificate>();
		da = new HashMap<String, X509Certificate>();

		// Initialisation des flux
		NativeIn = null; 
		ois = null; 
		NativeOut = null; 
		oos = null;
	}

	public void affichage_da() {
		Iterator<String> keySetIterator = da.keySet().iterator(); 
		while(keySetIterator.hasNext()){ 
			String key = keySetIterator.next(); 
			System.out.println("key: " + key + " value: " + da.get(key)); 
		}
	}

	public void affichage_ca() {
		Iterator<String> keySetIterator = ca.keySet().iterator(); 
		while(keySetIterator.hasNext()){ 
			String key = keySetIterator.next(); 
			System.out.println("key: " + key + " value: " + ca.get(key)); 
		}	
	}

	public void affichage() {
		// Ensemble des info de l’équipement
		System.out.println(monNom);
		System.out.println(monPort);
		System.out.println(maCle.getPublic());
		System.out.println(monCert);
	}

	public String getNom(){
		return monNom;
	}

	public int getPort(){
		return monPort;
	}

	public X509Certificate getX509(){
		return monCert;
	}

	public void initServer() throws IOException, ClassNotFoundException{
		System.out.println("Initialisation de l'équipement "+monNom+" en tant que serveur");
		System.out.println("en attente de connexion...");

		new Thread() {
			public void run(){
				try {
					socket = serverSocket.accept();

					System.out.println("Un équipement s'est connecté");

					// Création des flux natifs et évolués
					NativeIn = socket.getInputStream(); 
					ois = new ObjectInputStream(NativeIn); 
					NativeOut = socket.getOutputStream(); 
					oos = new ObjectOutputStream(NativeOut);

					// Récupération du CSR
					String pemCSR = (String) ois.readObject(); 
					System.out.println("Une demande CSR a été reçue");


					JcaPKCS10CertificationRequest csr = Certificat.pEMtoCSR(pemCSR);

					// Vérification du CSR
					if(Certificat.verifCSR(csr)){
						System.out.println("La demande CSR a été vérifiée");

						System.out.println("Certification de la clé publique");

						// Certification de la clé publique
						X509Certificate intermX509 = Certificat.cSRtoX509(X500Name.getInstance(
								monCert.getIssuerX500Principal().getEncoded()), csr, maCle.getPrivate(), 10);

						// Emission du certificat
						oos.writeObject(Certificat.x509toPEM(intermX509)); 
						oos.flush();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void askCSR(int port) throws Exception{
		socket = new Socket("localHost",port); 

		// Création des flux natifs et évolués
		NativeOut = socket.getOutputStream(); 
		oos = new ObjectOutputStream(NativeOut); 
		NativeIn = socket.getInputStream(); 
		ois = new ObjectInputStream(NativeIn);

		// Création et envoi du CSR
		String strCSR = Certificat.cSRtoPEM(Certificat.buildCSR(monCert.getSubjectX500Principal(), maCle));
		oos.writeObject(strCSR); 
		oos.flush();
		System.out.println("Demande de CSR");


		// Reception de la certification
		String pemcert = (String) ois.readObject(); 
		X509Certificate intermX509 = Certificat.pEMtoX509(pemcert);
		System.out.println("Réception de la certification de la clé publique");
		System.out.println(intermX509);

		// Fermeture des flux evolues et natifs
		ois.close();
		oos.close(); 
		NativeIn.close(); 
		NativeOut.close();

		// Fermeture de la connexion
		socket.close(); 
	}

}