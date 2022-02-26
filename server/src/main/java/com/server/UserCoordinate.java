package com.server;

import org.json.JSONObject;

public class UserCoordinate {

    public String nick;
    public String latitude;
    public String longitude;
    public String timestamp;

    public UserCoordinate(String nick, String latitude, String longitude, String timestamp){
        this.nick = nick;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
    
    public String getNick(){
        return this.nick;
    }

    public String getLatitude(){
        return this.latitude;
    }

    public String getLongitude(){
        return this.longitude;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

 

}
