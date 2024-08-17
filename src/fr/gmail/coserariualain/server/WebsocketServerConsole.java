package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.gmail.coserariualain.process.MyProcess;
import fr.gmail.coserariualain.utilities.ByteManipulator;

public class WebsocketServerConsole  implements Runnable {
	private final ServerSocket server;
	private final MyProcess proc;
	
	public WebsocketServerConsole(MyProcess proc) throws IOException {
		server = new ServerSocket(8001);
		this.proc = proc;
	}
	
	/**
	 * Build and send the handshake answer to the client following the 
	 * rfc 6455 
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
			int nbByteToRead = (messageLen == 126) ? 2 : 8;
			messageLen = 0;
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
	 * Set the payloadLen bytes in the frame
	 * 
	 * @param frame
	 * @param payloadLen
	 */
	private void insertPayloadLengthFrame(byte[] frame, int payloadLen) {
		// Byte 1 : payload length (MASK is set to 0)
		// if > 125 but < 65536, then the number is set on the next two bytes
		// else then the number is set on the next 8 bytes, since byteMessage 
		// length is expressed as int and not long, the 4 next bytes are set 
		// to 0. Bytes are sent in Big-Endian
		if (payloadLen < 126) {
			frame[1] = (byte) (payloadLen);
		} else if (payloadLen < 65536) {
			frame[1] = (byte) (126);
			// Getting byte 2 and 3 from the byteMessage.length, 
			// byte 0 and 1 are set to 0 because byteMessage < 65536
			frame[2] = (byte) (payloadLen >> 8);
			frame[3] = (byte) (payloadLen & 255);
		} else {
			frame[1] = (byte) 127;
			for (int i = 0; i < 4; i++) {
				frame[2 + i] = (byte) 0;
			}
			ByteManipulator.insertIntIntoByteArray(frame, payloadLen, 6);
		}
	}
	
	/**
	 * Sending text message to the client following websocket rfc 6455
	 * indications for the request's header.
	 * 
	 * @param output
	 * @param msg
	 * @throws IOException 
	 */
	private void sendMessage(OutputStream output, String msg) throws IOException {
		byte[] byteMessage = msg.getBytes(StandardCharsets.UTF_8);
		
		// Number of byte the header contains. Sever side, it only depend on 
		// the length of the payload, if < 126, then the header is 2 bytes long
		// if > 126 and < 2^16, then it's 4 bytes long else it's 10 byte long
		int headerBytes = (byteMessage.length < 65536) ? ((byteMessage.length < 126) ? 2 : 4) : 10;
		
		byte[] frame = new byte[byteMessage.length + headerBytes];
		
		frame[0] = (byte) 129; // FIN = 1, rsv1, 2, 3 = 0, optcode = txt = 0001
		
		insertPayloadLengthFrame(frame, byteMessage.length);
		
		System.arraycopy(byteMessage, 0, frame, headerBytes, byteMessage.length);
		
		output.write(frame);
		output.flush();
	}
	
	/**
	 * Keep the connection open with the client, send lasts line of the 
	 * process's log file. Receive command lines to execute.
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
				String msg = decodeNextMessage(input) + "\n";
				Files.write(Path.of("minecraftServer").resolve("log.txt"), msg.getBytes(), StandardOpenOption.APPEND);
				proc.addCommandToQueue(msg);
			}
			
			Path p = Path.of("minecraftServer").resolve("log.txt");
			var msg = Files.readAllLines(p).stream().map(String::toString).collect(Collectors.joining("<br>"));
			
			sendMessage(output, msg);
			
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			if (!proc.getIsAlive()) {
				connected = false;
			}
		}
	}
	
	@Override
	public void run() {
		Socket client = null;
		InputStream input = null;
		OutputStream output = null;
		try {
			client = server.accept();
			input = client.getInputStream();
			output = client.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		Scanner scan = new Scanner(input, "UTF-8").useDelimiter("\\r\\n\\r\\n");
		String data = scan.next();
		
		Matcher get = Pattern.compile("^GET").matcher(data);

		try {
			if (get.find()) {
				sendHandshakeAnswer(data, output);
				communicateWithClient(input, output);
				client.close();
			} else {
			// If the request is not a get request, then close the connection with the client
			client.close();
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		scan.close();
	}
}
