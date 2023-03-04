package main.util;

import main.Main;
import main.Web.Webserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleReadThread implements Runnable {

	private final Thread t;
	private final Logger log;
	private final BufferedReader reader;
	private final InputStreamReader sysinr;

	public ConsoleReadThread() {
		log = LoggerFactory.getLogger(this.getClass());

		sysinr = new InputStreamReader(System.in);
		reader = new BufferedReader(sysinr);
		t = new Thread(this, "ConsoleReadThread");
		t.start();
	}

	@Override
	public void run() {

		while (!t.isInterrupted()) {
			try {

				String line;

				if (System.in.available() == 0 || !sysinr.ready()) {
					continue;
				}
				if ((line = reader.readLine()) != null) {
					interpretConsoleContent(line);
				}

				Thread.sleep(5000);

			} catch (InterruptedException | IOException e) {

				if (e.getMessage().equalsIgnoreCase("Stream closed")) {
					t.interrupt();
					break;
				}
				log.error(e.getMessage(), e);
			}
		}

	}

	public void interpretConsoleContent(String s) {

		String[] commandargs = s.split(" ");

		switch (commandargs[0].toLowerCase()) {
		case "exit" -> onShutdown();

		case "help" -> log.info("Use Exit to Shutdown");

		default -> System.out.println("Use Exit to Shutdown");

		}

	}

	public void onShutdown() {
		Main.INSTANCE.onShutdown();
		if (t != null) {
			this.t.interrupt();
		}
		if (Webserver.server != null) {
			Webserver.server.stop(20);
		}
	}

}