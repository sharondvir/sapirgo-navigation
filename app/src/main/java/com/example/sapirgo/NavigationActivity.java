// NavigationActivity.java
package com.example.sapirgo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {

    private MapManager mapManager;
    private String destinationCode;
    private String floor;
    private String room;
    private LatLng targetLocation;
    private boolean popupShown = false;
    private boolean useFakeLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_navigation);

        mapManager = new MapManager(this);

        destinationCode = getIntent().getStringExtra("destination");
        Log.d("NAVIGATION", "destinationCode from intent: " + destinationCode);
        if (destinationCode == null || destinationCode.trim().isEmpty()) {
            Toast.makeText(this, "Destination missing. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        floor = getIntent().getStringExtra("floor");
        room = getIntent().getStringExtra("room");

        mapManager.initMap(getSupportFragmentManager(), R.id.map, googleMap -> {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            if (useFakeLocation) {
                LatLng fakeLocation = new LatLng(31.50948389, 34.59685592);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fakeLocation, 17));
                mapManager.showUserLocationMarker(fakeLocation); // ✅ שינוי כאן - הצגת סמן המשתמש
                requestPathFromServer(fakeLocation, destinationCode);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void requestPathFromServer(LatLng start, String dest) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading route...");
        dialog.setCancelable(false);
        dialog.show();

        String userLocation = start.latitude + "," + start.longitude;
        String url = buildUrl(userLocation, dest);
        if (url == null) return;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    dialog.dismiss();
                    List<LatLng> path = new ArrayList<>();
                    try {
                        JSONArray pathArray = response.getJSONArray("path");
                        for (int i = 0; i < pathArray.length(); i++) {
                            JSONObject point = pathArray.getJSONObject(i);
                            double lat = point.getDouble("latitude");
                            double lng = point.getDouble("longitude");
                            path.add(new LatLng(lat, lng));
                        }

                        if (path.isEmpty()) {
                            Toast.makeText(this, "No route found. Please check your location or try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        LatLng destination = path.get(path.size() - 1);
                        targetLocation = destination;

                        // ✅ Show the path inside the app with animation
                        mapManager.drawPath(path);
                        mapManager.animateMarkerAlongPath(path, 6000);

                        startLocationTracking();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing path", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Could not fetch path from server. Check network or try again.", Toast.LENGTH_LONG).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private String buildUrl(String userLocation, String destination) {
        switch (destination) {
            case "bomb_shelter":
                return Constants.BASE_URL + "/navigation/navigate-to-shelter?userLocation=" + userLocation;
            case "first_aid":
                return Constants.BASE_URL + "/navigation/navigate-to-medication?userLocation=" + userLocation + "&medicationType=firstAidKit";
            case "defibrillator":
                return Constants.BASE_URL + "/navigation/navigate-to-medication?userLocation=" + userLocation + "&medicationType=defibrillator";
            case "emergency_call":
                Toast.makeText(this, "Emergency call feature is not implemented yet.", Toast.LENGTH_SHORT).show();
                finish();
                return null;
            default:
                return Constants.BASE_URL + "/navigation/shortest-path?userLocation=" + userLocation + "&destination=" + destination;
        }
    }

    private void startLocationTracking() {
        if (!popupShown && targetLocation != null) {
            mapManager.startTrackingToTarget(targetLocation, this::showPopUp);
        }
    }

    private void showPopUp() {
        if (popupShown) return;
        popupShown = true;

        String message = (floor != null && room != null)
                ? "Hey! You need to go to floor " + floor + " and enter room " + room + "."
                : "You have arrived to your destination!";

        new AlertDialog.Builder(this)
                .setTitle("You're here 🎉")
                .setMessage(message)
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public interface OnLocationReceivedListener {
        void onLocationReceived(LatLng location);
    }
}