package ulisboa.tecnico.sirs.crypto;

import java.security.Key;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.BaseEncoding;

import java.io.FileInputStream;
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

			InputStream keystoreStream = new FileInputStream("resources/keystore.jck"); 
			KeyStore keystore = KeyStore.getInstance("JCEKS"); 
			keystore.load(keystoreStream, "sirssirs".toCharArray()); 
			if (!keystore.containsAlias("sirsaes")) { 
			 throw new RuntimeException("Alias for key not found"); 
			} 
			key = keystore.getKey("sirsaes", "sirssirs".toCharArray());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Ciphers a file
	 * @param data string to be ciphered
	 * @throws Exception 
	 */
	public String cipher(String data) throws Exception  {

        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpecification = new SecretKeySpec(key.getEncoded(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpecification);
            byte[] encryptedMessageInBytes = cipher.doFinal(data.getBytes("UTF-8"));
            String encMsg = BaseEncoding.base64().encode(encryptedMessageInBytes);
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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpecification = new SecretKeySpec(key.getEncoded(), "AES"); 
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpecification);
            byte[] encryptedTextBytes = BaseEncoding.base64().decode(data); 
            byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes); 
            String origMessage = new String(decryptedTextBytes); 
            return origMessage;
        } 
        catch (Exception e) 
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
	}

}
