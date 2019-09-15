package org.studbud.studbud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.studbud.studbud.WelcomePage.EMAIL_KEY;
import static org.studbud.studbud.WelcomePage.LOCATION_KEY;
import static org.studbud.studbud.WelcomePage.NAME_KEY;
import static org.studbud.studbud.WelcomePage.NUMBER_OF_PEOPLE_KEY;
import static org.studbud.studbud.WelcomePage.PAIR_KEY;
import static org.studbud.studbud.WelcomePage.PAIR_NAME_KEY;
import static org.studbud.studbud.WelcomePage.SUBJECT_KEY;
import static org.studbud.studbud.WelcomePage.TIME_KEY;

public class MainFeedFragment extends Fragment {

    private MainFeedFragmentListener listener;
    private TextView feed1, groupText, pairSubject, cancelRequest;
    private DocumentReference docRef;
    private FirebaseAuth mAuth;
    private RelativeLayout rel1, rel2;
    private RelativeLayout loading;
    private LinearLayout layout, requestResult;

    private String name, email;

    private CollectionReference colRef;
    private String otherUserName, otherUserEmail, otherUserSubject, otherUserTime, otherUserPair, otherUserLocation;
    private long otherUserNumber;
    private int layoutIDCounter = 1;
    private List<LinearLayout> userLayouts = new ArrayList<>();
    private TextView sorryText, loadingText, pairTitle, endTitle;
    private Button endButton;
    private ImageView userImage, pairImage;

    public interface MainFeedFragmentListener {
        void onPairRequestAccepted(int number, String subject, String time, String pair, String name, String email, String location);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_feed, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        feed1 = view.findViewById(R.id.pairText);
        groupText = view.findViewById(R.id.groupText);
        pairSubject = view.findViewById(R.id.pairSubject);
        rel1 = view.findViewById(R.id.FirstLayout);
        rel2 = view.findViewById(R.id.SecondLayout);
        loading = view.findViewById(R.id.loadingPanel);
        sorryText = view.findViewById(R.id.sorry);
        loadingText = view.findViewById(R.id.loading);
        layout = view.findViewById(R.id.dynamicResults);
        pairTitle = view.findViewById(R.id.pairTitle);
        endTitle = view.findViewById(R.id.endTitle);
        endButton = view.findViewById(R.id.endButton);
        userImage = view.findViewById(R.id.userImage);
        pairImage = view.findViewById(R.id.pairImage);
        requestResult = view.findViewById(R.id.requestResult);
        cancelRequest = view.findViewById(R.id.cancelRequest);

        rel1.setVisibility(View.GONE);
        rel2.setVisibility(View.GONE);
        userImage.setVisibility(View.GONE);
        pairImage.setVisibility(View.GONE);
        groupText.setVisibility(View.GONE);
        pairSubject.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);
        pairTitle.setVisibility(View.GONE);
        endTitle.setVisibility(View.GONE);
        endButton.setVisibility(View.GONE);
        requestResult.setVisibility(View.GONE);
        cancelRequest.setVisibility(View.GONE);
        endButton.setEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        name = mAuth.getCurrentUser().getDisplayName();
        email = mAuth.getCurrentUser().getEmail();



        setFeed();

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endButton.setEnabled(false);
                final DocumentReference docRefUser;
                docRefUser = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + email);
                docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                Map<String, Object> info = doc.getData();
                                final String pairEmail = info.get(PAIR_KEY).toString();
                                String time = info.get(TIME_KEY).toString();
                                final String location = info.get(LOCATION_KEY).toString();
                                final String subject = info.get(SUBJECT_KEY).toString();
                                final long numberOfPeople = (long) info.get(NUMBER_OF_PEOPLE_KEY);

                                Calendar calendar = Calendar.getInstance();
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int month = calendar.get(Calendar.MONTH);
                                int year = calendar.get(Calendar.YEAR);
                                String date = String.valueOf(month) + "/" + String.valueOf(day) + "/" + String.valueOf(year);

                                final String dateAndTime = date + " at " + time;

                                DocumentReference docRefPair = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + pairEmail);
                                docRefPair.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            Map<String, Object> info = doc.getData();
                                            String pairName = info.get(NAME_KEY).toString();

                                            createHistory(email, pairEmail, name, pairName, dateAndTime, location, subject, numberOfPeople);
                                            deleteDocuments(email, pairEmail);
                                        }
                                    }
                                });

                            }

                        }
                    }
                });
                //Todo Notify Partner of such deletion
                Toast.makeText(getActivity(), "You have ended your studying session.", Toast.LENGTH_LONG).show();



            }
        });

        cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest.setEnabled(false);
                final DocumentReference docRefUser;
                docRefUser = FirebaseFirestore.getInstance().document("StudyingInNeedOfBuddying/" + email);
                docRefUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), "Your request has been canceled!", Toast.LENGTH_LONG).show();
                        layout.removeAllViews();
                        setFeed();
                    }
                });
            }
        });

        return view;
    }

    public void deleteDocuments(String emailAddress, String PairAddress) {
        final DocumentReference docRefUser;
        docRefUser = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + emailAddress);
        docRefUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setFeed();
            }
        });

        final DocumentReference docRefPairUpdate;
        docRefPairUpdate = FirebaseFirestore.getInstance().document("StudyingInNeedOfBuddying/" + PairAddress);

        final DocumentReference docRefPair;
        docRefPair = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + PairAddress);
        docRefPair.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    Map<String, Object> info = doc.getData();
                    info.put(PAIR_KEY, "NONE");
                    docRefPairUpdate.set(info).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                docRefPair.delete();
                            }
                        }
                    });
                }
            }
        });
    }

    public void createHistory(final String email, final String pairEmail, final String name, final String pairName,
                              final String time, final String location, final String subject, final long numberOfPeople) {



        final DocumentReference docRefUser;
        docRefUser = FirebaseFirestore.getInstance().document("History/" + email);
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot historyDoc = task.getResult();

                if (historyDoc.exists()) {
                    Map<String, Object> info = historyDoc.getData();
                    int size = info.size();
                    Map<String, String> data = new HashMap<>();
                    data.put(PAIR_KEY, pairEmail);
                    data.put(PAIR_NAME_KEY, pairName);
                    data.put(TIME_KEY, time);
                    data.put(LOCATION_KEY, location);
                    data.put(SUBJECT_KEY, subject);
                    data.put(NUMBER_OF_PEOPLE_KEY, String.valueOf(numberOfPeople));

                    info.put(String.valueOf(size + 1), data);
                    docRefUser.set(info);
                }
                else {
                    Map<String, Object> info = new HashMap<>();
                    Map<String, String> data = new HashMap<>();
                    data.put(PAIR_KEY, pairEmail);
                    data.put(PAIR_NAME_KEY, pairName);
                    data.put(TIME_KEY, time);
                    data.put(LOCATION_KEY, location);
                    data.put(SUBJECT_KEY, subject);
                    data.put(NUMBER_OF_PEOPLE_KEY, String.valueOf(numberOfPeople));

                    info.put("1", data);
                    docRefUser.set(info);
                }

            }
        });
        final DocumentReference docRefPair;
        docRefPair = FirebaseFirestore.getInstance().document("History/" + pairEmail);
        docRefPair.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot historyDoc = task.getResult();
                if (historyDoc.exists()) {
                    Map<String, Object> info = historyDoc.getData();
                    int size = info.size();
                    Map<String, String> data = new HashMap<>();
                    data.put(PAIR_KEY, email);
                    data.put(PAIR_NAME_KEY, name);
                    data.put(TIME_KEY, time);
                    data.put(LOCATION_KEY, location);
                    data.put(SUBJECT_KEY, subject);
                    data.put(NUMBER_OF_PEOPLE_KEY, String.valueOf(numberOfPeople));

                    info.put(String.valueOf(size + 1), data);
                    docRefPair.set(info);
                }
                else {
                    Map<String, Object> info = new HashMap<>();
                    Map<String, String> data = new HashMap<>();
                    data.put(PAIR_KEY, email);
                    data.put(PAIR_NAME_KEY, name);
                    data.put(TIME_KEY, time);
                    data.put(LOCATION_KEY, location);
                    data.put(SUBJECT_KEY, subject);
                    data.put(NUMBER_OF_PEOPLE_KEY, String.valueOf(numberOfPeople));

                    info.put("1", data);
                    docRefPair.set(info);
                }

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudPairFragment.FindStudBudPairFragmentListener) {
            listener = (MainFeedFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " Must implement FindStudBudFragmentListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void setFeed() {

        endButton.setVisibility(View.GONE);
        endTitle.setVisibility(View.GONE);
        userImage.setVisibility(View.GONE);
        pairImage.setVisibility(View.GONE);
        groupText.setVisibility(View.GONE);
        pairSubject.setVisibility(View.GONE);
        cancelRequest.setVisibility(View.GONE);
        requestResult.setVisibility(View.GONE);
        feed1.setGravity(Gravity.CENTER);
        feed1.setTextSize(18);

        String email = "", name = "";
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
            name = mAuth.getCurrentUser().getDisplayName();
        }
        final String emailAddress = email;
        final String nameOfUser = name;



        docRef = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String pairEmail = String.valueOf(document.get(PAIR_KEY));
                        DocumentReference docRefPair = FirebaseFirestore.getInstance().document("CurrentlyStudying/" + pairEmail);

                        docRefPair.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot pairDoc = task.getResult();

                                Map<String, Object> info = pairDoc.getData();
                                long otherUserNumber = (long)info.get(NUMBER_OF_PEOPLE_KEY);
                                String otherUserSubject = info.get(SUBJECT_KEY).toString();
                                String otherUserTime = info.get(TIME_KEY).toString();
                                String otherUserLocation = info.get(LOCATION_KEY).toString();
                                String otherUserName = info.get(NAME_KEY).toString();
                                String otherUserEmailAddress = info.get(EMAIL_KEY).toString();


                                Calendar calendar = Calendar.getInstance();
                                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                                int currentMinute = calendar.get(Calendar.MINUTE);
                                boolean alreadyStudying = false;

                                String hour = otherUserTime.substring(0, 2);
                                int hourInt = Integer.parseInt(hour);
                                String minute = otherUserTime.substring(3,5);
                                int minuteInt = Integer.parseInt(minute);

                                if (otherUserTime.endsWith("PM")) {
                                    hourInt += 12;
                                }

                                if (hourInt < currentHour || (hourInt == currentHour && minuteInt <= currentMinute)){
                                    alreadyStudying = true;
                                }
                                String[] nameSplit = otherUserName.split("\\s+");
                                String firstName = nameSplit[0];
                                String setTimeText;
                                String setPairSubject;
                                if (alreadyStudying) {
                                    setTimeText = "\n\nYou and " + firstName + " are currently studying " + otherUserSubject + "!";
                                    setPairSubject = "\n\nYou're studying at: " + otherUserLocation;
                                }
                                else {
                                    setTimeText = "\n\nYou and " + firstName + " are planning on studying " + otherUserSubject + "!";
                                    setPairSubject = "\n\nYou're studying at: " + otherUserLocation + " at " + otherUserTime;
                                }



                                String setGroupText;
                                if (otherUserNumber == 1) {
                                    setGroupText = ("\n\n(This is a StudBud)\n\n");
                                }
                                else if (otherUserNumber == 2) {
                                    setGroupText = ("\n\n(This is a StudGroup)\n\n");
                                }
                                else {
                                    setGroupText = "\n\n(This could be either a StudBud or a StudGroup)\n\n";
                                }

                                mAuth = FirebaseAuth.getInstance();
                                String emailAddress = mAuth.getCurrentUser().getEmail();

                                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                                StorageReference filePathUser = mStorageRef.child("ProfilePictures").child(emailAddress);
                                GlideApp.with(getContext())
                                        .load(filePathUser)
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(userImage);

                                StorageReference filePathPair = mStorageRef.child("ProfilePictures").child(otherUserEmailAddress);
                                GlideApp.with(getContext())
                                        .load(filePathPair)
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(pairImage);

                                feed1.setText(setTimeText);
                                groupText.setText(setGroupText);
                                pairSubject.setText(setPairSubject);
                                userImage.setVisibility(View.VISIBLE);
                                pairImage.setVisibility(View.VISIBLE);
                                endTitle.setVisibility(View.VISIBLE);
                                endButton.setVisibility(View.VISIBLE);
                                groupText.setVisibility(View.VISIBLE);
                                pairSubject.setVisibility(View.VISIBLE);

                            }
                        });



                    }
                    else {

                        docRef = FirebaseFirestore.getInstance().document("StudyingInNeedOfBuddying/" + emailAddress);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> info = document.getData();
                                        long numberOfPeople = (long)info.get(NUMBER_OF_PEOPLE_KEY);
                                        String subject = info.get(SUBJECT_KEY).toString();
                                        String time = info.get(TIME_KEY).toString();
                                        String location = info.get(LOCATION_KEY).toString();
                                        String[] nameSplit = nameOfUser.split("\\s+");
                                        String firstName = nameSplit[0];

                                        String group = "StudBud or StudGroup";
                                        if (numberOfPeople == 1) {
                                            group = "StudBud";
                                        }
                                        else if (numberOfPeople == 2) {
                                            group = "StudGroup";
                                        }
                                        String text = "\n\n\n" + firstName + ", this is your study request:";


                                        Calendar calendar = Calendar.getInstance();
                                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                                        int currentMinute = calendar.get(Calendar.MINUTE);
                                        boolean alreadyStudying = false;

                                        String hour = time.substring(0, 2);
                                        int hourInt = Integer.parseInt(hour);
                                        String minute = time.substring(3,5);
                                        int minuteInt = Integer.parseInt(minute);

                                        if (time.endsWith("PM")) {
                                            hourInt += 12;
                                        }
                                        if (hourInt < currentHour || (hourInt == currentHour && minuteInt <= currentMinute)){
                                            alreadyStudying = true;
                                        }


                                        final LinearLayout userLayout = new LinearLayout(getContext());
                                        userLayout.setOrientation(LinearLayout.VERTICAL);

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        params.setMargins(0,0,0,0);

                                        String timeTense2;
                                        if (alreadyStudying) {
                                            timeTense2 = "    Since: ";
                                        }
                                        else {
                                            timeTense2 = "    At: ";
                                        }

                                        userLayout.setBackgroundResource(R.drawable.selector_background);
                                        float scale = getResources().getDisplayMetrics().density;
                                        final int dpAsPixels = (int) (scale + 0.5f);
                                        final int textPad = dpAsPixels * 10;
                                        userLayout.setGravity(Gravity.START);
                                        userLayout.setTag(R.string.button_flag_one, otherUserEmail);
                                        userLayout.setTag(R.string.button_flag_two, otherUserLocation);
                                        userLayout.setTag(R.string.button_flag_three, otherUserSubject);
                                        userLayout.setTag(R.string.button_flag_four, otherUserTime);


                                        SpannableStringBuilder builder = new SpannableStringBuilder();

                                        SpannableString nameText = new SpannableString(nameOfUser);
                                        nameText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, nameText.length(), 0);
                                        builder.append("   ");
                                        builder.append(nameText);

                                        SpannableString orText = new SpannableString("  |  ");
                                        orText.setSpan(new StyleSpan(Typeface.BOLD), 0, orText.length(), 0);
                                        builder.append(orText);

                                        SpannableString subjectText = new SpannableString(subject);
                                        subjectText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, subjectText.length(), 0);
                                        builder.append(subjectText);


                                        SpannableStringBuilder infoBuilder = new SpannableStringBuilder();

                                        SpannableString timeText = new SpannableString(group);
                                        timeText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, timeText.length(), 0);
                                        infoBuilder.append("    ");
                                        infoBuilder.append(timeText);

                                        SpannableString timeText2 = new SpannableString(timeTense2);
                                        timeText2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, timeTense2.length(), 0);
                                        infoBuilder.append("\n");
                                        infoBuilder.append(timeText2);

                                        String formattedTime = time;
                                        if (formattedTime.charAt(0) == '0') {
                                            formattedTime = formattedTime.substring(1);
                                        }
                                        SpannableString time2 = new SpannableString(formattedTime);
                                        time2.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, time2.length(), 0);
                                        infoBuilder.append(time2);

                                        SpannableString atText = new SpannableString(" at: ");
                                        atText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, atText.length(), 0);
                                        infoBuilder.append(atText);

                                        SpannableString locationText = new SpannableString(location);
                                        locationText.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, location.length(), 0);
                                        infoBuilder.append(locationText);



                                        TextView textViewName = new TextView(getContext());
                                        textViewName.setText(builder, TextView.BufferType.SPANNABLE);
                                        textViewName.setAllCaps(false);
                                        textViewName.setPadding(textPad, textPad, textPad, 0);
                                        textViewName.setTextSize(dpAsPixels * 5);


                                        TextView textViewInfo = new TextView(getContext());
                                        textViewInfo.setText(infoBuilder, TextView.BufferType.SPANNABLE);
                                        textViewInfo.setAllCaps(false);
                                        textViewInfo.setPadding(textPad, textPad, textPad ,textPad);
                                        textViewInfo.setTextSize(dpAsPixels * 4);

                                        LinearLayout textViews = new LinearLayout(getContext());
                                        textViews.setOrientation(LinearLayout.VERTICAL);
                                        textViews.setLayoutParams(params);
                                        textViews.addView(textViewName);
                                        textViews.addView(textViewInfo);

                                        ImageView img = new ImageView(getContext());
                                        img.setImageResource(R.drawable.user);
                                        final int width = 50;
                                        final int height = 50;
                                        img.setMaxWidth(width);
                                        img.setMaxHeight(height);

                                        LinearLayout.LayoutParams imageParam =new LinearLayout.LayoutParams(175, 175);
                                        imageParam.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                                        imageParam.leftMargin = 40;

                                        img.setLayoutParams(imageParam);

                                        LinearLayout picAndInfo = new LinearLayout(getContext());
                                        picAndInfo.setOrientation(LinearLayout.HORIZONTAL);

                                        picAndInfo.addView(img);
                                        picAndInfo.addView(textViews);

                                        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                                        StorageReference filePath = mStorageRef.child("ProfilePictures").child(emailAddress);
                                        GlideApp.with(getContext())
                                                .load(filePath)
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user)
                                                .apply(RequestOptions.circleCropTransform())
                                                .into(img);



                                        userLayout.addView(picAndInfo);
                                        userLayout.setClickable(true);
                                        userLayout.setFocusable(true);


                                        userLayout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                    // If we're running on Honeycomb or newer, then we can use the Theme's
                                                    // selectableItemBackground to ensure that the View has a pressed state
                                                    TypedValue outValue = new TypedValue();
                                                    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                                                    userLayout.setBackgroundResource(outValue.resourceId);

                                                }

                                            }
                                        });

                                        View v = new View(getContext());
                                        LinearLayout.LayoutParams vParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,4);
                                        vParams.setMargins(0,20,0,0);
                                        v.setLayoutParams(vParams);
                                        v.setBackgroundColor(Color.parseColor("#B3B3B3"));

                                        View v2 = new View(getContext());
                                        v2.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                4));
                                        v2.setBackgroundColor(Color.parseColor("#B3B3B3"));


                                        requestResult.addView(v);
                                        requestResult.addView(userLayout, params);
                                        requestResult.addView(v2);



                                        feed1.setText(text);
                                        feed1.setTextSize(14);
                                        feed1.setGravity(Gravity.START);
                                        requestResult.setVisibility(View.VISIBLE);
                                        cancelRequest.setVisibility(View.VISIBLE);
                                        cancelRequest.setClickable(true);
                                        cancelRequest.setFocusable(true);

                                    }
                                    else {
                                        feed1.setText("\n\n\n Not currently in a studying session. \n\n Find a StudBud!\n");
                                    }
                                }
                            }
                        });

                        getOtherUsers(layout);
                    }
                }
                else {
                    feed1.setText("\n\n\n Not currently in a studying session. \n\n Find a StudBud!\n");
                    getOtherUsers(layout);

                }

                makeFeedVisible();
            }
        });
    }

    public void makeFeedVisible() {
        loading.setVisibility(View.GONE);
        rel1.setVisibility(View.VISIBLE);
        rel2.setVisibility(View.VISIBLE);
    }

    public void getOtherUsers(final LinearLayout layout) {
        pairTitle.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);


        colRef = FirebaseFirestore.getInstance().collection("StudyingInNeedOfBuddying");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    loadingText.setVisibility(View.GONE);
                    if (task.getResult().size() == 0) {
                        sorryText.setVisibility(View.VISIBLE);
                    }

                    for (DocumentSnapshot document: task.getResult()){
                        Map<String, Object> info = document.getData();
                        otherUserEmail = document.getId();
                        otherUserName = info.get(NAME_KEY).toString();
                        otherUserNumber = (long)info.get(NUMBER_OF_PEOPLE_KEY);
                        otherUserSubject = info.get(SUBJECT_KEY).toString();
                        otherUserTime = info.get(TIME_KEY).toString();
                        otherUserPair = info.get(PAIR_KEY).toString();
                        otherUserLocation = info.get(LOCATION_KEY).toString();

                        if (otherUserEmail.equals(mAuth.getCurrentUser().getEmail()) && task.getResult().size() == 1) {
                            sorryText.setVisibility(View.VISIBLE);
                        }
                        else {
                            sorryText.setVisibility(View.GONE);
                        }

                        if (otherUserEmail.equals(mAuth.getCurrentUser().getEmail())) {
                            continue;
                        }

                        String otherSchoolEmail = otherUserEmail.substring(otherUserEmail.lastIndexOf("@") + 1);
                        String userSchoolEmail = email.substring(email.lastIndexOf("@") + 1);
                        if (!userSchoolEmail.equals(otherSchoolEmail)) {
                            continue;
                        }

                        String group = "    StudBud or StudGroup";
                        if (otherUserNumber == 1) {
                            group = "    StudBud";
                        }
                        else if (otherUserNumber == 2) {
                            group = "    StudGroup";
                        }

                        Calendar calendar = Calendar.getInstance();
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = calendar.get(Calendar.MINUTE);
                        boolean alreadyStudying = false;

                        String hour = otherUserTime.substring(0, 2);
                        int hourInt = Integer.parseInt(hour);
                        String minute = otherUserTime.substring(3,5);
                        int minuteInt = Integer.parseInt(minute);

                        if (otherUserTime.endsWith("PM")) {
                            hourInt += 12;
                        }

                        if (hourInt < currentHour || (hourInt == currentHour && minuteInt <= currentMinute)){
                            alreadyStudying = true;
                        }

                        if (otherUserPair.equals("NONE")) {

                            final LinearLayout userLayout = new LinearLayout(getContext());
                            userLayout.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0,0,0,0);

                            String timeTense2;
                            if (alreadyStudying) {
                                timeTense2 = "    Since: ";
                            }
                            else {
                                timeTense2 = "    At: ";
                            }

                            userLayout.setBackgroundResource(R.drawable.selector_background);
                            float scale = getResources().getDisplayMetrics().density;
                            final int dpAsPixels = (int) (scale + 0.5f);
                            final int textPad = dpAsPixels * 10;
                            userLayout.setGravity(Gravity.LEFT);
                            userLayout.setId(layoutIDCounter);
                            userLayout.setTag(R.string.button_flag_one, otherUserEmail);
                            userLayout.setTag(R.string.button_flag_two, otherUserLocation);
                            userLayout.setTag(R.string.button_flag_three, otherUserSubject);
                            userLayout.setTag(R.string.button_flag_four, otherUserTime);


                            SpannableStringBuilder builder = new SpannableStringBuilder();

                            SpannableString nameText = new SpannableString(otherUserName);
                            nameText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, nameText.length(), 0);
                            builder.append("   " + nameText);

                            SpannableString orText = new SpannableString("  |  ");
                            orText.setSpan(new StyleSpan(Typeface.BOLD), 0, orText.length(), 0);
                            builder.append(orText);

                            SpannableString groupText = new SpannableString(otherUserSubject);
                            groupText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, groupText.length(), 0);
                            builder.append(groupText);


                            SpannableStringBuilder infoBuilder = new SpannableStringBuilder();

                            SpannableString timeText = new SpannableString(group);
                            timeText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, timeText.length(), 0);
                            infoBuilder.append(timeText);

                            SpannableString timeText2 = new SpannableString(timeTense2);
                            timeText2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, timeTense2.length(), 0);
                            infoBuilder.append("\n");
                            infoBuilder.append(timeText2);

                            String formattedTime = otherUserTime;
                            if (formattedTime.charAt(0) == '0') {
                                formattedTime = formattedTime.substring(1);
                            }
                            SpannableString time = new SpannableString(formattedTime);
                            time.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, otherUserTime.length(), 0);
                            infoBuilder.append(time);

                            SpannableString atText = new SpannableString(" at: ");
                            atText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, atText.length(), 0);
                            infoBuilder.append(atText);

                            SpannableString locationText = new SpannableString(otherUserLocation);
                            locationText.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, otherUserLocation.length(), 0);
                            infoBuilder.append(locationText);



                            TextView textViewName = new TextView(getContext());
                            textViewName.setText(builder, TextView.BufferType.SPANNABLE);
                            textViewName.setAllCaps(false);
                            textViewName.setPadding(textPad, textPad, textPad, 0);
                            textViewName.setTextSize(dpAsPixels * 5);


                            TextView textViewInfo = new TextView(getContext());
                            textViewInfo.setText(infoBuilder, TextView.BufferType.SPANNABLE);
                            textViewInfo.setAllCaps(false);
                            textViewInfo.setPadding(textPad, textPad, textPad ,textPad);
                            textViewInfo.setTextSize(dpAsPixels * 4);

                            LinearLayout textViews = new LinearLayout(getContext());
                            textViews.setOrientation(LinearLayout.VERTICAL);
                            textViews.setLayoutParams(params);
                            textViews.addView(textViewName);
                            textViews.addView(textViewInfo);

                            ImageView img = new ImageView(getContext());
                            img.setImageResource(R.drawable.user);
                            final int width = 50;
                            final int height = 50;
                            img.setMaxWidth(width);
                            img.setMaxHeight(height);

                            LinearLayout.LayoutParams imageParam =new LinearLayout.LayoutParams(175, 175);
                            imageParam.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                            imageParam.leftMargin = 40;

                            img.setLayoutParams(imageParam);

                            LinearLayout picAndInfo = new LinearLayout(getContext());
                            picAndInfo.setOrientation(LinearLayout.HORIZONTAL);

                            picAndInfo.addView(img);
                            picAndInfo.addView(textViews);

                            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference filePath = mStorageRef.child("ProfilePictures").child(otherUserEmail);
                            GlideApp.with(getContext())
                                    .load(filePath)
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(img);



                            userLayout.addView(picAndInfo);
                            userLayout.setClickable(true);
                            userLayout.setFocusable(true);


                            userLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        // If we're running on Honeycomb or newer, then we can use the Theme's
                                        // selectableItemBackground to ensure that the View has a pressed state
                                        TypedValue outValue = new TypedValue();
                                        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                                        userLayout.setBackgroundResource(outValue.resourceId);

                                    }
                                    userLayout.setEnabled(false);
                                    disableAllButtons();
                                    String pairEmail = String.valueOf(userLayout.getTag(R.string.button_flag_one));
                                    String location = String.valueOf(userLayout.getTag(R.string.button_flag_two));
                                    String subject = String.valueOf(userLayout.getTag(R.string.button_flag_three));
                                    String time = String.valueOf(userLayout.getTag(R.string.button_flag_four));
                                    listener.onPairRequestAccepted((int)otherUserNumber, subject, time, pairEmail, name, email, location);

                                }
                            });


                            userLayouts.add(userLayout);



                            counter();

                            View v = new View(getContext());
                            LinearLayout.LayoutParams vParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,4);
                            vParams.setMargins(0,20,0,0);
                            v.setLayoutParams(vParams);
                            v.setBackgroundColor(Color.parseColor("#B3B3B3"));

                            View v2 = new View(getContext());
                            v2.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    4));
                            v2.setBackgroundColor(Color.parseColor("#B3B3B3"));


                            layout.addView(v);
                            layout.addView(userLayout, params);
                            layout.addView(v2);
                        }

                    }
                    if (layoutIDCounter == 1) {
                        sorryText.setVisibility(View.VISIBLE);
                    }

                }


            }
        });





    }

    public void counter() {
        layoutIDCounter++;
    }

    public void disableAllButtons() {
        for (LinearLayout l : userLayouts) {
            l.setEnabled(false);
        }

    }



}
