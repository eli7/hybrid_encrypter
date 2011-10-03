package test;

import static utils.ByteOperations.hexStringToByteArray;
import interfaces.BlockCipher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import core.Curupira1;

public class Curupira1Test {

    /**
     * Tests the Curupira-1 implementation and prints error messages if
     * validation errors occur.
     * 
     * @throws IOException
     */
    public static void test() throws IOException {
	System.out.println("Running 'Curupira1Test'...\n");

	testFile("TestVectors/JCurupira.Curupira1_testVector_96-bit key.txt",
		96);
	System.out.println();
	testFile("TestVectors/JCurupira.Curupira1_testVector_144-bit key.txt",
		144);
	System.out.println();
	testFile("TestVectors/JCurupira.Curupira1_testVector_192-bit key.txt",
		192);

	System.out.println("\nFinished");
    }

    private static void testFile(String fileName, int keyBits)
	    throws IOException {
	System.out.println("---> Test file: '" + fileName + "'");

	BlockCipher blockCipher = new Curupira1();
	ArrayList<HashMap<String, String>> testVectors = TestFileParser
		.parseTestFile(fileName);

	for (int i = 0; i < testVectors.size(); ++i) {
	    HashMap<String, String> vector = testVectors.get(i);

	    byte[] key = hexStringToByteArray(vector.get("key"));
	    byte[] plain = hexStringToByteArray(vector.get("plain"));
	    byte[] cipher = hexStringToByteArray(vector.get("cipher"));
	    byte[] decrypted = hexStringToByteArray(vector.get("decrypted"));

	    byte[] encryptedResult = new byte[plain.length];
	    byte[] decryptedResult = new byte[plain.length];

	    blockCipher.makeKey(key, keyBits);
	    blockCipher.encrypt(plain, encryptedResult);
	    if (!Arrays.equals(cipher, encryptedResult))
		error(vector.get("set"), vector.get("vector"));
	    blockCipher.decrypt(encryptedResult, decryptedResult);
	    if (!Arrays.equals(decrypted, decryptedResult))
		error(vector.get("set"), vector.get("vector"));

	    if (vector.containsKey("Iterated 1E2 times")) {
		byte[] iterated = hexStringToByteArray(vector
			.get("Iterated 1E2 times"));
		if (!testIteratedEncryption(blockCipher, keyBits, key, plain,
			iterated, 100))
		    error(vector.get("set"), vector.get("vector"));
	    }
	    if (vector.containsKey("Iterated 1E3 times")) {
		byte[] iterated = hexStringToByteArray(vector
			.get("Iterated 1E3 times"));
		if (!testIteratedEncryption(blockCipher, keyBits, key, plain,
			iterated, 1000))
		    error(vector.get("set"), vector.get("vector"));
	    }
	    if (vector.containsKey("Iterated 1E6 times")) {
		byte[] iterated = hexStringToByteArray(vector
			.get("Iterated 1E6 times"));
		if (!testIteratedEncryption(blockCipher, keyBits, key, plain,
			iterated, 1000000))
		    error(vector.get("set"), vector.get("vector"));
	    }
	}
	System.out.println("---> Done");
    }

    private static boolean testIteratedEncryption(BlockCipher blockCipher,
	    int keyBits, byte[] key, byte[] plain, byte[] iterated,
	    int iterations) {
	blockCipher.makeKey(key, keyBits);
	byte[] result = new byte[plain.length];
	byte[] temp = new byte[plain.length];
	System.arraycopy(plain, 0, temp, 0, plain.length);
	for (int i = 0; i < iterations; ++i) {
	    blockCipher.encrypt(temp, result);
	    System.arraycopy(result, 0, temp, 0, plain.length);
	}
	if (!Arrays.equals(iterated, result))
	    return false;
	return true;
    }

    private static void error(String set, String vector) {
	System.out.println("ERROR: set " + set + ", vector " + vector);
    }
}
