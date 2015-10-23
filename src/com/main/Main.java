package com.main;

import java.util.ArrayList;
import java.util.Scanner;

import com.equipment.Equipement;

public class Main {

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
			message = sc.nextLine();
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
						if (e.getNom().compareTo(commands[1]) == 0) {
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
				System.out.println("Commandes disponibles: quit, list, go_to [nom d'un équipement], add_equipment, help, demo.");
			} else if (m.equals("demo")) {
				Equipement e1 = new Equipement("e1", 11111);
				Equipement e2 = new Equipement("e2", 22222);
				Equipement e3 = new Equipement("e3", 33333);
				Equipement e4 = new Equipement("e4", 44444);
				equipements.add(e1);
				equipements.add(e2);
				equipements.add(e3);
				equipements.add(e4);
				System.out.println("Création de l'équipement e1 ayant pour port 11111");
				System.out.println("Création de l'équipement e2 ayant pour port 22222");
				System.out.println("Création de l'équipement e3 ayant pour port 33333");
				System.out.println("Création de l'équipement e4 ayant pour port 44444");
				connect_equipments(e1, e2);
			} else {
				System.out.println("Commande inconnue, utilisez 'help' pour plus d'informations");
			}
		}
		
		for (Equipement e: equipements) {
			e.setTerminate();
		}
	}

	public static void startEquipementGui(Equipement e, ArrayList<Equipement> equipements) throws Exception {
		System.out.print("Bienvenue dans l'interface de ");
		System.out.println(e.getNom());
		Boolean ctn = true;
		String message;
		String m;
		String[] commands;

		while(ctn) {
			System.out.print(e.getNom() + "> ");
			message = sc.nextLine();
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
								connect_equipments(e, e2);
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
				e.affichage_ca();
			} else if (m.equals("da_list")) {
				System.out.println("Liste des certificats contenus dans DA: ");
				e.affichage_da();
			} else {
				System.out.println("Commande inconnue, utilisez 'help' pour plus d'informations");
			}
		}		
	}

	public static void connect_equipments(Equipement e1, Equipement e2) throws Exception {
		System.out.println("Reconnaissance mutuelle de l'équipement " + e1.getNom() +
				" et de l'équipement " + e2.getNom());
		e2.initServer();
		e1.askCSR();
		e1.initServer();
		e2.askCSR();
		e1.synchronisation();
	}
}
