package com.example.hotelrentala3.Model;

import java.io.Serializable;

public class Hotel implements Serializable {
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private int availability;
    private double price;
    private double rating;

    public Hotel(String name, String location, double latitude, double longitude, int availability, double price, double rating) {
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availability = availability;
        this.price = price;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getAvailability() {
        return availability;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }
}


