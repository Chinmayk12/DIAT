package com.chinmay.diat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FloatingActionButton addept;
    AppCompatButton savedept;

    EditText searchEditText;
    ScrollView scrollView;
    LinearLayout linearLayout;
    ImageView aidept;
    EditText deptname;
    View headerView;

    ImageView administration;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        deptname = (EditText) findViewById(R.id.dialogDeptEditText);
        administration = (ImageView) findViewById(R.id.administration);
        savedept = (AppCompatButton) findViewById(R.id.addDeptButton);
        searchEditText = findViewById(R.id.searchViewSearch);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);

        // For Left Side Drawer (Slide Bar)
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Getting the view of the Drawer from navigation view.
        headerView = navigationView.getHeaderView(0);

        administration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Administration.class));
            }
        });
    }

    private void showCustomDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.add_dept_dialog, null);

        // Set the custom layout as the dialog's content
        dialog.setContentView(dialogLayout);

        //EditText editText = dialogLayout.findViewById(R.id.dialogDeptEditText);
        //AppCompatButton button = dialogLayout.findViewById(R.id.addDeptButton);

        // Set up the button click listener
        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click
                String inputText = editText.getText().toString();
                Toast.makeText(getApplicationContext(),inputText,Toast.LENGTH_SHORT).show();
                // Optionally dismiss the dialog
                dialog.dismiss();
            }
        });
*/
        // Show the dialog
        dialog.show();
    }

    public void openDrawer(View view)
    {
        drawerLayout.open();
    }

    public void closeDrawer(View view)
    {
        drawerLayout.close();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}