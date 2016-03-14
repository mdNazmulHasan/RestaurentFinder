package com.nerdcastle.nazmul.restaurentfinder;


public class Place {
    private String name;
    private String nearestPlaceId;
    private String vicinity;
    private String contact;
    private Double latitude;
    private Double longitude;
    private String rating;



    public Place() {
    }

    public Place(String name, String nearestPlaceId, String vicinity, Double latitude, Double longitude) {
        this.name = name;
        this.nearestPlaceId = nearestPlaceId;
        this.vicinity = vicinity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNearestPlaceId() {
        return nearestPlaceId;
    }

    public void setNearestPlaceId(String nearestPlaceId) {
        this.nearestPlaceId = nearestPlaceId;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Place(String name, String nearestPlaceId, String vicinity, String contact, Double latitude, Double longitude, String rating) {
        this.name = name;
        this.nearestPlaceId = nearestPlaceId;
        this.vicinity = vicinity;
        this.contact = contact;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}