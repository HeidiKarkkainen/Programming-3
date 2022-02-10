package com.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsParameters;

import java.io.*;

import java.net.InetSocketAddress;
import java.security.KeyStore;


import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;


public class Server {

    public static void main(String[] args) throws Exception {

        try {

        UserAuthenticator auth = new UserAuthenticator("coordinates");
        
        //create the http server to port 8001 with default logger
        HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);

        //create context that defines path for the resource
        final HttpContext finalContext =  server.createContext("/coordinates", new CoordinatesHandler());
        server.createContext("/registration", new RegistrationHandler(auth));

        finalContext.setAuthenticator(auth);

        SSLContext sslContext = chatServerSSLContext();

        server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
            public void configure (HttpsParameters params) {
            InetSocketAddress remote = params.getClientAddress();
            SSLContext c = getSSLContext();
            SSLParameters sslparams = c.getDefaultSSLParameters();
            params.setSSLParameters(sslparams);
            }
           });

        // creates a default executor
        server.setExecutor(null); 
        server.start();
           
        } catch(Exception e){
             e.printStackTrace();
         }
    }

    private static SSLContext chatServerSSLContext() throws Exception{

        char[] passphrase = "venetsia".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("/Users/heidikarkkainen/ws/programming3/group-0092-project/server/keystore.jks"), passphrase);
    
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
    
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
    
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    
    }
}
