package com.server;

import java.sql.SQLException;

import com.sun.net.httpserver.*;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAuthenticator extends BasicAuthenticator{

    private CoordinatesDatabase db = null;

    public UserAuthenticator (){
        super("coordinates");
        db = CoordinatesDatabase.getInstance();
    }

    @Override
    public boolean checkCredentials(String username, String password){

        boolean isValidUser;

        try {
            isValidUser = db.authenticateUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return isValidUser;
    }

    public boolean addUser (String username, String password, String email) throws JSONException, SQLException{

        boolean added = db.setUser(new JSONObject().put("username", username).put("password", password).put("email", email));

        if (!added){
            System.out.println("User already exists");
            return false;
        }          
        System.out.println(username + " registered");  
           
        return true;
    }   
}

