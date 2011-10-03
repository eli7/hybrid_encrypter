package core;

import interfaces.BlockCipher;
import interfaces.MAC;
import static utils.ByteOperations.xor;

/**
 * <p>
 * Implementation of the Marvin MAC algorithm.
 * </p>
 * <p>
 * This implementation was tailored to be used with the Curupira-1 block cipher
 * and the LetterSoup AEAD. Behavior is unknown in other contexts.
 * </p>
 * <p>
 * When used inside LetterSoup ({@code letterSoupMode}) this implementation
 * operates in a special way. In {@code letterSoupMode} the {@code getTag}
 * method returns a partial tag value that doesn't include the calculations of
 * part of the {@code A0} element of the algorithm and also hasn't been
 * encrypted and truncated as specified in the final steps of Marvin. The
 * completion of the described steps is delegated to the LetterSoup
 * implementation.
 * </p>
 */
public class Marvin implements MAC {

    private static final byte c = 0x2A;

    private BlockCipher cipher;
    private int blockBytes;
    private int mLength;
    private byte[] R;
    private byte[] O;
    private byte[] buffer;
    private boolean letterSoupMode;

    /**
     * @param letterSoupMode
     *            Must be {@code true} if this instance will be used inside
     *            LetterSoup and {@code false} otherwise. See the class
     *            description for more information.
     */
    public Marvin(boolean letterSoupMode) {
	this.letterSoupMode = letterSoupMode;
    }

    public void setCipher(BlockCipher cipher) {
	this.cipher = cipher;
	blockBytes = cipher.blockBits() / 8;
    }

    public void setKey(byte[] cipherKey, int keyBits) {
	cipher.makeKey(cipherKey, keyBits);
    }

    public void init() {
	buffer = new byte[blockBytes];
	R = new byte[blockBytes];
	O = new byte[blockBytes];

	// Step 2 of Algorithm 1 - Page 4
	byte[] leftPaddedC = new byte[blockBytes];
	leftPaddedC[blockBytes - 1] = c;
	cipher.encrypt(leftPaddedC, R);
	xor(R, leftPaddedC);
	System.arraycopy(R, 0, O, 0, blockBytes);
    }

    public void init(byte[] R) {
	buffer = new byte[blockBytes];
	this.R = new byte[blockBytes];
	O = new byte[blockBytes];

	System.arraycopy(R, 0, this.R, 0, blockBytes);
	System.arraycopy(R, 0, O, 0, blockBytes);
    }

    public void update(byte[] aData, int aLength) {
	byte[] M = new byte[blockBytes];
	byte[] A = new byte[blockBytes];
	int q = aLength / blockBytes;
	int r = aLength % blockBytes;

	// Steps 1, 3-5, 6-7 (only R) of Algorithm 1 - Page 4 
	xor(buffer, R);
	for (int i = 0; i < q; ++i) {
	    System.arraycopy(aData, i * blockBytes, M, 0, blockBytes);
	    updateOffset();
	    xor(M, O);
	    cipher.sct(M, A);
	    xor(buffer, A);
	}
	if (r != 0) {
	    System.arraycopy(aData, q * blockBytes, M, 0, r);
	    for (int i = r; i < blockBytes; ++i)
		M[i] = 0;
	    updateOffset();
	    xor(M, O);
	    cipher.sct(M, A);
	    xor(buffer, A);
	}
	mLength = aLength;
    }

    public byte[] getTag(byte[] tag, int tagBits) {
	if (tag == null)
	    tag = new byte[tagBits / 8];

	if (letterSoupMode) {
	    System.arraycopy(buffer, 0, tag, 0, blockBytes);
	    return tag;
	}

	// Steps 6-9 of Algorithm 1 - Page 4
	byte[] A = new byte[blockBytes];
	byte[] encryptedA = new byte[blockBytes];
	byte[] auxValue1 = new byte[blockBytes];
	byte[] auxValue2 = new byte[blockBytes];

	// auxValue1 = rpad(bin(n-tagBits)||1)
	byte diff = (byte) (cipher.blockBits() - tagBits);
	if (diff == 0) {
	    auxValue1[0] = (byte) 0x80;
	    auxValue1[1] = (byte) 0x00;
	} else if (diff < 0) {
	    auxValue1[0] = diff;
	    auxValue1[1] = (byte) 0x80;
	} else {
	    diff = (byte) ((diff << 1) | (0x01));
	    while (diff > 0)
		diff = (byte) (diff << 1);
	    auxValue1[0] = diff;
	    auxValue1[1] = (byte) 0x00;
	}

	// auxValue2 = lpad(bin(|M|))
	int processedBits = 8 * mLength;
	for (int i = 0; i < 4; ++i)
	    auxValue2[blockBytes - i - 1] = (byte) (processedBits >>> (8 * i));

	System.arraycopy(buffer, 0, A, 0, blockBytes);
	xor(A, auxValue1);
	xor(A, auxValue2);
	cipher.encrypt(A, encryptedA);

	for (int i = 0; i < tagBits / 8; ++i)
	    tag[i] = encryptedA[i];

	return tag;
    }

    private void updateOffset() {
	// Algorithm 6 - Page 19 (w = 8, k1 = 11, k2 = 13, k3 = 16)
	byte O0 = O[0];
	System.arraycopy(O, 1, O, 0, 11);
	O[9] = (byte) (O[9] ^ O0 ^ ((O0 & 0xFF) >>> 3) ^ ((O0 & 0xFF) >>> 5));
	O[10] = (byte) (O[10] ^ (O0 << 5) ^ (O0 << 3));
	O[11] = O0;
    }
}