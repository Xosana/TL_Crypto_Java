
import java.util.Scanner;

import equipment.Equipement;

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Equipement equip1 = new Equipement("FirstEquip", 2008,true);
		equip1.affichage();
		Equipement equip2 = new Equipement("SecondEquip", 2008,false);
		equip2.affichage();

		System.out.println("Enter something here : ");
	    
		String name;
	       int age;
	       Scanner in = new Scanner(System.in);
	 
	       // Reads a single line from the console 
	       // and stores into name variable
	       name = in.nextLine();
	 
	       // Reads a integer from the console
	       // and stores into age variable
	       age=in.nextInt();
	       in.close();            
	 
	       // Prints name and age to the console
	       System.out.println("Name :"+name);
	       System.out.println("Age :"+age);
	}
}
