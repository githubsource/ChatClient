package com.socket;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

/**
 * @author JavaDigest
 *
 */
public class EncryptionUtil {

    /**
     * String to hold name of the encryption algorithm.
     */
    public static final String ALGORITHM = "RSA";

    /**
     * String to hold the name of the private key file.
     */
    public static final String PRIVATE_KEY_FILE = "/Users/quynhnguyen/Documents/WORKING/ChatJava/key/private.key";

    /**
     * String to hold name of the public key file.
     */
    public static final String PUBLIC_KEY_FILE = "/Users/quynhnguyen/Documents/WORKING/ChatJava/key/public.key";

    /**
     * Generate key which contains a pair of private and public key using 1024
     * bytes. Store the set of keys in Prvate.key and Public.key files.
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void generateKey() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File(PRIVATE_KEY_FILE);
            File publicKeyFile = new File(PUBLIC_KEY_FILE);

            // Create files to store public and private key
            if (privateKeyFile.getParentFile() != null) {
                privateKeyFile.getParentFile().mkdirs();
            }
            privateKeyFile.createNewFile();

            if (publicKeyFile.getParentFile() != null) {
                publicKeyFile.getParentFile().mkdirs();
            }
            publicKeyFile.createNewFile();

            // Saving the Public key in a file
            ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            publicKeyOS.writeObject(key.getPublic());
            publicKeyOS.close();

            // Saving the Private key in a file
            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            privateKeyOS.writeObject(key.getPrivate());
            privateKeyOS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * The method checks if the pair of public and private key has been
     * generated.
     *
     * @return flag indicating if the pair of keys were generated.
     */
    public static boolean areKeysPresent() {

        File privateKey = new File(PRIVATE_KEY_FILE);
        File publicKey = new File(PUBLIC_KEY_FILE);

        if (privateKey.exists() && publicKey.exists()) {
            return true;
        }
        return false;
    }

    /**
     * Encrypt the plain text using public key.
     *
     * @param origin : original plain text
     * @param key :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static byte[] encrypt(byte[] origin, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(origin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }
    
    /**
     * Decrypt text using private key.
     *
     * @param text :encrypted text
     * @param key :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static byte[] decrypt(byte[] cipherBytes, PrivateKey key) {
        byte[] dectyptedText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(cipherBytes);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return dectyptedText;
    }
  
    /**
     * 
     * @param file: origin file
     * @return bytes of file
     */
    public static byte[] fileToBytes(File file) {
 
        FileInputStream fis = null;
        byte[] bytes = null;
        try {
            //File file = new File("java.pdf");
            fis = new FileInputStream(file);
            //System.out.println(file.exists() + "!!");
            //InputStream in = resource.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum); //no doubt here is 0
                    //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
//                    System.out.println("read " + readNum + " bytes,");
                }
            } catch (IOException ex) {
                Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            bytes = bos.toByteArray();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return bytes;
    }
    
    /**
     * 
     * @param bytes cipher bytes
     * @param filePath file to save decrypted bytes
     * @return file
     * @throws IOException 
     */
    public static File bytesToFile(final byte[] bytes, final String filePath) throws IOException {
        FileOutputStream fos = null;
        File someFile = null;
        try {
            //below is the different part
            someFile = new File(filePath);
            fos = new FileOutputStream(someFile);
            fos.write(bytes);
            fos.flush();
            fos.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return someFile;
    }
    
    public static byte[] plainFileToCipherBy(File f, String publicKey) {
        publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqxeo194EkZa2GHqw78A7nerPxLqEr6hn4O7sC\n" +
"cdoId0ZwfXm/TCoRuDEGtG1xKe2POJkaMmT8V6EQbVktkfj2qinpaRncIjAIG2rwH3Gg3qhcV5Cx\n" +
"WHeLnICvvpfHwcmyGIO8jGoGnxM2vyEP3A+RjdlMUHWwjkk2spuBApCwVQIDAQAB";
        try {
            final byte[] oringinBytes =  fileToBytes(f);
            PublicKey pubSaved = CipherUtil.loadPublicKey(publicKey);
            final byte[] cipherBytes = encrypt(oringinBytes, pubSaved);
            
            return cipherBytes;
          //  return cipherFile;
        } catch (Exception ex) {
            Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    /**
     * Test the EncryptionUtil
     */
    public static void amain(String[] args) {
    	ObjectInputStream inputStream = null;
        try {

            // Check if the pair of keys are present else generate those.
          //  if (!areKeysPresent()) {
        // Method generates a pair of keys using the RSA algorithm and stores it
                // in their respective files
            //    generateKey();
           // }
            
        	KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      	    KeyPair pair = gen.generateKeyPair();

      	    String pubKey = CipherUtil.savePublicKey(pair.getPublic());
      	    PublicKey pubSaved = CipherUtil.loadPublicKey(pubKey);
      	    System.out.println(pair.getPublic()+"\n"+pubSaved);
      	    
      	    System.out.println("======================00000============");
      	    
      	    String privKey = CipherUtil.savePrivateKey(pair.getPrivate());
      	    PrivateKey privSaved = CipherUtil.loadPrivateKey(privKey);
      	    System.out.println(pair.getPrivate()+"\n"+privSaved);

            // Encrypt the string using the public key
           // inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
           // final PublicKey publicKey = (PublicKey) inputStream.readObject();
            
           // inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
           // final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
            
            System.out.println("***** Encrypt string *******");
            final String originalText = "Text to be encrypted ";
            final byte[] cipherText = encrypt(originalText.getBytes(), pubSaved);

            // Decrypt the cipher text using the private key.            
            final byte[] plainText = decrypt(cipherText, privSaved);

            // Printing the Original, Encrypted and Decrypted Text
            System.out.println("Original: " + originalText);
            System.out.println("Encrypted: " + cipherText.toString());
            System.out.println("Decrypted: " + new String(plainText));
            
            System.out.println("***** Encrypt File *******");
            File oringinFile = new File("/Users/tranngocdien/Documents/java project/file/key/abc_origin.txt");
            final byte[] oringinBytes =  fileToBytes(oringinFile);
            final byte[] cipherBytes = encrypt(oringinBytes, pubSaved);
            final byte[] plainBytes = decrypt(cipherBytes, privSaved);
            
            File cipherFile = bytesToFile(cipherBytes, "/Users/tranngocdien/Documents/java project/file/key/abc_cipher.txt");

            File plainFile = bytesToFile(plainBytes, "/Users/tranngocdien/Documents/java project/file/key/abc_plain.txt");
            
            System.out.println("Original: " + new String(oringinBytes));
            System.out.println("Encrypted: " + cipherBytes.toString());
            System.out.println("Decrypted: " + new String(plainBytes));
            
        } catch (Exception e) {
        	try {
        		inputStream.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
            e.printStackTrace();
        }
    }
    
    public static String cipherTxt2PlainTxt(String cipherTxt, String privateKey) {
        try {
            PrivateKey privSaved = CipherUtil.loadPrivateKey(privateKey);
            final byte[] plainBytes = decrypt(cipherTxt.getBytes(), privSaved);
            return plainBytes.toString();
        } catch (Exception e) {
        }
        
        return cipherTxt;
    }
    
       public static String plainTxt2CipherTxt(String text, String publicKey) throws IOException, GeneralSecurityException {
        String ret = "";
        
        PublicKey pubSaved = CipherUtil.loadPublicKey(publicKey);
        final byte[] cipherText = encrypt(text.getBytes(), pubSaved);
        return cipherText.toString();
    }
}
