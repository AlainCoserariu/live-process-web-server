package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

public class Server {
	private HttpServer server;
	private final int port = 8000;
	
	public void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
		server.createContext("/", new CustomHandler());
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);
		server.start();
	}
}
