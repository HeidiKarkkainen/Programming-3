package com.server;

import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.ArrayList;


public class Server implements HttpHandler {

    private ArrayList<String> coordinates;
    
    private Server() {
        this.coordinates = new ArrayList<>();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        String requestParamValue = null;

        if (t.getRequestMethod().equalsIgnoreCase("POST")){
            handlePOSTRequest(t);
            handlePOSTResponse(t); 

        } else if (t.getRequestMethod().equalsIgnoreCase("GET")){
            requestParamValue = handleGETRequest(t);
            handleGETResponse(t, requestParamValue); 

        } else {
            handleResponse(t, "Not supported");
        }  
    }        
    
    public void handlePOSTRequest(HttpExchange t){

        InputStream stream = t.getRequestBody();

        String text = new BufferedReader(new InputStreamReader(stream, 
        StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

        coordinates.add(text);

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }

    private void handlePOSTResponse(HttpExchange httpExchange)  throws  IOException {

        httpExchange.sendResponseHeaders(200, -1);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.flush();
        outputStream.close();
    }

    private String handleGETRequest(HttpExchange httpExchange) throws IOException{
        return "";
    }

    private void handleGETResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

        StringBuilder responseCoordinates = new StringBuilder();
 
        for (int i = 0; i < coordinates.size(); i++){
            responseCoordinates.append(coordinates.get(i));
            responseCoordinates.append("\n");
        }

        byte[] bytes = responseCoordinates.toString().getBytes("UTF-8");

        if (responseCoordinates.length() == 0){
            bytes = "No coordinates".getBytes("UTF-8");          
        }

        httpExchange.sendResponseHeaders(200, bytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();        
    }

    private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

        StringBuilder htmlBuilder = new StringBuilder(); 

        htmlBuilder.append(requestParamValue);

        String htmlResponse = htmlBuilder.toString();

        httpExchange.sendResponseHeaders(400, htmlResponse.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();        

    }

    public static void main(String[] args) throws Exception {
        //create the http server to port 8001 with default logger
        HttpServer server = HttpServer.create(new InetSocketAddress(8001),0);
        //create context that defines path for the resource
        server.createContext("/coordinates", new Server());
        // creates a default executor
        server.setExecutor(null); 
        server.start(); 
    }
}
