package com.example.quicksmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import kotlinx.coroutines.sync.SemaphoreAndMutexImpl;

public class signupActivity extends AppCompatActivity {
    EditText name,email,phn_no,pass;
    Button reg_signup;

    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        mAuth=FirebaseAuth.getInstance();

        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        phn_no=findViewById(R.id.phn_no);
        pass=findViewById(R.id.pass);
        reg_signup=findViewById(R.id.reg_signup);
        String emailRegex = "\\b[a-z0-9._%-]+@[a-z0-9.-]+\\.[a-z]{2,4}\\b";







        reg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (name.getText().toString().trim().equals("")) {
                    name.setError("Name Required");
                }
                else if (email.getText().toString().trim().equals("")) {
                    email.setError("Email Required");
                }
                else if(!email.getText().toString().trim().matches(emailRegex)){
                    email.setError("Invalid Email");
                }
                else if (phn_no.getText().toString().trim().equals("")) {
                    phn_no.setError("Phone Number Required");
                }

                else if (pass.getText().toString().trim().equals("")) {
                    pass.setError("Password Required");
                }
                else if (pass.getText().toString().trim().length() < 6) {
                    pass.setError("Min. 6 Char Password Required");
                }
                else {
                    sendOTP();

                }


            }
        });


    }

    private void sendOTP() {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(signupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Intent intent=new Intent(signupActivity.this,otpActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("name",name.getText().toString());
                bundle.putString("email",email.getText().toString());
                bundle.putString("phn_no",phn_no.getText().toString());
                bundle.putString("pass",pass.getText().toString());
                bundle.putString("verificationId",verificationId);
                intent.putExtras(bundle);
                startActivity(intent);
                Toast.makeText(signupActivity.this, "OTP Sent", Toast.LENGTH_LONG).show();

            }
        };


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phn_no.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);



    }
}