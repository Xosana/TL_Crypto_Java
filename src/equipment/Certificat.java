package equipment;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.cert.*;
import org.bouncycastle.cert.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public class Certificat {

	private static  BigInteger serialNumber = BigInteger.ZERO;

	/**
	 * Build a sample V1 certificate to use as a CA root certificate
	 * @param keyPair
	 * @param validityDays
	 * @return X509Certificate
	 * @throws Exception
	 */
	public static X509Certificate buildSelfCert(String name, KeyPair keyPair, int validityDays)
			throws Exception
	{
		Security.addProvider(new BouncyCastleProvider());
		serialNumber = serialNumber.add(BigInteger.ONE); // Numéro de série du certificat

		X509v1CertificateBuilder certBldr = new JcaX509v1CertificateBuilder(
				new X500Principal("CN="+name),
				serialNumber,
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + 1000*60*60*24*validityDays),
				new X500Principal("CN="+name+"'s Root Certificate"),
				keyPair.getPublic());
		ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA")
				.setProvider("BC").build(keyPair.getPrivate());
		return new JcaX509CertificateConverter().setProvider("BC")
				.getCertificate(certBldr.build(signer));
	}

	/**
	 * Méthode de conversion CSR -> X509
	 * @param csr
	 * @param caKey
	 * @param validityDays
	 * @return X509Certificate
	 * @throws Exception
	 */
	public static X509Certificate cSRtoX509(
			JcaPKCS10CertificationRequest csr, PrivateKey caKey, int validityDays)
					throws Exception
	{
		Security.addProvider(new BouncyCastleProvider());
		serialNumber = serialNumber.add(BigInteger.ONE); // Numéro de série du certificat

		X509v3CertificateBuilder certBldr = new JcaX509v3CertificateBuilder(
				csr.getSubject(),
				serialNumber,
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + 1000*60*60*24*validityDays),
				csr.getSubject(),
				csr.getPublicKey());
		ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA")
				.setProvider("BC").build(caKey);
		return new JcaX509CertificateConverter().setProvider("BC")
				.getCertificate(certBldr.build(signer));
	}

	/**
	 * Méthode de vérification du certificat
	 * @param pubkey
	 * @return boolean
	 */
	public static boolean verifX509(X509Certificate x509, PublicKey pubkey) {
		// Vérification de la signature du certificat à l’aide
		// de la clé publique passée en argument
		try {
			x509.checkValidity(new Date());
			x509.verify(pubkey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** 
	 * Méthode d'encodage X509 -> PEM
	 * @param c
	 * @return String(X509)
	 * @throws Exception
	 */
	public static String x509toPEM(X509Certificate c) throws Exception {
		StringWriter sw = new StringWriter();
		JcaPEMWriter pw = new JcaPEMWriter(sw);
		pw.writeObject(c); 
		pw.flush();
		pw.close();
		return sw.toString();
	}

	/** 
	 * Méthode de décodage PEM -> X509
	 * @param pemcert
	 * @return X509Certificate
	 * @throws Exception
	 */
	public static X509Certificate pEMtoX509(String pemcert) throws Exception {
		StringReader sr = new StringReader(pemcert);
		PEMParser parser = new PEMParser(sr);
		X509CertificateHolder certH = (X509CertificateHolder) parser.readObject();
		parser.close();
		return new JcaX509CertificateConverter()
				.setProvider("BC")
				.getCertificate(certH); 
	}

	/////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Build a CSR
	 * @param X500Principal
	 * @param keyPair
	 * @return JcaPKCS10CertificationRequest
	 * @throws OperatorCreationException
	 * @throws PKCSException
	 */
	public static JcaPKCS10CertificationRequest buildCSR(X500Principal subject, KeyPair keyPair) throws OperatorCreationException, PKCSException{
		Security.addProvider(new BouncyCastleProvider());
		JcaPKCS10CertificationRequestBuilder requestBuilder =
				new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
		/*req.isSignatureValid(new JcaContentVerifierProviderBuilder()
				.setProvider("BC").build(keyPair.getPublic()));*/
		return new JcaPKCS10CertificationRequest( requestBuilder.build(
				new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC")
				.build(keyPair.getPrivate())));
	}

	/**
	 * Méthode de vérification d'un CSR
	 * @param JcaPKCS10CertificationRequest
	 * @return Boolean
	 */
	public static boolean verifCSR(JcaPKCS10CertificationRequest csr){
		try {
			csr.isSignatureValid(new JcaContentVerifierProviderBuilder()
					.setProvider("BC").build(csr.getPublicKey()));
			return true;
		} catch (InvalidKeyException | OperatorCreationException | NoSuchAlgorithmException | PKCSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/** Méthode d'encodage CSR -> PEM
	 * @param PKCS10CertificationRequest
	 * @return String(PKCS10CertificationRequest)
	 * @throws Exception
	 */
	public static String cSRtoPEM(JcaPKCS10CertificationRequest c) throws Exception {
		StringWriter sw = new StringWriter();
		JcaPEMWriter pw = new JcaPEMWriter(sw);
		pw.writeObject(c); 
		pw.flush();
		pw.close();
		return sw.toString();
	}

	/** 
	 * Méthode de décodage PEM -> CSR
	 * @param String PEMCertificate
	 * @return PKCS10CertificationRequest
	 * @throws Exception
	 */
	public static JcaPKCS10CertificationRequest pEMtoCSR(String pemCSR) throws Exception {
		StringReader sr = new StringReader(pemCSR);
		PEMParser parser = new PEMParser(sr);
		JcaPKCS10CertificationRequest certReq = (JcaPKCS10CertificationRequest) parser.readObject();
		parser.close();
		return certReq; 
	}




}