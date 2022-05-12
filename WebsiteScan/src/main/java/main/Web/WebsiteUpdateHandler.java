package main.Web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.Main;
import main.Ping;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class WebsiteUpdateHandler implements HttpHandler {
    private final Logger log = Main.INSTANCE.getMainLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        BufferedReader read = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        OutputStream os = exchange.getResponseBody();
        read.close();

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

            log.debug("OPTIONS request accepted - returning CORS allow");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);

            return;
        }

        sendWebsites(exchange, os);
    }

    public void sendWebsites(HttpExchange exchange, OutputStream os) {
        JsonObject response = new JsonObject();
        JsonObject head = new JsonObject();

        JsonArray sites = new JsonArray();

        Ping ping = Main.INSTANCE.getPing();
        ping.updateWebsites();

        head.addProperty("time", OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        ConcurrentHashMap<String, Boolean> websites = ping.getWebsites();

        ConcurrentHashMap.KeySetView<String, Boolean> view = websites.keySet();

        for (int i = 0; i < view.size(); i++) {

            String website = String.valueOf(view.toArray()[i]);
            String[] urlsplit = website.split("\\.");

            int l = urlsplit.length;

            String sld = urlsplit[l-2];
            String name;

            if(sld.startsWith("https://")){
                name = upperCaseFirst(sld.substring(8));
            }else if(sld.startsWith("http://")) {
                name = upperCaseFirst(sld.substring(7));
            }else{
                name = sld;
            }

            JsonObject obj = new JsonObject();

            obj.addProperty("id", i);
            obj.addProperty("name", name);
            obj.addProperty("url", website);
            obj.addProperty("enabled", websites.get(website));

            sites.add(obj);

        }

        response.add("head",head);
        response.add("sites", sites);

        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.toString().getBytes(StandardCharsets.UTF_8).length);
            os.write(response.toString().getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public String upperCaseFirst(String str) {

        char[] arr = str.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);

        return new String(arr);
    }
}
