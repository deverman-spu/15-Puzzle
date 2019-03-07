package com.example.deverman.puzzle;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnMessageSendListener, ScoresFragment.OnReturnListener {

    final static String TAG = "15Puzzle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.fragment_container) != null)
        {
            if(savedInstanceState != null)
            {
                return;
            }

            MainActivityFragment puzzleFragment = new MainActivityFragment();

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, puzzleFragment, null).commit();
        }
    }

    @Override
    public void onMessageSend(int moves) {

        ScoresFragment scoresFragment = new ScoresFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("MOVES", moves);

        scoresFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, scoresFragment, null);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onReturn() {
        MainActivityFragment mainFragment = new MainActivityFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragment, null);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
