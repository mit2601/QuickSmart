package com.example.quicksmart;

public class SuggestionModel {

    private String name;
    private double lat, lng;
    private boolean isCurrentLocation;

    // Normal location
    public SuggestionModel(String name, double lat, double lng, double distance) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.isCurrentLocation = false;
    }

    // Current location
    public SuggestionModel(String type) {
        this.name = "Use Current Location";
        this.isCurrentLocation = true;
    }

    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public boolean isCurrentLocation() { return isCurrentLocation; }
}