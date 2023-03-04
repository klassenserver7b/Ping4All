package main;

import main.util.MySql;
import main.util.StringboolBuffer;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

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
		try (ResultSet set = MySql.onQuery("SELECT * From websitescan.websites")) {

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
				logger.debug("pinged " + key + " - " + buff.message());
			}

		});

		String otime = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		System.out.println(otime.replaceAll("T", " ").substring(0, 19));

		request.keySet().forEach(key -> {
			String time = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			time = time.replaceAll("T", " ").substring(0, 19);
			StringboolBuffer buff = request.get(key);

			try {
				MySql.onUpdate("INSERT INTO pinglogs(website,timestamp,success,ping,ping_errorcode) VALUES(?,?,?,?,?)",
						key, time, buff.bool(), buff.elapsedTime(), buff.message());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});

	}

	public StringboolBuffer ping(String adress) {

		if (!adress.startsWith("http")) {
			adress = "http://" + adress;
		}

		logger.debug("pinging " + adress);

		HttpGet httpget = new HttpGet(adress);
		StringboolBuffer buff;

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			long timeBefore = System.currentTimeMillis();
			CloseableHttpResponse response = httpclient.execute(httpget);
			long elapsedTime = System.currentTimeMillis() - timeBefore;

			if (response.getStatusLine().getStatusCode() != 200) {
				String message = "REQUEST FAILED - ERROR CODE: " + response.getStatusLine().getStatusCode();
				buff = new StringboolBuffer(message, false, 0);
			} else {
				String message = ("REQUEST SUCCESSFUL - ELAPSED TIME: " + elapsedTime + "ms");
				buff = new StringboolBuffer(message, true, elapsedTime);
			}
			return buff;

		} catch (UnknownHostException e) {
			buff = new StringboolBuffer("REQUEST FAILED - UNKNOWN HOST", false, 0);
			return buff;
		} catch (ClientProtocolException e) {

			buff = new StringboolBuffer("HTTP PROTOCOL ERROR", false, 0);
			return buff;
		} catch (IOException e) {
			buff = new StringboolBuffer("ERROR - CONNECTION ABORTED", false, 0);
			return buff;
		}

	}

	public ConcurrentHashMap<String, Boolean> getWebsites() {
		return fulllist;
	}

}