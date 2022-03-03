package com.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsParameters;

import java.io.*;

import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.util.Scanner;


public class Server {

    public static void main(String[] args) throws Exception {

        Scanner reader = new Scanner(System.in);

        try {

        UserAuthenticator auth = new UserAuthenticator();
        
        //create the http server to port 8001 with default logger
        HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);

        //create context that defines path for the resource
        final HttpContext finalContext =  server.createContext("/coordinates", new CoordinatesHandler());
        server.createContext("/registration", new RegistrationHandler(auth));

        finalContext.setAuthenticator(auth);

        SSLContext sslContext = coordinatesServerSSLContext(args[0], args[1]);

        server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
            public void configure (HttpsParameters params) {
            InetSocketAddress remote = params.getClientAddress();
            SSLContext c = getSSLContext();
            SSLParameters sslparams = c.getDefaultSSLParameters();
            params.setSSLParameters(sslparams);
            }
           });

        // creates a default executor
        server.setExecutor(Executors.newCachedThreadPool());

        CoordinatesDatabase db = CoordinatesDatabase.getInstance();
        
        db.open("coordinates.db");

        server.start();

        boolean running = true;

        while (running){
            String message = reader.nextLine();
            if (message.equals("/quit")){
                running = false;
                server.stop(3);
                db.closeDB();
            }
        }

        reader.close();

        } catch(Exception e){
             e.printStackTrace();
        }
    }

    private static SSLContext coordinatesServerSSLContext(String keystore, String password) throws Exception{

        char[] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystore), passphrase);

    
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
    
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
    
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    
    }
}
