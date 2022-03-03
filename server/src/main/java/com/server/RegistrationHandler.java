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
import java.sql.SQLException;
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

        Headers headers = exchange.getRequestHeaders();
        String contentType = "";
        JSONObject obj = null;
        String response = "";
        int code = 0;

        try {

            if (headers.containsKey("Content-Type")){
                contentType = headers.get("Content-Type").get(0);
                System.out.println("Content-type available");
            } else {
                System.out.println("No Content-Type");
                code = 411;
                response = "No content type in request";
            }

            System.out.println("Content-type is: " + contentType);

            if (contentType.equalsIgnoreCase("application/json")){
                System.out.println("menee tanne");
                InputStream stream = exchange.getRequestBody();

                String newUser = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")); 
                stream.close();

                System.out.println("");

                try {
                    obj = new JSONObject(newUser);                 
                    System.out.println(obj);
                } catch (JSONException e){
                    System.out.println("json parse error, faulty user json");               
                }
                    
                if (obj.getString("username").length() == 0){
                         System.out.println("menee tannekin");
                         code = 412;
                         response = "No user credentials";
                } else if (obj.getString("username").equalsIgnoreCase("SELECT") || 
                    obj.getString("username").equalsIgnoreCase("WHERE") ||
                    obj.getString("username").equalsIgnoreCase("FROM") ||
                    obj.getString("username").equalsIgnoreCase("CREATE TABLE") ||
                    obj.getString("username").equalsIgnoreCase("DROP TABLE") ||
                    obj.getString("username").equalsIgnoreCase("INSERT") ||
                    obj.getString("username").equalsIgnoreCase("UPDATE") ||
                    obj.getString("username").equalsIgnoreCase("DELETE") ||
                    obj.getString("username").equals("*")){
                    code = 400;
                    response = "Not allowed username";               
                } else {
                
                    System.out.println("enta tanne");
                    
                    if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0 || obj.getString("email").length() == 0 ){
                        System.out.println("mutta ei tanne");
                        code = 413;
                        response = "No proper user credentials";
                        
                    } else {
                        System.out.println("user info: " + obj.getString("username") + " " + obj.getString("password") + " " + obj.getString("email"));
                        
                        boolean result;
                        try {
                            result = auth.addUser(obj.getString("username"), obj.getString("password"), obj.getString("email"));
                        } catch (Exception e) {
                            System.out.println("adding user SQL statement failed"); 
                            code = 500;
                            response = "Unable to add user to database";
                            return Arrays.asList(code, response);
                        }

                        if (result == false){
                            
                            code = 405;
                            response = "user already exists";
                        } else {
                            
                            code = 200;
                            System.out.println("User registered");
                            response = "User registered";
                        }
                    }
                } 
            } else {
                code = 407;
                response = "Content type is not application/json.";
            }
        
        } catch (Exception e){
            code = 500;
            response = "internal server error";
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
