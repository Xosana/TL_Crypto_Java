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
		
		equip1.setServeur();
		equip2.sendCertificat();
		
	}
}
