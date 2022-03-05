package com.server;

import com.sun.net.httpserver.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;

public class RegistrationHandler implements HttpHandler{

    private UserAuthenticator auth;

    public RegistrationHandler(UserAuthenticator newAuth){

        auth = newAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")){

            List<Object> responseInfo = handlePOSTRequest(exchange);
            handlePOSTResponse(exchange, responseInfo);

        } else {
            handleResponse(exchange);
        }       
    }        
    
    public List<Object> handlePOSTRequest(HttpExchange exchange)throws IOException {

        String contentType = "";
        JSONObject obj = null;
        String response = "";
        int code = 0;

        Headers headers = exchange.getRequestHeaders();

        try {

            if (headers.containsKey("Content-Type")){
                contentType = headers.get("Content-Type").get(0);
            } else {
                code = 411;
                response = "No content type in request";
            }

            if (contentType.equalsIgnoreCase("application/json")){
                InputStream stream = exchange.getRequestBody();

                String newUser = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")); 
                stream.close();

                try {
                    obj = new JSONObject(newUser);                 
                } catch (JSONException e){
                    System.out.println("JSON parse error, faulty user JSON");               
                }
                    
                if (obj.getString("username").length() == 0){
                    code = 412;
                    response = "No proper user credentials";
             
                } else {
 
                    if (obj.getString("password").length() == 0 || obj.getString("email").length() == 0 ){
                        code = 412;
                        response = "No proper user credentials";
                        
                    } else {
                        
                        boolean result;

                        try {
                            result = auth.addUser(obj.getString("username"), obj.getString("password"), obj.getString("email"));
                        } catch (Exception e) {
                            code = 500;
                            response = "Unable to add user to database";
                            return Arrays.asList(code, response);
                        }

                        if (result == false){
                            code = 405;
                            response = "User already exists";
                        } else {                       
                            code = 200;
                            response = "User registered";
                        }
                    }
                } 
            } else {
                code = 407;
                response = "Content type is not application/json";
            }
        
        } catch (Exception e){
            code = 500;
            response = "Internal Server Error";
        }
        return Arrays.asList(code, response);
    }

    private void handlePOSTResponse(HttpExchange exchange, List<Object> responseInfo)  throws  IOException {     

            int code = (int) responseInfo.get(0);
            String response = responseInfo.get(1).toString();

            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();    
    }

    private void handleResponse(HttpExchange httpExchange)  throws  IOException {

        byte[] bytes = "Only POST is accepted".getBytes("UTF-8");

        httpExchange.sendResponseHeaders(401, bytes.length);
      
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();        
    }      
}
