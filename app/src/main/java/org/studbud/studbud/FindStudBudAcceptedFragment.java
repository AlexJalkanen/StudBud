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
import android.widget.Button;
import android.widget.ImageButton;

public class FindStudBudAcceptedFragment extends Fragment {

    private FindStudBudAcceptedFragmentListener listener;
    private ImageButton back;
    private Button next;


    public interface FindStudBudAcceptedFragmentListener {
        void backToMainFeed();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_studbud_accepted, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        back = view.findViewById(R.id.find_studBud_subject_back_button);
        next = view.findViewById(R.id.numberBudsRequestedContinueButton);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.backToMainFeed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
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
        if (context instanceof FindStudBudAcceptedFragmentListener) {
            listener = (FindStudBudAcceptedFragmentListener)context;
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
