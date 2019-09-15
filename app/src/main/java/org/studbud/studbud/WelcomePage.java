package org.studbud.studbud;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

public class WelcomePage extends AppCompatActivity
        implements FindStudBudFragment.FindStudBudFragmentListener, FindStudBudSubjectFragment.FindStudBudSubjectFragmentListener,
        FindStudBudTimeFragment.FindStudBudTimeFragmentListener, FindStudBudPairFragment.FindStudBudPairFragmentListener,
        FindStudBudRequestedFragment.FindStudBudRequestedFragmentListener, FindStudBudLocationFragment.FindStudBudLocationFragmentListener,
        FindStudBudAcceptedFragment.FindStudBudAcceptedFragmentListener, MainFeedFragment.MainFeedFragmentListener,
        HistoryFragment.HistoryFragmentListener {

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    public String userName, userEmail;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private FindStudBudFragment findStudBudFragment;
    private FindStudBudSubjectFragment findStudBudSubjectFragment;
    private FindStudBudTimeFragment findStudBudTimeFragment;
    private MainFeedFragment mainFeedFragment;
    private FindStudBudPairFragment findStudBudPairFragment;
    private NavigationView navigationView;
    private FindStudBudRequestedFragment findStudBudRequestedFragment;
    private FindStudBudLocationFragment findStudBudLocationFragment;
    private FindStudBudAcceptedFragment findStudBudAcceptedFragment;
    private HistoryFragment historyFragment;

    public static final String NUMBER_OF_PEOPLE_KEY = "numberOfPeople";
    public static final String SUBJECT_KEY = "subject";
    public static final String TIME_KEY = "time";
    public static final String PAIR_KEY = "pair";
    public static final String NAME_KEY = "name";
    public static final String EMAIL_KEY = "email";
    public static final String LOCATION_KEY = "location";
    public static final String TOKEN_KEY = "token";
    public static final String PAIR_NAME_KEY = "pairName";
    private DocumentReference docRefCurrent, docRefAfter, docCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);


        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.menu);
        actionbar.setTitle(Html.fromHtml("<font color='#ffffff'>StudBud</font>"));

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, 0,0);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            userName = user.getDisplayName();
            userEmail = user.getEmail();
        }



        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerTitle(Gravity.BOTTOM, userName);


        navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.nav_name);
        TextView nav_email = hView.findViewById(R.id.nav_email);

        nav_user.setText(userName);
        nav_email.setText(userEmail);

        ImageView profile_pic = hView.findViewById(R.id.headerProfilePic);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference filePath = mStorageRef.child("ProfilePictures").child(mAuth.getCurrentUser().getEmail());
        GlideApp.with(getApplicationContext())
                .load(filePath)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .apply(RequestOptions.circleCropTransform())
                .into(profile_pic);

        mainFeedFragment = new MainFeedFragment();
        findStudBudFragment = new FindStudBudFragment();
        findStudBudSubjectFragment = new FindStudBudSubjectFragment();
        findStudBudTimeFragment = new FindStudBudTimeFragment();
        findStudBudPairFragment = new FindStudBudPairFragment();
        findStudBudRequestedFragment = new FindStudBudRequestedFragment();
        findStudBudLocationFragment = new FindStudBudLocationFragment();
        findStudBudAcceptedFragment = new FindStudBudAcceptedFragment();
        historyFragment = new HistoryFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mainFeedFragment, "MainFeed").commit();
            navigationView.setCheckedItem(R.id.menu_none);
        }



        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_find:
                                docCheck = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + mAuth.getCurrentUser().getEmail());
                                docCheck.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            if (!doc.exists()) {
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        new FindStudBudFragment()).addToBackStack(null).commit();
                                            }
                                            else {
                                                Toast.makeText(WelcomePage.this, "You are currently in a study session.\nPlease end your current session first.", Toast.LENGTH_LONG).show();
                                                navigationView.setCheckedItem(R.id.menu_none);
                                            }
                                        }
                                    }
                                });
                                break;
                            case R.id.nav_previous:
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        historyFragment).addToBackStack(null).commit();
                                break;

                            case R.id.nav_signOut:
                                mDrawerLayout.closeDrawer(Gravity.START, false);
                                mAuth.signOut();
                                Intent signOut = new Intent(WelcomePage.this, LoginInActivity.class);
                                signOut.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(signOut);
                                finish();
                                break;

                        }
                        // set item as selected to persist highlight
                        //menuItem.setChecked(true);
                        // close drawer when item is tapped


                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here


                        mDrawerLayout.closeDrawers();

                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        Fragment currentFragment = manager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FindStudBudRequestedFragment) {
            return;
        }
        if (currentFragment instanceof FindStudBudAcceptedFragment) {
            return;
        }

        super.onBackPressed();
        Fragment newCurrentFragment = manager.findFragmentById(R.id.fragment_container);
        if (newCurrentFragment instanceof MainFeedFragment) {
            navigationView.setCheckedItem(R.id.menu_none);
        }

    }

    @Override
    public void onNumberOfPeopleSent(int number) {
        Bundle bundle = new Bundle();
        bundle.putInt("numberOfPeople", number);
        findStudBudPairFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                findStudBudPairFragment).addToBackStack(null).commit();
    }

    @Override
    public void onSubjectSent(int number, String subject) {
        Bundle bundle = new Bundle();
        bundle.putInt("numberOfPeople", number);
        bundle.putString("subject", subject);
        findStudBudTimeFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                findStudBudTimeFragment).addToBackStack(null).commit();
    }

    @Override
    public void onTimeSent(int number, String subject, String time) {
        Bundle bundle = new Bundle();
        bundle.putInt("numberOfPeople", number);
        bundle.putString("subject", subject);
        bundle.putString("time", time);
        findStudBudLocationFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                findStudBudLocationFragment).addToBackStack(null).commit();
    }

    @Override
    public void onPairRequestSent(int number) {
        Bundle bundle = new Bundle();
        bundle.putInt("numberOfPeople", number);
        findStudBudSubjectFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                findStudBudSubjectFragment).addToBackStack(null).commit();

    }

    @Override
    public void onPairRequestAccepted(int number, String subject, String time, String pair, String name, String email, String location) {

       //Update pair first
        docRefCurrent = FirebaseFirestore.getInstance().document("StudyingInNeedOfBuddying/" + pair);
        docRefAfter = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + pair);

        final String emailAddress = email;
        final String userName = name;
        docRefCurrent.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    docRefAfter.set(doc.getData());
                    Map<String, Object> updatePairData = new HashMap<>();
                    updatePairData.put(PAIR_KEY,emailAddress);
                    docRefAfter.update(updatePairData);
                    docRefCurrent.delete();

                    //Update user with pair info
                    docRefAfter.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                Map<String, Object> info = document.getData();
                                String otherUserEmail = document.getId().toString();
                                long otherUserNumber = (long)info.get(NUMBER_OF_PEOPLE_KEY);
                                String otherUserSubject = info.get(SUBJECT_KEY).toString();
                                String otherUserTime = info.get(TIME_KEY).toString();
                                String otherUserLocation = info.get(LOCATION_KEY).toString();

                                Map<String, Object> dataToSave = new HashMap<>();
                                dataToSave.put(NUMBER_OF_PEOPLE_KEY,otherUserNumber);
                                dataToSave.put(SUBJECT_KEY,otherUserSubject);
                                dataToSave.put(TIME_KEY, otherUserTime);
                                dataToSave.put(PAIR_KEY,otherUserEmail);
                                dataToSave.put(LOCATION_KEY, otherUserLocation);
                                dataToSave.put(NAME_KEY,userName);
                                dataToSave.put(EMAIL_KEY,emailAddress);


                                DocumentReference docRefNew = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + emailAddress);
                                docRefNew.set(dataToSave);

                                docRefCurrent = FirebaseFirestore.getInstance().document("StudyingInNeedOfBuddying/" + emailAddress);
                                docRefCurrent.delete();

                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        findStudBudAcceptedFragment).addToBackStack(null).commit();

                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onSubmitSession(int number, String subject, String time, String pair, String name, String email, String location) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(NUMBER_OF_PEOPLE_KEY,number);
        dataToSave.put(SUBJECT_KEY,subject);
        dataToSave.put(TIME_KEY, time);
        dataToSave.put(PAIR_KEY,pair);
        dataToSave.put(NAME_KEY,name);
        dataToSave.put(EMAIL_KEY,email);
        dataToSave.put(LOCATION_KEY, location);

        docRefCurrent = FirebaseFirestore.getInstance().document("StudyingInNeedOfBuddying/" + userEmail);
        docRefCurrent.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        findStudBudRequestedFragment).addToBackStack(null).commit();
            }
        });
    }
    @Override
    public void backToMainFeed() {
        popBackStackTillEntry(0);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                mainFeedFragment).addToBackStack("MainFeed").commit();
        navigationView.setCheckedItem(R.id.menu_none);
    }



    public void popBackStackTillEntry(int entryIndex) {

        if (getSupportFragmentManager() == null) {
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() <= entryIndex) {
            return;
        }
        FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(
                entryIndex);
        if (entry != null) {
            getSupportFragmentManager().popBackStackImmediate(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }





}
