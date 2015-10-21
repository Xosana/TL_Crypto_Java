package equipment;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;




public class Certificat_OLD {

	static private BigInteger serialNumber = BigInteger.ZERO;

	public X509Certificate x509;

	// Constructeur d'un certificat auto-signé
	Certificat_OLD(String nom, KeyPair cle, int validityDays) {

		Date startDate = new Date(System.currentTimeMillis()); // La validité du certificat est comptée à partir de sa date de création
		Date expiryDate = new Date(System.currentTimeMillis() + 1000*60*60*24*validityDays ); // Date d'expiration du certificat

		serialNumber = serialNumber.add(BigInteger.ONE); // Numéro de série du certificat

		X500Principal subjectName = new X500Principal("CN = "+nom); // Nom de l'équipement

		X509v1CertificateBuilder certGen = new JcaX509v1CertificateBuilder(subjectName, serialNumber, startDate, expiryDate, subjectName, cle.getPublic());

		ContentSigner sigGen = null;

		try {
			sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(cle.getPrivate());
			x509 = new JcaX509CertificateConverter().getCertificate(certGen.build(sigGen));
			x509.checkValidity(new Date());
			x509.verify(x509.getPublicKey());
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException 
				| NoSuchProviderException | SignatureException 
				| OperatorCreationException e) { e.printStackTrace(); }

	}


	// 2ème constructeur permettant de convertir le type x509certificate en type Certificat
	public Certificat_OLD(X509Certificate certificate) {
		x509 = certificate; 
	}

	public boolean verifCertif(PublicKey pubkey) {
		// Vérification de la signature du certificat à l’aide
		// de la clé publique passée en argument
		try {
			x509.verify(pubkey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) { e.printStackTrace();
				return false;
		}
		return true;
	}

	public static String encodePEM(Certificat_OLD c) throws Exception {
		StringWriter sw = new StringWriter();
		JcaPEMWriter pw = new JcaPEMWriter(sw);
		pw.writeObject(c.x509); 
		pw.flush();
		pw.close();
		return sw.toString();
	}

	public static Certificat_OLD decodePEM(String pemcert) throws Exception {
		StringReader sr = new StringReader(pemcert);
		PEMParser parser = new PEMParser(sr);
		X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
		parser.close();

		JcaX509CertificateConverter conv = new JcaX509CertificateConverter();
		return new Certificat_OLD(conv.getCertificate(holder)); 
	}

	public X509Certificate getX509(){
		return x509;
	}
	
	

}