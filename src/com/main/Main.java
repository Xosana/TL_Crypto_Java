package com.main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import com.equipment.Equipement;

public class Main {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static Scanner sc;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("Bienvenue dans la meilleur projet du monde!");

		sc = new Scanner(System.in);
		String message;
		String[] commands;
		String m;
		Boolean ctn = true;
		ArrayList<Equipement> equipements = new ArrayList<Equipement>();

		while(ctn){
			System.out.print("menu> ");
			message = sc.next();
			commands = message.split(" ");
			m = commands[0];

			if(m.equals("quit")) { // Quitter l'application
				System.out.println("Fermeture de l'application");
				ctn = false;
			} else if(m.equals("list")) { // Afficher la liste des équipements
				System.out.println("Liste des équipements de votre réseau domestique:");
				for (Equipement e: equipements) {
					System.out.println(e.getNom());
				}
			} else if(m.equals("add_equipment")){ // Ajouter un équipement
				System.out.println("Ajout d'un équipement à votre réseau domestique \n");
				Boolean wentTo = false;
				do {
					System.out.println("Entrer le nom de l'équipement (sans espace): ");
					m = sc.next().split(" ")[0];
					for (Equipement e: equipements) {
						if (e.getNom().equals(m)) {
							System.out.println("Cet équipement existe déjà, vous avez essayé de faire planter notre programme?");
							wentTo = true;
						}
					}
				} while (wentTo);
				System.out.println("Entrer le port écouté par l'équipement:");
				int port = sc.nextInt();
				Equipement e = new Equipement(m, port);
				String output = "Nouvel équipement " + m + " crée en écoute sur le port " + port; 
				System.out.println(output);
				equipements.add(e);
			} else if(m.equals("go_to")) {		//Aller dans le gui d'un équipement
				if (commands.length > 1) {
					Boolean wentTo = false;
					for (Equipement e: equipements) {
						if (e.getNom().equals(commands[1])) {
							startEquipementGui(e, equipements);
							wentTo = true;
						}
					}
					if (!wentTo) {
						System.out.println("Nom d'équipement inconnu");
					}
				} else {
					System.out.println("Utilisez 'go_to [nom d'un équipement]'");
				}
			} else if (m.equals("help")) {
				System.out.println("Commandes disponibles: quit, list, go_to [nom d'un équipement], add_equipment, help.");
			} else {
				System.out.println("Commande inconnue, utilisez 'help' pour plus d'informations");
			}
		}
	}

	public static void startEquipementGui(Equipement e, ArrayList<Equipement> equipements) {
		System.out.print("Bienvenue dans l'interface de ");
		System.out.println(e.getNom());
		Boolean ctn = true;
		String message;
		String m;
		String[] commands;

		while(ctn) {
			System.out.print(e.getNom() + "> ");
			message = sc.next();
			commands = message.split(" ");
			m = commands[0];

			if (m.equals("back")) {
				System.out.println("Retour au menu d'accueil");
				ctn = false;
			} else if (m.equals("help")) {
				System.out.println("Commandes disponibles: back, help, info, connect_to [nom d'un équipement], ca_list, da_list.");
			} else if (m.equals("info")) {
				e.affichage();
			} else if (m.equals("connect_to")) {
				if (commands.length > 1) {
					Boolean wentTo = false;
					for (Equipement e2: equipements) {
						if (e2.getNom().equals(commands[1])) {
							wentTo = true;
							if (e2 != e) {
								System.out.println("Intialisation de la procédure de connexion à l'équipement " + commands[1]);
							} else {
								System.out.println("Arrêtez de faire le malin!");
							}
						}
					}
					if (!wentTo) {
						System.out.println("Cet équipement est inconnu dans le réseau");
					}
				} else {
					System.out.println("Utilisez 'connect_to [nom d'un équipement]'");
				}
			} else if (m.equals("ca_list")) {
				System.out.println("Liste des certificats contenus dans CA: ");
			} else if (m.equals("da_list")) {
				System.out.println("Liste des certificats contenus dans DA: ");
			} else {
				System.out.println("Commande inconnue, utilisez 'help' pour plus d'informations");
			}
		}		
	}
}
