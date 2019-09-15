package org.studbud.studbud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import static org.studbud.studbud.WelcomePage.LOCATION_KEY;
import static org.studbud.studbud.WelcomePage.NUMBER_OF_PEOPLE_KEY;
import static org.studbud.studbud.WelcomePage.PAIR_KEY;
import static org.studbud.studbud.WelcomePage.PAIR_NAME_KEY;
import static org.studbud.studbud.WelcomePage.SUBJECT_KEY;
import static org.studbud.studbud.WelcomePage.TIME_KEY;


public class HistoryFragment extends Fragment {

    private HistoryFragmentListener listener;
    private FirebaseAuth mAuth;
    private TextView sorryText, loadingText;
    private ImageButton back;
    private int layoutIDCounter = 1;

    public interface HistoryFragmentListener {
        void backToMainFeed();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        LinearLayout linearLayout = view.findViewById(R.id.dynamicResults);
        mAuth = FirebaseAuth.getInstance();

        back = view.findViewById(R.id.history_back_button);
        sorryText = view.findViewById(R.id.sorry);
        loadingText = view.findViewById(R.id.loading);

        loadingText.setVisibility(View.VISIBLE);
        sorryText.setVisibility(View.GONE);

        getOtherUsers(linearLayout);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudPairFragment.FindStudBudPairFragmentListener) {
            listener = (HistoryFragmentListener)context;
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
        loadingText.setVisibility(View.VISIBLE);
        String email = mAuth.getCurrentUser().getEmail();

        DocumentReference docRefUser;
        docRefUser = FirebaseFirestore.getInstance().document("History/" + email);
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        loadingText.setVisibility(View.GONE);
                        Map<String, Object> info = doc.getData();
                        int size = info.size();

                        for (int i = size; i >= 1; i--) {
                            Map<String, String> data = (Map<String, String>)info.get(String.valueOf(i));
                            String location = data.get(LOCATION_KEY);
                            String numberOfPeople = data.get(NUMBER_OF_PEOPLE_KEY);
                            String pairEmail = data.get(PAIR_KEY);
                            String pairName = data.get(PAIR_NAME_KEY);
                            String subject = data.get(SUBJECT_KEY);
                            String time = data.get(TIME_KEY);

                            String[] timeSplit = time.split("\\s+");
                            String date = timeSplit[0];
                            String hour = timeSplit[2] + " " + timeSplit[3];

                            LinearLayout userLayout = new LinearLayout(getContext());
                            userLayout.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0,0,0,0);

                            userLayout.setBackgroundResource(R.drawable.selector_background);
                            float scale = getResources().getDisplayMetrics().density;
                            final int dpAsPixels = (int) (scale + 0.5f);
                            final int textPad = dpAsPixels * 10;
                            userLayout.setGravity(Gravity.START);
                            userLayout.setId(layoutIDCounter);

                            String group = "    StudBud or StudGroup";
                            if (numberOfPeople.equals("1")) {
                                group = "    StudBud";
                            }
                            else if (numberOfPeople.equals("2")) {
                                group = "    StudGroup";
                            }


                            SpannableStringBuilder builder = new SpannableStringBuilder();

                            SpannableString nameText = new SpannableString(pairName);
                            nameText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, nameText.length(), 0);
                            builder.append("   ");
                            builder.append(nameText);

                            SpannableString orText = new SpannableString("  |  ");
                            orText.setSpan(new StyleSpan(Typeface.BOLD), 0, orText.length(), 0);
                            builder.append(orText);

                            SpannableString groupText = new SpannableString(subject);
                            groupText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, groupText.length(), 0);
                            builder.append(groupText);


                            SpannableStringBuilder infoBuilder = new SpannableStringBuilder();

                            SpannableString timeText = new SpannableString(group);
                            timeText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, timeText.length(), 0);
                            infoBuilder.append(timeText);

                            SpannableString timeText2 = new SpannableString("    On: ");
                            timeText2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, timeText2.length(), 0);
                            infoBuilder.append("\n");
                            infoBuilder.append(timeText2);

                            SpannableString timeText3 = new SpannableString(date);
                            timeText3.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, timeText3.length(), 0);
                            infoBuilder.append(timeText3);

                            SpannableString atText1 = new SpannableString(" at: ");
                            atText1.setSpan(new ForegroundColorSpan(Color.GRAY), 0, atText1.length(), 0);
                            infoBuilder.append(atText1);

                            SpannableString timeText4 = new SpannableString(hour);
                            timeText4.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, timeText4.length(), 0);
                            infoBuilder.append(timeText4);

                            SpannableString atText = new SpannableString("    At: ");
                            atText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, atText.length(), 0);
                            infoBuilder.append("\n");
                            infoBuilder.append(atText);

                            SpannableString locationText = new SpannableString(location);
                            locationText.setSpan(new ForegroundColorSpan(Color.rgb(34, 214, 255)), 0, locationText.length(), 0);
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
                            StorageReference filePath = mStorageRef.child("ProfilePictures").child(pairEmail);
                            GlideApp.with(getContext())
                                    .load(filePath)
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(img);



                            userLayout.addView(picAndInfo);
                            userLayout.setClickable(true);
                            userLayout.setFocusable(true);

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
                    else {
                        loadingText.setVisibility(View.GONE);
                        sorryText.setVisibility(View.VISIBLE);
                    }


                }
            }
        });


    }

    public void counter() {
        layoutIDCounter++;
    }



}
