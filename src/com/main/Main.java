package com.main;

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

	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		System.out.println("Welcome");

		Scanner sc = new Scanner(System.in);
		System.out.println("Entrer le nom de l'équipement :");
		String nom = sc.next();

		System.out.println("Entrer le port de l'équipement :");
		int port = sc.nextInt();

		Equipement eqpt = new Equipement(nom, port);

		//eqpt.initServer();
		eqpt.askCSR(2000);


		/*Equipement equip1 = new Equipement("FirstEquip", 2008,true);
		//equip1.affichage();

		Equipement equip2 = new Equipement("SecondEquip", 2008,false);

		System.out.println(equip1.id);
		System.out.println(equip2.id);




		//equip2.affichage();

		equip1.setServeur();
		equip2.sendCSR();

		System.out.println(equip1.getX509());
		System.out.println(equip2.getX509());

		//Implémenter le serveur pour l'équipement
		//Chiffrer un certificat avec une clé publique
		//Envoyer ce chiffré
		//Décoder le chiffré
		//Equipement = Thread pour échanger des Certif?
		 */

		/*HashMap<String, Integer> cache = new HashMap<String, Integer>();
		cache.put("Twenty One", 21);
		cache.put("Twenty Two", 22);

		System.out.println(cache.get("Twenty Two"));*/

	}


}
