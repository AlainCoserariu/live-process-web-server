package fr.gmail.coserariualain.utilities;

public class ByteManipulator {
	public static byte[] intToByteArray(int i) {
		byte[] res = new byte[4];
		
		res[0] = (byte) (i >> 24);
		res[1] = (byte) (i >> 16);
		res[2] = (byte) (i >> 8);
		res[3] = (byte) (i);
		
		return res;
	}
	
	/**
	 * Insert an int into a byteArray from a specified position
	 * 
	 * @param array
	 * @param n
	 * @param pos
	 */
	public static void insertIntIntoByteArray(byte[] array, int n, int pos) {
		for (int i = 0; i < 4; i++) {
			array[pos + i] = (byte) ((n >> ((3 - i) * 8)) & 0xff);
		}
	}
}
