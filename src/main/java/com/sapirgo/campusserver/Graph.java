package com.sapirgo.campusserver;
import java.util.*;

public class Graph {
    private final Map<String, Point> points = new HashMap<>();//Map to store the points
    private final Map<String, Set<Point>> neighborMap = new HashMap<>();//Map to store the neighbors

    //Function to add point to the graph with id, latitude, longitude and neighbors
    public void addPointToGraph(String id, double lat, double lon, List<String> neighbors,String type) {
        points.put(id, new Point(id, lat, lon, neighbors,type));
    }

    //This function should be changed -> find the neighbors.
    public void computeNeighborsForEachPoint(double maxDistance) {
        for (Point point : points.values()) {
            neighborMap.computeIfAbsent(point.getId(), k -> new HashSet<>());
            for (Point other_point : points.values()) {
                if (!point.getId().equals(other_point.getId())) {
                    double distance = calculateHaversineDistance(point, other_point);
                    if (distance <= maxDistance) {
                        neighborMap.get(point.getId()).add(other_point);
                    }
                }
            }
        }
    }

    //Dijkstra's algorithm to find the shortest path -> given start and end point
    public List<Point> findShortestPath(String userlocation, String end) {
        if (!points.containsKey(userlocation) || !points.containsKey(end)) {
            return new ArrayList<>(); // if `userlocation` or `end` is missing -> returns an empty list
        }
        Map<String, Double> distances = new HashMap<>();
        for (String id : points.keySet()) {
            distances.put(id, Double.MAX_VALUE);  // Initialize the distance to infinity
        }
        distances.put(userlocation, 0.0);
        // Sort the priority queue based on the shortest known distance from the start point
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        queue.add(userlocation);
        Map<String, String> previous = new HashMap<>();

        //Dijkstra's algorithm
        while (!queue.isEmpty()) {
            String currentId = queue.poll(); // Get the point with the smallest distance (closest point)
            Point current = points.get(currentId);
            if (currentId.equals(end)) break;
            for (Point neighbor : neighborMap.getOrDefault(currentId, new HashSet<>())) {
                double newDist = distances.get(currentId) + calculateHaversineDistance(current, neighbor);
                if (newDist < distances.get(neighbor.getId())) {
                    distances.put(neighbor.getId(), newDist);
                    previous.put(neighbor.getId(), currentId);
                    queue.remove(neighbor.getId()); // Ensure old distances are removed
                    queue.add(neighbor.getId());    // Add updated neighbor with new priority
                }
            }
        }
        return reconstructPath(previous, userlocation, end);
    }

    // Function that returns the shortest path from user's location to the wanted building's closest entrance
    public List<Point> getShortestPathToClosetEntrance(String userLocation,List<String>entrances) {
        List<Point> shortestPath = new ArrayList<>();
        double minDistance = Double.MAX_VALUE;
        double distance;
        for (String entranceId : entrances) {
            List<Point> path = findShortestPath(userLocation, entranceId);
            if (!path.isEmpty()) {
                distance = calculatePathDistance(path);
                if (distance < minDistance) {
                    minDistance = distance;
                    shortestPath = path;
                }
            }
        }
        return shortestPath;
    }

    protected double calculatePathDistance(List<Point> path) {
        double total_distance = 0;
        for(int i=0; i<path.size()-1; i++) {
            // Calculate the distance between two close points and add it to the total distance
            total_distance += calculateHaversineDistance(path.get(i), path.get(i+1));
        }
        return total_distance;
    }


    //Function that traces back the path from destination to the start point by using "previous" map
    private List<Point> reconstructPath(Map<String, String> previous, String start, String end) {
        List<Point> path = new ArrayList<>();
        String at = end;
        while (at!=null) {
            path.add(points.get(at));
            at = previous.get(at);
        }
        Collections.reverse(path);
        if(path.isEmpty() || path.get(0).getId().equals(start)) { // If the path is invalid -> return empty list
            return new ArrayList<>();
        }
        return path;
    }

    //Function that calculates distance between 2 geographical points on earth by Haversine formula
    private double calculateHaversineDistance(Point a, Point b) {
        final int R = 6371; // Radius of the earth
        double lon1 = Math.toRadians(a.getLongitude());
        double lat1 = Math.toRadians(a.getLatitude());
        double lon2 = Math.toRadians(b.getLongitude());
        double lat2 = Math.toRadians(b.getLatitude());
        // Haversine formula
        double lonDistance = lon2 - lon1;
        double latDistance = lat2 - lat1;
        double aCalc = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2))
                + Math.cos(lat1) * Math.cos(lat2) * (Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2));
        double c = 2 * Math.atan2(Math.sqrt(aCalc), Math.sqrt(1 - aCalc));
        return (R * c); // Distance in km between the two points
    }

    public Set<String> getShelterIds() {
        Set<String> sheltersIds = new HashSet<>();
        for(Map.Entry<String,Point> entry : points.entrySet()){
            if("shelter".equals(entry.getValue().getType())){
                sheltersIds.add(entry.getKey());
            }
        }
        return sheltersIds;
    }
}
