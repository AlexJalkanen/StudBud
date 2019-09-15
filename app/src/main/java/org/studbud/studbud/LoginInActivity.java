package org.studbud.studbud;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static org.studbud.studbud.WelcomePage.TOKEN_KEY;

public class LoginInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailLogin, passwordLogin;
    private Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setEnabled(true);



        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(LoginInActivity.this, Register.class);
                startActivity(register);
            }
        });

        findViewById(R.id.forgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent passReset = new Intent(LoginInActivity.this, ForgotPassword.class);
                startActivity(passReset);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailLogin.getText().toString();
                String password = passwordLogin.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {
                    loginButton.setEnabled(false);
                    signIn(email, password);
                }
                else if (email.isEmpty()) {
                    emailLogin.setError("Please Enter an Email Address.");
                    emailLogin.requestFocus();
                    return;
                }
                else if (password.isEmpty()) {
                    passwordLogin.setError("Please Enter a Password.");
                    passwordLogin.requestFocus();
                    return;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Login Credentials.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();



        Intent intent = getIntent();
        String emailFromReg = intent.getStringExtra("EmailAddress");
        String passwordFromReg = intent.getStringExtra("Password");

        if (emailFromReg != null) {
            emailLogin.setText(emailFromReg);

        }

        if (passwordFromReg != null) {
            passwordLogin.setText(passwordFromReg);
        }

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (!currentUser.isEmailVerified()) {
                emailLogin.setError("Email Not Verified.");
                emailLogin.requestFocus();
                mAuth.signOut();
                return;
            }
            else {
                //Toast.makeText(getApplicationContext(), "Signed in with: " + currentUser.getEmail(),
                 //       Toast.LENGTH_LONG).show();
                Intent homePage = new Intent(LoginInActivity.this, WelcomePage.class);
                startActivity(homePage);
                finish();
            }


        }
        //updateUI(currentUser);
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (!user.isEmailVerified()) {
                                loginButton.setEnabled(true);
                                emailLogin.setError("Email Not Verified.");
                                emailLogin.requestFocus();
                                mAuth.signOut();
                                return;
                            }

                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (task.isSuccessful()) {
                                        final String token = task.getResult().getToken();

                                        final DocumentReference docRefCurrent;
                                        docRefCurrent = FirebaseFirestore.getInstance().document("Users/" + mAuth.getCurrentUser().getEmail());

                                        docRefCurrent.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();

                                                    Map<String, Object> info = document.getData();

                                                    if (!info.get(TOKEN_KEY).equals(token)) {
                                                        docRefCurrent.update(TOKEN_KEY, token);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Token Not Validated!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            Intent homePage = new Intent(LoginInActivity.this, WelcomePage.class);
                            startActivity(homePage);
                            finish();

                            /*
                            if (user.getPhoneNumber() != null) {
                                Intent homePage = new Intent(LoginInActivity.this, WelcomePage.class);
                                startActivity(homePage);
                                finish();
                            }
                            else {
                                Intent verifyPhone = new Intent(LoginInActivity.this, phone_number.class);
                                startActivity(verifyPhone);
                                finish();
                            }



                            */
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            passwordLogin.setError("Incorrect Email and/or Password.");
                            passwordLogin.requestFocus();
                            loginButton.setEnabled(true);
                            return;
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

}