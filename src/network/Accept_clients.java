package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import equipment.Certificat;
import equipment.Certificat_OLD;

public class Accept_clients implements Runnable {

	ServerSocket socketServer;
	PrivateKey caKey;
	Socket socket;
	InputStream NativeIn = null; 
	ObjectInputStream ois = null; 
	OutputStream NativeOut = null; 
	ObjectOutputStream oos = null;

	public Accept_clients(ServerSocket s, PrivateKey k){
		socketServer = s;
		caKey = k;
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
						X509Certificate intermX509 = Certificat.cSRtoX509(csr, caKey, 10);
						System.out.println(intermX509);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// Emission d’un String
				oos.writeObject("Au revoir"); 
				oos.flush();

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
		} catch (IOException e) {
			// Do nothing
		}

	}

}
