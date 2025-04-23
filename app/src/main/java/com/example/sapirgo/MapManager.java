package com.example.sapirgo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapManager {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private final Activity activity;
    private GoogleMap googleMap;

    public MapManager(Activity activity) {
        this.activity = activity;
    }

    public void initMap(FragmentManager fragmentManager, int mapId, OnMapReadyListener listener) {
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(mapId);
        if (mapFragment == null) return;

        mapFragment.getMapAsync(map -> {
            this.googleMap = map;

            // Enable user location if permissions are granted
            enableUserLocation();

            // Add custom campus map overlay
            addCampusOverlay(map);

            // Add predefined markers (e.g., buildings)
            addStaticBuildingMarkers(map);

            // Notify listener that the map is ready
            listener.onMapReady(map);
        });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            Toast.makeText(activity, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCampusOverlay(GoogleMap map) {
        // Define bounds of the image overlay (adjust based on your image)
        LatLng southwest = new LatLng(31.5075, 34.5949);
        LatLng northeast = new LatLng(31.5095, 34.5966);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

//        GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
//                .image(BitmapDescriptorFactory.fromResource(R.drawable.sapir_signed_map))
//                .positionFromBounds(bounds)// rotate overlay if needed
//                .transparency(0.3f); // 0 = opaque, 1 = fully transparent
//
//        map.addGroundOverlay(overlayOptions);
    }

    private void addStaticBuildingMarkers(GoogleMap map) {
//        map.addMarker(new MarkerOptions()
//                .position(new LatLng(31.5089, 34.5954))
//                .title("Sapir Library"));
    }




    public void animateMarkerAlongPath(List<LatLng> path, long durationMillis) {
        if (googleMap == null || path == null || path.size() < 2) return;

        // Add a moving marker (can customize icon)
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(path.get(0))
                .title("You")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // Total animation duration and frame rate
        long interval = 50; // ms between updates
        long totalSteps = durationMillis / interval;
        int pathSize = path.size();
        int[] currentIndex = {0};

        Handler handler = new Handler(Looper.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (currentIndex[0] < pathSize - 1) {
                    LatLng start = path.get(currentIndex[0]);
                    LatLng end = path.get(currentIndex[0] + 1);

                    // Interpolate between start and end
                    double fraction = 0.05; // You can make this smoother
                    double lat = start.latitude + fraction * (end.latitude - start.latitude);
                    double lng = start.longitude + fraction * (end.longitude - start.longitude);
                    marker.setPosition(new LatLng(lat, lng));

                    if (Math.abs(lat - end.latitude) < 0.00005 && Math.abs(lng - end.longitude) < 0.00005) {
                        currentIndex[0]++;
                    }

                    handler.postDelayed(this, interval);
                }
            }
        };

        handler.post(runnable);
    }

    public void showUserLocationMarker(LatLng location) {
        if (googleMap != null && location != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.man_icon))
                    .title("You"));
        }
    }


    public void drawPath(List<LatLng> path) {
        if (googleMap != null && path != null && !path.isEmpty()) {
            // (2) Draw the path on the map
            googleMap.addPolyline(new PolylineOptions()
                    .addAll(path)
                    .color(Color.BLUE)
                    .width(10f));

            // (2) Add markers at start and end
            googleMap.addMarker(new MarkerOptions()
                    .position(path.get(0))
                    .title("Start"));

            googleMap.addMarker(new MarkerOptions()
                    .position(path.get(path.size() - 1))
                    .title("Destination"));

            // Zoom to fit the path
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : path) {
                boundsBuilder.include(point);
            }
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));

            // Zoom to the first point
            LatLng userLatLng = path.get(0);
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
        }
    }

    public void getUserLocation(NavigationActivity.OnLocationReceivedListener listener) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(activity);
            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    listener.onLocationReceived(latLng);
                } else {
                    Toast.makeText(activity, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(activity, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void startTrackingToTarget(LatLng target, Runnable onArrival) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest request = LocationRequest.create()
                    .setInterval(3000)
                    .setFastestInterval(2000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;
                    android.location.Location userLoc = locationResult.getLastLocation();
                    float[] distance = new float[1];
                    android.location.Location.distanceBetween(
                            userLoc.getLatitude(), userLoc.getLongitude(),
                            target.latitude, target.longitude,
                            distance
                    );
                    if (distance[0] < 10) {
                        onArrival.run();
                        client.removeLocationUpdates(this);
                    }
                }
            }, activity.getMainLooper());
        }
    }

    public interface OnMapReadyListener {
        void onMapReady(GoogleMap map);
    }
}
