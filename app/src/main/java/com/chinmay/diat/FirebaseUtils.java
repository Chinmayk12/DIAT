package com.chinmay.diat;

import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public FirebaseUtils() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void fetchAndDisplayInitials(final TextView textView) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String username = documentSnapshot.getString("username");
                                if (username != null && !username.isEmpty()) {
                                    String initials = getInitials(username);
                                    textView.setText(initials);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the error
                        }
                    });
        }
    }

    private String getInitials(String username) {
        String[] nameParts = username.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
            if (initials.length() >= 2) {
                break;
            }
        }
        return initials.toString().toUpperCase();
    }

    public void fetchAndDisplayUserInfo(TextView drawerUserName, TextView drawerUserEmail) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String name = document.getString("username");
                        String email = document.getString("email");

                        if (name != null) {
                            drawerUserName.setText(name);
                        }
                        if (email != null) {
                            drawerUserEmail.setText(email);
                        }
                    }
                }
            });
        }
    }
}
