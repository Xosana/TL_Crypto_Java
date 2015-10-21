package com.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import com.equipment.Certificat;
import com.equipment.Equipement;

public class Accept_clients implements Runnable {

	Equipement eqpt;
	ServerSocket socketServer;
	PrivateKey privKey;
	Socket socket;
	InputStream NativeIn = null; 
	ObjectInputStream ois = null; 
	OutputStream NativeOut = null; 
	ObjectOutputStream oos = null;

	public Accept_clients(ServerSocket s, PrivateKey k, Equipement eq){
		socketServer = s;
		privKey = k;
		eqpt = eq;
	}

	@Override
	public void run() {
		try {
			while(true){
				socket = socketServer.accept(); // Attente de connexions
				System.out.println("Un équipement est connecté !");

				// Creation des flux natifs et evolués
				NativeIn = socket.getInputStream(); 
				ois = new ObjectInputStream(NativeIn); 
				NativeOut = socket.getOutputStream(); 
				oos = new ObjectOutputStream(NativeOut);

				// Réception et vérification d’un CSR
				String pemCSR = (String) ois.readObject(); 
				try {
					JcaPKCS10CertificationRequest csr = Certificat.pEMtoCSR(pemCSR);
					if(Certificat.verifCSR(csr)){
						X509Certificate intermX509 = Certificat.cSRtoX509(X500Name.getInstance(eqpt.getX509().getIssuerX500Principal().getEncoded()), csr, privKey, 10);
						
						// Emission du certificat
						oos.writeObject(Certificat.x509toPEM(intermX509)); 
						oos.flush();
						//System.out.println(intermX509);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				// Fermeture des flux evolues et natifs
				ois.close();
				oos.close(); 
				NativeIn.close(); 
				NativeOut.close();


				socket.close();
				
				Thread.sleep(2000);
			}
		} catch (IOException | ClassNotFoundException | InterruptedException e) { e.printStackTrace(); }
		
		try {
			socketServer.close();
			System.out.println("Server.Close");
		} catch (IOException e) {
			// Do nothing
		}

	}

}
