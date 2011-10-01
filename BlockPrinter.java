public class BlockPrinter {
    public static String byteToHexString(byte b) {
        // Pad with zeros if necessary.
        String hexString = Integer.toHexString(0xFF & b);
        StringBuffer paddedHexString = new StringBuffer();
        if (hexString.length() == 1) {
            paddedHexString.append('0');
        }
        return paddedHexString.append(hexString).toString();
    }

    // Prints a 3x2t matrix stored in column major order.
    public static void printM2t(byte[] block, int t) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2 * t; j++) {
                System.out.print(byteToHexString(block[i + 3 * j]) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // Prints a 3x4 matrix stored in column major order.
    public static void printM4(byte[] block) {
        printM2t(block, 2);
    }
}
