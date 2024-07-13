package com.chinmay.diat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    TextView signup, forgotpassword;
    TextInputLayout email, password;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    AppCompatButton loginbtn;
    CardView googlelogincard, facebooklogincard;
    String uid;

    @SuppressLint("MissingInflatedId")
    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.login);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loginbtn = findViewById(R.id.login_btn);
        googlelogincard = findViewById(R.id.googlelogincard);
        facebooklogincard = findViewById(R.id.facebooklogincard);
        signup = findViewById(R.id.noAccountSignUp);
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_password);
        forgotpassword = findViewById(R.id.forgotPassword);

        // If the user is already logged in, start Home activity
        if (mAuth.getCurrentUser() != null) {
            startHomeActivity();
        }

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailLogin();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Signup.class));
            }
        });
    }

    public void emailLogin() {
        String emailtxt = email.getEditText().getText().toString().trim();
        String passwordtxt = password.getEditText().getText().toString().trim();

        if (emailtxt.isEmpty() || passwordtxt.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.signInWithEmailAndPassword(emailtxt, passwordtxt)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified() && isAllowedDomain(user.getEmail())) {
                                    saveUserDataToFirebase();
                                } else {
                                    mAuth.signOut();
                                    Toast.makeText(getApplicationContext(), "Email not verified or not allowed.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                email.getEditText().setText("");
                                password.getEditText().setText("");
                                Toast.makeText(getApplicationContext(), "Invalid Email Or Password.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void saveUserDataToFirebase() {
        uid = mAuth.getCurrentUser().getUid();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();

        Map<String, String> userData = new HashMap<>();
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("username", username);

        db.collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startHomeActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error writing document", e);
                    }
                });
    }

    private void startHomeActivity() {
        startActivity(new Intent(getApplicationContext(), Home.class));
        finishAffinity();
    }

    private boolean isAllowedDomain(String email) {
        String allowedDomain = "gmail.com"; // Change to your allowed domain
        String domain = email.substring(email.indexOf("@") + 1);
        return domain.equals(allowedDomain);
    }
}
