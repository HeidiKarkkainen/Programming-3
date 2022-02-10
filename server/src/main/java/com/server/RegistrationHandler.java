package com.server;

import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class RegistrationHandler implements HttpHandler{

    private UserAuthenticator user;

    public RegistrationHandler(UserAuthenticator user){

        this.user = user;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

       // System.out.println("Handle called");

        String requestParamValue = null;

        if (t.getRequestMethod().equalsIgnoreCase("POST")){
            boolean userAdded = handlePOSTRequest(t);
            handlePOSTResponse(t, userAdded); 

        // } else if (t.getRequestMethod().equalsIgnoreCase("GET")){
        //     handleGETResponse(t, requestParamValue); 

        } else {
            handleResponse(t, "Not supported");
        }  
    }        
    
    public boolean handlePOSTRequest(HttpExchange t)throws  IOException {

        InputStream stream = t.getRequestBody();
        String username = "";
        String password = "";

        String text = new BufferedReader(new InputStreamReader(stream, 
        StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

        String[] sign = text.split(":");

        if (sign.length == 2) {
            username = sign[0];
            password = sign[1];
        }

        if (username == "" || password == ""){
            System.out.println("bad credentials: " + username + "-" + password);
            byte[] bytes = "Data is not okay".getBytes("UTF-8");
            t.sendResponseHeaders(400, bytes.length);
            OutputStream outputStream = t.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();

            return false;
        }

        boolean success = user.addUser(username, password);

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }  

        return success;
    }

    private void handlePOSTResponse(HttpExchange httpExchange, boolean userAdded)  throws  IOException {     

        if (userAdded){          
            httpExchange.sendResponseHeaders(200, -1);
        } else {
            byte[] bytes = "User already registered".getBytes("UTF-8");
            httpExchange.sendResponseHeaders(403, bytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }
    }

    private void handleGETResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

        byte[] bytes = "Not supported".getBytes("UTF-8");

        httpExchange.sendResponseHeaders(400, bytes.length);
      
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();        
    }

    private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

        byte[] bytes = "Not supported".getBytes("UTF-8");

        httpExchange.sendResponseHeaders(400, bytes.length);
      
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();        
    }   
    
}
