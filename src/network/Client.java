package network;
import java.io.*;
import java.net.*;

public class Client {
	int ServerPort;
	String ServerName;
	Socket clientSocket = null; 
	InputStream NativeIn = null; 
	ObjectInputStream ois = null; 
	OutputStream NativeOut = null; 
	ObjectOutputStream oos = null;

	public Client(String ServerName, int ServerPort) throws UnknownHostException, IOException, ClassNotFoundException{

		this.ServerName = ServerName;
		this.ServerPort = ServerPort;

		clientSocket = new Socket(ServerName,ServerPort); 

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
