import java.io.Console;

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

		Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        
        String login = c.readLine("Enter your login: ");
	}
}
