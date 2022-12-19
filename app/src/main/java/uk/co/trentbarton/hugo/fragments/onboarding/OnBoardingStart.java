package uk.co.trentbarton.hugo.fragments.onboarding;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.OnBoardingActivity;
import uk.co.trentbarton.hugo.dialogs.AutoCompletePlaceDialog;

public class OnBoardingStart extends Fragment {

    ImageView nextButton;
    ProgressBar progress;
    TextView nameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_on_boarding_start, container, false);
        nextButton = view.findViewById(R.id.onBoardingNextArrow);
        progress = view.findViewById(R.id.onBoardingStartProgress);
        nameText = view.findViewById(R.id.onBoardingNameText);

        nextButton.setOnClickListener(v -> {
            loadNextFragment();
        });

        nameText.setOnClickListener(v -> launchPlaceFinder());

        return view;

    }

    public void launchPlaceFinder(){

        Context c = getContext();

        if(c != null){
            AutoCompletePlaceDialog dialog = new AutoCompletePlaceDialog(c, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setOnSelectedListener(place -> {
                nameText.setText(place.getName());
                ((OnBoardingActivity) getActivity()).assignPlace(place);
            });
            dialog.setOwnerActivity(getActivity());
            dialog.show();
        }
    }

    private void loadNextFragment() {

        String placeName = nameText.getText().toString();

        if(placeName.isEmpty()){
            Toast.makeText(getActivity(), "Please provide a place name or postcode so we can help to show you relevant stops nearby", Toast.LENGTH_LONG).show();
            return;
        }

        ((OnBoardingActivity) getActivity()).changeFragment(OnBoardingActivity.OnBoardingFragmentType.MAP);

    }

}
