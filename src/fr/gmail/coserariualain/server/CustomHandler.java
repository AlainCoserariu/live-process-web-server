package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CustomHandler implements HttpHandler {

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
	
	private String handlePostRequest(HttpExchange exchange) {
		if (exchange.getRequestURI().toString().equals("/start_server")) {
			System.out.println("Starting the server !");
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
