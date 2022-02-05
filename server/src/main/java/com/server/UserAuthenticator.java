package com.server;

import java.util.Map;
import java.util.Hashtable;

import com.sun.net.httpserver.*;

public class UserAuthenticator extends BasicAuthenticator{

    private Map<String, String> users = null;

    public UserAuthenticator (String realm){
        super(realm);
        users = new Hashtable<String, String>();
        users.put("dummy", "passwd");
    }

    public boolean checkCredentials(String username, String password){

        if (username.equals("dummy") && password.equals("passwd")){
            return true;
        } else {
            return false;
        }

    }
    
}

