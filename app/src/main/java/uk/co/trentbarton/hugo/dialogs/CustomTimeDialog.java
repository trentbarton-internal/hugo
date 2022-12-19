package uk.co.trentbarton.hugo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import org.joda.time.LocalDateTime;

import java.util.Date;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;

public class CustomTimeDialog extends Dialog {

    protected TextView titleTextView, nowButton, thirtyMinsButton, sixtyMinsButton, arriveButton, leaveButton;
    private SingleDateAndTimePicker timePicker;
    protected Button acceptButton, cancelButton;
    protected OnDialogClickListener mCancelButtonListener, mAcceptButtonListener;
    private String mTitle = "Please select departure or arrival time";
    private boolean isLeavingTime = true;
    private String timeFrom = "now";




    public CustomTimeDialog(@NonNull Context context) {
        super(context);
    }

    public CustomTimeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomTimeDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_time);

        titleTextView = findViewById(R.id.dialog_title_text);
        acceptButton = findViewById(R.id.dialog_accept_button);
        cancelButton = findViewById(R.id.dialog_cancel_button);
        timePicker = findViewById(R.id.dialog_time_picker);
        nowButton = findViewById(R.id.dialog_now_button);
        thirtyMinsButton = findViewById(R.id.dialog_thirty_mins_button);
        sixtyMinsButton = findViewById(R.id.dialog_sixty_mins_button);
        arriveButton = findViewById(R.id.dialog_time_arrival);
        leaveButton = findViewById(R.id.dialog_time_departure);
        timePicker.setStepMinutes(1);
        
        cancelButton.setOnClickListener(v -> {
            if(mCancelButtonListener != null){
                if(mCancelButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        acceptButton.setOnClickListener(v -> {
            if(mAcceptButtonListener != null){
                if(mAcceptButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        arriveButton.setOnClickListener(v -> {
            isLeavingTime = false;
            arriveButton.setTextColor(Color.WHITE);
            arriveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.dark_green_background_10dp_corners));
            leaveButton.setTextColor(ContextCompat.getColor(getContext(),R.color.background_dark_grey));
            leaveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.light_grey_background_10dp_corners));
        });

        leaveButton.setOnClickListener(v -> {
            isLeavingTime = true;
            leaveButton.setTextColor(Color.WHITE);
            leaveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.dark_green_background_10dp_corners));
            arriveButton.setTextColor(ContextCompat.getColor(getContext(),R.color.background_dark_grey));
            arriveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.light_grey_background_10dp_corners));
        });

        nowButton.setOnClickListener(v -> {
            timeFrom = "now";
            timePicker.setDefaultDate(new Date());
            selectTextBox(nowButton);
            unselectTextBox(thirtyMinsButton);
            unselectTextBox(sixtyMinsButton);
            timePicker.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.background_dark_grey));
        });

        thirtyMinsButton.setOnClickListener(v -> {
            timeFrom = "thirty";
            timePicker.setDefaultDate(LocalDateTime.now().plusMinutes(30).toDate());
            selectTextBox(thirtyMinsButton);
            unselectTextBox(nowButton);
            unselectTextBox(sixtyMinsButton);
            timePicker.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.background_dark_grey));
        });

        sixtyMinsButton.setOnClickListener(v -> {
            timeFrom = "sixty";
            timePicker.setDefaultDate(LocalDateTime.now().plusHours(1).toDate());
            selectTextBox(sixtyMinsButton);
            unselectTextBox(thirtyMinsButton);
            unselectTextBox(nowButton);
            timePicker.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.background_dark_grey));
        });

        timePicker.addOnDateChangedListener((displayed, date) -> {
            timeFrom = "timePicker";
            timePicker.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.primaryDarkGreen));

            unselectTextBox(sixtyMinsButton);
            unselectTextBox(thirtyMinsButton);
            unselectTextBox(nowButton);
        });

        titleTextView.setText(mTitle);

        try{
            this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setCancelable(true);
            this.setCanceledOnTouchOutside(true);
        }catch(Exception ignore){}
    }

    public void setIsLeavingTime(boolean isLeaving){
        if(isLeaving){
            isLeavingTime = true;
            leaveButton.setTextColor(Color.WHITE);
            leaveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.dark_green_background_10dp_corners));
            arriveButton.setTextColor(ContextCompat.getColor(getContext(),R.color.background_dark_grey));
            arriveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.light_grey_background_10dp_corners));
        }else{
            isLeavingTime = false;
            arriveButton.setTextColor(Color.WHITE);
            arriveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.dark_green_background_10dp_corners));
            leaveButton.setTextColor(ContextCompat.getColor(getContext(),R.color.background_dark_grey));
            leaveButton.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.light_grey_background_10dp_corners));
        }
    }

    public CustomTimeDialog setTitle(String title){
        mTitle = title;

        if(titleTextView != null){
            titleTextView.setText(title);
        }

        return this;
    }

    public CustomTimeDialog setAcceptButtonListener(OnDialogClickListener listener){
        mAcceptButtonListener = listener;
        return this;
    }

    public CustomTimeDialog setCancelButtonListener(OnDialogClickListener listener){
        mCancelButtonListener = listener;
        return this;
    }

    private void unselectTextBox(TextView tv){
        tv.setBackgroundColor(Color.TRANSPARENT);
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.background_dark_grey));
    }

    private void selectTextBox(TextView tv){
        tv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primaryDarkGreen));
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.pure_white));
    }

    public boolean isLeavingTime(){
        return this.isLeavingTime;
    }

    public boolean isTimeNow(){
        return this.timeFrom.equals("now");
    }

    public LocalDateTime getDateSet(){

        try{
            if(timeFrom.equalsIgnoreCase("now")){
                return LocalDateTime.now();
            }else if(timeFrom.equalsIgnoreCase("thirty")){
                return LocalDateTime.now().plusMinutes(30);
            }else if(timeFrom.equalsIgnoreCase("sixty")){
                return LocalDateTime.now().plusHours(1);
            }else{
                return LocalDateTime.fromDateFields(timePicker.getDate());
            }
        }catch(Exception e){
            return LocalDateTime.now();
        }
    }

    public void setIsNow(boolean isTimeNow) {
        if(isTimeNow){
            timeFrom = "now";
            try{
                timePicker.setDefaultDate(new Date());
                selectTextBox(nowButton);
                unselectTextBox(thirtyMinsButton);
                unselectTextBox(sixtyMinsButton);
                timePicker.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.background_dark_grey));
            }catch(Exception ignore){
                //Ignore for now but monitor if we can
            }
        }
    }

    public void setDate(Date date){
        if(date != null){
            timeFrom = "timePicker";
            timePicker.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.primaryDarkGreen));
            unselectTextBox(sixtyMinsButton);
            unselectTextBox(thirtyMinsButton);
            unselectTextBox(nowButton);
            timePicker.setDefaultDate(date);
        }
    }
}
