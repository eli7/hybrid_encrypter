package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFileParser {

    /**
     * Parses the data of a test file and returns a list of test vectors. Each
     * test vector is a key-value map with the necessary data to run a test.
     * 
     * @param fileName
     *            Name of the file to be parsed.
     * @return A list of test vectors.
     * @throws IOException
     */
    public static ArrayList<HashMap<String, String>> parseTestFile(
	    String fileName) throws IOException {
	ArrayList<HashMap<String, String>> testVectors = new ArrayList<HashMap<String, String>>();

	Pattern pattern1 = Pattern
		.compile(".*Set ([0-9]*), vector# ([0-9]*):.*");
	Pattern pattern2 = Pattern.compile("(.*)=(.*)");
	Matcher matcher;

	BufferedReader reader = new BufferedReader(new FileReader(fileName));
	String line = reader.readLine();
	while (line != null && !line.startsWith("Set "))
	    line = reader.readLine();

	while (line != null) {
	    HashMap<String, String> testVector = new HashMap<String, String>();

	    matcher = pattern1.matcher(line);
	    if (!matcher.matches())
		throw new RuntimeException("Parse Error! File: '" + fileName
			+ '"');
	    testVector.put("set", matcher.group(1));
	    testVector.put("vector", matcher.group(2));

	    line = reader.readLine();
	    while (line != null && !line.startsWith("Set ")) {
		if (!line.trim().isEmpty() && line.contains("=")) {
		    matcher = pattern2.matcher(line);
		    if (!matcher.matches())
			throw new RuntimeException("Parse Error! File: '"
				+ fileName + '"');
		    testVector.put(matcher.group(1).trim(), matcher.group(2)
			    .trim());
		}
		line = reader.readLine();
	    }
	    testVectors.add(testVector);
	}

	reader.close();

	return testVectors;
    }
}