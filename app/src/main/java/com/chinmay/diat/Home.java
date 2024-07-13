package com.chinmay.diat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    DrawerLayout drawerLayout;
    EditText searchEditText;
    RecyclerView recyclerView;
    DepartmentAdapter adapter;
    List<DepartmentModel> departmentModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        drawerLayout = findViewById(R.id.drawerLayout);
        searchEditText = findViewById(R.id.searchViewSearch);
        recyclerView = findViewById(R.id.recyclerViewDepartments);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        departmentModelList = new ArrayList<>();
        adapter = new DepartmentAdapter(this, departmentModelList);
        recyclerView.setAdapter(adapter);

        // Manually add departments (replace with your actual data)
        addDepartment("Administration", R.drawable.administration_logo);
        addDepartment("Academics",R.drawable.academics_logo);
        addDepartment("Research", R.drawable.research_logo);
        addDepartment("Faculty",R.drawable.faculty);
        addDepartment("Students", R.drawable.students);
        addDepartment("Non DIAT Students", R.drawable.others_students);
        addDepartment("Reimbursement",R.drawable.reimbursement_logo);
        adapter.filter(" ");    // For to display the all departments when we open the app


        // Add more departments as needed

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });
    }

    private void addDepartment(String name, int iconResId) {
        DepartmentModel departmentModel = new DepartmentModel(name, iconResId);
        departmentModelList.add(departmentModel);
        adapter.notifyDataSetChanged();
    }

    public void openDrawer(View view) {
        drawerLayout.open();
    }

    public void closeDrawer(View view) {
        drawerLayout.close();
    }
}
