package main.Web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.util.MySql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetLogsHandler extends GenericHandler implements HttpHandler {
	private final Logger log;

	public GetLogsHandler() {
		super();
		log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		OutputStream os = exchange.getResponseBody();
		String website = read.readLine();
		read.close();

		website = website.replaceAll("getlogs:", "");

		exchange = super.sendCors(exchange);
		if (exchange == null) {
			return;
		}
		getLogs(exchange, os, website);
	}

	public void getLogs(HttpExchange exchange, OutputStream os, String website) {
		if (!(website == null || website.equalsIgnoreCase(""))) {
			List<JsonObject> logs = new ArrayList<>();
			JsonArray response = new JsonArray();

			try (ResultSet data = MySql.onQuery("SELECT * FROM pinglogs WHERE website = ?", website)) {

				if (data != null) {
					int i = 0;
					while (data.next()) {
						JsonObject obj = new JsonObject();
						obj.addProperty("id", i);
						obj.addProperty("time", data.getString("timestamp"));
						obj.addProperty("name",
								data.getString("website").replaceAll("http://", "").replaceAll("https://", ""));
						obj.addProperty("success", data.getBoolean("success"));
						obj.addProperty("ping", data.getInt("ping"));
						obj.addProperty("log", data.getString("ping_errorcode"));

						logs.add(obj);
						i++;
					}

					for (int j = logs.size() - 1; j >= 0; j--) {
						response.add(logs.get(j));
					}

					super.sendResponse(exchange, os, response);

				} else {
					throw new NullPointerException("data Result = null");
				}

			} catch (SQLException | NullPointerException e) {
				log.error(e.getMessage(), e);
				try {
					String erresp = "Something went wrong! - please try again in a few seconds!";
					exchange.getResponseHeaders().add("Content-Type", "text/plain");
					exchange.sendResponseHeaders(500, erresp.getBytes(StandardCharsets.UTF_8).length);
					os.write(erresp.getBytes(StandardCharsets.UTF_8));
					os.close();
				} catch (IOException e1) {
					log.error(e.getMessage(), e);
				}
			}

		} else {
			super.sendRequestError(exchange, os);
		}
	}
}
