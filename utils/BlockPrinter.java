package utils;

import static utils.ByteOperations.byteToHexString;

public class BlockPrinter {

    /**
     * Prints a 3x2t matrix stored in column major order.
     * 
     * @param block
     *            The matrix to be printed.
     * @param t
     *            Half the number of columns of the matrix.
     */
    public static void printM2t(byte[] block, int t) {
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 2 * t; j++) {
		System.out.print(byteToHexString(block[i + 3 * j]) + " ");
	    }
	    System.out.println();
	}
	System.out.println();
    }

    /**
     * Prints a 3x4 matrix stored in column major order.
     * 
     * @param block
     *            The matrix to be printed.
     */
    public static void printM4(byte[] block) {
	printM2t(block, 2);
    }
}
