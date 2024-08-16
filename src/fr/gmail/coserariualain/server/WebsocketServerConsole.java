package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gmail.coserariualain.process.MyProcess;

public class WebsocketServerConsole {
	private final ServerSocket server;
	private final MyProcess proc;
	
	public WebsocketServerConsole(MyProcess proc) throws IOException {
		server = new ServerSocket(8001);
		this.proc = proc;
	}
	
	/**
	 * Build and send the handshake answer to the client following the 
	 * protocol described on this page : https://developer.mozilla.org/fr/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java 
	 *
	 * @param data
	 * @param output
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private void sendHandshakeAnswer(String data, OutputStream output) throws NoSuchAlgorithmException, IOException {
		Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
	    match.find();
	    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
	            + "Connection: Upgrade\r\n"
	            + "Upgrade: websocket\r\n"
	            + "Sec-WebSocket-Accept: "
	            + Base64.getEncoder().encodeToString(
	            		MessageDigest
	            		.getInstance("SHA-1")
	            		.digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
	            				.getBytes("UTF-8")))
	            + "\r\n\r\n")
	    		.getBytes("UTF-8");

	    output.write(response, 0, response.length);
	}
	
	/**
	 * Find the message length, following RFC 6455, if the byte read minus  
	 * 128 is between 0 and 125, then the value is the length of the message, 
	 * if it is 126, the the length is the two following bytes, and if it is 
	 * 127, the it is the 8 following bytes.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private long getMessageLength(InputStream input) throws IOException {
		long messageLen = input.read() - 128;
		if (messageLen == 126 || messageLen == 127) {
			messageLen = 0;
			int nbByteToRead = (messageLen == 126) ? 2 : 8;
			for (byte b : input.readNBytes(nbByteToRead)) {
				messageLen = (messageLen << 8) + (b & 0xFF);
			}
		}
		
		return messageLen;
	}
	
	private String decodeNextMessage(InputStream input) throws IOException {
		int end = input.read();
		if (end != 129) {
			System.out.println("Message recieved in multuple fragment or is not a text, we are fucked...");
		}
		
		// We cast the length to int because the readNbytes method accept only int
		// Normally messages should never be larger than 2^32 bytes, so it should be OK
		int messageLen = (int) getMessageLength(input);
		
		byte[] key = input.readNBytes(4);
		
		
		byte[] encoded = input.readNBytes(messageLen);
		byte[] decoded = new byte[messageLen];
		
		for (int i = 0; i < encoded.length; i++) {
			decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
		}
		
		return new String(decoded);
	}
	
	/**
	 * Keep the connection open with the client, send lasts line of the 
	 * process's log file. Recieve command lines to execute.
	 * 
	 * @param input
	 * @param output
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void communicateWithClient(InputStream input, OutputStream output) throws IOException {
		boolean connected = true;
		while (connected) {
			if (input.available() > 0) {
				System.out.println("New message recieved : " + decodeNextMessage(input));
			}
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void waitConnection() throws IOException, NoSuchAlgorithmException {
		Socket client = server.accept();
		
		InputStream input = client.getInputStream();
		OutputStream output = client.getOutputStream();
		
		Scanner scan = new Scanner(input, "UTF-8").useDelimiter("\\r\\n\\r\\n");
		String data = scan.next();
		
		Matcher get = Pattern.compile("^GET").matcher(data);

		if (get.find()) {
		    sendHandshakeAnswer(data, output);
		    communicateWithClient(input, output);
		    client.close();
		} else {
			// If the request is not a get request, then close the connection with the client
			client.close();
		}
		scan.close();
	}
}
