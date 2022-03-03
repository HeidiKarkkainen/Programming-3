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
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;


public class CoordinatesHandler implements HttpHandler {

    //private ArrayList<UserCoordinate> coordinates;

    CoordinatesHandler() {
        //this.coordinates = new ArrayList<>();
    }

    String response = "";
    int code = 0;
    

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            List<Object> responseInfo= handlePOSTRequest(exchange);
            handlePOSTResponse(exchange, responseInfo);
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            handleGETrequest(exchange);
            handleGETresponse(exchange);
        }
 
    }

    public List<Object> handlePOSTRequest(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String nick = "";
        double longitude = 0.0;
        double latitude = 0.0;
        String timestamp = "";
        String description = "";
        String contentType = "";
        JSONObject obj = null;
        String response = "";
        int code = 0;

        try {

            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
                System.out.println("Content-type available");
            } else {
                System.out.println("No Content-Type");
                code = 411;
                response = "No content type in request";
            }

            System.out.println("Content-type is: " + contentType);

            if (contentType.equalsIgnoreCase("application/json")) {
                System.out.println("menee tanne");
                InputStream stream = exchange.getRequestBody();

                String newCoordinates = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                stream.close();

                try {
                    obj = new JSONObject(newCoordinates);
                    nick = obj.getString("username");
                    longitude = Double.valueOf(obj.getString("longitude"));
                    latitude = Double.valueOf(obj.getString("latitude"));
                    timestamp = obj.getString("sent");
                    description = obj.getString("description");

                    System.out.println(obj);

                } catch (JSONException e) {
                    System.out.println("json parse error, faulty user json");
                    code = 400;
                    response = "Not a user";
                }

                if (obj.getString("username").length() == 0) {
                    System.out.println("jihuu");
                    code = 412;
                    response = "No user credentials";
                } else if  (nick.equalsIgnoreCase("SELECT") || 
                    obj.getString("username").equalsIgnoreCase("WHERE") ||
                    obj.getString("username").equalsIgnoreCase("FROM") ||
                    obj.getString("username").equalsIgnoreCase("CREATE TABLE") ||
                    obj.getString("username").equalsIgnoreCase("DROP") ||
                    obj.getString("username").equalsIgnoreCase("INSERT") ||
                    obj.getString("username").equalsIgnoreCase("UPDATE") ||
                    obj.getString("username").equalsIgnoreCase("DELETE") ||
                    obj.getString("username").equals("*")){
                    code = 400;
                    response = "Not allowed username";
                    System.out.println("ei sallittu username");         
                
                } else if (obj.getString("longitude").length() == 0 || obj.getString("latitude").length() == 0) {
                    System.out.println("jahaa");
                    code = 413;
                    response = "Coordinate(s) missing";

                } else if (longitude < -180 || longitude > 180 ||
                            latitude < -90 || latitude > 90) {
                            System.out.println("ei sallittu koordinaatti");

                } else if  (obj.getString("description").equalsIgnoreCase("SELECT") || 
                        obj.getString("description").equalsIgnoreCase("WHERE") ||
                        obj.getString("description").equalsIgnoreCase("FROM") ||
                        obj.getString("description").equalsIgnoreCase("CREATE TABLE") ||
                        description.equalsIgnoreCase("DROP TABLE") ||
                        obj.getString("description").equalsIgnoreCase("INSERT") ||
                        obj.getString("description").equalsIgnoreCase("UPDATE") ||
                        obj.getString("description").equalsIgnoreCase("DELETE") ||
                        obj.getString("description").equals("*")){
                        code = 400;
                        response = "Not allowed description"; 

                } else {  

                    try{
                        System.out.println("jee, tanne paatyi");
                        UserCoordinate c = new UserCoordinate(nick, latitude, longitude, timestamp, description);
                        CoordinatesDatabase.getInstance().setCoordinates(obj);
                        code = 200;
                        response = "Coordinates added";

                    } catch(NumberFormatException e){
                        code = 400;
                        response = "Bad request";
                        System.out.println("Not a double");
                    } catch(NullPointerException e){
                        code = 400;
                        response = "Not coordinate(s)";
                        System.out.println("String is null");
                    } catch (DateTimeParseException e) {
                        code = 400;
                        response = "Not timestamp";
                        System.out.println("Bad timestamp");
                    } catch (ArithmeticException e) {
                        code = 400;
                        response = "Unacceptable timestamp value";
                        System.out.println("Bad timestamp");
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
            response = "internal server error";
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

    private void handleGETrequest(HttpExchange exchange) throws IOException {

        try {

            JSONArray responseCoordinates = CoordinatesDatabase.getInstance().getCoordinates();
            //exchange.getPrincipal().getUsername()

            System.out.println("Namakin tulostuu: " + responseCoordinates);
            code = 200;
            response = responseCoordinates.toString(responseCoordinates.length());

            System.out.print("JSONstring: " + response);

        } catch (Exception e) {
            code = 500;
            response = "internal server error";
        }
    }

    private void handleGETresponse(HttpExchange exchange) throws IOException {

        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

        //JSONArray responseCoordinates = CoordinatesDatabase.getInstance().getCoordinates(exchange.getPrincipal().getUsername());
            
    
}
