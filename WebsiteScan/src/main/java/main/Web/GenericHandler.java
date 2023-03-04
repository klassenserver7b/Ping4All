package main.Web;

import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class GenericHandler {

	private final Logger log;

	public GenericHandler() {
		log = LoggerFactory.getLogger(getClass());
	}

	public String validateLink(String website) {
		if (!(website.startsWith("http://") || website.startsWith("https://"))) {
			website = "http://" + website;

		}
		return website;
	}

	public HttpExchange sendCors(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

		if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

			log.debug("OPTIONS request accepted - returning CORS allow");
			exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD");
			exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
			exchange.sendResponseHeaders(204, -1);
			return null;

		}
		return exchange;
	}

	public void sendResponse(HttpExchange exchange, OutputStream os, JsonElement response) {
		try {
			exchange.getResponseHeaders().add("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, response.toString().getBytes(StandardCharsets.UTF_8).length);
			os.write(response.toString().getBytes(StandardCharsets.UTF_8));
			os.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void sendRequestError(HttpExchange exchange, OutputStream os) {
		try {
			String response = "Request Error - please try again in a few minutes!";
			exchange.getResponseHeaders().add("Content-Type", "text/plain");
			exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
