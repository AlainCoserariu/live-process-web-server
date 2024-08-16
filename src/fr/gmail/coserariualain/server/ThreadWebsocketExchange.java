package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ThreadWebsocketExchange implements Runnable {

	private final WebsocketServerConsole websocketServer;
	
	public ThreadWebsocketExchange(WebsocketServerConsole websocketServer) {
		this.websocketServer = websocketServer;
	}
	
	@Override
	public void run() {
		try {
			websocketServer.waitConnection();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}
	
}
