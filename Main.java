public class Main {
    public static void main(String[] args) {
        // 96 bits key
        byte[] plainText = {0, 0, 0, 0,
                            0, 0, 0, 0,
                            0, 0, 0, 0};
        byte[] cipherKey96 = {(byte)0x80, 0x00, 0x00, 0x00,
                              0x00, 0x00, 0x00, 0x00,
                              0x00, 0x00, 0x00, 0x00};
        int keyBits = 96;
        BlockCipher blockCipher = new Curupira();
        blockCipher.makeKey(cipherKey96, keyBits);
        byte[] cipherText = new byte[12];
        blockCipher.encrypt(plainText, cipherText);
        System.out.println("Cipher text 96:");
        BlockPrinter.printM4(cipherText);
        blockCipher.decrypt(cipherText, plainText);
        System.out.println("Plain text:");
        BlockPrinter.printM4(plainText);
        // 144 bits key
        byte[] cipherKey144 = {(byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00,
                               0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                               0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        keyBits = 144;
        blockCipher.makeKey(cipherKey144, keyBits);
        cipherText = new byte[12];
        blockCipher.encrypt(plainText, cipherText);
        System.out.println("Cipher text 144:");
        BlockPrinter.printM4(cipherText);
        blockCipher.decrypt(cipherText, plainText);
        System.out.println("Plain text:");
        BlockPrinter.printM4(plainText);

        // 192 bits key
        byte[] cipherKey192 = {(byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        keyBits = 192;
        blockCipher.makeKey(cipherKey192, keyBits);
        cipherText = new byte[12];
        blockCipher.encrypt(plainText, cipherText);
        System.out.println("Cipher text 192:");
        BlockPrinter.printM4(cipherText);
        blockCipher.decrypt(cipherText, plainText);
        System.out.println("Plain text:");
        BlockPrinter.printM4(plainText);
    }
}
