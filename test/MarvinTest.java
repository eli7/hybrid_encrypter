package test;

import static utils.ByteOperations.hexStringToByteArray;
import interfaces.BlockCipher;
import interfaces.MAC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import core.Curupira1;
import core.Marvin;

public class MarvinTest {

    /**
     * Tests the Marvin implementation and prints error messages if validation
     * errors occur.
     * 
     * @throws IOException
     */
    public static void test() throws IOException {
	System.out.println("Running 'MarvinTest'...\n");

	testFile("TestVectors/JCurupira.Marvin(Curupira1)_testVector_0.txt");
	System.out.println();
	testFile("TestVectors/JCurupira.Marvin(Curupira1)_testVector_1.txt");

	System.out.println("\nFinished");
    }

    private static void testFile(String fileName) throws IOException {
	System.out.println("---> Test file: '" + fileName + "'");

	BlockCipher blockCipher = new Curupira1();
	MAC mac = new Marvin(false);
	mac.setCipher(blockCipher);

	ArrayList<HashMap<String, String>> testVectors = TestFileParser
		.parseTestFile(fileName);

	for (int i = 0; i < testVectors.size(); ++i) {
	    HashMap<String, String> vector = testVectors.get(i);

	    byte[] key = hexStringToByteArray(vector.get("key"));
	    byte[] msg = getMessage(vector);
	    byte[] tag04 = hexStringToByteArray(vector.get("tag (04)"));
	    byte[] tag08 = hexStringToByteArray(vector.get("tag (08)"));
	    byte[] tag12 = hexStringToByteArray(vector.get("tag (12)"));

	    mac.setKey(key, key.length * 8);
	    mac.init();
	    mac.update(msg, msg.length);

	    byte[] tag04result = mac.getTag(null, 32);
	    byte[] tag08result = mac.getTag(null, 64);
	    byte[] tag12result = mac.getTag(null, 96);

	    if (!Arrays.equals(tag04, tag04result))
		error(vector.get("set"), vector.get("vector"));
	    if (!Arrays.equals(tag08, tag08result))
		error(vector.get("set"), vector.get("vector"));
	    if (!Arrays.equals(tag12, tag12result))
		error(vector.get("set"), vector.get("vector"));
	}
	System.out.println("---> Done");
    }

    private static byte[] getMessage(HashMap<String, String> vector) {
	for (String key : vector.keySet())
	    if (key.startsWith("msg"))
		return hexStringToByteArray(vector.get(key));
	return null;
    }

    private static void error(String set, String vector) {
	System.out.println("ERROR: set " + set + ", vector " + vector);
    }
}
