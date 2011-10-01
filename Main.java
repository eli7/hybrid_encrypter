public class Main {
    public static void main(String[] args) {
        byte[] plainText = {0, 0, 0, 0,
                            0, 0, 0, 0,
                            0, 0, 0, 0};
        byte[] cipherKey = {0, 0, 0, 0,
                            0, 0, 0, 0,
                            0, 0, 0, 0};
        final int keyBits = 96;
        BlockCipher blockCipher = new Curupira();
        blockCipher.makeKey(cipherKey, keyBits);
        byte[] cipherText = new byte[12];
        blockCipher.encrypt(plainText, cipherText);
        System.out.println("Cipher text:");
        BlockPrinter.printM4(cipherText);
    }
}
