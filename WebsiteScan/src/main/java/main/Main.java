package main;

import main.Web.Webserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.util.MySql;
import main.util.SQLManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static Main INSTANCE;
    public static boolean exit;
    private Thread loop;
    private Thread shutdownT;
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
        Shutdown();
        runLoop(ping);
        try {
            Webserver.runServer();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void Shutdown() {
        this.shutdownT = new Thread(() -> {
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                while ((line = reader.readLine()) != null) {
                    if (line.equalsIgnoreCase("exit")) {
                        logger.info("Schutdown initiated");
                        exit = true;

                        onShutdown();
                        reader.close();
                        break;
                    }
                    System.out.println("Use Exit to Shutdown");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.shutdownT.setName("Shutdown");
        this.shutdownT.start();
    }

    public void onShutdown() {
        if (this.loop != null) {
            this.loop.interrupt();
        }
        if (shutdownT != null) {
            this.shutdownT.interrupt();
        }
        if(Webserver.server!=null) {
            Webserver.server.stop(20);
        }
    }

    public Logger getMainLogger() {
        return logger;
    }

    public Ping getPing(){
        return ping;
    }
}
