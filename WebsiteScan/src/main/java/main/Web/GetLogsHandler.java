package main.Web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetLogsHandler implements HttpHandler {
    private final Logger log = Main.INSTANCE.getMainLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        OutputStream os = exchange.getResponseBody();
        String website = read.readLine();
        read.close();

        website = website.replaceAll("getlogs:", "");

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

            log.debug("OPTIONS request accepted - returning CORS allow");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);

            return;
        }

        getLogs(exchange, os, website);
    }

    public void getLogs(HttpExchange exchange, OutputStream os, String website) {
        if (!(website == null || website.equalsIgnoreCase(""))) {
            List<JsonObject> logs = new ArrayList<>();
            JsonArray response = new JsonArray();

            try {

                ResultSet data = MySql.onQuery("SELECT * FROM pinglogs WHERE website = '" + website + "'");

                if (data != null) {
                    int i = 0;
                    while (data.next()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", i);
                        obj.addProperty("time", data.getString("timestamp"));
                        obj.addProperty("name", data.getString("website").replaceAll("http://", "").replaceAll("https://", ""));
                        obj.addProperty("success", data.getBoolean("success"));
                        obj.addProperty("ping", data.getInt("ping"));
                        obj.addProperty("log", data.getString("ping_errorcode"));

                        logs.add(obj);
                        i++;
                    }

                    for(int j = logs.size()-1; j >= 0;j-- ){
                        response.add(logs.get(j));
                    }

                    try {
                        exchange.getResponseHeaders().add("Content-Type", "text/plain");
                        exchange.sendResponseHeaders(200, response.toString().getBytes(StandardCharsets.UTF_8).length);
                        os.write(response.toString().getBytes(StandardCharsets.UTF_8));
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    throw new NullPointerException("data Result = null");
                }

            } catch (SQLException | NullPointerException e) {

                try {

                    String errresp = "Something went wrong! - please try again in a few seconds!";
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(500, errresp.getBytes(StandardCharsets.UTF_8).length);
                    os.write(errresp.getBytes(StandardCharsets.UTF_8));
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
