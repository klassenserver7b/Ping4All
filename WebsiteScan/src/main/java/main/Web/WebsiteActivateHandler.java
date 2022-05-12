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

public class WebsiteActivateHandler implements HttpHandler {
    private final Logger log = Main.INSTANCE.getMainLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        OutputStream os = exchange.getResponseBody();
        String comm = read.readLine();
        read.close();

        String[] split = comm.split("&%&");

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

            log.debug("OPTIONS request accepted - returning CORS allow");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);

            return;
        }

        activateWebsites(exchange, os, split);
    }

    public void activateWebsites(HttpExchange exchange, OutputStream os, String[] comm) {

        if (comm.length == 2) {
            String response;
            String website = comm[1];
            boolean shouldenable;

            if (comm[0].equalsIgnoreCase("activate")) {
                shouldenable = true;
            } else if (comm[0].equalsIgnoreCase("deactivate")) {
                shouldenable = false;

            } else {

                try {
                    response = "JavaScript Error - please try again in a few minutes!";
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;

            }

            try {
                MySql.onUpdate("UPDATE websites SET enabled=" + shouldenable + " WHERE website='" + website + "'");

                Main.INSTANCE.getPing().updateWebsites();
                response = "Website successful " + comm[0] + "d!";
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();

            } catch (IOException | SQLException e) {

                try {
                    response = "Something went wrong! - please try again in a few seconds!";
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        } else {

            try {
                String response = "Request Error - please try again in a few minutes!";
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
