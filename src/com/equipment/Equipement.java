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

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;


public class Equipement {

	private String monNom; // Identité de l’équipement
	private int monPort; // Numéro de port d’écoute
	private KeyPair maCle; // Paire de clés de l’équipement
	private X509Certificate monCert; // Certificat auto-signé

	private HashMap<String, PublicKey> trustedKeys;
	private ArrayList<X509Certificate> ca;
	private ArrayList<X509Certificate> da;

	private ServerSocket initServerSocket; // Serveur d'écoute de reconnaissance mutuelle
	private ServerSocket serverSocket; // Serveur d'écoute de l'équipement
	private Socket socket;
	private InputStream NativeIn; // Flux natif entrant
	private ObjectInputStream ois; // Flux évolué entrant
	private OutputStream NativeOut; // Flux natif sortant
	private ObjectOutputStream oos; // Flux évolué sortant

	private static final int INIT_PORT = 7777; // Port de reconnaissance mutuelle
	private static final String HOST = "localHost";


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
		ca = new ArrayList<X509Certificate>();
		da = new ArrayList<X509Certificate>();

		// Initialisation des flux
		NativeIn = null; 
		ois = null; 
		NativeOut = null; 
		oos = null;

		Thread listeningThread;
		listeningThread = new Thread() {
			public void run() {
				try {
					serverSocket = new ServerSocket(monPort);

					while (true) {
						socket = serverSocket.accept();
						NativeIn = socket.getInputStream(); 
						ois = new ObjectInputStream(NativeIn); 
						NativeOut = socket.getOutputStream(); 
						oos = new ObjectOutputStream(NativeOut);

						ArrayList<String> pemCerts = (ArrayList<String>) ois.readObject();
						ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>() ;
						for (String pemCert: pemCerts) {
							certs.add(Certificat.pEMtoX509(pemCert));
						}
						synchroServer(certs);
					}
					//					ois.close();
					//					oos.close(); 
					//					NativeIn.close(); 
					//					NativeOut.close();
					//					serverSocket.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		listeningThread.start();

	}

	public void affichage_certs_issuer(ArrayList<X509Certificate> certs) {
		for (X509Certificate cert: certs) {
			System.out.println(Certificat.getIssuer(cert));
		}
	}

	public void affichage_da() {
		affichage_certs_issuer(da);
	}

	public void affichage_ca() {
		affichage_certs_issuer(ca);
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

						// Ajout l'équipement dans trustedKeys
						trustedKeys.put(csr.getSubject().getRDNs()[0].getFirst().getValue().toString(), csr.getPublicKey());

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
							initServerSocket.close();
						} catch (IOException e) {
							// Do nothing
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void askCSR() throws Exception{
		socket = new Socket(HOST, INIT_PORT); // Connection sur le port d'initialisation

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


		// Réception de la certification
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

		synchronisation();
	}

	public void synchronisation() throws Exception{
		ArrayList<String> cadaPEM = new ArrayList<String>();
		ArrayList<Integer> portList = new ArrayList<Integer>();
		for(X509Certificate c: ca){
			cadaPEM.add(Certificat.x509toPEM(c));
			portList.add(Certificat.getPort(c));
		}

		for(X509Certificate c: da){
			cadaPEM.add(Certificat.x509toPEM(c));
		}

		for(int i: portList){
			synchro_client(i, cadaPEM);
		}
	}

	public void synchro_client(int port, ArrayList<String> cadaPEM) throws UnknownHostException, IOException{
		socket = new Socket("localHost",port);

		// Création des flux natifs et évolués
		NativeOut = socket.getOutputStream(); 
		oos = new ObjectOutputStream(NativeOut); 

		// Envoi de la liste ca+da
		oos.writeObject(cadaPEM); 
		oos.flush();


		// Fermeture des flux evolues et natifs
		oos.close(); 
		NativeOut.close();

		// Fermeture de la connexion
		socket.close(); 
	}

	//We receive an array of certificates, we check if we already have them
	//then we verify them. If they match the criteria we add them to da
	public void synchroServer(final ArrayList<X509Certificate> certs) {
		new Thread() {
			public void run() {
				Boolean hasAddedNew = false;
				synchronized(da) {  //Synchronize during the update of da, to avoid concurrency issues
					for (X509Certificate cert: certs) {
						if (!da.contains(cert) && !ca.contains(cert)) {	
							String issuer = Certificat.getIssuer(cert);
							if (trustedKeys.containsKey(issuer)) {
								Boolean isVerified =  Certificat.verifX509(cert, trustedKeys.get(issuer));
								if (isVerified) {
									da.add(cert);
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