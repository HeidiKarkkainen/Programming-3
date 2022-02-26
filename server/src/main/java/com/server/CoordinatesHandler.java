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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class CoordinatesHandler implements HttpHandler {

    private ArrayList<UserCoordinate> coordinates;

    CoordinatesHandler() {
        this.coordinates = new ArrayList<>();
    }

    String contentType = "";
    String response = "";
    int code = 0;
    JSONObject obj = null;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            handlePOSTRequest(exchange);
            handlePOSTResponse(exchange);

        }

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            handleGET(exchange);

        }

    }

    public void handlePOSTRequest(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String nick = "";
        String longitude = "";
        String latitude = "";
        String timestamp = "";

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
                    longitude = obj.getString("longitude");
                    latitude = obj.getString("latitude");
                    timestamp = obj.getString("sent");

                    System.out.println(obj);

                } catch (JSONException e) {
                    System.out.println("json parse error, faulty user json");
                    code = 400;
                    response = "Not a user";
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                OffsetDateTime time = null;
                LocalDateTime timeLocal = null;

                try {
                    time = OffsetDateTime.parse(timestamp, formatter);
                } catch (DateTimeParseException e) {
                }

                if (time != null) {
                    timeLocal = time.toLocalDateTime();
                }

                if (obj.getString("username").length() == 0) {
                    System.out.println("jihuu");
                    code = 412;
                    response = "No user credentials";
                } else if (obj.getString("longitude").length() == 0 || obj.getString("latitude").length() == 0) {
                    System.out.println("jahaa");
                    code = 413;
                    response = "Coordinate(s) missing";
                } else if (timeLocal == null) {
                    code = 413;
                    response = "Timestamp missing";
                } else {
                    System.out.println("jee, tanne paatyi");
                    UserCoordinate c = new UserCoordinate(nick, latitude, longitude, timestamp);
                    CoordinatesDatabase.getInstance().setCoordinates(obj);
                    code = 200;
                    response = "Coordinates added";
                }

            } else {
                code = 407;
                response = "Content type is not application/json.";
            }

        } catch (Exception e) {
            code = 500;
            response = "internal server error";
        }
    }

    private void handlePOSTResponse(HttpExchange exchange) throws IOException {

        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();

    }

    private void handleGET(HttpExchange exchange) throws IOException {

        try {

            JSONArray responseCoordinates = CoordinatesDatabase.getInstance().getCoordinates(exchange.getPrincipal().getUsername());

                System.out.println("Namakin tulostuu: " + responseCoordinates);

                String string = responseCoordinates.toString(responseCoordinates.length());

                System.out.print("JSONstring: " + string);

                byte[] bytes = string.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            

        } catch (Exception e) {
            code = 500;
            response = "internal server error";
        }
    }
}
