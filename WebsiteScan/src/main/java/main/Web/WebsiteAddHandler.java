package main.Web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.util.MySql;
import main.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WebsiteAddHandler extends GenericHandler implements HttpHandler {

	private final Logger log;

	public WebsiteAddHandler() {
		super();
		log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		OutputStream os = exchange.getResponseBody();

		String website = read.readLine();
		read.close();

		website = website.replaceAll("add:", "");

		website = super.validateLink(website);

		exchange = super.sendCors(exchange);
		if (exchange == null) {
			return;
		}
		addWebsite(website, exchange, os);
	}

	public void addWebsite(String website, HttpExchange exchange, OutputStream os) throws IOException {
		String response;

		if (!Validator.isValidURL(website)) {

			response = "Please insert a valid Website!";

			exchange.getResponseHeaders().add("Content-Type", "text/plain");
			exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();
			return;
		}
		try (ResultSet set = MySql.onQuery("Select website FROM websites")) {

			boolean alreadylisted = false;

			if (set != null) {
				while (set.next()) {

					String dbwebsite = set.getString("website");

					if (dbwebsite.equalsIgnoreCase(website)) {
						alreadylisted = true;
						break;
					}

				}
			}

			if (alreadylisted) {
				response = "Website already listed!";

				exchange.getResponseHeaders().add("Content-Type", "text/plain");
				exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);

			} else {

				response = "Website successful added!";

				MySql.onUpdate("INSERT INTO websites(website, enabled) VALUES(?, ?)", website, true);
				exchange.getResponseHeaders().add("Content-Type", "text/plain");
				exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

			}

			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();

		} catch (SQLException e) {
			log.error(e.getMessage(),e);

			response = "Couldn't add website!";

			exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);

			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();

		}
	}
}
