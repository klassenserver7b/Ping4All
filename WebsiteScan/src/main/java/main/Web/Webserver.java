package main.Web;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class Webserver {


    public static HttpServer server;
    public static final int HTTP_PORT = 80;

    public static void runServer() throws IOException {

        server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/addWebsite", new WebsiteAddHandler());
        server.createContext("/removeWebsite", new WebsiteRemoveHandler());
        server.createContext("/updateWebsiteList", new WebsiteUpdateHandler());
        server.createContext("/activate", new WebsiteActivateHandler());
        server.createContext("/getlogs", new GetLogsHandler());
        server.setExecutor(null);
        server.start();

    }



}


