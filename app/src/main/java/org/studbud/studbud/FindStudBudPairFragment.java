package org.studbud.studbud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.studbud.studbud.WelcomePage.LOCATION_KEY;
import static org.studbud.studbud.WelcomePage.NUMBER_OF_PEOPLE_KEY;
import static org.studbud.studbud.WelcomePage.PAIR_KEY;
import static org.studbud.studbud.WelcomePage.SUBJECT_KEY;
import static org.studbud.studbud.WelcomePage.TIME_KEY;
import static org.studbud.studbud.WelcomePage.NAME_KEY;



public class FindStudBudPairFragment extends Fragment {

    private FindStudBudPairFragmentListener listener;
    private int numberOfPeople;
    private String name, email;
    private ImageButton back;
    private Button makeMyOwn, selectPair;

    private CollectionReference colRef;
    private FirebaseAuth mAuth;
    private String otherUserName, otherUserEmail, otherUserSubject, otherUserTime, otherUserPair, otherUserLocation;
    private long otherUserNumber;
    private int layoutIDCounter = 1;
    private List<LinearLayout> userLayouts = new ArrayList<>();
    private LinearLayout layoutSelected;
    private TextView sorryText, loadingText;



    public interface FindStudBudPairFragmentListener {
        void onPairRequestSent(int number);
        void onPairRequestAccepted(int number, String subject, String time, String pair, String name, String email, String location);
        void backToMainFeed();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_studbud_pair, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        LinearLayout linearLayout = view.findViewById(R.id.dynamicResults);

        back = view.findViewById(R.id.find_studBud_subject_back_button);
        makeMyOwn = view.findViewById(R.id.numberBudsPairContinue);
        selectPair = view.findViewById(R.id.selectStudBudPair);
        selectPair.setEnabled(false);
        sorryText = view.findViewById(R.id.sorry);
        loadingText = view.findViewById(R.id.loading);


        mAuth = FirebaseAuth.getInstance();
        name = "NULL";
        email = "NULL";
        if (mAuth.getCurrentUser() != null) {
            name = mAuth.getCurrentUser().getDisplayName();
            email = mAuth.getCurrentUser().getEmail();
        }




        numberOfPeople = -1;
        if (getArguments() != null) {
            numberOfPeople = getArguments().getInt("numberOfPeople");

        }

        getOtherUsers(linearLayout);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });

        makeMyOwn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPairRequestSent(numberOfPeople);
            }
        });

        selectPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutSelected == null) {
                    //Set some error
                    return;
                }
                selectPair.setEnabled(false);
                String pairEmail = String.valueOf(layoutSelected.getTag(R.string.button_flag_one));
                String location = String.valueOf(layoutSelected.getTag(R.string.button_flag_two));
                String subject = String.valueOf(layoutSelected.getTag(R.string.button_flag_three));
                String time = String.valueOf(layoutSelected.getTag(R.string.button_flag_four));
                listener.onPairRequestAccepted(numberOfPeople, subject, time, pairEmail, name, email, location);
            }
        });





        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudPairFragmentListener) {
            listener = (FindStudBudPairFragmentListener)context;
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

    public void getOtherUsers(final LinearLayout layout) {


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

                        if (numberOfPeople == 1) {
                            if (otherUserNumber == 2) {
                                continue;
                            }
                        }
                        else if (numberOfPeople == 2) {
                            if (otherUserNumber == 1) {
                                continue;
                            }
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

                            LinearLayout userLayout = new LinearLayout(getContext());
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


                            final LinearLayout userLayoutToAdd = userLayout;
                            userLayoutToAdd.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        // If we're running on Honeycomb or newer, then we can use the Theme's
                                        // selectableItemBackground to ensure that the View has a pressed state
                                        TypedValue outValue = new TypedValue();
                                        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                                        userLayoutToAdd.setBackgroundResource(outValue.resourceId);
                                    }
                                    selectPair.setEnabled(true);
                                    uncheckOtherLayouts(userLayoutToAdd);
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


    public void uncheckOtherLayouts(LinearLayout layout) {
        for (LinearLayout l : userLayouts) {
            if (l.getId() == layout.getId()) {
                layoutSelected = l;
                l.setSelected(true);
                l.setActivated(true);
                l.setBackgroundResource(R.drawable.selector_background);

            }
            else {
                l.setSelected(false);
                l.setActivated(false);
                l.setBackgroundResource(R.drawable.selector_background);
            }
        }
    }






}
