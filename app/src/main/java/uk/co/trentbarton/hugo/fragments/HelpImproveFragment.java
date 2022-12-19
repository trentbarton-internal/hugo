package uk.co.trentbarton.hugo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenu;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.SendFeedbackParams;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;

public class HelpImproveFragment extends Fragment {

    private ScaleRatingBar lookAndFeel, easeOfUse, features;
    private ImageView everyJourneyIcon, mostJourneysIcon, someJourneysIcon, hardlyAnyJourneyIcon, noJourneysIcon;
    private LinearLayout everyJourneyButton, mostJourneysButton, someJourneysButton, hardlyAnyJourneysButton, noJourneysButton;
    private EditText bitsLiked, bitsDisliked, improvements;
    private SlideUpMenu menu;
    private Button sendFeedbackButton;
    private ProgressBar progress;
    private String selectedFrequencyText = "For every journey";
    private boolean valuesChanged = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_improve, container, false);
        init(view);
        setListeners();
        return view;

    }

    private void init(View view) {

        menu = view.findViewById(R.id.slidingMenu);

        lookAndFeel = view.findViewById(R.id.lookAndFeelRating);
        easeOfUse = view.findViewById(R.id.easeOfUseRating);
        features = view.findViewById(R.id.featuresRating);

        everyJourneyIcon = view.findViewById(R.id.everyJourneyIcon);
        mostJourneysIcon = view.findViewById(R.id.mostJourneyIcon);
        someJourneysIcon = view.findViewById(R.id.someJourneyIcon);
        hardlyAnyJourneyIcon = view.findViewById(R.id.hardlyAnyJourneyIcon);
        noJourneysIcon = view.findViewById(R.id.noJourneyIcon);

        everyJourneyButton =  view.findViewById(R.id.everyJourneyButton);
        mostJourneysButton = view.findViewById(R.id.mostJourneyButton);
        someJourneysButton = view.findViewById(R.id.someJourneyButton);
        hardlyAnyJourneysButton = view.findViewById(R.id.hardlyAnyJourneyButton);
        noJourneysButton = view.findViewById(R.id.noJourneyButton);

        sendFeedbackButton = view.findViewById(R.id.sendFeedbackButton);
        progress = view.findViewById(R.id.feedbackProgress);

        bitsLiked = view.findViewById(R.id.bitsLiked);
        bitsDisliked = view.findViewById(R.id.bitsDisliked);
        improvements = view.findViewById(R.id.improvements);
    }

    private void setListeners() {

        bitsLiked.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valuesChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                valuesChanged = true;
            }
        });

        bitsDisliked.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valuesChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                valuesChanged = true;
            }
        });

        improvements.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valuesChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                valuesChanged = true;
            }
        });

        lookAndFeel.setOnRatingChangeListener((baseRatingBar, v, b) -> {
            valuesChanged = true;
        });

        easeOfUse.setOnRatingChangeListener((baseRatingBar, v, b) -> {
            valuesChanged = true;
        });

        features.setOnRatingChangeListener((baseRatingBar, v, b) -> {
            valuesChanged = true;
        });

        everyJourneyButton.setOnClickListener((e) -> {
            valuesChanged = true;
            selectedFrequencyText = "For every journey";
            everyJourneyIcon.setImageResource(R.drawable.green_tick);
            mostJourneysIcon.setImageResource(R.drawable.unselected_icon);
            someJourneysIcon.setImageResource(R.drawable.unselected_icon);
            hardlyAnyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            noJourneysIcon.setImageResource(R.drawable.unselected_icon);
        });

        mostJourneysButton.setOnClickListener((e) -> {
            valuesChanged = true;
            selectedFrequencyText = "For most journeys";
            everyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            mostJourneysIcon.setImageResource(R.drawable.green_tick);
            someJourneysIcon.setImageResource(R.drawable.unselected_icon);
            hardlyAnyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            noJourneysIcon.setImageResource(R.drawable.unselected_icon);
        });

        someJourneysButton.setOnClickListener((e) -> {
            valuesChanged = true;
            selectedFrequencyText = "For some journeys";
            everyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            mostJourneysIcon.setImageResource(R.drawable.unselected_icon);
            someJourneysIcon.setImageResource(R.drawable.green_tick);
            hardlyAnyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            noJourneysIcon.setImageResource(R.drawable.unselected_icon);
        });

        hardlyAnyJourneysButton.setOnClickListener((e) -> {
            valuesChanged = true;
            selectedFrequencyText = "For hardly any journeys";
            everyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            mostJourneysIcon.setImageResource(R.drawable.unselected_icon);
            someJourneysIcon.setImageResource(R.drawable.unselected_icon);
            hardlyAnyJourneyIcon.setImageResource(R.drawable.green_tick);
            noJourneysIcon.setImageResource(R.drawable.unselected_icon);
        });

        noJourneysButton.setOnClickListener((e) -> {
            valuesChanged = true;
            selectedFrequencyText = "I never use it";
            everyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            mostJourneysIcon.setImageResource(R.drawable.unselected_icon);
            someJourneysIcon.setImageResource(R.drawable.unselected_icon);
            hardlyAnyJourneyIcon.setImageResource(R.drawable.unselected_icon);
            noJourneysIcon.setImageResource(R.drawable.green_tick);
        });

        sendFeedbackButton.setOnClickListener((e) -> {

            if(!valuesChanged){
                Toast.makeText(getActivity(), "Please tell us of your experience first by changing the values above", Toast.LENGTH_LONG).show();
                return;
            }

            if(!HugoPreferences.canSendFeedback(getActivity())){
                Toast.makeText(getActivity(), "Please wait at least 5 minutes before submitting more feedback", Toast.LENGTH_LONG).show();
                return;
            }

            SendFeedbackParams mParams = new SendFeedbackParams(getActivity());
            mParams.setEaseOfUseScore((int) easeOfUse.getRating())
                    .setFeaturesScore((int) features.getRating())
                    .setLookAndFeelScore((int) lookAndFeel.getRating())
                    .setBitsDisliked(bitsDisliked.getText().toString())
                    .setBitsLiked(bitsLiked.getText().toString())
                    .setImproveComments(improvements.getText().toString())
                    .setFrequencyOfUse(selectedFrequencyText);
            progress.setVisibility(View.VISIBLE);
            DataRequestTask task = new DataRequestTask(mParams);
            task.setOnTaskCompletedListener(bool -> {
                progress.setVisibility(View.GONE);
                if(bool){
                    Toast.makeText(getActivity(), "Thanks for your feedback", Toast.LENGTH_LONG).show();
                    HugoPreferences.setFeedbackSent(getActivity());
                    clearAllValues();
                }else{
                    Toast.makeText(getActivity(), "There was a problem submitting feedback, please try again", Toast.LENGTH_LONG).show();
                }
            });
            task.execute(getActivity());
        });
    }

    private void clearAllValues() {

        lookAndFeel.setRating(3.0f);
        easeOfUse.setRating(3.0f);
        features.setRating(3.0f);
        selectedFrequencyText = "For every journey";
        everyJourneyIcon.setImageResource(R.drawable.green_tick);
        mostJourneysIcon.setImageResource(R.drawable.unselected_icon);
        someJourneysIcon.setImageResource(R.drawable.unselected_icon);
        hardlyAnyJourneyIcon.setImageResource(R.drawable.unselected_icon);
        noJourneysIcon.setImageResource(R.drawable.unselected_icon);
        bitsLiked.setText("");
        bitsDisliked.setText("");
        improvements.setText("");
        valuesChanged = false;

    }
}
