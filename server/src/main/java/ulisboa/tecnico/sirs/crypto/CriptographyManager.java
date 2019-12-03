package ulisboa.tecnico.sirs.crypto;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import javax.crypto.Cipher;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;


/**
 * Class responsible for:
 * - manage keys
 * - cipher and decipher
 * - sign and validate
 */
public class CriptographyManager {

	private static PublicKey pubKey;
	private static PrivateKey priKey;

	public static void main(String[] s){
		try {

			InputStream ins = CriptographyManager.class.getResourceAsStream("keystore.jks");

			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(ins, "s3cr3t".toCharArray());   //Keystore password
			KeyStore.PasswordProtection keyPassword =       //Key password
					new KeyStore.PasswordProtection("s3cr3t".toCharArray());

			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("mykey", keyPassword);

			Certificate cert = keyStore.getCertificate("mykey");

			pubKey = cert.getPublicKey();
			priKey = privateKeyEntry.getPrivateKey();

		} catch (NoSuchAlgorithmException | CertificateException 
				| IOException | KeyStoreException | UnrecoverableEntryException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Ciphers a file
	 * @param data string to be ciphered
	 */
	public static String cipher(String data)  {

		String result = null;

		try {
			Cipher encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, pubKey);

			byte[] cipherText = encryptCipher.doFinal(data.getBytes(UTF_8));

			result = Base64.getEncoder().encodeToString(cipherText);
		} catch (Exception e) {

		}

		return result;

	}

	/**
	 * Deciphers the file
	 * @param data string to be deciphered
	 */
	public static String decipher(String data) {

		String result = null;

		try {
			byte[] bytes = Base64.getDecoder().decode(data);

			Cipher decriptCipher = Cipher.getInstance("RSA");
			decriptCipher.init(Cipher.DECRYPT_MODE, priKey);

			result =  new String(decriptCipher.doFinal(bytes), UTF_8);
		} catch (Exception e) {

		}

		return result;
	}

}
