package com.example.hotelrentala3.Model;

public class Room {
    private String hotelName;
    private String location;
    private String type;
    private String capacity;
    private long price;

    public Room(String hotelName, String location, String type, String capacity, long price) {
        this.hotelName = hotelName;
        this.location = location;
        this.type = type;
        this.capacity = capacity;
        this.price = price;
    }

    public String getHotelName() { return hotelName; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public String getCapacity() { return capacity; }
    public long getPrice() { return price; }
}

