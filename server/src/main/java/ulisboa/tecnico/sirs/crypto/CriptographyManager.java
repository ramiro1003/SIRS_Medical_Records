package ulisboa.tecnico.sirs.crypto;

import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class responsible for:
 * - manage keys
 * - cipher and decipher
 */
public class CriptographyManager {

	private static Key key;

	public CriptographyManager(){
		try {
			String keystorePwd = getKeystoreCredentials();
			
			InputStream keystoreStream = new FileInputStream("resources/keystore.jck"); 
			KeyStore keystore = KeyStore.getInstance("JCEKS"); 
			keystore.load(keystoreStream, keystorePwd.toCharArray()); 
			
			if (!keystore.containsAlias("sirsaes")) { 
				throw new RuntimeException("Alias for key not found"); 
			} 
			key = keystore.getKey("sirsaes", keystorePwd.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	private static String getKeystoreCredentials() {
		// Read server keystore credentials
		String path = "resources/cryptokeystore.txt";
		String keystorePwd = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String ksLine = reader.readLine();
			reader.close();
			
			keystorePwd = ksLine.split("=")[1];
			
		} catch (IOException e) {
			System.out.println("Exception: Could not read keystore credentials.");
			e.printStackTrace();
		}
		return keystorePwd;
	}
	
	
	
	/**
	 * Ciphers a file
	 * @param data string to be ciphered
	 * @throws Exception 
	 */
	public String cipher(String data) throws Exception  {

		try
		{
			//get iv
			IvParameterSpec iv = genInitializeVector();

			//cipher the string
			SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
			byte[] encryptedMessageInBytes = cipher.doFinal(data.getBytes());

			//join iv with encrypted message
			byte[] encryptedIVAndText = new byte[16 + encryptedMessageInBytes.length];
			System.arraycopy(iv.getIV(), 0, encryptedIVAndText, 0, 16);
			System.arraycopy(encryptedMessageInBytes, 0, encryptedIVAndText, 16, encryptedMessageInBytes.length);

			//get the final string
			String encMsg = Base64.getEncoder().encodeToString(encryptedIVAndText);
			return encMsg;
		} 
		catch (Exception e) 
		{
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	/**
	 * Deciphers the file
	 * @param data string to be deciphered
	 * @throws Exception 
	 */
	public String decipher(String data) throws Exception {

		try
		{
			byte[] cleanData = Base64.getDecoder().decode(data);
			// Extract IV.
			byte[] iv = new byte[16];
			System.arraycopy(cleanData, 0, iv, 0, iv.length);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Extract encrypted part.
			int encryptedSize = cleanData.length - 16;
			byte[] encryptedBytes = new byte[encryptedSize];
			System.arraycopy(cleanData, 16, encryptedBytes, 0, encryptedSize);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKeySpecification = new SecretKeySpec(key.getEncoded(), "AES"); 
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpecification, ivParameterSpec);
	        byte[] decrypted = cipher.doFinal(encryptedBytes);
			String origMessage = new String(decrypted); 
			return origMessage;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	private IvParameterSpec  genInitializeVector() {
		int ivSize = 16;
		byte[] iv = new byte[ivSize];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}

}
