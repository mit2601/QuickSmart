package com.example.quicksmart;

import static androidx.core.content.ContextCompat.startActivity;

import static com.example.quicksmart.ConstantSp.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button login,signup;
    public static EditText username,pass;
    TextView fgtpass;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login=findViewById(R.id.main_login);
        pass=findViewById(R.id.pass);
        username=findViewById(R.id.username);
        signup=findViewById(R.id.main_signup);
        fgtpass=findViewById(R.id.fgtpass);
        sp=getSharedPreferences(ConstantSp.PREF,MODE_PRIVATE);

        


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,signupActivity.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (username.getText().toString().trim().equals("")) {
                    username.setError("Email Required");
                }
                else if (pass.getText().toString().trim().equals("")) {
                    pass.setError("Password Required");
                }
                else if (pass.getText().toString().trim().length() < 6) {
                    pass.setError("Min. 6 Char Password Required");
                }
                else {
                    String email=username.getText().toString();
                    String password=pass.getText().toString();

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                        
                        if (task.isSuccessful()){
                            onLogin();
                        }
                        else{
                            String message;

                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e) {
                                message = "Account not found";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                message = "Invalid email or password";
                            } catch (FirebaseNetworkException e) {
                                message = "No internet connection";
                            } catch (Exception e) {
                                message = "Login failed. Try again";
                            }

                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                        
                        
                        
                        
                    });
                

                }

                }

            private void onLogin() {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            usermodel user = documentSnapshot.toObject(usermodel.class);
                            if (user != null) {
                                sp.edit().putString(ConstantSp.EMAIL,user.getEmail()).apply();
                                sp.edit().putString(ConstantSp.NAME,user.getName()).apply();
                                sp.edit().putString(ConstantSp.PHN_NO,user.getPhn_no()).apply();
                                sp.edit().putString(ConstantSp.UID,uid).apply();




                                Toast.makeText(MainActivity.this, "Welcome " + sp.getString(NAME,"Guest"), Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this, homeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }


        });





    }
}