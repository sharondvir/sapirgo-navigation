// Home.java
package com.example.sapirgo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class Home extends BaseMenu {
    private LinearLayout first_aid;
    private LinearLayout bomb_shelter;
    private LinearLayout emergency_call;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        first_aid = findViewById(R.id.first_aid_ic);
        bomb_shelter = findViewById(R.id.bomb_shelter_ic);
        emergency_call = findViewById(R.id.emergency_call_ic);
        searchInput = findViewById(R.id.search_route);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchInput.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        first_aid.setOnClickListener(v -> openNavigation("first_aid"));
        bomb_shelter.setOnClickListener(v -> openNavigation("bomb_shelter"));
        emergency_call.setOnClickListener(v -> openNavigation("emergency_call"));

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                String input = searchInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (!isValidInput(input)) {
                    searchInput.setError("Invalid input. Please enter a valid building, class number or keyword.");
                    return true;
                }
                searchInput.setError(null);
                if (input.matches("\\d{4,5}")) {
                    String building, floor, room;
                    if (input.length() == 5) {
                        building = input.substring(0, 2);
                        floor = input.substring(2, 3);
                        room = input.substring(3);
                    } else {
                        building = "0" + input.charAt(0);
                        floor = input.substring(1, 2);
                        room = input.substring(2);
                    }
                    Intent intent = new Intent(Home.this, NavigationActivity.class);
                    intent.putExtra("destination", building);
                    intent.putExtra("floor", floor);
                    intent.putExtra("room", room);
                    startActivity(intent);
                } else {
                    openNavigation(input.toLowerCase());
                }
                return true;
            }
            return false;
        });
    }

    private boolean isValidInput(String input) {
        List<String> validBuildings = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "20");
        if (input.matches("\\d{4,5}")) {
            String building = input.length() == 5 ? input.substring(0, 2) : "0" + input.charAt(0);
            String floor = input.length() == 5 ? input.substring(2, 3) : input.substring(1, 2);
            String room = input.length() == 5 ? input.substring(3) : input.substring(2);
            return validBuildings.contains(building) && !(floor.equals("0") && (room.equals("0") || room.equals("00")));
        }
        if (input.matches("\\d{2}")) return validBuildings.contains(input);
        List<String> validKeywords = Arrays.asList("bomb_shelter", "first_aid", "defibrillator", "emergency_call");
        return validKeywords.contains(input.toLowerCase());
    }

    private void openNavigation(String destination) {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("destination", destination);
        startActivity(intent);
    }
}