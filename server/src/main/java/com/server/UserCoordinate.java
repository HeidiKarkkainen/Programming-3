package com.server;

public class UserCoordinate {

    public String nick;
    public double latitude;
    public double longitude;
    public String timestamp;
    public String description;

    public UserCoordinate(String nick, double latitude, double longitude, String timestamp, String description){
        this.nick = nick;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.description = description;
    }
    
    public String getNick(){
        return this.nick;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public String getDescription(){
        return this.description;
    }
}
