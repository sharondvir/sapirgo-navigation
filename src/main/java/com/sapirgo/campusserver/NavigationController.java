package com.sapirgo.campusserver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/navigation")
class NavigationController {
    //Class that handles firebase loading, entraces to buildings mapping and finding the shortest path
    private final Firestore firestore;
    private final Graph graph;
    private final Map<String, List<String>> entranceToBuildingsMap = new HashMap<>();
    private final Map<String, List<String>> buildingToEntrancesMap = new HashMap<>();
    private final List<MedicationLocation> defibrillators = new ArrayList<>();
    private final List<MedicationLocation> firstAidKits = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);


    // Constructor
    public NavigationController(Firestore firestore) throws ExecutionException, InterruptedException {
        this.firestore = firestore;
        this.graph = new Graph();
        logger.info("Initializing NavigationController - loading points and medical locations");
        loadPointsFromFirebase(); // loading points from Firebase
        this.defibrillators.addAll(loadMedicalLocationsFromJson("defibrillators_buildings.json")); // loading medical locations from json
        this.firstAidKits.addAll(loadMedicalLocationsFromJson("firstAid_buildings.json")); // loading medical locations from json
        logger.info("Loaded {} defibrillators and {} first aid kits", defibrillators.size(), firstAidKits.size());
    }

    //Function to load medical locations from json file
    private List<MedicationLocation> loadMedicalLocationsFromJson(String file_name){
        try{
            InputStream inputstream = getClass().getClassLoader().getResourceAsStream(file_name);
            if (inputstream == null) {
                throw new RuntimeException("File not found: " + file_name);
            }
            ObjectMapper mapper = new ObjectMapper();
            return Arrays.asList(mapper.readValue(inputstream, MedicationLocation[].class));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    //Function to load points from Firebase
    private void loadPointsFromFirebase() throws ExecutionException, InterruptedException {
        loadCollectionToGraph("Entrances");
        loadCollectionToGraph("Path");
        loadCollectionToGraph("Shelters");
        this.graph.computeNeighborsForEachPoint(0.1); //less than 100 meters considers as a neighbor
    }

    // Function that loads collections from Firebase to the graph
    private void loadCollectionToGraph(String collectionName) throws ExecutionException, InterruptedException, NullPointerException {
        ApiFuture<QuerySnapshot> future = this.firestore.collection(collectionName).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        logger.info("Loading collection '{}' from Firebase...", collectionName);
        String type;
        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            double lat = document.getDouble("latitude");
            double lon = document.getDouble("longitude");
            List<String> neighbors = new ArrayList<>();

            if ("Entrances".equals(collectionName)) {
                Object buildingObj = document.get("building");
                List<String> buildingsList = new ArrayList<>();

                if (buildingObj instanceof List<?>) {
                    for (Object item : (List<?>) buildingObj) {
                        if (item instanceof String buildingName) {
                            buildingsList.add((String) item);
                            this.buildingToEntrancesMap.computeIfAbsent(buildingName, k -> new ArrayList<>()).add(id);
                        }
                    }
                }
                this.entranceToBuildingsMap.put(id, buildingsList); // store the building names for each entrance
            }
            switch (collectionName){
                case "Entrances" -> type = "entrance";
                case "Path" -> type = "path";
                case "Shelters" -> type = "shelter";
                default -> type = "unknown";
            }
            this.graph.addPointToGraph(id, lat, lon, neighbors,type); // add point to the graph (path,entrance,bomb shelter)
        }
    }

    //Function to get the shortest path to the user -> from current location to destination
    @GetMapping("/shortest-path")
    public Map<String, List<LatLngDTO>> getShortestPath(@RequestParam String userLocation, @RequestParam String destination) {
        logger.info("Request for shortest path from '{}' to building '{}'", userLocation, destination);
        List<String> entrances = buildingToEntrancesMap.getOrDefault(destination, new ArrayList<>());
        List<Point> shortestPath = graph.getShortestPathToClosetEntrance(userLocation, entrances);
        return getStringListMap(shortestPath);
    }

    @GetMapping("/navigate-to-shelter")
    public Map<String, List<LatLngDTO>> navigateToShelter(@RequestParam String userLocation) {
        logger.info("Request for nearest shelter from user location '{}'", userLocation);
        Set<String> shelterIds = graph.getShelterIds(); // Get all shelter IDs
        List<Point> shortestPath = graph.getShortestPathToClosetEntrance(userLocation, new ArrayList<>(shelterIds)); // כאן ההמרה החשובה

        return getStringListMap(shortestPath);
    }

    private Map<String, List<LatLngDTO>> getStringListMap(List<Point> shortestPath) {
        List<LatLngDTO> path = shortestPath.stream()
                .map(p -> new LatLngDTO(p.getLatitude(), p.getLongitude()))
                .toList();

        Map<String, List<LatLngDTO>> response = new HashMap<>();
        response.put("path", path);
        return response;
    }


    @GetMapping("/building_entrances")
    public Map<String,List<String>> getEntranceForBuildings(@RequestParam String building) {
        // Function that returns all the entrance points for a given building
        Map<String, List<String>> response = new HashMap<>();
        List<String> entrances = this.buildingToEntrancesMap.getOrDefault(building, new ArrayList<>());
        response.put("entrances", entrances);
        return response;
    }

    // Function that returns the shortest path to the closest entrance of the wanted medication
    @GetMapping("/navigate-to-medication")
    public Map<String, List<LatLngDTO>> navigateToMedication(@RequestParam String userLocation, @RequestParam String medicationType) {
        logger.info("Request for nearest '{}' from user location '{}'", medicationType, userLocation);
        List<MedicationLocation> medicationLocations =
                switch (medicationType) {
                 case "defibrillator" -> this.defibrillators;
                 case "firstAidKit" -> this.firstAidKits;
                 default -> Collections.emptyList();
        };
        List<Point> shortestPath = new ArrayList<>();
        double shortestDistance = Double.MAX_VALUE;

        for (MedicationLocation location : medicationLocations) {
            List<String> entrances = buildingToEntrancesMap.get(location.getBuilding());
            if (entrances != null && !entrances.isEmpty()) {
                List<Point> path = graph.getShortestPathToClosetEntrance(userLocation, entrances);
                double distance = graph.calculatePathDistance(path);
                if (!path.isEmpty() && distance < shortestDistance) {
                    shortestDistance = distance;
                    shortestPath = path;
                }
            }
        }
        List<LatLngDTO> finalPath = shortestPath.stream()
                .map(p -> new LatLngDTO(p.getLatitude(), p.getLongitude()))
                .toList();

        return Map.of("path", finalPath);
    }
}

