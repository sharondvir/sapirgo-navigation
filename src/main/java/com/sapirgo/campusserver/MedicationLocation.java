package com.sapirgo.campusserver;

public class MedicationLocation {
    private String building; // building number
    private String description; // description of the location

    public MedicationLocation(){
        // Default constructor
    }
    public MedicationLocation(String building, String description) {
        this.building = building;
        this.description = description;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
