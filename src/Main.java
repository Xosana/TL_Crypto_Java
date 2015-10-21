import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import equipment.Certificat;
import equipment.Equipement;

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Equipement equip1 = new Equipement("FirstEquip", 2008,true);
		//equip1.affichage();
		
		Equipement equip2 = new Equipement("SecondEquip", 2008,false);
		JcaPKCS10CertificationRequest req2 = Certificat.buildCSR(equip2.getSubject(), equip2.getKP());
		
		System.out.println(req2.getPublicKey());
		System.out.println(req2.getSubject());
		
		

		
		//equip2.affichage();
		
		equip1.setServeur();
		equip2.sendCSR();
		
		//Implémenter le serveur pour l'équipement
		//Chiffrer un certificat avec une clé publique
		//Envoyer ce chiffré
		//Décoder le chiffré
		//Equipement = Thread pour échanger des Certif?
		//Serial Number EQPT?
		
	}
}
