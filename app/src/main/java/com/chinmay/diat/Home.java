package com.chinmay.diat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    DrawerLayout drawerLayout;
    FirebaseAuth mAuth;
    FirebaseUser user;
    EditText searchEditText;
    ScrollView scrollView;
    LinearLayout linearLayout;
    ImageView administration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        drawerLayout = findViewById(R.id.drawerLayout);
        administration = findViewById(R.id.administration);
        searchEditText = findViewById(R.id.searchViewSearch);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);

        administration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Administration.class));
            }
        });

        // Add text change listener to EditText for searching
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterDepartments(s.toString());
            }
        });
    }

    // Method to filter departments based on search query
    private void filterDepartments(String query) {
        // Assuming you have a list of departments or views to filter
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View childView = linearLayout.getChildAt(i);
            // Replace this with your logic to filter views based on department names
            if (childView instanceof CardView) {
                CardView cardView = (CardView) childView;
                // Example: Assuming you have a TextView inside CardView
                TextView departmentTextView = cardView.findViewById(R.id.textViewDepartmentName);
                if (departmentTextView != null) {
                    String departmentName = departmentTextView.getText().toString().toLowerCase();
                    if (departmentName.contains(query.toLowerCase())) {
                        cardView.setVisibility(View.VISIBLE);
                    } else {
                        cardView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void openDrawer(View view) {
        drawerLayout.open();
    }

    public void closeDrawer(View view) {
        drawerLayout.close();
    }
}
