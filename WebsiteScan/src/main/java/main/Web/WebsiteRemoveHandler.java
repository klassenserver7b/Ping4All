package main.Web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.Main;
import main.util.MySql;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class WebsiteRemoveHandler implements HttpHandler {

    private final Logger log = Main.INSTANCE.getMainLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        OutputStream os = exchange.getResponseBody();

        String website = read.readLine();
        read.close();
        website = website.replaceAll("remove:", "");

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

        removeWebsite(website, exchange, os);
    }

    public void removeWebsite(String website, HttpExchange exchange, OutputStream os) {
        String response;

        try {

            response = "Website successful removed!";

            MySql.onUpdate("Delete FROM websites WHERE website = '" + website + "'");
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);


            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();

        } catch (SQLException | IOException e) {

            response = "Couldn't remove website!";

            try {

                exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }


        }
    }
}