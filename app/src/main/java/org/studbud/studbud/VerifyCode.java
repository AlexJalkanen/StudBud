package org.studbud.studbud;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.reinaldoarrosi.maskededittext.MaskedEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyCode extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private MaskedEditText editTextCode;
    private String codeSent, phoneNumber;
    private PhoneAuthProvider.ForceResendingToken mToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    private String emailID;
    private String passwordID;
    private AuthCredential credential;

    private FirebaseUser user;

    @Override
    public void onStart() {
        super.onStart();
        sendVerificationCode();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        mAuth = FirebaseAuth.getInstance();

        editTextCode = findViewById(R.id.verificationCode);

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phone_number");


        findViewById(R.id.VerifyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });
        findViewById(R.id.ResendCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });
    }

    private void verifySignInCode() {
        String code = editTextCode.getText(true).toString();
        int codeLength = 6;
        user = mAuth.getCurrentUser();
        Toast.makeText(getApplicationContext(), user.getEmail(),
                Toast.LENGTH_LONG).show();

        if (code.length() != codeLength) {
            editTextCode.setError("Invalid Code.");
            editTextCode.requestFocus();
            return;
        }
        if (!codeSent.isEmpty()) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Make new activity

                            Toast.makeText(getApplicationContext(), "Verification Successful.",
                                    Toast.LENGTH_LONG).show();
                            Intent homePage = new Intent(VerifyCode.this, WelcomePage.class);
                            startActivity(homePage);
                            finish();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                editTextCode.setError("Invalid Code.");
                                editTextCode.requestFocus();
                                return;
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error Verifying Code.",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
    }

    public void setVerCode(String code) {
        this.codeSent = code;
        return;
    }
    public void setToken(PhoneAuthProvider.ForceResendingToken token) {
        this.mToken = token;
        return;
    }
    public void setPhoneNumber(String phone) {
        this.phoneNumber = phone;
        return;
    }

    private void sendVerificationCode() {


        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Toast.makeText(getApplicationContext(), "Auto Verified.",
                        Toast.LENGTH_LONG).show();
                mAuth.getCurrentUser().updatePhoneNumber(credential);
                Intent homePage = new Intent(VerifyCode.this, WelcomePage.class);
                startActivity(homePage);
                finish();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(getApplicationContext(), "Invalid Phone Number.",
                            Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(getApplicationContext(), "Error Verifying Phone.",
                            Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getApplicationContext(), "Invalid Phone Number.",
                        Toast.LENGTH_LONG).show();
                return;
                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                setToken(token);
                setVerCode(verificationId);


            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallBacks,mToken);

    }
}
