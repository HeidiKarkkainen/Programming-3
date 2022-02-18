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
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

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

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")){
   
            handlePOSTRequest(exchange);
            handlePOSTResponse(exchange);

        } 
        
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")){

            handleGET(exchange);
            //handleGETResponse(exchange); 
        }    
        // } else {
        //      handleResponse(exchange);
        // }  
        
    }        
    
    public void handlePOSTRequest(HttpExchange exchange)throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String nick = "";
        String longitude = "";
        String latitude = "";

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

                String newCoordinates = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")); 
                stream.close();

                try {
                    obj = new JSONObject(newCoordinates);  
                    nick = obj.getString("username"); 
                    longitude = obj.getString("longitude");
                    latitude = obj.getString("latitude");
  
                    System.out.println(obj);

                } catch (JSONException e){
                    System.out.println("json parse error, faulty user json");  
                    code = 400;
                    response = "Not a user";             
                }                   
                   
                if  (obj.getString("username").length() == 0 ){
                    System.out.println("jihuu");
                    code = 412;
                    response = "No user credentials";
                } else if (obj.getString("longitude").length() == 0 || obj.getString("latitude").length() == 0) {
                    System.out.println("jahaa");
                    code = 413;
                    response = "Coordinate(s) missing";
                        
                } else {                 
                    System.out.println ("jee, tanne paatyi");
                    UserCoordinate c = new UserCoordinate(nick, latitude, longitude);
                    coordinates.add(c);
                    code = 200;
                    response = "Coordinates added";
                }                 
            
            } else {
                code = 407;
                response = "Content type is not application/json.";
            }

        } catch (Exception e){
            code = 500;
            response = "internal server error";
        }      
    }

    // @Override
    // public void handle(HttpExchange t) throws IOException {

    //     String requestParamValue = null;

    //     if (t.getRequestMethod().equalsIgnoreCase("POST")){
    //         handlePOSTRequest(t);
    //         handlePOSTResponse(t); 

    //     } else if (t.getRequestMethod().equalsIgnoreCase("GET")){
    //         requestParamValue = handleGETRequest(t);
    //         handleGETResponse(t, requestParamValue); 

    //     } else {
    //         handleResponse(t, "Not supported");
    //     }  
    // }        
    

    // private void handleGETResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

    //     StringBuilder responseCoordinates = new StringBuilder();
 
    //     for (int i = 0; i < coordinates.size(); i++){
    //         responseCoordinates.append(coordinates.get(i));
    //         responseCoordinates.append("\n");
    //     }

    //     byte[] bytes = responseCoordinates.toString().getBytes("UTF-8");

    //     if (responseCoordinates.length() == 0){
    //         bytes = "No coordinates".getBytes("UTF-8");          
    //     }

    //     httpExchange.sendResponseHeaders(200, bytes.length);
    //     OutputStream outputStream = httpExchange.getResponseBody();
    //     outputStream.write(bytes);
    //     outputStream.flush();
    //     outputStream.close();        
    // }

    // private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

    //     StringBuilder htmlBuilder = new StringBuilder(); 

    //     htmlBuilder.append(requestParamValue);

    //     String htmlResponse = htmlBuilder.toString();

    //     httpExchange.sendResponseHeaders(400, htmlResponse.length());
    //     OutputStream outputStream = httpExchange.getResponseBody();
    //     outputStream.write(htmlResponse.getBytes());
    //     outputStream.flush();
    //     outputStream.close();        

    // } 

    private void handlePOSTResponse(HttpExchange exchange)  throws  IOException {     

        // if (userAdded){          
        //     httpExchange.sendResponseHeaders(200, -1);
        // } else {
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        
    }

    private void handleGET(HttpExchange exchange) throws IOException{

        JSONArray responseCoordinates = new JSONArray();

        if (coordinates.isEmpty()){ 
            exchange.sendResponseHeaders(204, -1);
        }
        else {
            

            for (int i = 0; i < coordinates.size(); i++){
                JSONObject obj2 = new JSONObject();
                obj2.put("username", coordinates.get(i).nick);
                obj2.put("longitude", coordinates.get(i).longitude);
                obj2.put("latitude", coordinates.get(i).latitude);
                responseCoordinates.put(obj2);
                System.out.println("Nama tulostuu: " + obj2);
            }

            System.out.println("Namakin tulostuu: " + responseCoordinates);

            String jsonstring = responseCoordinates.toString(responseCoordinates.length());
            
            System.out.print("JSONstring: " + jsonstring);
    
            byte[] bytes = jsonstring.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();

        }


    }

}

