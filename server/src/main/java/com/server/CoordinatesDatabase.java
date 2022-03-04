package com.server;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import java.security.SecureRandom;

import org.apache.commons.codec.digest.Crypt; 

import org.json.*;

public class CoordinatesDatabase {

    private Connection dbConnection = null;
    private static CoordinatesDatabase dbInstance = null;
    String dbName;
    boolean dbExists = false;
    SecureRandom secureRandom = new SecureRandom();

    public static synchronized CoordinatesDatabase getInstance(){
        if (null == dbInstance){
            dbInstance = new CoordinatesDatabase();
        }
        return dbInstance;
    }

    private CoordinatesDatabase(){

    }

    public void open(String dbName) throws SQLException{

        this.dbName = dbName;

        File file = new File(dbName);
        
        if (file.isFile()){
            dbExists = true;
            String database = "jdbc:sqlite:" + dbName;
            dbConnection = DriverManager.getConnection(database);
        } else {
            initializeDatabase(dbName);            
        }

        if (dbConnection == null) {
            System.out.println("can't open database!");
        }
    }
    
    private void initializeDatabase(String dbName) throws SQLException{

        String database = "jdbc:sqlite:" + dbName;

        dbConnection = DriverManager.getConnection(database);

        if (null != dbConnection){
            String createBasicDB = "create table users (username varchar(50) NOT NULL PRIMARY KEY, password varchar(50) NOT NULL, salt varchar(500) NOT NULL, email varchar(50));" +
            "create table coordinates (nick varchar(50) NOT NULL, longitude varchar(50) NOT NULL, latitude varchar(50) NOT NULL, time INTEGER NOT NULL, description varchar(1024), PRIMARY KEY(nick, longitude, latitude, time))";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createBasicDB);
            createStatement.close();
            System.out.println("Database successfully created");
        }
    }

    public void closeDB() throws SQLException{
        if (null != dbConnection){
            dbConnection.close();
            dbConnection = null;
            System.out.println("Closing db connection");
        }
    }

    public synchronized boolean setUser(JSONObject user) throws SQLException {

        if(checkIfUserExists(user.getString("username"))){
            return false;
        }

        byte[] bytes = new byte[13];
        secureRandom.nextBytes(bytes);

        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes; 

        String hashedPassword = Crypt.crypt(user.getString("password"), salt);

		String setUserString = "insert into users " +
					"VALUES('" + user.getString("username") + 
                    "','" + hashedPassword +
                    "','" + salt +
                    "','" + user.getString("email") + 
                    "')"; 
         
		Statement createStatement;
		createStatement = dbConnection.createStatement();
		createStatement.executeUpdate(setUserString);
		createStatement.close();

        return true;
    }

    public boolean checkIfUserExists(String givenUsername) throws SQLException{

        Statement queryStatement = null;
        ResultSet rs = null;

        String checkUser = "select username from users where username = '" + givenUsername + "'";

        try {
            queryStatement = dbConnection.createStatement();
        } catch (SQLException e) {
            System.out.println("db connection failed");
            throw e;
        }

        try {
            rs = queryStatement.executeQuery(checkUser);            
        } catch (Exception e) {
            System.out.println("query failed");
            throw e;
        }

        if(rs.next()){
            System.out.println("user exists");
            return true;
        }else{
            System.out.println("added user " + givenUsername);
            return false;
        }
        
    }

    public boolean authenticateUser(String givenUsername, String givenPassword) throws SQLException{

        Statement queryStatement = null;
        ResultSet rs;

        String getMessagesString = "select username, password from users where username = '" + givenUsername + "'";

        queryStatement = dbConnection.createStatement();
		rs = queryStatement.executeQuery(getMessagesString);

        if(rs.next() == false){
            return false;
        }else {
            String password = rs.getString("password");
           if (password.equals(Crypt.crypt(givenPassword, password))) {
                return true;
            } else {
                return false;
            }
        }    
    }
        
    public void setCoordinates(JSONObject coordinates) throws SQLException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        OffsetDateTime time = null;
        long unixTime;
        String description = "";

        time = OffsetDateTime.parse(coordinates.getString("sent"), formatter);
        unixTime = time.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
       
        Double.parseDouble(coordinates.getString("longitude"));
        Double.parseDouble(coordinates.getString("latitude"));

        if (coordinates.getString("description").length() == 0){
            description = "nodata";
        } else {
            description = coordinates.getString("description");
        }
        
        String setCoordinatesString = "insert into coordinates " +
					"VALUES('" +
                    coordinates.getString("username") +
                    "','" +
                    coordinates.getString("longitude") +
                    "','" +
                    coordinates.getString("latitude") +
                    "','" +
                    unixTime +
                    "','" +
                    description +
                    "')"; 
        
        Statement createStatement;

        try {
            createStatement = dbConnection.createStatement();
            
        } catch (Exception e) {
            System.out.println("dbConnection.createStatement FAILED");
            throw e;
        }
        try {
            createStatement.executeUpdate(setCoordinatesString);
            
        } catch (Exception e) {
            System.out.println("createStatement.executeUpdate FAILED");
            throw e;
        }
		createStatement.close();
    }
    
    public JSONArray getCoordinates() throws SQLException {

        Statement queryStatement = null;
        
        JSONArray array = new JSONArray();

        String getCoordinatesString = "select nick, longitude, latitude, time, description from coordinates ";

        queryStatement = dbConnection.createStatement();
		ResultSet rs = queryStatement.executeQuery(getCoordinatesString);

        while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("nick", rs.getString("nick"));
            obj.put("longitude", rs.getString("longitude"));
            obj.put("latitude", rs.getString("latitude"));
            obj.put("description", rs.getString("description"));
            obj.put("sent", OffsetDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong("time")), ZoneOffset.UTC));
            
            array.put(obj);
		}
        
        return array;

    }

    public JSONArray getCoordinates2(JSONObject timequery) throws SQLException {

        Statement queryStatement = null;
        
        JSONArray array = new JSONArray();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        OffsetDateTime startTime = null;
        OffsetDateTime endTime = null;
        long unixTime1;
        long unixTime2;

        startTime = OffsetDateTime.parse(timequery.getString("timestart"), formatter);
        unixTime1 = startTime.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();

        endTime = OffsetDateTime.parse(timequery.getString("timeend"), formatter);
        unixTime2 = endTime.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();

        String getCoordinatesString = "select nick, longitude, latitude, time, description from coordinates " +
        "where time between '" + unixTime1 + "'AND'" + unixTime2 + "'"; 
        
        queryStatement = dbConnection.createStatement();
		ResultSet rs = queryStatement.executeQuery(getCoordinatesString);

        while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("nick", rs.getString("nick"));
            obj.put("longitude", rs.getString("longitude"));
            obj.put("latitude", rs.getString("latitude"));
            obj.put("description", rs.getString("description"));
            obj.put("sent", OffsetDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong("time")), ZoneOffset.UTC));
            
            array.put(obj);
		}
        return array;
    }

    public JSONArray getCoordinates3(String name) throws SQLException {

        Statement queryStatement = null;
        
        JSONArray array = new JSONArray();

        String getCoordinatesString = "select nick, longitude, latitude, time, description from coordinates " +
        "where nick = '" + name + "'"; 
        
        queryStatement = dbConnection.createStatement();
		ResultSet rs = queryStatement.executeQuery(getCoordinatesString);

        while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("nick", rs.getString("nick"));
            obj.put("longitude", rs.getString("longitude"));
            obj.put("latitude", rs.getString("latitude"));
            obj.put("description", rs.getString("description"));
            obj.put("sent", OffsetDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong("time")), ZoneOffset.UTC));            
            array.put(obj);
		}
        
        return array;
    }
}
