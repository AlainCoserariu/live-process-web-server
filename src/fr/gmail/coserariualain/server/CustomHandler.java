package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.gmail.coserariualain.process.MyProcess;

public class CustomHandler implements HttpHandler {

	private final MyProcess proc;
	
	public CustomHandler(MyProcess proc) {
		this.proc = proc;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {		
		String requestParamValue = null;
		
		String requestMethod = exchange.getRequestMethod();
		
		if ("GET".equals(requestMethod)) {
			requestParamValue = handleGetRequest(exchange);
		} else if ("POST".equals(requestMethod)) {
			requestParamValue = handlePostRequest(exchange);
		}
		
		handleResponse(exchange, requestParamValue);
	}
	
	private String handleGetRequest(HttpExchange exchange) {
		String uri = exchange.getRequestURI().toString();
		if (uri.equals("/")) {
			uri = "/index";
		}
		
		return uri;
	}
	
	private String handlePostRequest(HttpExchange exchange) throws IOException {
		if (exchange.getRequestURI().toString().equals("/start_server")) {
			proc.startProcess();
		}
		
		
		return "/server_management";
	}
	
	private void handleResponse(HttpExchange exchange, String requestParamValue) throws IOException {
		OutputStream outputStream = exchange.getResponseBody();
				
		String response;
		try (var input = CustomHandler.class.getResourceAsStream("/resources" + requestParamValue + ".html")) {
			response = new String(input.readAllBytes());
		}
		
		exchange.sendResponseHeaders(200, response.length());
		outputStream.write(response.getBytes());
		outputStream.flush();
		outputStream.close();
	}
}
