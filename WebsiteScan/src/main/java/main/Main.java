package main;

import main.Web.Webserver;
import main.util.ConsoleReadThread;
import main.util.MySql;
import main.util.SQLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

	public static Main INSTANCE;
	public static boolean exit;
	private Thread loop;
	private final Logger logger;

	private final Ping ping;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		INSTANCE = this;
		this.logger = LoggerFactory.getLogger("main.Main");

		MySql.connect();
		SQLManager.initializeDB();

		ping = new Ping();
		startShutdown();
		runLoop(ping);
		try {
			Webserver.runServer();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void runLoop(Ping ping) {
		this.loop = new Thread(() -> {
			if (!this.loop.isInterrupted()) {
				long time = System.currentTimeMillis();

				while (!exit) {
					if (System.currentTimeMillis() >= time + 1000) {
						time = System.currentTimeMillis();

						onSecond(ping);

					}
				}
			}
		});

		this.loop.setName("loop");
		this.loop.start();
	}

	private int min = 0;
	private int sec = 60;
	private boolean minlock = false;

	public void onSecond(Ping ping) {

		if (min % 30 == 0 && !minlock) {
			logger.info("Websiteupdate requested");
			ping.updateWebsites();

			minlock = true;
		}

		if (sec % 30 == 0) {
			logger.info("Pingrequest requested");
			ping.sendPingrequests();

		}

		if (sec <= 0) {
			sec = 60;
			min++;

			minlock = false;

			if (min >= 60) {
				min = 0;
			}
		} else {
			sec--;
		}

	}

	public void startShutdown() {
		new ConsoleReadThread();
	}

	public void onShutdown() {
		if (this.loop != null) {
			this.loop.interrupt();
		}
	}

	public Logger getMainLogger() {
		return logger;
	}

	public Ping getPing() {
		return ping;
	}
}
