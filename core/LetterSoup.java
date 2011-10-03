package core;

import interfaces.AEAD;
import interfaces.BlockCipher;
import interfaces.MAC;
import static utils.ByteOperations.xor;

/**
 * <p>
 * Implementation of the LetterSoup AEAD-mode.
 * </p>
 * <p>
 * This implementation was tailored to be used with the Curupira-1 block cipher
 * and the Marvin MAC. Behavior is unknown in other contexts.
 * </p>
 */
public class LetterSoup implements AEAD {

    private MAC mac;
    private BlockCipher cipher;
    private int blockBytes;
    private int mLength;
    private int hLength;
    private byte[] iv;
    private byte[] A;
    private byte[] D;
    private byte[] R;
    private byte[] L;

    public void setMAC(MAC mac) {
	this.mac = mac;
    }

    public void setCipher(BlockCipher cipher) {
	this.mac.setCipher(cipher);
	this.cipher = cipher;
	blockBytes = cipher.blockBits() / 8;
    }

    public void setKey(byte[] cipherKey, int keyBits) {
	cipher.makeKey(cipherKey, keyBits);
    }

    public void setIV(byte[] iv, int ivLength) {
	this.iv = new byte[ivLength];
	System.arraycopy(iv, 0, this.iv, 0, ivLength);
	
	L = null;
	
	// Step 2 of Algorithm 2 - Page 6
	R = new byte[blockBytes];
	byte[] leftPaddedN = new byte[blockBytes];
	System.arraycopy(iv, 0, leftPaddedN, blockBytes - ivLength, blockBytes);
	cipher.encrypt(leftPaddedN, R);
	xor(R, leftPaddedN);
    }

    public void update(byte[] aData, int aLength) {
	// Step 4 of Algorithm 2 - Page 6 (L and part of D)
	L = new byte[blockBytes];
	D = new byte[blockBytes];
	hLength = aLength;
	cipher.encrypt(new byte[blockBytes], L);
	mac.init(L);
	mac.update(aData, aLength);
	mac.getTag(D, cipher.blockBits());
    }

    public byte[] encrypt(byte[] mData, int mLength, byte[] cData) {
	// Step 3 of Algorithm 2 - Page 6 (C and part of A)
	A = new byte[blockBytes];
	this.mLength = mLength;

	if (cData == null)
	    cData = new byte[mLength];
	LFSRC(mData, mLength, cData);

	mac.init(R);
	mac.update(cData, mLength);
	mac.getTag(A, cipher.blockBits());

	return cData;
    }

    public byte[] decrypt(byte[] cData, int cLength, byte[] mData) {
	if (mData == null)
	    mData = new byte[cLength];
	LFSRC(cData, cLength, mData);
	return mData;
    }

    public byte[] getTag(byte[] tag, int tagBits) {
	if (tag == null)
	    tag = new byte[tagBits / 8];

	// Step 3 of Algorithm 2 - Page 6 (completes the part of A due to M)
	byte[] Atemp = new byte[blockBytes];
	System.arraycopy(A, 0, Atemp, 0, blockBytes);
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
	for (int i = 0; i < 4; ++i)
	    auxValue2[blockBytes - i - 1] = (byte) ((mLength * 8) >>> (8 * i));

	System.arraycopy(Atemp, 0, A, 0, blockBytes);
	xor(Atemp, auxValue1);
	xor(Atemp, auxValue2);

	// Steps 4-6 of Algorithm 2 - Page 6 (completes the part of A due to H)
	if (L != null) {
	    // auxValue2 = lpad(bin(|H|))
	    auxValue2 = new byte[blockBytes];
	    for (int i = 0; i < 4; ++i)
		auxValue2[blockBytes - i - 1] = (byte) ((hLength * 8) >>> (8 * i));
	    byte[] Dtemp = new byte[blockBytes];
	    System.arraycopy(D, 0, Dtemp, 0, blockBytes);
	    xor(Dtemp, auxValue1);
	    xor(Dtemp, auxValue2);
	    cipher.sct(Dtemp, auxValue1);
	    xor(Atemp, auxValue1);
	}

	// Step 7 of Algorithm 2 - Page 6
	cipher.encrypt(Atemp, auxValue1);
	for (int i = 0; i < tagBits / 8; ++i)
	    tag[i] = auxValue1[i];

	return tag;
    }

    private void LFSRC(byte[] mData, int mLength, byte[] cData) {
	// Algorithm 8 - Page 20
	byte[] M = new byte[blockBytes];
	byte[] C = new byte[blockBytes];
	byte[] O = new byte[blockBytes];
	System.arraycopy(R, 0, O, 0, blockBytes);
	int q = mLength / blockBytes;
	int r = mLength % blockBytes;

	for (int i = 0; i < q; ++i) {
	    System.arraycopy(mData, i * blockBytes, M, 0, blockBytes);
	    updateOffset(O);
	    cipher.encrypt(O, C);
	    xor(C, M);
	    System.arraycopy(C, 0, cData, i * blockBytes, blockBytes);
	}
	if (r != 0) {
	    System.arraycopy(mData, q * blockBytes, M, 0, r);
	    updateOffset(O);
	    cipher.encrypt(O, C);
	    xor(C, M);
	    System.arraycopy(C, 0, cData, q * blockBytes, r);
	}
    }

    private void updateOffset(byte[] O) {
	// Algorithm 6 - Page 19 (w = 8, k1 = 11, k2 = 13, k3 = 16)
	byte O0 = O[0];
	System.arraycopy(O, 1, O, 0, 11);
	O[9] = (byte) (O[9] ^ O0 ^ ((O0 & 0xFF) >>> 3) ^ ((O0 & 0xFF) >>> 5));
	O[10] = (byte) (O[10] ^ (O0 << 5) ^ (O0 << 3));
	O[11] = O0;
    }
}
