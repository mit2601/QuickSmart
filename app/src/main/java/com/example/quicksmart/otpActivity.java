package com.example.quicksmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class otpActivity extends AppCompatActivity {

    TextView phn_no, chg_phn;
    Button verify, resend;

    FirebaseFirestore db;

    private String verificationId;
    private EditText num_otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        phn_no = findViewById(R.id.phn_no);
        chg_phn = findViewById(R.id.chg_phn);
        verify = findViewById(R.id.verify);
        resend = findViewById(R.id.resend);
        num_otp = findViewById(R.id.otp_no);
        db = FirebaseFirestore.getInstance();

        Bundle bundle = getIntent().getExtras();

        String phn = bundle.getString("phn_no");
        phn_no.setText(phn);
        String name = bundle.getString("name");
        String email = bundle.getString("email");
        String pass = bundle.getString("pass");
        verificationId = bundle.getString("verificationId");


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num_otp.getText().toString().trim().isEmpty()) {
                    num_otp.setError("OTP Required");
                } else if (num_otp.getText().toString().trim().length() != 6) {
                    num_otp.setError("Enter 6 digit OTP");
                } else {
                    if (verificationId != null) {
                        verify.setEnabled(false);
                        verify.setText("Please wait...");
                        String code = num_otp.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.getEmail() != null) {
                                    Toast.makeText(otpActivity.this,
                                            "Account already exists. Please login.",
                                            Toast.LENGTH_LONG).show();

                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(otpActivity.this, MainActivity.class));
                                    finish();
                                    return;
                                } else if (task.isSuccessful()) {
                                    onotpVerify(name, email, phn, pass);
                                } else {
                                    Toast.makeText(otpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                                    verify.setEnabled(true);
                                    verify.setText("Verify");
                                }
                            }
                        });

                    } else {
                        Toast.makeText(otpActivity.this, "VerificationID Failed", Toast.LENGTH_SHORT).show();
                    }
                }


            }

            private void onotpVerify(String name, String email, String phn, String pass) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                AuthCredential emailCredential = EmailAuthProvider.getCredential(email, pass);
                auth.getCurrentUser()
                        .linkWithCredential(emailCredential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String uid = auth.getCurrentUser().getUid();
                                    // usermodel constructor sets default role to "user" and blocked to false
                                    usermodel userdata = new usermodel(name, email, phn);

                                    db.collection("Users").document(uid).set(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(otpActivity.this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(otpActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();

                                            } else {
                                                Toast.makeText(otpActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(otpActivity.this, signupActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            }
                        });
            }
        });
        chg_phn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(otpActivity.this, signupActivity.class);
                startActivity(intent);
            }
        });


    }
}
