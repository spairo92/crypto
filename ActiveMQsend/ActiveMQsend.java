import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.crypto.*;
import javax.crypto.spec.*;

public class ActiveMQsend {
	
	static String IV = "AAAAAAAAAAAAAAAA";
	static String plaintext;
	static String encryptionKey = "0123456789abcdef";

	public static void main(String[] args) {
		QueueSender sender = new QueueSender("tcp://localhost:61616", "admin", "admin");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try 
		{
			while(true)
			{
				System.out.println("Write your msg, IT HAS TO BE 16 CHARACTERS (16bytes): ");
				//get what user types
				String plaintext = in.readLine();
				//encrypt msg
				byte[] cipher = encrypt(plaintext, encryptionKey);
				String str = new String(cipher);
				 
				//print encrypted msg
//				System.out.print("cipher:  ");
//				for (int i=0; i<cipher.length; i++)
//					System.out.print(new Integer(cipher[i]));
//				System.out.println("");
					
			    //send encrypted message
				sender.sendMessage("spyro's_queue", str );
				System.out.println();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	//method which AES encrypts messages
	public static byte[] encrypt(String plainText, String encryptionKey) throws Exception {
	    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
	    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
	    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    return cipher.doFinal(plainText.getBytes("UTF-8"));
	}	
}
