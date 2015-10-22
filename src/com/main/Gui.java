package com.main;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

import com.equipment.Equipement;

public class Gui implements Runnable {

	// Couleur de texte
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private Console console;

	private HashMap<BigInteger, Equipement> eqpts;

	public Gui() throws Exception{
		console = System.console();
		eqpts = new HashMap<BigInteger, Equipement>();
	
		eqpts.put(Equipement.getBi().add(BigInteger.ONE), new Equipement("FirstEquip", 2008));
		eqpts.put(Equipement.getBi().add(BigInteger.ONE), new Equipement("SecondEquip", 2008));
	}


	@Override
	public void run() {
		//Format de txt rouge 
		//System.out.println(ANSI_RED + "This text is red!" + ANSI_RESET);

		clear();
		menuA();

		while(true){
			// IHM, prise en compte de la commande de l'utilisateur
			String cmd = console.readLine("Entrer une commande : ");

			if(cmd.compareTo("q")==0){ // Quitter l'application
				clear();
				System.out.println("Fermeture de l'application");
				System.exit(1);
			} else if(cmd.compareTo("a")==0){ // Retourner à l'accueil
				clear();
				menuA();
			} else if(cmd.compareTo("l")==0){ // Afficher la liste des équipements
				clear();
				System.out.println("Liste des équipements de votre réseau domestique \n");
				Iterator<BigInteger> keySetIterator = eqpts.keySet().iterator();
				while(keySetIterator.hasNext()){ 
					BigInteger key = keySetIterator.next(); 
					System.out.println(key +" "+eqpts.get(key).getNom()); 
				}
			} else if(cmd.compareTo("n")==0){ // Ajouter un équipement
				clear();
				System.out.println("Ajout d'un équipement à votre réseau domestique \n");

				String nom = console.readLine("Entrer le nom de l'équipement : ");
				
				try {
					eqpts.put(Equipement.getBi().add(BigInteger.ONE), new Equipement(nom, 2008));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println(Equipement.getBi() +" "+eqpts.get(Equipement.getBi()).getNom()+" a été créé"); 

			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////////


	public static void clear(){
		System.out.print("\033[H\033[2J");  
		System.out.flush();  
	}

	public static void menuA(){
		// Chargement du Menu
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader("resources/Menu"));
			String line = in.readLine();
			while(line != null)
			{
				System.out.println(line);
				line = in.readLine();
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
