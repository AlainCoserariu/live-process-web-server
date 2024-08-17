package fr.gmail.coserariualain.server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.gmail.coserariualain.process.MyProcess;

public class CustomHandler implements HttpHandler {

	private final MyProcess proc;
	private final WebsocketServerConsole thread;
	
	public CustomHandler(MyProcess proc) throws IOException {
		thread = new WebsocketServerConsole(proc);
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
		if (exchange.getRequestURI().toString().equals("/start-server")) {
			proc.startProcess();
		}
		
		return "/server-management";
	}
	
	/**
	 * Send the response to the client
	 * 
	 * @param exchange
	 * @param requestParamValue URI sent to the client
	 * @throws IOException
	 */
	private void handleResponse(HttpExchange exchange, String requestParamValue) throws IOException {
		OutputStream outputStream = exchange.getResponseBody();
				
		String response = buildResponse(requestParamValue);
		
		exchange.sendResponseHeaders(200, response.length());
		outputStream.write(response.getBytes());
		outputStream.flush();
		outputStream.close();
	}
	
	/**
	 * Construct the body of the response to the client.
	 * This method call the appropriate method to build the html page to send 
	 * to the client.
	 * Builders method can change "$varX" present in the html file to modify 
	 * element of the page for instance.
	 * 
	 * @param requestParamValue
	 * @return The body of the http request
	 * @throws IOException
	 */
	private String buildResponse(String requestParamValue) throws IOException {
		String response;
		
		if (!requestParamValue.endsWith(".js")) {
			requestParamValue = requestParamValue + ".html";
		}
		
		try (var input = CustomHandler.class.getResourceAsStream("/resources" + requestParamValue)) {
			response = new String(input.readAllBytes());
		}
		
		return switch (requestParamValue) {
		case "/index.html": yield buildResponseIndex(response);
		case "/server-management.html": yield buildResponseServerManagement(response);
		default: yield response;
		};
	}
	
	private String buildResponseIndex(String response) {
		if (proc.getIsAlive()) {
			response = response.replace("$var1", "<p>The server is already started</p>"
					+ "<form action=\"/server-management\" method=\"post\">\n"
					+ "        	<button type=\"submit\">Manage the server</button>\n"
					+ "		</form>");
		} else {
			response = response.replace("$var1", "<form action=\"/start-server\" method=\"post\">\n"
					+ "        	<button type=\"submit\">Start the server</button>\n"
					+ "		</form>");			
		}
		
		return response;
	}
	
	private String buildResponseServerManagement(String response) throws IOException {
		new Thread(thread).start();
		return response;
	}
}
