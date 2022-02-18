package com.server;

public class UserCoordinate {

    public String nick;
    public String latitude;
    public String longitude;

    public UserCoordinate(String nick, String latitude, String longitude){
        this.nick = nick;
        this.latitude = latitude;
        this.longitude = longitude;
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

}
