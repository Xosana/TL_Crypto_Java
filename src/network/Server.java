package network;
import java.io.*;
import java.net.*;

public class Server {

	ServerSocket serverSocket = null; 
	Socket NewServerSocket = null; 


	public Server(int monPort) throws IOException{
		
		serverSocket = new ServerSocket(monPort); // Creation de socket (TCP)
		Thread t = new Thread(new Accept_clients(serverSocket)); // Gestion des connexions par un thread
		t.start();

		/*// Arret du serveur
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("serverSocket closed trop tôt");
		}*/
	}
}