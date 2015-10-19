package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class Accept_clients implements Runnable {

	ServerSocket socketServer;
	Socket socket;
	InputStream NativeIn = null; 
	ObjectInputStream ois = null; 
	OutputStream NativeOut = null; 
	ObjectOutputStream oos = null;

	Accept_clients(ServerSocket s){
		socketServer = s;
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

				// Reception d’un String
				String res = (String) ois.readObject(); 
				System.out.println(res);

				// Emission d’un String
				oos.writeObject("Au revoir"); 
				oos.flush();

				// Fermeture des flux evolues et natifs
				ois.close();
				oos.close(); 
				NativeIn.close(); 
				NativeOut.close();


				socket.close();

			}
		} catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
	}

}
