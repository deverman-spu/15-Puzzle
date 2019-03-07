package com.example.deverman.puzzle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import java.util.StringTokenizer;

public class MainActivityFragment extends Fragment {

    private final String TAG = "puzzle";

    // We know the numbers the puzzle will use, declare it 'solved' here
    int[][] puzzle = {{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};

    // Declare all our buttons
    private Button mButtonA1;
    private Button mButtonA2;
    private Button mButtonA3;
    private Button mButtonA4;
    private Button mButtonB1;
    private Button mButtonB2;
    private Button mButtonB3;
    private Button mButtonB4;
    private Button mButtonC1;
    private Button mButtonC2;
    private Button mButtonC3;
    private Button mButtonC4;
    private Button mButtonD1;
    private Button mButtonD2;
    private Button mButtonD3;
    private Button mButtonD4;
    private Button mButtonNewGame;
    private Button mButtonSaveGame;
    private Button mButtonLoadGame;

    // Keep track of all our moves
    private TextView mTextViewMoveCount;
    private int move_count = 0;

    // Timer to keep track of how long it took to solve the puzzle
    Chronometer mTimer;

    // Variable for shared preferences
    SharedPreferences sharedPref;

    public MainActivityFragment() {
        // Required empty public constructor
    }

    OnMessageSendListener messageSendListener;

    public interface OnMessageSendListener
    {
        public void onMessageSend(int id);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            messageSendListener = (OnMessageSendListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement OnMessageSend...");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fragment stays alive when the activity onDestroy is called
        setRetainInstance(true);

        sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            View view = inflater.inflate(R.layout.fragment_main, container, false);

            mButtonA1 = (Button) view.findViewById(R.id.btnA1);
            mButtonA2 = (Button) view.findViewById(R.id.btnA2);
            mButtonA3 = (Button) view.findViewById(R.id.btnA3);
            mButtonA4 = (Button) view.findViewById(R.id.btnA4);
            mButtonB1 = (Button) view.findViewById(R.id.btnB1);
            mButtonB2 = (Button) view.findViewById(R.id.btnB2);
            mButtonB3 = (Button) view.findViewById(R.id.btnB3);
            mButtonB4 = (Button) view.findViewById(R.id.btnB4);
            mButtonC1 = (Button) view.findViewById(R.id.btnC1);
            mButtonC2 = (Button) view.findViewById(R.id.btnC2);
            mButtonC3 = (Button) view.findViewById(R.id.btnC3);
            mButtonC4 = (Button) view.findViewById(R.id.btnC4);
            mButtonD1 = (Button) view.findViewById(R.id.btnD1);
            mButtonD2 = (Button) view.findViewById(R.id.btnD2);
            mButtonD3 = (Button) view.findViewById(R.id.btnD3);
            mButtonD4 = (Button) view.findViewById(R.id.btnD4);
            mButtonNewGame = (Button) view.findViewById(R.id.btnNewGame);
            mButtonSaveGame = (Button) view.findViewById(R.id.btnSaveGame);
            mButtonLoadGame = (Button) view.findViewById(R.id.btnLoadGame);
            mTextViewMoveCount = (TextView) view.findViewById(R.id.txtMoveCount);
            mTimer = (Chronometer) view.findViewById(R.id.timer);
            mTimer.setFormat("Time: %s");

            createPuzzle(puzzle);
            printPuzzle(puzzle);

            // declare behavior for new game button
            mButtonNewGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createPuzzle(puzzle);
                    printPuzzle(puzzle);
                }
            });

            // declare behavior for save game button
            mButtonSaveGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("move-count_", move_count);

                    long time = SystemClock.elapsedRealtime() - mTimer.getBase();
                    mTimer.stop();
                    editor.putLong("timer_", time);

                    int count = 0;
                    int[] tempArray = new int[16];

                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                                tempArray[count] = puzzle[i][j];        // it might be possible to cut out this step of making a 1D array
                                count++;                                // but for now this works, problems kept creeping up the other way
                        }
                    }

                    StringBuilder str = new StringBuilder();
                    for (int i = 0; i < tempArray.length; i++) {
                        str.append(tempArray[i]).append(",");
                    }
                    editor.putString("puzzle_", str.toString());

                    disable_all();          // don't let user play after timer is stopped and board is saved

                    editor.commit();        // dont forget to commit the changes
                }
            });

            // declare behavior for load game button
            mButtonLoadGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Restore move count (easy)
                    int temp_move_count = sharedPref.getInt("move-count_", 999);
                    if (temp_move_count != 0) {
                        mTextViewMoveCount.setText(String.valueOf(move_count));
                        move_count = temp_move_count;
                    }

                    // Restore timer (harder)
                    long time;
                    time = sharedPref.getLong("timer_", 0);
                    if(time != 0) {
                        mTimer.setBase(SystemClock.elapsedRealtime() - time);
                        mTimer.start();
                    }

                    // Restore puzzle board (hardest)
                    // Pull integer values from string and put them into 1D array
                    String savedString = sharedPref.getString("puzzle_", "-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1");
                    StringTokenizer st = new StringTokenizer(savedString, ",");
                    int[] savedList = new int[16];
                    for (int i = 0; i < 16; i++) {
                        savedList[i] = Integer.parseInt(st.nextToken());
                    }

                    // Check if we found a saved puzzle or not
                    if (savedList[0] == -1)
                    {
                        Toast toast = Toast.makeText(getContext(),R.string.save_missing,Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0,36);
                        toast.show();
                    }
                    else {
                        // Expand 1D array to 2D puzzle array
                        int count = 0;
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 4; j++) {
                                puzzle[i][j] = savedList[count];        // it might be possible to cut out this step of making a 1D array
                                count++;
                            }
                        }
                        printPuzzle(puzzle);
                    }
                }
            });

            // time to declare behavior for all our game buttons
            // a lot of repeated code, but buttons are named as follows:
            // [Letter Row] and [Number Column]
            // A1 is first square, A2 is next to it, and B1 is under it
            //
            // When button is clicked we check find location and check if move is valid
            // If it is valid, we swap and print the new puzzle
            mButtonA1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonA1.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonA2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonA2.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonA3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonA3.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonA4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonA4.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonB1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonB1.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonB2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonB2.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonB3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonB3.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonB4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonB4.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonC1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonC1.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonC2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonC2.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonC3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonC3.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonC4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonC4.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonD1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonD1.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonD2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonD2.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonD3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonD3.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            mButtonD4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findLocation(puzzle, Integer.parseInt(mButtonD4.getText().toString()));
                    printPuzzle(puzzle);
                }
            });

            return view;
        } catch (Exception e) {
        Log.e(TAG, "onCreateView", e);
        throw e;
        }
    }

    // Checks to make sure that the player's suggested tile is movable
    public boolean checkValid(int moveRow, int moveColumn, int blankRow, int blankColumn)
    {
        // Check tiles on both sides of the player's suggested tile
        // Check tiles on top and bottom of player's suggested tile
        if ((moveRow == blankRow && (moveColumn - 1 == blankColumn || moveColumn + 1 == blankColumn)) || (moveColumn == blankColumn && (moveRow - 1 == blankRow || moveRow + 1 == blankRow)))
            return true;
        else
            return false;
    }

    // Finds the column and row of the players tile
    public void findLocation(int[][] puzzle, int moveTile) {
        int moveRow = -1;
        int moveColumn = -1;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (puzzle[i][j] == moveTile) {     // searching for the column and row of the user's tile
                    moveRow = i;                    // this way they just enter the number of the tile
                    moveColumn = j;                 // rather than the row and column
                    break;
                }
            }
            if (moveRow != -1 && moveColumn != -1)  // simple check to make sure that the tile has been found
                break;
        }
        findBlank(puzzle, moveRow, moveColumn);
    }

    // finds the column and row of the blank space
    public void findBlank(int[][] puzzle, int moveRow, int moveColumn) {
        int blankRow = -1;
        int blankColumn = -1;

        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j++) {
                if (puzzle[i][j] == 0) {        // searching for row and column of the blank tile
                    blankRow = i;
                    blankColumn = j;
                    break;
                }
            }
            if (blankRow != -1 && blankColumn != -1)    // simple check to make sure it's been found
                break;
        }

        if (checkValid(moveRow, moveColumn, blankRow, blankColumn)) {     // check to make sure the player's tile and the blank space are compatible
            swap(puzzle, moveRow, moveColumn, blankRow, blankColumn);           // if they are swap their location
        } else {
            // Display invalid move message in toast
            Toast toast = Toast.makeText(getContext(),R.string.invalid_move,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0,36);
            toast.show();
        }
    }

    // checks the puzzle to see if it has been solved
    public boolean checkPuzzle(int [][] puzzle) {
        int[][] winner = {{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};       // this is how the puzzle should look when solved

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (puzzle[i][j] != winner[i][j]) {         // check player's puzzle against the correct one
                    return false;
                }
            }
        }
        mTimer.stop();      // stop timer

        // Display success message in a toast
        Toast toast = Toast.makeText(getContext(),R.string.solved_puzzle,Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0,36);
        toast.show();
        messageSendListener.onMessageSend(move_count);
        return true;
    }

    // the puzzle has already been declared, but this will scramble it
    public void createPuzzle(int[][] puzzle) {
        do{
            // now let's populate this table - we know it's 4x4 so no reason to use .length
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int x = (int)(Math.random()*4);         // generating random rows/columns to swap around
                    int y = (int)(Math.random()*4);

                    if (puzzle[i][j] != 0 && puzzle[x][y] != 0) {       // we want to keep 0 in the bottom right corner
                        swap(puzzle, i, j, x, y);
                    }
                }
            }
        }while (getInversions(puzzle) %2 != 0);         // check to make sure the total number of inversions is even

        // Reset move count to 0
        move_count = 0;

        mTextViewMoveCount.setText(String.valueOf(move_count));

        // Start timer again
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.start();
    }

    // One way to check for the validity of a puzzle is to count the total number of inversions the puzzle has
    // If the final number of inversions is odd then the puzzle is unsolvable. If it is even then the puzzle is solvable
    // This information was obtained from http://mathworld.wolfram.com/15Puzzle.html
    // The original work was done by: (Johnson 1879) & (Story 1879)
    public int getInversions(int[][] puzzle) {
        int total = 0, count = 0;
        int[] tempArray = new int[16];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (puzzle[i][j] != 0) {
                    tempArray[count] = puzzle[i][j];        // it might be possible to cut out this step of making a 1D array
                    count++;                                // but for now this works, problems kept creeping up the other way
                }
            }
        }

        for (int i = 14; i >= 0; i--) {     // start at index 14 since index 15 is 0
            count = 0;
            for (int j = (i-1); j < i && j >= 0; j--) {   // start checking the values beneath i
                if (tempArray[i] < tempArray[j]) {          // this is where we check for the number of inversions
                    count++;
                }
            }
            total += count;       // then keep a running total of all of them
        }
        return total;
    }

    // generic value swap method
    public void swap(int[][] puzzle, int i, int j, int x, int y) {
        int temp = puzzle[i][j];
        puzzle[i][j] = puzzle[x][y];
        puzzle[x][y] = temp;

        // Increase move count and update textview accordingly
        move_count++;
        mTextViewMoveCount.setText(String.valueOf(move_count));

        // Check if puzzle is solved after this move
        checkPuzzle(puzzle);
    }

    // generic array print method
    public void printPuzzle(int[][] puzzle) {

        //**TODO Find better way to update text on buttons
        // For now, long if/else block
        // Buttons are named as follows:
        // [Letter Row] and [Number Column]
        // A1 is first square, A2 is next to it, and B1 is under it

        // Check if the square is the empty piece, if so make it transparent and disable it
        // Otherwise print the number and make sure it is enabled
        if (puzzle[0][0] != 0)
        {
            mButtonA1.setText(String.valueOf(puzzle[0][0]));
            mButtonA1.setEnabled(true);
            mButtonA1.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonA1.setText(String.valueOf(puzzle[0][0]));
            mButtonA1.setEnabled(false);
            mButtonA1.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[0][1] != 0)
        {
            mButtonA2.setText(String.valueOf(puzzle[0][1]));
            mButtonA2.setEnabled(true);
            mButtonA2.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonA2.setText(String.valueOf(puzzle[0][1]));
            mButtonA2.setEnabled(false);
            mButtonA2.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[0][2] != 0)
        {
            mButtonA3.setText(String.valueOf(puzzle[0][2]));
            mButtonA3.setEnabled(true);
            mButtonA3.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonA3.setText(String.valueOf(puzzle[0][2]));
            mButtonA3.setEnabled(false);
            mButtonA3.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[0][3] != 0)
        {
            mButtonA4.setText(String.valueOf(puzzle[0][3]));
            mButtonA4.setEnabled(true);
            mButtonA4.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonA4.setText(String.valueOf(puzzle[0][3]));
            mButtonA4.setEnabled(false);
            mButtonA4.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[1][0] != 0)
        {
            mButtonB1.setText(String.valueOf(puzzle[1][0]));
            mButtonB1.setEnabled(true);
            mButtonB1.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonB1.setText(String.valueOf(puzzle[1][0]));
            mButtonB1.setEnabled(false);
            mButtonB1.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[1][1] != 0)
        {
            mButtonB2.setText(String.valueOf(puzzle[1][1]));
            mButtonB2.setEnabled(true);
            mButtonB2.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonB2.setText(String.valueOf(puzzle[1][1]));
            mButtonB2.setEnabled(false);
            mButtonB2.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[1][2] != 0)
        {
            mButtonB3.setText(String.valueOf(puzzle[1][2]));
            mButtonB3.setEnabled(true);
            mButtonB3.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonB3.setText(String.valueOf(puzzle[1][2]));
            mButtonB3.setEnabled(false);
            mButtonB3.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[1][3] != 0)
        {
            mButtonB4.setText(String.valueOf(puzzle[1][3]));
            mButtonB4.setEnabled(true);
            mButtonB4.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonB4.setText(String.valueOf(puzzle[1][3]));
            mButtonB4.setEnabled(false);
            mButtonB4.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[2][0] != 0)
        {
            mButtonC1.setText(String.valueOf(puzzle[2][0]));
            mButtonC1.setEnabled(true);
            mButtonC1.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonC1.setText(String.valueOf(puzzle[2][0]));
            mButtonC1.setEnabled(false);
            mButtonC1.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[2][1] != 0)
        {
            mButtonC2.setText(String.valueOf(puzzle[2][1]));
            mButtonC2.setEnabled(true);
            mButtonC2.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonC2.setText(String.valueOf(puzzle[2][1]));
            mButtonC2.setEnabled(false);
            mButtonC2.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[2][2] != 0)
        {
            mButtonC3.setText(String.valueOf(puzzle[2][2]));
            mButtonC3.setEnabled(true);
            mButtonC3.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonC3.setText(String.valueOf(puzzle[2][2]));
            mButtonC3.setEnabled(false);
            mButtonC3.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[2][3] != 0)
        {
            mButtonC4.setText(String.valueOf(puzzle[2][3]));
            mButtonC4.setEnabled(true);
            mButtonC4.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonC4.setText(String.valueOf(puzzle[2][3]));
            mButtonC4.setEnabled(false);
            mButtonC4.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[3][0] != 0)
        {
            mButtonD1.setText(String.valueOf(puzzle[3][0]));
            mButtonD1.setEnabled(true);
            mButtonD1.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonD1.setText(String.valueOf(puzzle[3][0]));
            mButtonD1.setEnabled(false);
            mButtonD1.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[3][1] != 0)
        {
            mButtonD2.setText(String.valueOf(puzzle[3][1]));
            mButtonD2.setEnabled(true);
            mButtonD2.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonD2.setText(String.valueOf(puzzle[3][1]));
            mButtonD2.setEnabled(false);
            mButtonD2.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[3][2] != 0)
        {
            mButtonD3.setText(String.valueOf(puzzle[3][2]));
            mButtonD3.setEnabled(true);
            mButtonD3.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonD3.setText(String.valueOf(puzzle[3][2]));
            mButtonD3.setEnabled(false);
            mButtonD3.setTextColor(Color.TRANSPARENT);
        }
        if (puzzle[3][3] != 0)
        {
            mButtonD4.setText(String.valueOf(puzzle[3][3]));
            mButtonD4.setEnabled(true);
            mButtonD4.setTextColor(Color.BLACK);
        }
        else
        {
            mButtonD4.setText(String.valueOf(puzzle[3][3]));
            mButtonD4.setEnabled(false);
            mButtonD4.setTextColor(Color.TRANSPARENT);
        }
    }

    // generic method to disable all tiles, used on game saves or ends
    private void disable_all()
    {
        mButtonA1.setEnabled(false);
        mButtonA2.setEnabled(false);
        mButtonA3.setEnabled(false);
        mButtonA4.setEnabled(false);
        mButtonB1.setEnabled(false);
        mButtonB2.setEnabled(false);
        mButtonB3.setEnabled(false);
        mButtonB4.setEnabled(false);
        mButtonC1.setEnabled(false);
        mButtonC2.setEnabled(false);
        mButtonC3.setEnabled(false);
        mButtonC4.setEnabled(false);
        mButtonD1.setEnabled(false);
        mButtonD2.setEnabled(false);
        mButtonD3.setEnabled(false);
        mButtonD4.setEnabled(false);
    }
}
