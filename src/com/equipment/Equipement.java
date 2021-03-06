package com.equipment;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;


public class Equipement {

	private String monNom; // Identité de l’équipement
	private int monPort; // Numéro de port d’écoute
	private KeyPair maCle; // Paire de clés de l’équipement
	private X509Certificate monCert; // Certificat auto-signé

	private HashMap<String, PublicKey> trustedKeys; // HashMap des équipements de confiance
	private ArrayList<X509Certificate> ca; // Ensemble des autorités de certification
	private ArrayList<X509Certificate> da; // Ensemble des autorités dérivées

	private Boolean running = true;
	private static final int INIT_PORT = 7777; // Port de reconnaissance mutuelle
	private static final String HOST = "localHost";
	public static Semaphore serverInitialized = new Semaphore(0);
	public static Semaphore hasTakenSynchroData = new Semaphore(0);
	public static Semaphore hasTakenCertificate = new Semaphore(0);

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
		monCert = Certificat.buildSelfCert(monNom, monPort, maCle, 10);
		Certificat.verifX509(monCert, maCle.getPublic());

		// Initialisation de CA et DA
		trustedKeys = new HashMap<String, PublicKey>();
		trustedKeys.put(monNom, maCle.getPublic());
		ca = new ArrayList<X509Certificate>();
		da = new ArrayList<X509Certificate>();

		// Initialisation du port d'écoute de l'équipement
		Thread listeningThread;
		listeningThread = new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					InputStream nativeIn = null; // Flux natif entrant
					ObjectInputStream ois = null; // Flux évolué entrant
					OutputStream nativeOut = null; // Flux natif sortant
					ObjectOutputStream oos = null; // Flux évolué sortant
					ServerSocket serverSocket = new ServerSocket(monPort);
					Socket socket = null;;
					while (running) { // Ecoute continue
						socket = serverSocket.accept();
						nativeIn = socket.getInputStream(); 
						ois = new ObjectInputStream(nativeIn); 
						nativeOut = socket.getOutputStream(); 
						oos = new ObjectOutputStream(nativeOut);
						ArrayList<String> pemCerts = (ArrayList<String>) ois.readObject();
//						hasTakenSynchroData.release();
						ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>() ;
						for (String pemCert: pemCerts) {
							certs.add(Certificat.pEMtoX509(pemCert));
						}
						synchroServer(certs);
					}

					ois.close();
					oos.close(); 
					nativeIn.close();
					nativeOut.close();
					socket.close();
					serverSocket.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		listeningThread.start();
	}

	// Gestion de l'écoute de l'équipement sur son port
	public void setTerminate() {
		running = false;
	}

	// Affichage des ensembles CA ou DA
	public void affichage_certs_issuer(ArrayList<X509Certificate> certs) {
		System.out.println("Nombre de certificats = " + certs.size());
		for (X509Certificate cert: certs) {
			String issuer = Certificat.getIssuer(cert);
			String subject = Certificat.getSubject(cert);
			System.out.println("Cert_" + issuer + "{Pub_" + subject + "}");
		}
	}

	public void affichage_da() {
		affichage_certs_issuer(da);
	}

	public void affichage_ca() {
		affichage_certs_issuer(ca);
	}

	// Affichage de l'ensemble des informations de l'équipement
	public void affichage() {
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

	// Initialisation de la reconnaissance mutuelle en tant que serveur
	public void initServer() throws IOException, ClassNotFoundException{

		new Thread() {
			public void run(){
				try {						
					ServerSocket initServerSocket = new ServerSocket(INIT_PORT); // Creation du ServerSocket sur un port spécifique aux initialisation
					serverInitialized.release(); // Gestion de la concurrence pour askCSR
					System.out.println("Initialisation de l'équipement "+monNom+" en tant que serveur");
					Socket socket = initServerSocket.accept();

					// Création des flux natifs et évolués
					OutputStream NativeOut = socket.getOutputStream(); 
					ObjectOutputStream oos = new ObjectOutputStream(NativeOut); 
					InputStream NativeIn = socket.getInputStream(); 
					ObjectInputStream ois = new ObjectInputStream(NativeIn);

					// Récupération du CSR
					String pemCSR = (String) ois.readObject(); 
					//					System.out.println("L'équipement "+monNom+" reçoit la demande");


					JcaPKCS10CertificationRequest csr = Certificat.pEMtoCSR(pemCSR);

					// Vérification du CSR
					if(Certificat.verifCSR(csr)){
						// Ajout l'équipement dans trustedKeys
						trustedKeys.put(csr.getSubject().getRDNs()[0].getFirst().getValue().toString(), csr.getPublicKey());

						// Certification de la clé publique
						X509Certificate intermX509 = Certificat.cSRtoX509(X500Name.getInstance(
								monCert.getIssuerX500Principal().getEncoded()), csr, maCle.getPrivate(), 10);

						// Emission du certificat
						oos.writeObject(Certificat.x509toPEM(intermX509)); 
						oos.flush();

						// Fermeture des flux evolues et natifs
						hasTakenCertificate.acquire();
						ois.close();
						oos.close(); 
						NativeIn.close(); 
						NativeOut.close();

						socket.close();
						initServerSocket.close();
						System.out.println("Socket serveur fermée par l'équipement " + getNom());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	// Demande de certification de la clé publique de l'équipement
	public void askCSR() throws Exception{
		System.out.println("Demande de connexion par l'équipement" + getNom());
		serverInitialized.acquire();	 //Attendre que l'équipement ait initialisé son "mode serveur"
		Socket socket = new Socket(HOST, INIT_PORT); // Connection sur le port d'initialisation

		// Création des flux natifs et évolués
		OutputStream NativeOut = socket.getOutputStream(); 
		ObjectOutputStream oos = new ObjectOutputStream(NativeOut); 
		InputStream NativeIn = socket.getInputStream(); 
		ObjectInputStream ois = new ObjectInputStream(NativeIn);

		// Création et envoi du CSR
		System.out.println("L'équipement "+monNom+" envoie la demande de certification de sa clé publique");
		String strCSR = Certificat.cSRtoPEM(Certificat.buildCSR(monCert.getSubjectX500Principal(), maCle));
		oos.writeObject(strCSR); 
		oos.flush();

		// Réception de la certification
		System.out.println("L'équipement "+monNom+" reçoie la certification de sa clé publique");
		String pemcert = (String) ois.readObject(); 
		X509Certificate intermX509 = Certificat.pEMtoX509(pemcert);

		// Fermeture des flux evolues et natifs
		hasTakenCertificate.release();
		ois.close();
		oos.close(); 
		NativeIn.close(); 
		NativeOut.close();
		socket.close();

		synchronized(ca) {
			ca.add(intermX509);
		}
	}
	
	// Initialisation de la synchronisation après la reconnaissance mutuelle
	// L'équipement ayant reçu la certification de sa clé envoie 
	// son CA et son DA à tous les équipements présents dans son CA -> Propagation de l'information
	public void synchronisation() throws Exception{
		ArrayList<String> cadaPEM = new ArrayList<String>(); // Union de CA et DA
		ArrayList<Integer> portList = new ArrayList<Integer>();
		System.out.println("L'équipement " + getNom() + " lance une synchronisation");
		synchronized (ca) {
			for(X509Certificate c: ca){
				cadaPEM.add(Certificat.x509toPEM(c));
				portList.add(Certificat.getPort(c));
			}
		}

		synchronized (da) {
			for(X509Certificate c: da){
				cadaPEM.add(Certificat.x509toPEM(c));
			}
		}

		for(int i: portList){
			synchro_client(i, cadaPEM); // Envoie de l'ensemble aux eqpts de CA
		}
	}

	// Envoie de l'ensemble CA U DA à l'équipement écoutant sur le port port
	public void synchro_client(int port, ArrayList<String> cadaPEM) throws UnknownHostException, IOException, InterruptedException{
		@SuppressWarnings("resource")
		Socket socket = new Socket(HOST, port);
		// Création des flux natifs et évolués
		OutputStream NativeOut = socket.getOutputStream(); 
		ObjectOutputStream oos = new ObjectOutputStream(NativeOut); 

		// Envoi de la liste ca+da
		oos.writeObject(cadaPEM); 
		oos.flush();
		// Fermeture des flux evolues et natifs
//		hasTakenSynchroData.acquire();
//		oos.close(); 
//		NativeOut.close();
//		socket.close(); 
	}

	//We receive an array of certificates, we check if we already have them
	//then we verify them. If they match the criteria we add them to da
	public void synchroServer(final ArrayList<X509Certificate> certs) {
		new Thread() {
			public void run() {
				Boolean hasAddedNew = false;
				for (X509Certificate cert: certs) {
					synchronized(da) {  //Synchronize during the update of da, to avoid concurrency issues
						if (!da.contains(cert) && !ca.contains(cert)) {	
							String issuer = Certificat.getIssuer(cert);
							if (trustedKeys.containsKey(issuer)) {
								Boolean isVerified =  Certificat.verifX509(cert, trustedKeys.get(issuer));
								if (isVerified) {
									da.add(cert);
									//Add the public keys of the subject in our trusted keys if we verified his certificate
									synchronized(trustedKeys) {
										String subject = Certificat.getSubject(cert);
										if (!trustedKeys.containsKey(subject)) {
											trustedKeys.put(subject, cert.getPublicKey());
										}
									}
									hasAddedNew = true;
								}
							}
						}
					}
				}
				//End condition for the synchronisation, when you're da is not updated you don't call synchronize anymore
				if (hasAddedNew) {
					try {
						synchronisation();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}