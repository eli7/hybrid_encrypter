interface AEAD { 
    /** 
     * Provide the underlying message authentication code. 
     * 
     * @param  mac         the underlying MAC. 
     */ 
    void setMAC(MAC mac); 
    /** 
     * Provide the underlying block cipher. 
     * 
     * @param  cipher      the underlying block cipher. 
     */ 
    void setCipher(BlockCipher cipher);     /** 
     * Provide the cipher key. 
     * 
     * @param  cipherKey   the cipher key. 
       @param  keyBits     the key size in bits. 
     */ 
    void setKey(byte[] cipherKey, int keyBits); 
    /** 
     * Provide a statistically unique initialization vector (IV) 
for new operation of authenticated encryption with  associated 
data. 
     * 
     * @param  iv          the IV (must be different at each call) 
     * @param  ivLength    its length in bytes. 
     */ 
    void setIV(byte[] iv, int ivLength); 
    /** 
     * Provide a chunk of associated data for authentication. 
     * 
     * @param  aData       the associated data chunk. 
     * @param  aLength     its length in bytes. 
     */ 
    void update(byte[] aData, int aLength); 
    /** 
     * Provide a chunk of plaintext for authenticated encryption. 
     * 
     * @param  mData       the plaintext chunk. 
     * @param  mLength     its length in bytes. 
     * @param  cData       ciphertext buffer for encrypted chunk. 
     * 
     * @return the ciphertext. 
     * If cData is null, a new buffer is automatically allocated, 
otherwise the provided buffer is returned. 
     */ 
    byte[] encrypt(byte[] mData, int mLength, byte[] cData); 
    /** 
     * Provide a chunk of ciphertext for authenticated decryption. 
     * 
     * @param  cData       the ciphertext chunk. 
     * @param  cLength     its length in bytes. 
     * @param  mData       plaintext buffer for decrypted chunk. 
     * 
     * @return the ciphertext. 
     * If mData is null, a new buffer is automatically allocated, 
otherwise the provided buffer is returned. 
     */ 
    byte[] decrypt(byte[] cData, int cLength, byte[] mData);     /** 
     * Complete if necessary the data processing and 
     * get the MAC tag (of specified size) 
     * of the whole message provided. 
     * 
     * @param  tag         the MAC tag buffer. 
     * @param  tagBits     the desired tag size in bits. 
     * 
     * @return MAC tag of the whole message (if the tag parameter 
is null, a new one is allocated automatically, otherwise the input 
buffer is returned) 
     */ 
    byte[] getTag(byte[] tag, int tagBits); 
} 

