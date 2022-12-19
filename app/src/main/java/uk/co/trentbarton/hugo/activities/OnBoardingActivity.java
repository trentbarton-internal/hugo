package uk.co.trentbarton.hugo.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.fragments.onboarding.OnBoardingEnd;
import uk.co.trentbarton.hugo.fragments.onboarding.OnBoardingMap;
import uk.co.trentbarton.hugo.fragments.onboarding.OnBoardingStart;

public class OnBoardingActivity extends AppCompatActivity {

    Button closeButton;
    TomTomPlace mSelectedPlace;
    OnBoardingStart onBoardingStart;

    public enum OnBoardingFragmentType{
        START, MAP, END
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            Intent intent = new Intent(OnBoardingActivity.this, MainNavigationActivity.class);
            startActivity(intent);
            finish();
        });
        onBoardingStart = new OnBoardingStart();
        changeFragment(OnBoardingFragmentType.START);
    }

    public void changeFragment(OnBoardingFragmentType mType){

        Fragment fragment = null;

        switch(mType){
            case START:
                fragment = onBoardingStart;
                break;
            case MAP:
                fragment = new OnBoardingMap();
                ((OnBoardingMap)fragment).setPlace(mSelectedPlace);
                break;
            case END:
                fragment = new OnBoardingEnd();
                break;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.mainContentScreen, fragment);
        transaction.commit();
    }

    public void assignPlace(TomTomPlace place) {
        mSelectedPlace = place;
    }




}
