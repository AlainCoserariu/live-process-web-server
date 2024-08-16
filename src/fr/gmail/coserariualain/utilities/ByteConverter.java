package fr.gmail.coserariualain.utilities;

public class ByteConverter {
	public static byte[] intToByteArray(int i) {
		byte[] res = new byte[4];
		
		res[0] = (byte) (i >> 24);
		res[1] = (byte) (i >> 16);
		res[2] = (byte) (i >> 8);
		res[3] = (byte) (i);
		
		return res;
	}
}
