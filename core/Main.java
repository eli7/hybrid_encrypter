package core;

import java.io.IOException;

import test.Curupira1Test;
import test.LetterSoupTest;
import test.MarvinTest;

public class Main {
    public static void main(String[] args) throws IOException {
	boolean testMode;

	if (args.length > 0 && args[0].equalsIgnoreCase("test"))
	    testMode = true;
	else
	    testMode = false;

	if (testMode) {
	    Curupira1Test.test();
	    MarvinTest.test();
	    LetterSoupTest.test();
	} else {
	    UI ui = new UI();
	    ui.start();
	}
    }
}
