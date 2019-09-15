package org.studbud.studbud;

import android.app.TimePickerDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class FindStudBudTimeFragment extends Fragment {

    private FindStudBudTimeFragmentListener listener;
    private TextView num, sub;
    private int numberOfPeople;
    private String subject;
    private ImageButton back;
    private Button next;
    private EditText EditTime;
    private TimePickerDialog timePickerDialog;
    private boolean now, later;
    private Button nowButton, laterButton;
    private Calendar calendar;
    private int currentHour, currentMinute;
    private String amPm;



    public interface FindStudBudTimeFragmentListener {
        void onTimeSent(int number, String subject, String time);
        void backToMainFeed();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_studbud_time, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        num = view.findViewById(R.id.numberOfPeople);
        sub = view.findViewById(R.id.subjectTextView);
        back = view.findViewById(R.id.find_studBud_subject_back_button);
        EditTime = view.findViewById(R.id.timeText);
        nowButton = view.findViewById(R.id.timeNow);
        laterButton = view.findViewById(R.id.timeLater);
        next = view.findViewById(R.id.numberBudsTimeContinueButton);

        numberOfPeople = 999;
        subject = "No Subject";
        if (getArguments() != null) {
            numberOfPeople = getArguments().getInt("numberOfPeople");
            subject = getArguments().getString("subject");
        }
        updateNumberOfPeopleText(numberOfPeople);
        updateSubjectText(subject);

        now = false;
        later = false;

        nowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!now) {
                    now = true;
                    later = false;
                    nowButton.setTextColor(Color.rgb(249, 249, 249));
                    nowButton.setBackgroundResource(R.drawable.button_pressed_bg);

                    laterButton.setTextColor(Color.rgb(34, 214, 255));
                    laterButton.setBackgroundResource(R.drawable.button_bg);
                    EditTime.setVisibility(View.GONE);
                }
            }
        });

        laterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!later) {
                    later = true;
                    now = false;
                    laterButton.setTextColor(Color.rgb(249, 249, 249));
                    laterButton.setBackgroundResource(R.drawable.button_pressed_bg);

                    nowButton.setTextColor(Color.rgb(34, 214, 255));
                    nowButton.setBackgroundResource(R.drawable.button_bg);
                    EditTime.setVisibility(View.VISIBLE);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });

        EditTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        if (hourOfDay < currentHour) {
                            hourOfDay = currentHour;
                            minutes = currentMinute;
                        }
                        else if (hourOfDay == currentHour) {
                            if (minutes < currentMinute) {
                                hourOfDay = currentHour;
                                minutes = currentMinute;
                            }
                        }
                            if (hourOfDay >= 12) {
                                amPm = "PM";
                                if (hourOfDay > 12) {
                                    hourOfDay -= 12;
                                }
                            }
                            else {
                                amPm = "AM";
                                if (hourOfDay < 1) {
                                    hourOfDay += 12;
                                }
                            }

                        EditTime.setText(String.format("%02d:%02d ",hourOfDay,minutes) + amPm);
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.show();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time;
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);


                if (now) {
                    int hourOfDay = currentHour;
                    int minutes = currentMinute;
                    if (hourOfDay >= 12) {
                        amPm = "PM";
                        if (hourOfDay > 12) {
                            hourOfDay -= 12;
                        }
                    }
                    else {
                        amPm = "AM";
                        if (hourOfDay < 1) {
                            hourOfDay += 12;
                        }
                    }
                    time = String.format("%02d:%02d ",hourOfDay,minutes) + amPm;
                    listener.onTimeSent(numberOfPeople, subject, time);
                }
                else if(later) {
                    time = EditTime.getText().toString();
                    if (time.isEmpty()) {
                        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                        nowButton.startAnimation(shake);
                        laterButton.startAnimation(shake);
                        return;
                    }
                    listener.onTimeSent(numberOfPeople,subject,time);
                }
                else {
                    Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                    nowButton.startAnimation(shake);
                    laterButton.startAnimation(shake);
                }
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

    public void updateSubjectText(String subject) {
        sub.setText("You are studying " + subject + ".");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FindStudBudTimeFragmentListener) {
            listener = (FindStudBudTimeFragmentListener)context;
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

}
