package org.studbud.studbud;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.ImageButton;

public class FindStudBudFragment extends Fragment {

    private Button oneStudBud, studGroup, either, next;
    private ImageButton back;
    private boolean oneStud, group, both;
    private FindStudBudFragmentListener listener;


    public interface FindStudBudFragmentListener {
        void onNumberOfPeopleSent(int number);
        void backToMainFeed();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_studbud, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        oneStudBud = view.findViewById(R.id.oneStudBudButton);
        studGroup = view.findViewById(R.id.studGroupButton);
        either = view.findViewById(R.id.eitherButton);
        back = view.findViewById(R.id.find_studBud_back_button);
        next = view.findViewById(R.id.numberBudsContinueButton);
        oneStud = false;
        group = false;
        both = false;

        oneStudBud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oneStud) {
                    oneStud = true;
                    group = false;
                    both = false;
                    oneStudBud.setTextColor(Color.rgb(249, 249, 249));
                    oneStudBud.setBackgroundResource(R.drawable.button_pressed_bg);

                    studGroup.setTextColor(Color.rgb(34, 214, 255));
                    studGroup.setBackgroundResource(R.drawable.button_bg);
                    either.setTextColor(Color.rgb(34, 214, 255));
                    either.setBackgroundResource(R.drawable.button_bg);
                }
            }
        });

        studGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!group) {
                    oneStud = false;
                    group = true;
                    both = false;
                    studGroup.setTextColor(Color.rgb(249, 249, 249));
                    studGroup.setBackgroundResource(R.drawable.button_pressed_bg);

                    oneStudBud.setTextColor(Color.rgb(34, 214, 255));
                    oneStudBud.setBackgroundResource(R.drawable.button_bg);
                    either.setTextColor(Color.rgb(34, 214, 255));
                    either.setBackgroundResource(R.drawable.button_bg);
                }
            }
        });

        either.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!both) {
                    oneStud = false;
                    group = false;
                    both = true;
                    either.setTextColor(Color.rgb(249, 249, 249));
                    either.setBackgroundResource(R.drawable.button_pressed_bg);

                    studGroup.setTextColor(Color.rgb(34, 214, 255));
                    studGroup.setBackgroundResource(R.drawable.button_bg);
                    oneStudBud.setTextColor(Color.rgb(34, 214, 255));
                    oneStudBud.setBackgroundResource(R.drawable.button_bg);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = 0;
                if (oneStud && !group && !both) {
                    num = 1;
                    listener.onNumberOfPeopleSent(num);

                }
                else if (!oneStud && group && !both) {
                    num = 2;
                    listener.onNumberOfPeopleSent(num);

                }
                else if (!oneStud && !group && both) {
                    num = 0;
                    listener.onNumberOfPeopleSent(num);

                }
                else {
                    Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                    oneStudBud.startAnimation(shake);
                    studGroup.startAnimation(shake);
                    either.startAnimation(shake);

                }
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudFragmentListener) {
            listener = (FindStudBudFragmentListener)context;
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
