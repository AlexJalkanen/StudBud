package org.studbud.studbud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.studbud.studbud.WelcomePage.TOKEN_KEY;

public class Register extends AppCompatActivity implements SelectPhotoDialog.OnPhotoSelectedListener {


    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private EditText firstName, lastName, email, password, cPassword;
    private Button pictureUpload, register;
    public static final int REQUEST_CODE = 1111;
    private ImageView profilePic;
    private Uri selectedUri;
    private ProgressDialog progressDialog;
    private TextView name, emailText, pass1, pass2, pic;

    private static Pattern passwordRange = Pattern
            .compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,32})");

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final ScrollView sv = findViewById(R.id.scrollableContent);


        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.emailReg);
        password = findViewById(R.id.passwordReg);
        cPassword = findViewById(R.id.cPasswordReg);
        pictureUpload = findViewById(R.id.uploadPictureButton);
        profilePic = (ImageView) findViewById(R.id.profilePicDisplay);
        progressDialog = new ProgressDialog(this);
        register = findViewById(R.id.registrationContinue);

        name = findViewById(R.id.NameText);
        emailText = findViewById(R.id.EmailText);
        pass1 = findViewById(R.id.PasswordText);
        pass2 = findViewById(R.id.cPasswordText);
        pic = findViewById(R.id.pictureText);


        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();



        email.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocus();
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.smoothScrollTo(0, emailText.getTop());
                    }
                });
                return false;
            }
        });
        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocus();
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.smoothScrollTo(0, pass1.getTop());
                    }
                });

                return false;
            }
        });
        cPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocus();
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.smoothScrollTo(0, pass2.getTop());
                    }
                });
                return false;
            }
        });

        findViewById(R.id.registrationBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(Register.this, LoginInActivity.class);
                back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(back);
                finish();
            }
        });

        pictureUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!verifyPermissions()) {
                    return;
                }
                showDialog();

            }
        });

        findViewById(R.id.registrationContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstNameString = firstName.getText().toString();
                String lastNameString = lastName.getText().toString();
                String emailString = email.getText().toString();
                String password1 = password.getText().toString();
                String password2 = cPassword.getText().toString();

                firstNameString = firstNameString.replaceAll(" ", "");
                lastNameString = lastNameString.replaceAll(" ", "");
                emailString = emailString.replaceAll(" ", "");

                final String finalFirstNameString = firstNameString;
                final String finalLastNameString = lastNameString;
                final String finalPassword = password1;
                final String finalEmail = emailString;

                if (firstNameString.isEmpty()) {
                    firstName.setError("Please Enter Your First Name.");
                    firstName.requestFocus();
                    return;
                } else if (!firstNameString.matches("[a-zA-Z]+")) {
                    firstName.setError("Please Enter a Valid First Name.");
                    firstName.requestFocus();
                    return;
                } else if (lastNameString.isEmpty()) {
                    lastName.setError("Please Enter Your Last Name.");
                    lastName.requestFocus();
                    return;
                } else if (!lastNameString.matches("[a-zA-Z]+")) {
                    lastName.setError("Please Enter a Valid Last Name.");
                    lastName.requestFocus();
                    return;
                } else if (emailString.isEmpty() || !emailString.contains("@") || !emailString.endsWith(".edu")) {
                    email.setError("Please Enter a Valid .edu Email.");
                    email.requestFocus();
                    return;
                } else if (password1.isEmpty() || !password1.equals(password2)) {
                    cPassword.setError("Passwords Do Not Match.");
                    cPassword.requestFocus();
                    return;
                }
                if (!validatePassword(password1)) {
                    password.setError("Password Must Contain a Number, Uppercase Letter, and Must Be 8-32 Characters.");
                    password.requestFocus();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(emailString, password1).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                boolean accountExists = false;

                                if (!task.isSuccessful())
                                {
                                    try
                                    {
                                        throw task.getException();
                                    }
                                    // if user enters wrong email.
                                    catch (FirebaseAuthWeakPasswordException weakPassword)
                                    {
                                        password.setError("Please Enter a Valid Password.");
                                        password.requestFocus();
                                    }
                                    // if user enters wrong password.
                                    catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                                    {
                                        email.setError("Please Enter a Valid .edu Email.");
                                        email.requestFocus();
                                    }
                                    catch (FirebaseAuthUserCollisionException existEmail)
                                    {
                                        accountExists = true; //Todo
                                        email.setError("Email Already Exists.");
                                        email.requestFocus();
                                    }
                                    catch (Exception e)
                                    {

                                    }
                                }
                                else {
                                    if (mAuth.getCurrentUser() != null) {
                                        mAuth.getCurrentUser().sendEmailVerification();
                                    }
                                    register.setEnabled(false);
                                    signIn(finalEmail, finalPassword, finalFirstNameString, finalLastNameString);
                                }
                            }
                        }
                );
            }
        });
    }

    public boolean validatePassword(String pass) {
        Matcher match = passwordRange.matcher(pass);
        return match.matches();
    }

    public void signIn(final String email, final String password, final String firstName, final String lastName) {
       progressDialog.setMessage("Registering...");
       progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = mAuth.getCurrentUser();


                            StorageReference filePath = mStorageRef.child("ProfilePictures").child(user.getEmail());

                            if (selectedUri != null) {
                                filePath.putFile(selectedUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            //Oh no
                                        }
                                    }
                                });
                            }

                            final StorageReference tokenPath = mStorageRef.child("Tokens").child(user.getEmail());
                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (task.isSuccessful()) {
                                        String token = task.getResult().getToken();

                                        Map<String, Object> dataToSave = new HashMap<>();
                                        dataToSave.put(TOKEN_KEY, token);

                                        DocumentReference docRefCurrent;
                                        docRefCurrent = FirebaseFirestore.getInstance().document("Users/" + user.getEmail());
                                        docRefCurrent.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Yay
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Token Not Generated!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName).build();
                            currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Continue
                                    }
                                }
                            });
                            progressDialog.hide();
                            progressDialog.dismiss();
                            if (!user.isEmailVerified()) {
                                Intent SignIn = new Intent(Register.this, LoginInActivity.class);
                                SignIn.putExtra("EmailAddress",email);
                                SignIn.putExtra("Password", password);
                                SignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(SignIn);
                                finish();
                                return;
                            }

                            Intent welcomePage = new Intent(Register.this, WelcomePage.class);
                            startActivity(welcomePage);

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            register.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error Signing Up, Please Try Again.",
                                    Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private boolean verifyPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        permissions[1]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(Register.this,
                    permissions,REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }

    @Override
    public void getImagePath(Uri imagePath) {
        profilePic.setImageURI(imagePath);
        profilePic.setVisibility(View.VISIBLE);
        selectedUri = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        profilePic.setImageBitmap(bitmap);
        profilePic.setVisibility(View.VISIBLE);
        selectedUri = getImageUri(this, bitmap);

    }

    private void showDialog() {
        SelectPhotoDialog dialog = SelectPhotoDialog.newInstance("Choose a picture");
        dialog.setRetainInstance(true);
        dialog.show(getFragmentManager(), "photo_dialog");
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
