package com.chinmay.diat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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

    ImageView aidept;
    EditText deptname;
    View headerView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Instances
        addept = (FloatingActionButton) findViewById(R.id.adddeptbutton);
        //deptname = (EditText) findViewById(R.id.dialogDeptEditText);
        aidept = (ImageView) findViewById(R.id.ai_branch);

        //savedept = (AppCompatButton) findViewById(R.id.addDeptButton);

        // For Left Side Drawer (Slide Bar)
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Getting the view of the Drawer from navigation view.
        headerView = navigationView.getHeaderView(0);

        addept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });
        aidept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AI_dept.class));
            }
        });

        findViewById(R.id.cs_branch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),computer_dept.class));
            }
        });

        findViewById(R.id.mech_branch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Mech_dept.class));
            }
        });

        findViewById(R.id.robo_branch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Robotics_dept.class));
            }
        });

    }

    private void showCustomDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);

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