import javax.crypto.*;
import java.security.*;
import javax.crypto.spec.*;
import java.util.Random;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

class BruteForceDES implements Runnable {
	private long startKey;
	private long endKey;
	private long startTime;
	private SealedObject sldObj;
	private int thread_id;
    private String plainstr;

	// Cipher for the class
	Cipher des_cipher;
	
	// Key for the class
	SecretKeySpec the_key = null;

	// Byte arrays that hold key block
	byte[] deskeyIN = new byte[8];
	byte[] deskeyOUT = new byte[8];
		
	// Constructor: initialize the cipher
	public BruteForceDES () {
		try {
			des_cipher = Cipher.getInstance("DES");
		} 
		catch ( Exception e ) {
			System.out.println("Failed to create cipher.  Exception: " + e.toString()+ " Message: " + e.getMessage()) ; 
		}
	}
	
	// constructor
	public BruteForceDES (int id, long startKey, long endKey, long startTime, SealedObject sldObj, String plainstr) {
		try {
			des_cipher = Cipher.getInstance("DES");
		} 
		catch ( Exception e ) {
			System.out.println("Failed to create cipher.  Exception: " + e.toString()+" Message: " + e.getMessage()); 
		}
		// Initialize the private variables and sealed object
		this.thread_id = id;
		this.startKey = startKey;
		this.endKey = endKey;
		this.startTime = startTime;
		this.sldObj = sldObj;
        this.plainstr = plainstr;
	}

	public void run() {
		// Search for the right key
		for ( long i = startKey; i < endKey; i++ ) {
			// Set the key and decipher the object
			this.setKey ( i );
			String decryptstr = this.decrypt ( sldObj );
			
			// Does the object contain the known plaintext
			if (( decryptstr != null ) && ( decryptstr.contains(this.plainstr))) {
				//  Remote printlns if running for time.
				System.out.println ( "Decrypted string: " + decryptstr );
			}
		}
	}

	// Decrypt the SealedObject
	//
	//   arguments: SealedObject that holds on encrypted String
	//   returns: plaintext String or null if a decryption error
	//     This function will often return null when using an incorrect key.
	//
	public String decrypt ( SealedObject cipherObj ) {
		try {
			return (String)cipherObj.getObject(the_key);
		}
		catch ( Exception e ) {
			//      System.out.println("Failed to decrypt message. " + ". Exception: " + e.toString()  + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}
	
	// Encrypt the message
	//
	//  arguments: a String to be encrypted
	//  returns: a SealedObject containing the encrypted string
	//
	public SealedObject encrypt ( String plainstr ) {
		try {
			des_cipher.init ( Cipher.ENCRYPT_MODE, the_key );
			return new SealedObject( plainstr, des_cipher );
		}
		catch ( Exception e ) {
			System.out.println("Failed to encrypt message. " + plainstr +
							   ". Exception: " + e.toString() + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}
	
	//  Build a DES formatted key
	//
	//  Convert an array of 7 bytes into an array of 8 bytes.
	//
	private static void makeDESKey(byte[] in, byte[] out) {
        out[0] = (byte) ((in[0] >> 1) & 0xff);
        out[1] = (byte) ((((in[0] & 0x01) << 6) | (((in[1] & 0xff)>>2) & 0xff)) & 0xff);
        out[2] = (byte) ((((in[1] & 0x03) << 5) | (((in[2] & 0xff)>>3) & 0xff)) & 0xff);
        out[3] = (byte) ((((in[2] & 0x07) << 4) | (((in[3] & 0xff)>>4) & 0xff)) & 0xff);
        out[4] = (byte) ((((in[3] & 0x0F) << 3) | (((in[4] & 0xff)>>5) & 0xff)) & 0xff);
        out[5] = (byte) ((((in[4] & 0x1F) << 2) | (((in[5] & 0xff)>>6) & 0xff)) & 0xff);
        out[6] = (byte) ((((in[5] & 0x3F) << 1) | (((in[6] & 0xff)>>7) & 0xff)) & 0xff);
        out[7] = (byte) (   in[6] & 0x7F);
		
        for (int i = 0; i < 8; i++) {
          out[i] = (byte) (out[i] << 1);
        }
    }

	// Set the key (convert from a long integer)
	public void setKey ( long theKey ) {
		try {
			// convert the integer to the 8 bytes required of keys
			deskeyIN[0] = (byte) (theKey        & 0xFF );
			deskeyIN[1] = (byte)((theKey >>  8) & 0xFF );
			deskeyIN[2] = (byte)((theKey >> 16) & 0xFF );
			deskeyIN[3] = (byte)((theKey >> 24) & 0xFF );
			deskeyIN[4] = (byte)((theKey >> 32) & 0xFF );
			deskeyIN[5] = (byte)((theKey >> 40) & 0xFF );
			deskeyIN[6] = (byte)((theKey >> 48) & 0xFF );

			// theKey should never be larger than 56-bits, so this should always be 0
			deskeyIN[7] = (byte)((theKey >> 56) & 0xFF );
			
			// turn the 56-bits into a proper 64-bit DES key
			makeDESKey(deskeyIN, deskeyOUT);
			
			// Create the specific key for DES
			the_key = new SecretKeySpec ( deskeyOUT, "DES" );
		}
		catch ( Exception e ) {
			System.out.println("Failed to assign key" +  theKey +
							   ". Exception: " + e.toString() + ". Message: " + e.getMessage()) ;
		}
	}
	
	
	// Program demonstrating how to create a random key and then search for the key value.
	public static void main ( String[] args ) throws IOException {
		if ( 3 != args.length ) {
			System.out.println ("Usage: java BruteForceDES #threads key_size_in_bits <filename>");
			return;
		}
		//store follwing arguments: threads and key size in bits 
		int numThreads = Integer.parseInt(args[0]);
		long keybits = Long.parseLong(args[1]);

	    long maxkey = ~(0L);
	    maxkey = maxkey >>> (64 - keybits);

		// Create a simple cipher
		BruteForceDES enccipher = new BruteForceDES ();
		
		// Get a number between 0 and 2^64 - 1
		Random generator = new Random ();
		long key = generator.nextLong();
		
		// Mask off the high bits so we get a short key
		key = key & maxkey;
		
		// Set up a key
		enccipher.setKey ( key ); 
		
		// Get the filename
        String filename = args[2];
        // Read in the file to encrypt
        File inputFile = new File(filename);

        // Turn it into a string
        if (!inputFile.exists()) {
            System.err.println("error: Input file not found.");
            System.exit(1);
        }
        byte[] encoded = Files.readAllBytes(Paths.get(filename));

        String plainstr = new String(encoded, StandardCharsets.US_ASCII);
		
		// Encrypt. Distribute the encrypted object to all threads
		SealedObject sldObj = enccipher.encrypt ( plainstr );
		
		// Here ends the set-up.  Pretending like we know nothing except sldObj,
		// discover what key was used to encrypt the message.
		
        // key bits per thread
		long keysPerThread = maxkey / (long) numThreads; 
		long remainderKeys = maxkey / (long) numThreads + maxkey % (long) numThreads;
        
		// Get and store the current time -- for timing
		long runstart = System.currentTimeMillis();
		
		// Store thread objects
		Thread[] threads = new Thread[numThreads];

		// Create and start new BruteForceDES threads
		for(int i = 0; i < numThreads; i++) {
            long start = i * keysPerThread;
            
			if(i == numThreads - 1) {
				keysPerThread = remainderKeys;
			}
			
			long end = start + keysPerThread;
			
			threads[i] = new Thread(new BruteForceDES(i, start, end, runstart, sldObj, plainstr));
			threads[i].start();
		}

		// Join all threads
		for(int j = 0; j < numThreads; j++) {
			try {
				threads[j].join();
			}
			catch(InterruptedException e)
			{
				System.out.println("Thread interrupted.  Exception: " + e.toString()+" Message: " + e.getMessage()) ;
        		return;
			}
		}
		
		// Output search time
		long elapsed = System.currentTimeMillis() - runstart;
        System.out.println ( "Keys searched: " + (maxkey+1) + " in " + elapsed + "ms");
	}
}

