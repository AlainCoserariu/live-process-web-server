package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

import fr.gmail.coserariualain.process.MyProcess;

public class Server {
	private HttpServer server;
	private final int port = 8000;
	private final String processCommand = "./minecraftServer/processtest.sh";
	
	public void startServer() throws IOException { 
		MyProcess proc = new MyProcess(processCommand);
		
		// Start the http server
		server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
		server.createContext("/", new CustomHandler(proc));
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);
		server.start();
	}
}
