package com.sapirgo.campusserver;
import lombok.Data;

import java.util.*;

@Data
class Point {
        private String id;
        private double latitude;
        private double longitude;
        private Set<String> neighbors;
        private String type;

        public Point(String id, double latitude, double longitude, Collection<String> neighbors,String type) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.neighbors = (neighbors instanceof Set) ? (Set<String>) neighbors : new HashSet<>(neighbors);
            this.type = type;
        }

    public String getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Set<String> getNeighbors() { return neighbors; }
    public String getType() { return type; }

    public void setId(String id) { this.id = id; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setNeighbors(Set<String> neighbors) { this.neighbors = neighbors; }
    public void setType(String type) { this.type = type; }

}