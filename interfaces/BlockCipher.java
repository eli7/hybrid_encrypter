package interfaces;

public interface BlockCipher {

    /**
     * Block size in bits.
     */
    int blockBits();

    /**
     * Key size in bits.
     */
    int keyBits();

    /**
     * Setup the cipher key for this block cipher instance.
     * 
     * @param cipherKey
     *            the cipher key.
     * @param keyBits
     *            size of the cipher key in bits.
     */
    void makeKey(byte[] cipherKey, int keyBits);

    /**
     * Encrypt exactly one block of plaintext.
     * 
     * @param mBlock
     *            plaintext block.
     * @param cBlock
     *            ciphertext block.
     */
    void encrypt(byte[] mBlock, byte[] cBlock);

    /**
     * Decrypt exactly one block of ciphertext.
     * 
     * @param cBlock
     *            ciphertext block.
     * @param mBlock
     *            plaintext block.
     */
    void decrypt(byte[] cBlock, byte[] mBlock);

    /**
     * Apply a square-complete transform to exactly one block of ciphertext.
     * 
     * @param cBlock
     *            ciphertext block.
     * @param mBlock
     *            plaintext block.
     */
    void sct(byte[] cBlock, byte[] mBlock);
}
