package com.example.sapirgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseMenu extends AppCompatActivity {
    protected LinearLayout report_btn, about_us_btn, profile_btn;
    protected FloatingActionButton settings_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void setupMenuButtons() {
        // home_btn = findViewById(R.id.home_btn);
        about_us_btn = findViewById(R.id.about_us_btn);
        settings_btn = findViewById(R.id.fab);
        report_btn = findViewById(R.id.report_btn);
        profile_btn = findViewById(R.id.profile_btn);


        //Menu buttons functionality
        if (settings_btn != null) {
            settings_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseMenu.this, Settings.class);
                    startActivity(intent);
                }
            });
        }
        if (about_us_btn != null) {
            about_us_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseMenu.this, About_us.class);
                    startActivity(intent);
                }
            });
        }
        if (report_btn != null) {
            report_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseMenu.this, Report.class);
                    startActivity(intent);
                }
            });
        }

        if (profile_btn != null) {
            profile_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseMenu.this, Profile.class);
                    startActivity(intent);
                }
            });

        }
    }
}