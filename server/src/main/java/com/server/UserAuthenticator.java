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

        System.out.println("checkCredentials: checking user: " + username + " " + password + "\n");

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

        if (db.checkIfUserExists(username)){
            System.out.println("user already exists");
            return false;
        } else {
            System.out.println("registering user");
            db.setUser(new JSONObject().put("username", username).put("password", password).put("email", email));
        }
        System.out.println(username + " registered");
        
        return true;
    }
    
}

