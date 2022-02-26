package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.*;

public class CoordinatesDatabase {

    private Connection dbConnection = null;
    private static CoordinatesDatabase dbInstance = null;
    String dbName;
    boolean dbExists = false;

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
            String createBasicDB = "create table users (username varchar(50) NOT NULL PRIMARY KEY, password varchar(50) NOT NULL, email varchar(50));" +
            "create table coordinates (username varchar(50) NOT NULL, longitude varchar(50) NOT NULL, latitude varchar(50) NOT NULL, time varchar(50) NOT NULL, PRIMARY KEY(username, longitude, latitude, time))";
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

    public boolean setUser(JSONObject user) throws SQLException {

        if(checkIfUserExists(user.getString("username"))){
            return false;
        }

		String setUserString = "insert into users " +
					"VALUES('" + user.getString("username") + "','" + user.getString("password")+ "','" + user.getString("email") + "')"; 

                    
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
            System.out.println("checkIfUserExists: db connection failed");
            throw e;
        }

        try {
            rs = queryStatement.executeQuery(checkUser);            
        } catch (Exception e) {
            System.out.println("checkIfUserExists: query failed");
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

            System.out.println("cannot find such user");
            return false;

        }else{
            String pass = rs.getString("password");

            if(pass.equals(givenPassword)){
                return true;
            }else{
                return false;
            }
        }    
    }
        

    public void setCoordinates(JSONObject coordinates) throws SQLException {

        System.out.println("setCoordinates: " + coordinates.toString());

		String setCoordinatesString = "insert into coordinates " +
					"VALUES('" +
                    coordinates.getString("username") +
                    "','" +
                    coordinates.getString("longitude") +
                    "','" +
                    coordinates.getString("latitude") +
                    "','" +
                    coordinates.getString("sent") + "')"; 
		Statement createStatement;

        try {
            createStatement = dbConnection.createStatement();
            
        } catch (Exception e) {
            System.out.println("setCoordinates: dbConnection.createStatement FAILED");
            throw e;
        }
        try {
            createStatement.executeUpdate(setCoordinatesString);
            
        } catch (Exception e) {
            System.out.println("setCoordinates: createStatement.executeUpdate FAILED");
            throw e;
        }
		createStatement.close();
    }
    
    public JSONArray getCoordinates(String username) throws SQLException {

        Statement queryStatement = null;
        
        JSONArray array = new JSONArray();

        String getCoordinatesString = "select username, longitude, latitude, time from coordinates";
        //"where username = '" + username + "'";

        queryStatement = dbConnection.createStatement();
		ResultSet rs = queryStatement.executeQuery(getCoordinatesString);

        while (rs.next()) {
            JSONObject obj = new JSONObject();
            //obj.put("id", rs.getInt("rowid"));
            obj.put("username", rs.getString("username"));
            obj.put("longitude", rs.getString("longitude"));
            obj.put("latitude", rs.getString("latitude"));
            obj.put("sent", rs.getString("time"));
            array.put(obj);

		}

        return array;

    }
}
