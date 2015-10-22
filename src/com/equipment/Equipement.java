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
import java.util.ArrayList;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;


public class Equipement {

	private String monNom; // Identité de l’équipement
	private int monPort; // Numéro de port d’écoute
	private KeyPair maCle; // Paire de clés de l’équipement
	private X509Certificate monCert; // Certificat auto-signé

	private ArrayList<X509Certificate> ca;
	private ArrayList<X509Certificate> da;

	private ServerSocket initServerSocket; // Serveur d'écoute de reconnaissance mutuelle
	private ServerSocket serverSocket; // Serveur d'écoute de l'équipement
	private Socket socket;
	private InputStream NativeIn; // Flux natif entrant
	private ObjectInputStream ois; // Flux évolué entrant
	private OutputStream NativeOut; // Flux natif sortant
	private ObjectOutputStream oos; // Flux évolué sortant
	
	private static final int INIT_PORT = 7777;


	public Equipement (String nom, int port) throws Exception {
		// Constructeur de l’equipement identifié par nom
		// et qui « écoutera » sur le port port.
		monNom = nom;
		monPort = port;
		//serverSocket = new ServerSocket(monPort); // Creation de socket (TCP)

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
		ca = new ArrayList<X509Certificate>();
		da = new ArrayList<X509Certificate>();

		// Initialisation du serveur d'écoute sur monPort
		serverSocket = new ServerSocket(monPort);
		
		// Initialisation des flux
		NativeIn = null; 
		ois = null; 
		NativeOut = null; 
		oos = null;
	}

	public void affichage_da() {
		for(X509Certificate x509: da ){
			System.out.println(x509.getSubjectX500Principal().toString());
		}
	}

	public void affichage_ca() {
		for(X509Certificate x509: ca ){
			System.out.println(x509.getIssuerX500Principal().toString());
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
		
		initServerSocket = new ServerSocket(INIT_PORT); // Creation du ServerSocket sur un port spécifique aux initialisations

		new Thread() {
			public void run(){
				try {
					socket = initServerSocket.accept();

					// Création des flux natifs et évolués
					NativeIn = socket.getInputStream(); 
					ois = new ObjectInputStream(NativeIn); 
					NativeOut = socket.getOutputStream(); 
					oos = new ObjectOutputStream(NativeOut);

					// Récupération du CSR
					String pemCSR = (String) ois.readObject(); 
					System.out.println("L'équipement "+monNom+" reçoit la demande");


					JcaPKCS10CertificationRequest csr = Certificat.pEMtoCSR(pemCSR);

					// Vérification du CSR
					if(Certificat.verifCSR(csr)){
						System.out.println("L'équipement "+monNom+" vérifie la demande avec succès");

						System.out.println("L'équipement "+monNom+" génère et envoie la certification de la clé publique");

						// Certification de la clé publique
						X509Certificate intermX509 = Certificat.cSRtoX509(X500Name.getInstance(
								monCert.getIssuerX500Principal().getEncoded()), csr, maCle.getPrivate(), 10);

						System.out.println("L'équipement "+monNom+" envoie le certificat");
						// Emission du certificat
						oos.writeObject(Certificat.x509toPEM(intermX509)); 
						oos.flush();
						
						// Fermeture des flux evolues et natifs
						ois.close();
						oos.close(); 
						NativeIn.close(); 
						NativeOut.close();
						
						try {
							serverSocket.close();
						} catch (IOException e) {
							// Do nothing
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void askCSR() throws Exception{
		socket = new Socket("localHost",7777); // Connection sur le port d'initialisation

		// Création des flux natifs et évolués
		NativeOut = socket.getOutputStream(); 
		oos = new ObjectOutputStream(NativeOut); 
		NativeIn = socket.getInputStream(); 
		ois = new ObjectInputStream(NativeIn);

		System.out.println("L'équipement "+monNom+" envoie la demande de certification de sa clé publique");
		
		// Création et envoi du CSR
		String strCSR = Certificat.cSRtoPEM(Certificat.buildCSR(monCert.getSubjectX500Principal(), maCle));
		oos.writeObject(strCSR); 
		oos.flush();


		// Reception de la certification
		String pemcert = (String) ois.readObject(); 
		X509Certificate intermX509 = Certificat.pEMtoX509(pemcert);
		
		System.out.println("L'équipement "+monNom+" reçoie la certification de sa clé publique");

		// Fermeture des flux evolues et natifs
		ois.close();
		oos.close(); 
		NativeIn.close(); 
		NativeOut.close();

		// Fermeture de la connexion
		socket.close(); 
		
		ca.add(intermX509);
	}

}