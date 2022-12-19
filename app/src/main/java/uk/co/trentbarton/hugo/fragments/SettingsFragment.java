package uk.co.trentbarton.hugo.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.JourneyResults;
import uk.co.trentbarton.hugo.activities.SettingsActivity;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenu;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenuItem;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DeleteAlarmParams;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.dialogs.CustomTextDialog;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.interfaces.RefreshInterface;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.tasks.LocationForegroundService;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;

public class SettingsFragment extends Fragment implements RefreshInterface {

    private SlideUpMenu mMenu;
    private LinearLayout messagesButton, rateButton, helpImproveButton, privacyPolicyButton, contactUsButton, nextBusAlarm, alightingAlarm, journeyHolder;
    private TextView nextBusAlarmEmptyMessage, alightingAlarmEmptyMessage, nextBusAlarmName, alightingAlarmName, journeysEmptyMessage, journeyFromText, journeyToText;
    private ImageView nextBusAlarmSettings, alightingAlarmSettings, journeySettings, journeyInfo, alarmsInfo;

    public SettingsFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_main, container, false);
        init(view);
        setListeners();
        return view;
    }

    private void init(View view){
        mMenu = view.findViewById(R.id.settings_slide_up_menu);
        messagesButton = view.findViewById(R.id.fragment_settings_messages_button);
        rateButton = view.findViewById(R.id.fragment_settings_rate_button);
        helpImproveButton = view.findViewById(R.id.fragment_settings_help_improve_button);
        privacyPolicyButton = view.findViewById(R.id.fragment_settings_privacy_policy_button);
        contactUsButton = view.findViewById(R.id.fragment_settings_contact_us_button);
        nextBusAlarmEmptyMessage = view.findViewById(R.id.settings_main_empty_next_bus_due_alarm_message);
        alightingAlarmEmptyMessage = view.findViewById(R.id.settings_main_empty_when_to_get_off_alarm_message);
        nextBusAlarmName = view.findViewById(R.id.settings_main_next_bus_alarm_text);
        nextBusAlarmSettings = view.findViewById(R.id.settings_main_next_bus_alarm_settings);
        nextBusAlarm = view.findViewById(R.id.settings_main_next_bus_alarm);
        alightingAlarm = view.findViewById(R.id.settings_main_when_to_get_off_alarm);
        alightingAlarmName = view.findViewById(R.id.settings_main_when_to_get_off_alarm_text);
        alightingAlarmSettings = view.findViewById(R.id.settings_main_when_to_get_off_alarm_settings);
        journeyHolder = view.findViewById(R.id.settings_main_current_journey_holder);
        journeysEmptyMessage = view.findViewById(R.id.settings_main_empty_journeys_message);
        journeyFromText = view.findViewById(R.id.settings_main_journey_from_text);
        journeyToText = view.findViewById(R.id.settings_main_journey_to_text);
        journeySettings = view.findViewById(R.id.settings_main_current_journey_action);
        journeyInfo = view.findViewById(R.id.fragment_settings_journey_user_info);
        alarmsInfo = view.findViewById(R.id.fragment_settings_alarms_user_info);

    }

    private void setListeners() {

        messagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.OPTION_KEY, SettingsActivity.SettingsScreen.MESSAGE_SETTINGS);
            startActivity(intent);
        });

        journeyInfo.setOnClickListener(v -> {
            CustomTextDialog dialog = new CustomTextDialog(getContext());
            dialog.setTitle("See your current journey");
            dialog.setContentText("Once you have selected a start and end position and gone though to select an option it will be visible here while any part of the journey is still active, you can shoose to resume that journey from here as well.");
            dialog.show();
        });

        alarmsInfo.setOnClickListener(v -> {
            CustomTextDialog dialog = new CustomTextDialog(getContext());
            dialog.setTitle("See your current alarms");
            dialog.setContentText("There are 2 types of alarm that appear here, in the journey planner you have the option to be notified when you approach the stop you need and in the live screen you can let hugo monitor the progress of a selected bus and have the app tell you when it is close by. Both of these are available here to cancel should you need to.");
            dialog.show();
        });


        rateButton.setOnClickListener(v -> {

            try {
                final String appPackageName = getActivity().getPackageName();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (Exception ignore) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=uk.co.trentbarton.hugo")));
            }
        });

        helpImproveButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.OPTION_KEY, SettingsActivity.SettingsScreen.IMPROVE_HUGO);
            startActivity(intent);
        });

        privacyPolicyButton.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.trentbarton.co.uk/hugoTest/privacypolicy.html")));
        });

        contactUsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.OPTION_KEY, SettingsActivity.SettingsScreen.CONTACT_US);
            startActivity(intent);
        });

        nextBusAlarmSettings.setOnClickListener(v -> {
            showNextBusMenu();
        });

        alightingAlarmSettings.setOnClickListener(v -> {
            if(LocationForegroundService.IS_SERVICE_RUNNING){
                showAlightingBusMenu();
            }else{
                alightingAlarmEmptyMessage.setVisibility(View.VISIBLE);
                alightingAlarm.setVisibility(View.GONE);
            }
        });

        journeySettings.setOnClickListener(v -> {
            showJourneyMenu();
        });

    }

    private void showAlightingBusMenu() {

        ArrayList<SlideUpMenuItem> items = new ArrayList<>();
        SlideUpMenuItem item = new SlideUpMenuItem(getContext(),"Remove Alarm", Color.RED);
        item.setOnClickListener(v -> {
            Intent cancelIntent = new Intent(getActivity(), LocationForegroundService.class);
            cancelIntent.setAction(LocationForegroundService.CANCEL_FOREGROUND_ACTION);
            getContext().startService(cancelIntent);
            alightingAlarmEmptyMessage.setVisibility(View.VISIBLE);
            alightingAlarm.setVisibility(View.GONE);
            mMenu.slideDown();
        });
        item.setAsLastItem();
        items.add(item);
        mMenu.assignItems(items);
        mMenu.slideUp();
    }

    private void showNextBusMenu() {

        ArrayList<SlideUpMenuItem> items = new ArrayList<>();
        SlideUpMenuItem item = new SlideUpMenuItem(getContext(),"Remove Alarm", Color.RED);
        item.setOnClickListener(v -> {
            DeleteAlarmParams mParams = new DeleteAlarmParams(getContext());
            mParams.addAlarm(HugoPreferences.getActiveAlarm(getContext()));
            DataRequestTask task = new DataRequestTask(mParams);
            task.setOnTaskCompletedListener(successful -> {
                if(successful){
                    HugoPreferences.setActiveAlarm(getContext(), null);
                    Toast.makeText(getContext(), "You will no longer get a notice about your selected vehicle", Toast.LENGTH_SHORT).show();
                    nextBusAlarmEmptyMessage.setVisibility(View.VISIBLE);
                    nextBusAlarm.setVisibility(View.GONE);
                }else{

                    CustomYesNoDialog dialog = new CustomYesNoDialog(getContext())
                            .setTitle("Server delete failed")
                            .setContentText("The server failed to delete your alarm, do you want to force delete? note, you may still get notified about a previous alarm");
                    dialog.setAcceptButtonListener((OnDialogClickListener) () -> {
                        HugoPreferences.setActiveAlarm(getContext(), null);
                        nextBusAlarmEmptyMessage.setVisibility(View.VISIBLE);
                        nextBusAlarm.setVisibility(View.GONE);
                        return true;
                    });
                    dialog.show();
                }
            });
            task.execute(getContext());
            mMenu.slideDown();
        });
        item.setAsLastItem();
        items.add(item);
        mMenu.assignItems(items);
        mMenu.slideUp();
    }

    private void showJourneyMenu(){

        try{
            ArrayList<SlideUpMenuItem> items = new ArrayList<>();
            SlideUpMenuItem item = new SlideUpMenuItem(getContext(),"Resume journey", ContextCompat.getColor(getContext(), R.color.blue_text_colour));
            item.setOnClickListener(v -> {
                mMenu.slideDown();
                Intent intent = new Intent(getActivity(), JourneyResults.class);
                intent.putExtra("index", HugoPreferences.getLastJourneyItemChosen(getContext()));
                startActivity(intent);
            });
            SlideUpMenuItem deleteItem = new SlideUpMenuItem(getContext(),"Delete journey", Color.RED);
            deleteItem.setOnClickListener(v -> {
                mMenu.slideDown();
                HugoPreferences.setLastJourneyData(getContext(), "");
                HugoPreferences.setLastJourneyItemChosen(getContext(), -1);
                journeyHolder.setVisibility(View.GONE);
                journeysEmptyMessage.setVisibility(View.VISIBLE);
            });
            deleteItem.setAsLastItem();
            items.add(item);
            items.add(deleteItem);
            mMenu.assignItems(items);
            mMenu.slideUp();
        }catch(Exception ignore){}

    }

    @Override
    public void RefreshUI() {

        if(getContext() == null) return;

        if(LocationForegroundService.IS_SERVICE_RUNNING){
            //Get off alarm is running
            alightingAlarmEmptyMessage.setVisibility(View.GONE);
            alightingAlarm.setVisibility(View.VISIBLE);
            alightingAlarmName.setText(HugoPreferences.getAlightingAlarmStopName(getContext()));
        }

        if(HugoPreferences.getActiveAlarm(getContext()) != null){
            nextBusAlarmEmptyMessage.setVisibility(View.GONE);
            nextBusAlarm.setVisibility(View.VISIBLE);
            nextBusAlarmName.setText(HugoPreferences.getActiveAlarm(getContext()).getStopName());
        }else{
            nextBusAlarmEmptyMessage.setVisibility(View.VISIBLE);
            nextBusAlarm.setVisibility(View.GONE);
            nextBusAlarmName.setText("");
        }

        new Thread(new Runnable() {
            public void run() {
                try{
                    // a potentially  time consuming task to retrieve and parse the Journey data stored in preferences so do in a worker thread.
                    ArrayList<Journey> lastJourneyData = HugoPreferences.getLastJourneyData(getContext());
                    if(lastJourneyData != null){
                        int position = HugoPreferences.getLastJourneyItemChosen(getContext());
                        Journey journey = lastJourneyData.get(position);
                        getActivity().runOnUiThread(() -> {
                            journeysEmptyMessage.setVisibility(View.GONE);
                            journeyHolder.setVisibility(View.VISIBLE);
                            journeyFromText.setText(journey.getFriendlyFromName());
                            journeyToText.setText(journey.getFriendlyToName());
                        });
                    }else{
                        journeysEmptyMessage.setVisibility(View.VISIBLE);
                        journeyHolder.setVisibility(View.GONE);
                    }
                }catch(Exception ignore){}
            }
        }).start();




    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshUI();
    }
}
