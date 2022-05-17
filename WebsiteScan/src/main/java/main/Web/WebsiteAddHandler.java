package main.Web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.Main;
import main.util.MySql;
import main.util.Validator;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WebsiteAddHandler implements HttpHandler {

    private final Logger log = Main.INSTANCE.getMainLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        OutputStream os = exchange.getResponseBody();

        String website = read.readLine();
        read.close();

        website = website.replaceAll("add:", "");

        if (!(website.startsWith("http://") || website.startsWith("https://"))) {
            website = "http://" + website;
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

            log.debug("OPTIONS request accepted - returning CORS allow");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);

            return;
        }
        addWebsite(website, exchange, os);
    }

    public void addWebsite(String website, HttpExchange exchange, OutputStream os) throws IOException {
        String response;
        try {
            if (Validator.isValidURL(website)) {
                ResultSet set = MySql.onQuery("Select website FROM websites");
                boolean alreadylisted = false;

                if (set != null) {
                    while (set.next()) {

                        String dbwebsite = set.getString("website");

                        if (!(dbwebsite.startsWith("http://") || dbwebsite.startsWith("https://"))) {
                            dbwebsite = "http://" + dbwebsite;
                        }

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

                    MySql.onUpdate("INSERT INTO websites(website, enabled) VALUES('" + website + "', true)");
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

                }

            } else {

                response = "Please insert a valid Website!";

                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);


            }
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (SQLException e) {

            response = "Couldn't add website!";

            exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);

            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();


        }
    }
}
