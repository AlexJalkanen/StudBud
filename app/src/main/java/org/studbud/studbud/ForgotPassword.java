package org.studbud.studbud;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPassword extends AppCompatActivity {

    private EditText email;
    private Button sendPass;
    private ImageButton back;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id.emailPassReset);
        sendPass = findViewById(R.id.resetPasswordButton);
        back = findViewById(R.id.back_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnLogin = new Intent(ForgotPassword.this, LoginInActivity.class);
                returnLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(returnLogin);
                finish();
            }
        });

        sendPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = email.getText().toString();

                if (emailAddress.isEmpty()) {
                    email.setError("Please Enter an Email Address.");
                    email.requestFocus();
                    return;
                }
                sendPassReset(emailAddress);

            }
        });

    }

    private void sendPassReset(String emailAdd) {
        final String emailAddress = emailAdd;
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPassword.this, "Password Link Sent.", Toast.LENGTH_LONG).show();
                    Intent Login = new Intent(ForgotPassword.this, LoginInActivity.class);
                    Login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Login.putExtra("EmailAddress",emailAddress);
                    startActivity(Login);
                    finish();
                }
                else {
                    email.setError("Please Enter a Valid Email Address.");
                    email.requestFocus();
                    return;
                }
            }
        });


    }
}
