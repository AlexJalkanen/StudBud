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

public class FindStudBudSubjectFragment extends Fragment {

    private FindStudBudSubjectFragmentListener listener;
    private TextView num;
    private int numberOfPeople;
    private ImageButton back;
    private EditText subject;
    private Button next;


    public interface FindStudBudSubjectFragmentListener {
        void onSubjectSent(int number, String subject);
        void backToMainFeed();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_studbud_subject, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        num = view.findViewById(R.id.numberOfPeople);
        back = view.findViewById(R.id.find_studBud_subject_back_button);
        subject = view.findViewById(R.id.subjectText);
        next = view.findViewById(R.id.numberBudsSubjectContinueButton);

        numberOfPeople = 999;
        if (getArguments() != null) {
            numberOfPeople = getArguments().getInt("numberOfPeople");
        }
        updateNumberOfPeopleText(numberOfPeople);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectString = subject.getText().toString();
                if (subjectString.length() <= 1) {
                    Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                    subject.startAnimation(shake);
                    subject.setError("Please Enter a Subject.");
                    return;
                }
                listener.onSubjectSent(numberOfPeople,subjectString);
            }
        });


        return view;
    }

    public void updateNumberOfPeopleText(int number) {
        if (number == 0) {
            num.setText("Studying with either a StudBud or StudGroup.");
        }
        else if (number == 1) {
            num.setText("You are studying with a StudBud.");
        }
        else if (number == 2) {
            num.setText("You are studying with a StudGroup.");
        }
        else {
            num.setText("Error choosing StudBuds!");
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudSubjectFragmentListener) {
            listener = (FindStudBudSubjectFragmentListener)context;
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
