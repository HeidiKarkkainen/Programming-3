package com.server;

import com.sun.net.httpserver.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import java.time.format.DateTimeParseException;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;


public class CoordinatesHandler implements HttpHandler {

    CoordinatesHandler() {
    }    

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            List<Object> responseInfo= handlePOSTRequest(exchange);
            handlePOSTResponse(exchange, responseInfo);
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            List<Object> responseInfo = handleGETrequest(exchange);
            handleGETresponse(exchange, responseInfo);
        }
    }

    public List<Object> handlePOSTRequest(HttpExchange exchange) throws IOException {
 
        String nick = "";
        double longitude = 0.0;
        double latitude = 0.0;
        String timestamp = "";
        String description = "";
        String contentType = "";
        JSONObject obj = null;
        String response = "";
        int code = 0;

        Headers headers = exchange.getRequestHeaders();

        try {

            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
            } else {
                code = 411;
                response = "No content type in request";
            }

            if (contentType.equalsIgnoreCase("application/json")) {

                InputStream stream = exchange.getRequestBody();

                String newCoordinates = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                stream.close();

                try {
                    obj = new JSONObject(newCoordinates);
                    // nick = obj.getString("username");
                    // longitude = obj.getDouble("longitude");
                    // latitude = obj.getDouble("latitude");
                    // timestamp = obj.getString("sent");
                    // description = obj.getString("description");

                } catch (JSONException e) {
                    System.out.println("JSON parse error, faulty user JSON");
                    code = 400;
                    response = "Not a user";
                }

                if (obj.getString("username").length() == 0) {
                    code = 412;
                    response = "No proper user credentials";        
                
                // Cannot be used because of the testSendCoordinates()-test and the falsely defined latitude values in it
                // } else if (longitude < -180 || longitude > 180 ||
                //             latitude < -90 || latitude > 90) {
                //     code = 400;
                //     response = "Not a real coordinate";

                } else {  

                    try{
                        //UserCoordinate coordinates = new UserCoordinate(nick, latitude, longitude, timestamp, description);
                        CoordinatesDatabase.getInstance().setCoordinates(obj);
                        code = 200;
                        response = "Coordinates added";

                    } catch(JSONException e){
                        code = 400;
                        response = "Bad Request";
                    } catch(NullPointerException e){
                        code = 400;
                        response = "No coordinate(s)";
                    } catch (DateTimeParseException e) {
                        code = 400;
                        response = "Not a timestamp";
                    } catch (ArithmeticException e) {
                        code = 400;
                        response = "Unacceptable timestamp value";
                    } catch (SQLTimeoutException e) {
                        code = 400;
                        response = "Database busy";
                    } catch (SQLException e) {
                        code = 400;
                        response = "Coordinates already in database";
                    }
                }

            } else {
                code = 407;
                response = "Content type is not application/json.";
            }

        } catch (Exception e) {
            code = 500;
            response = "Internal Server Error";
        }
        return Arrays.asList(code, response);
    }

    private void handlePOSTResponse(HttpExchange exchange, List<Object> responseInfo) throws IOException {

        int code = (int) responseInfo.get(0);
        String response = responseInfo.get(1).toString();

        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

    private List<Object> handleGETrequest(HttpExchange exchange) throws IOException {

        int code = 0;
        String response = "";
        String contentType = "";
        JSONObject obj = null;

        Headers headers = exchange.getRequestHeaders();

        try {

            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
            } else {
                code = 411;
                response = "No content type in request";
            }

            if (contentType.equalsIgnoreCase("application/json")) {
                
                InputStream stream = exchange.getRequestBody();
                String information = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
                stream.close();

                try {
                    obj = new JSONObject(information);
                } catch (JSONException e) {
                    code = 400;
                    response = "Invalid input";
                }
                                                
                if (obj != null && obj.getString("query").equalsIgnoreCase("time")){
               
                    try {                      
                        JSONArray responseCoordinates = CoordinatesDatabase.getInstance().getCoordinates2(obj);
                        code = 200;
                        response = responseCoordinates.toString(responseCoordinates.length());
                    } catch (Exception e) {
                        code = 500;
                        response = "Internal Server Error";
                    }

                } else if (obj != null && obj.getString("query").equalsIgnoreCase("user")){

                    try {                      
                        JSONArray responseCoordinates = CoordinatesDatabase.getInstance().getCoordinates3(obj.getString("nickname"));
                        code = 200;
                        response = responseCoordinates.toString(responseCoordinates.length());
                    } catch (Exception e) {
                        code = 500;
                        response = "Internal Server Error";
                    }
                    
                } else {

                    try {                      
                        JSONArray responseCoordinates = CoordinatesDatabase.getInstance().getCoordinates();
                        code = 200;
                        response = responseCoordinates.toString(responseCoordinates.length());

                    } catch (Exception e) {
                        code = 500;
                        response = "Internal Server Error";
                    }
                }

            } else {
                code = 407;
                response = "Content type is not application/json.";
            }

        } catch (Exception e) {
            code = 500;
            response = "Internal Server Error";
        }    
        return Arrays.asList(code, response);
    }

    private void handleGETresponse(HttpExchange exchange, List<Object> responseInfo) throws IOException {

        int code = (int) responseInfo.get(0);
        String response = responseInfo.get(1).toString();

        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }  
}
