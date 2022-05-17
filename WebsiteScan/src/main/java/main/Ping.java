package main;

import main.util.StringboolBuffer;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import main.util.MySql;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class Ping {
    private final ConcurrentHashMap<String, Boolean> fulllist = new ConcurrentHashMap<>();
    public Logger logger = Main.INSTANCE.getMainLogger();


    public void updateWebsites() {

        fulllist.clear();
        ResultSet set;
        try {

            set = MySql.onQuery("SELECT * From websitescan.websites");

            if (set != null) {
                while (set.next()) {
                    boolean isenabled = set.getBoolean("enabled");
                    String website = set.getString("website");
                    fulllist.put(website, isenabled);

                }
            }

            logger.info("Successfully updated " + fulllist.size() + " websites");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public void sendPingrequests() {
        ConcurrentHashMap<String, StringboolBuffer> request = new ConcurrentHashMap<>();

        fulllist.keySet().forEach(key -> {

            if (fulllist.get(key)) {
                StringboolBuffer buff = ping(key);
                request.put(key, buff);
                logger.debug("pinged " + key + " - " + buff.getString());
            }

        });

        String otime = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.println(otime.replaceAll("T", " ").substring(0, 19));

        request.keySet().forEach(key -> {
            String time = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            time = time.replaceAll("T", " ").substring(0, 19);
            StringboolBuffer buff = request.get(key);

            try {
                MySql.onUpdate("INSERT INTO pinglogs(website,timestamp,success,ping,ping_errorcode)" +
                        " VALUES('" + key + "', '"
                        + time + "', "
                        + buff.getBoolean() + ", "
                        + buff.getElapsedtime()+", '"
                        + buff.getString() + "')");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });


    }

    public StringboolBuffer ping(String adress) {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        if (!adress.startsWith("http")) {
            adress = "http://" + adress;
        }

        logger.debug("pinging " + adress);

        HttpGet httpget = new HttpGet(adress);
        StringboolBuffer buff = new StringboolBuffer();
        try {
            long timebefore = System.currentTimeMillis();
            CloseableHttpResponse response = httpclient.execute(httpget);
            long elapsedtime = System.currentTimeMillis() - timebefore;

            if (response.getStatusLine().getStatusCode() != 200) {
                buff.setBool(false);
                buff.setString("REQUEST FAILED - ERROR CODE: " + response.getStatusLine().getStatusCode());
            } else {
                buff.setBool(true);
                buff.setString("REQUEST SUCCESSFUL - ELAPSED TIME: " + elapsedtime + "ms");
                buff.setTime(elapsedtime);
            }
            return buff;

        } catch (UnknownHostException e) {
            buff.setBool(false);
            buff.setString("REQUEST FAILED - UNKNOWN HOST");
            return buff;
        } catch (ClientProtocolException e) {
            buff.setBool(false);
            buff.setString("HTTP PROTOCOL ERROR");
            return buff;
        } catch (IOException e) {
            buff.setBool(false);
            buff.setString("ERROR - CONNECTION ABORTED");
            return buff;
        }

    }

    public ConcurrentHashMap<String, Boolean> getWebsites() {
        return fulllist;
    }

}