package com.sapirgo.campusserver;

public class LatLngDTO {
    // Class that used for represent a geographical point (send to Android App)
    private double latitude;
    private double longitude;

    public LatLngDTO() {
    }

    public LatLngDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
