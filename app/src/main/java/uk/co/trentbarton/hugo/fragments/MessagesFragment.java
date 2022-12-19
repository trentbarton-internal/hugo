package uk.co.trentbarton.hugo.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.trentbarton.hugo.activities.ChooseServiceActivity;
import uk.co.trentbarton.hugo.activities.MainNavigationActivity;
import uk.co.trentbarton.hugo.activities.MessagesActivity;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.GetUserDetailsParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.MessageReadParams;
import uk.co.trentbarton.hugo.dataholders.Message;
import uk.co.trentbarton.hugo.dataholders.UserDetails;
import uk.co.trentbarton.hugo.datapersistence.AlertsStatus;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.interfaces.OnMessageClickedListener;
import uk.co.trentbarton.hugo.interfaces.RefreshInterface;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;

public class MessagesFragment extends Fragment implements RefreshInterface, OnMessageClickedListener {

    private TextView editButton, servicesSubscribedTextStart, servicesSubscribedTextNumber,servicesSubscribedTextEnd,messageEmptyMessage, alertsEmptyMessage, alertsShowAllButton, messagesShowAllButton;
    private LinearLayout mAlertsHolder, mMessagesHolder;
    private final String TAG = MessagesFragment.class.getSimpleName();
    UserDetails mDetails;
    private int unopenedAlerts = 0;
    private boolean SHOW_ALL_ALERTS = false;
    private boolean SHOW_ALL_MESSAGES = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages_main, container, false);
        editButton = view.findViewById(R.id.editButton);
        servicesSubscribedTextStart = view.findViewById(R.id.fragment_messages_main_service_subscription_textview_start);
        servicesSubscribedTextNumber = view.findViewById(R.id.fragment_messages_main_service_subscription_textview_number);
        servicesSubscribedTextEnd = view.findViewById(R.id.fragment_messages_main_service_subscription_textview_end);
        mAlertsHolder = view.findViewById(R.id.alerts_message_holder_for_alerts);
        mMessagesHolder = view.findViewById(R.id.alerts_message_holder_for_messages);
        alertsEmptyMessage = view.findViewById(R.id.alerts_empty_alerts_list_message);
        messageEmptyMessage = view.findViewById(R.id.alerts_empty_messages_list_message);
        alertsShowAllButton = view.findViewById(R.id.alerts_show_all_alerts_Button);
        messagesShowAllButton = view.findViewById(R.id.alerts_show_all_messages_Button);
        assignListeners();
        startTask();
        return view;
    }

    private void startTask() {

        GetUserDetailsParams params = new GetUserDetailsParams(getContext());
        DataRequestTask task = new DataRequestTask(params);
        task.setOnTaskCompletedListener(bool -> {
            Log.d(TAG, "Info received back from server");
            if(bool && task.getResponse() != null){
                Log.d(TAG, "Task successful");
                mDetails = (UserDetails) task.getResponse();
                updateAllLists();
            }else{
                if(getContext() != null){
                    Toast.makeText(getContext(), "Couldn't retrieve messages from the server", Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "Task Failed");
            }
        });
        task.execute(getContext());

    }

    private void updateAllLists() {
        if(mDetails == null){
            return;
        }
        unopenedAlerts = 0;
        mMessagesHolder.removeAllViews();
        mAlertsHolder.removeAllViews();
        messageEmptyMessage.setVisibility(View.VISIBLE);
        alertsEmptyMessage.setVisibility(View.VISIBLE);
        addAllMessages(mDetails);
        addAllAlerts(mDetails);
        updateUnOpenedDot();

    }

    private void assignListeners() {

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChooseServiceActivity.class);
            startActivity(intent);
        });

        messagesShowAllButton.setOnClickListener(v -> {
            SHOW_ALL_MESSAGES = !SHOW_ALL_MESSAGES;
            updateAllLists();
        });

        alertsShowAllButton.setOnClickListener(v -> {
            SHOW_ALL_ALERTS = !SHOW_ALL_ALERTS;
            updateAllLists();
        });
    }

    private void addAllMessages(UserDetails details) {

        int totalMessagesShowing = 0;
        int maxMessages = 0;

        if(!details.getUserMessages().isEmpty()){

            for(Message msg : details.getUserMessages()){
                //If user has subscribed to this message type then show it

                if(msg.isBeenDeleted()){
                    continue;
                }

                if(HugoPreferences.userSubscribed(getContext(), msg.getTypeCompareName(getContext()))){

                    if(!SHOW_ALL_MESSAGES && totalMessagesShowing >= 3){
                        maxMessages++;
                    }else{
                        messageEmptyMessage.setVisibility(View.GONE);

                        View view = msg.getView(getContext());
                        if(view != null) {
                            mMessagesHolder.addView(view);
                            msg.setOnMessageClickListener(this);
                            totalMessagesShowing++;
                            maxMessages++;
                        }
                    }
                }
            }

            if(maxMessages <= 3){
                messagesShowAllButton.setText("");
            }else{
                if(SHOW_ALL_MESSAGES){
                    messagesShowAllButton.setText("show less");
                }else{
                    messagesShowAllButton.setText("show all");
                }
            }

        }else{
            messagesShowAllButton.setText("");
        }
    }

    private void addAllAlerts(UserDetails details){

        int totalAlertsShowing = 0;
        int maxMessages = 0;

        if(!details.getTrafficAlerts().isEmpty()){

            for(Message disruption : details.getTrafficAlerts()){

                if(AlertsStatus.getInstance(getContext()).isAlertDeleted(disruption.getMessageId())){
                    disruption.setmBeenRead(true);
                    disruption.setBeenDeleted(true);
                    continue; //We don't include any deleted alerts
                }

                if(!SHOW_ALL_ALERTS && totalAlertsShowing >= 3){
                    maxMessages++;
                }else{
                    //If we get to here we have at least one alert that has not been deleted
                    alertsEmptyMessage.setVisibility(View.GONE);
                    AlertsStatus.getInstance(getContext()).addNewAlert(disruption.getMessageId());
                    disruption.setOnMessageClickListener(this);
                    View view = disruption.getView(getContext());
                    if(view != null){
                        mAlertsHolder.addView(view);
                        totalAlertsShowing++;
                        maxMessages++;
                    }

                    if(AlertsStatus.getInstance(getContext()).isAlertOpened(disruption.getMessageId())){
                        disruption.setViewToOpened();
                    }
                }
            }

            if(maxMessages <= 3){
                alertsShowAllButton.setText("");
            }else {
                if (SHOW_ALL_ALERTS) {
                    alertsShowAllButton.setText("show less");
                } else {
                    alertsShowAllButton.setText("show all");
                }
            }
        }else{
            alertsShowAllButton.setText("");
        }
    }

    public void updateUnOpenedDot(){
        unopenedAlerts = 0;

        try{
            for(Message message : mDetails.getTrafficAlerts()){
                if(!message.hasBeenRead() && !message.isBeenDeleted()){
                    unopenedAlerts++;
                }
            }

            for(Message message : mDetails.getUserMessages()){
                if(!message.hasBeenRead() && HugoPreferences.userSubscribed(getContext(), message.getTypeCompareName(getContext()))){
                    unopenedAlerts++;
                }
            }

            ((MainNavigationActivity)getActivity()).updateUnreadMessagePeek(unopenedAlerts);
        }catch(Exception e){}
    }

    @Override
    public void RefreshUI() {
        updateAllLists();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getContext() != null && servicesSubscribedTextNumber != null){

            int serviceNumber = HugoPreferences.getNumberOfServicesSubscribedTo(getContext());

            if(serviceNumber == 0){
                servicesSubscribedTextNumber.setVisibility(View.GONE);
                servicesSubscribedTextStart.setText("You are following travel alerts for 0 ");
            }else{
                servicesSubscribedTextNumber.setVisibility(View.VISIBLE);
                servicesSubscribedTextStart.setText("You are following travel alerts for");

                if(serviceNumber < 10){
                    servicesSubscribedTextNumber.setText(" " + serviceNumber + " ");
                }else{
                    servicesSubscribedTextNumber.setText("" + serviceNumber);
                }

                if(serviceNumber == 1){
                    servicesSubscribedTextEnd.setText("route");
                }else{
                    servicesSubscribedTextEnd.setText("routes");
                }

            }
        }
        RefreshUI();
    }

    @Override
    public void onMessageClick(Message message) {

        if(message.getmType() == Message.MessageType.TRAVEL_ALERT){
            AlertsStatus.getInstance(getContext()).registerAlertOpened(message.getMessageId());
            try{
                AlertsStatus.getInstance(getContext()).saveDetails();
            }catch(Exception ignore){}
        }else{
            MessageReadParams params = new MessageReadParams(getContext());
            params.addMessage(message);
            DataRequestTask task = new DataRequestTask(params);
            task.execute(getContext());

        }

        updateUnOpenedDot();
        Intent intent = new Intent(getActivity(), MessagesActivity.class);
        try{
            intent.putExtra("message", message.toJsonString());
        }catch(Exception e){
            Toast.makeText(getContext(), "Oops, something has gone wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);

    }
}
