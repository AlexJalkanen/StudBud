package org.studbud.studbud;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class FindStudBudLocationFragment extends Fragment {

    private FindStudBudLocationFragmentListener listener;
    private int numberOfPeople;
    private String subject, time, name, email;
    private ImageButton back;
    private EditText location;
    private Button next;
    private FirebaseAuth mAuth;


    public interface FindStudBudLocationFragmentListener {
        void onSubmitSession(int number, String subject, String time, String pair, String name, String email, String location);
        void backToMainFeed();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_studbud_location, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        back = view.findViewById(R.id.find_studBud_subject_back_button);
        location = view.findViewById(R.id.locationText);
        next = view.findViewById(R.id.locationSubmitButton);
        mAuth = FirebaseAuth.getInstance();

        numberOfPeople = -1;
        subject = "NULL";
        time = "NULL";
        if (getArguments() != null) {
            numberOfPeople = getArguments().getInt("numberOfPeople");
            subject = getArguments().getString("subject");
            time = getArguments().getString("time");
        }
        name = "NULL";
        email = "NULL";
        if (mAuth.getCurrentUser() != null) {
            name = mAuth.getCurrentUser().getDisplayName().toString();
            email = mAuth.getCurrentUser().getEmail().toString();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationString = location.getText().toString();
                if (locationString.length() <= 1) {
                    Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                    location.startAnimation(shake);
                    location.setError("Please Enter a Location.");
                    return;
                }
                next.setEnabled(false);
                String pair = "NONE";
                listener.onSubmitSession(numberOfPeople, subject, time, pair, name, email, locationString);
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudLocationFragmentListener) {
            listener = (FindStudBudLocationFragmentListener)context;
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

}
