package com.example.deverman.puzzle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.StringTokenizer;


public class ScoresFragment extends Fragment {

    private final String TAG = "15Puzzle";

    private TextView mSuccessOrFailText;
    private EditText mNameSubmit;
    private Button mSubmit;
    private Button mBack;
    private TextView mNameR1;
    private TextView mNameR2;
    private TextView mNameR3;
    private TextView mNameR4;
    private TextView mNameR5;
    private TextView mMovesR1;
    private TextView mMovesR2;
    private TextView mMovesR3;
    private TextView mMovesR4;
    private TextView mMovesR5;

    // Declare array to hold our names and scores with default values
    private String[] list_names = {"Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder"};
    private int[] list_moves = { 995, 996, 997, 998, 999 };

    // Hold the value of how many moves the player made
    private int mMoves;
    private String mName;

    // Variable for shared preferences
    SharedPreferences sharedPref;


    public ScoresFragment() {
        // Required empty public constructor
    }

    // To return to main puzzle fragment
    OnReturnListener returnListener;

    // Nothing to return out of this fragment
    public interface OnReturnListener
    {
        public void onReturn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        //fragment stays alive when the activity onDestroy is called
        setRetainInstance(true);

        //start of sharedPreferences method for restoring saved data
        sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);

        // Attempt to restore leaderboard
        String savedNames = sharedPref.getString("names_", "Placeholder,Placeholder,Placeholder,Placeholder,Placeholder");
        StringTokenizer st = new StringTokenizer(savedNames, ",");
        for (int i = 0; i < 5; i++)
        {
            list_names[i] = st.nextToken();
        }

        // Attempt to restore leaderboard
        String savedMoves = sharedPref.getString("moves_", "995,996,997,998,999");
        StringTokenizer st2 = new StringTokenizer(savedMoves, ",");
        for (int i = 0; i < 5; i++)
        {
            list_moves[i] = Integer.parseInt(st2.nextToken());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        // Give values to our variables
        mNameR1 = (TextView) view.findViewById(R.id.txtNameR1);
        mNameR2 = (TextView) view.findViewById(R.id.txtNameR2);
        mNameR3 = (TextView) view.findViewById(R.id.txtNameR3);
        mNameR4 = (TextView) view.findViewById(R.id.txtNameR4);
        mNameR5 = (TextView) view.findViewById(R.id.txtNameR5);
        mMovesR1 = (TextView) view.findViewById(R.id.txtMovesR1);
        mMovesR2 = (TextView) view.findViewById(R.id.txtMovesR2);
        mMovesR3 = (TextView) view.findViewById(R.id.txtMovesR3);
        mMovesR4 = (TextView) view.findViewById(R.id.txtMovesR4);
        mMovesR5 = (TextView) view.findViewById(R.id.txtMovesR5);
        mSuccessOrFailText = (TextView) view.findViewById(R.id.txtSuccessOrFail);
        mNameSubmit = (EditText) view.findViewById(R.id.etxtName);
        mSubmit = (Button) view.findViewById(R.id.btnSubmit);
        mBack = (Button) view.findViewById(R.id.btnBack);

        // Initially have these hidden
        mNameSubmit.setVisibility(View.INVISIBLE);
        mSubmit.setVisibility(View.INVISIBLE);
        mBack.setVisibility(View.INVISIBLE);

        // Get the number of moves the player made
        Bundle bundle = getArguments();
        if(bundle != null)
            mMoves = bundle.getInt("MOVES");

        // Update the table with current values
        update();

        // Check the score to see if it's eligible for a high score
        if (checkScore(mMoves) == false)
        {
            // Display failure message and hide controls if it's not high enough
            mSuccessOrFailText.setText(getString(R.string.fail_msg));
            mNameSubmit.setVisibility(View.INVISIBLE);
            mSubmit.setVisibility(View.INVISIBLE);
            mBack.setVisibility(View.VISIBLE);
        } else {
            // Otherwise congratulate and let user enter their name
            mSuccessOrFailText.setText(getString(R.string.success_msg));
            mNameSubmit.setVisibility(View.VISIBLE);
            mSubmit.setVisibility(View.VISIBLE);
            mBack.setVisibility(View.INVISIBLE);
        }

        //define the behavior of the submit button
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get name, sort the table, and update the view
                mName = mNameSubmit.getText().toString();
                sort();
                update();

                // Hide these elements so they can't submit scores over and over
                mNameSubmit.setVisibility(View.INVISIBLE);
                mSubmit.setVisibility(View.INVISIBLE);

                // Save table data
                SharedPreferences.Editor editor = sharedPref.edit();

                // Save Names
                int count = 0;
                String[] tempArray = new String[5];

                for (int i = 0; i < 5; i++)
                {
                    tempArray[count] = list_names[i];
                    count++;
                }
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < tempArray.length; i++) {
                    str.append(tempArray[i]).append(",");
                }
                editor.putString("names_", str.toString());

                // Save Moves
                count = 0;
                int tempArray2[] = new int[5];

                for (int i = 0; i < 5; i++)
                {
                    tempArray2[count] = list_moves[i];
                    count++;
                }
                StringBuilder str2 = new StringBuilder();
                for (int i = 0; i < tempArray2.length; i++) {
                    str2.append(tempArray2[i]).append(",");
                }
                editor.putString("moves_", str2.toString());

                editor.commit();        // dont forget to commit the changes

                // Give user the back button to return to previous fragment
                mBack.setVisibility(View.VISIBLE);

                //define the behavior of the next button (go to next pokemon)
                mBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        returnListener.onReturn();
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            returnListener = (ScoresFragment.OnReturnListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement OnReturn...");
        }
    }

    // Checks score to see if it's a high score
    public boolean checkScore(int moves) {
        // Checks if any of the scores are 0 (invalid scores so user is a winner)
        if (list_moves[0] == 0 || list_moves[1] == 0 || list_moves[2] == 0 || list_moves[3] == 0 || list_moves[4] == 0)
            return true;

        if (moves < list_moves[4])
            return true;            // if we're lower than the last place score we definitely belong in the table

        return false;  // if we haven't returned true by now, it's false
    }

    // method to sort the table
    // find where we want to insert, shift the elements down, then insert
    public void sort(){
        // used to shift list accordingly
        int temp_count = 0;

        // start at the bottom, figure out where we need to be
        for (int i = 3; i >= 0; i--) {
            if (mMoves > list_moves[i] || i == 0) {
                // we found where we need to be
                if (i == 0) {
                    temp_count = i;
                } else {
                    temp_count = i + 1;
                    break;
                }
            }
        }

        // start at the bottom, shift elements down until it's time to insert our new score
        for (int i = 4; i >= temp_count; i--)
        {
            if (i == temp_count) {
                list_names[i] = mName;
                list_moves[i] = mMoves;
            } else {
                list_names[i] = list_names[i - 1];
                list_moves[i] = list_moves[i - 1];
            }
        }
    }

    // Update all of our labels to new values
    public void update(){
        mNameR1.setText(list_names[0]);
        mNameR2.setText(list_names[1]);
        mNameR3.setText(list_names[2]);
        mNameR4.setText(list_names[3]);
        mNameR5.setText(list_names[4]);
        mMovesR1.setText(String.valueOf(list_moves[0]));
        mMovesR2.setText(String.valueOf(list_moves[1]));
        mMovesR3.setText(String.valueOf(list_moves[2]));
        mMovesR4.setText(String.valueOf(list_moves[3]));
        mMovesR5.setText(String.valueOf(list_moves[4]));
    }
}
