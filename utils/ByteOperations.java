package utils;

public class ByteOperations {

    /**
     * @param hex
     *            A hexadecimal string.
     * @return The big-endian byte array representation of the string. Returns
     *         {@code null} if an empty string is passed.
     */
    public static byte[] hexStringToByteArray(String hex) {
	if (hex.isEmpty())
	    return null;
	if (hex.length() % 2 != 0)
	    hex = "0" + hex;
	byte[] array = new byte[hex.length() / 2];
	for (int i = 0; i < hex.length() / 2; ++i)
	    array[i] = (byte) Integer.parseInt(
		    hex.substring(i * 2, (i + 1) * 2), 16);
	return array;
    }

    /**
     * @param b
     *            A byte value.
     * @return The hexadecimal representation of the byte. The returned string
     *         always has length 2 and is left padded with a zero if necessary.
     */
    public static String byteToHexString(byte b) {
	String hexString = Integer.toHexString(0xFF & b);
	return hexString.length() == 1 ? "0" + hexString : hexString;
    }

    /**
     * Calculates the value of {@code a xor b} and stores the result in
     * {@code a}. An error will occur if {@code b} is shorter than {@code a}.
     * 
     * @param a
     *            The first byte array.
     * @param b
     *            The second byte array.
     */
    public static void xor(byte[] a, byte[] b) {
	for (int i = 0; i < a.length; ++i)
	    a[i] = (byte) (a[i] ^ b[i]);
    }
}
