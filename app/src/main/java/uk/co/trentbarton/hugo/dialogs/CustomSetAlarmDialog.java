package uk.co.trentbarton.hugo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;

public class CustomSetAlarmDialog extends Dialog {

    private NumberPicker mNumberPicker;
    protected Button acceptButton, cancelButton;
    protected OnDialogClickListener mCancelButtonListener, mAcceptButtonListener;
    private int mMinNumber = 5;
    private int mMaxNumber = 60;

    public CustomSetAlarmDialog(@NonNull Context context) {
        super(context);
    }

    public CustomSetAlarmDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomSetAlarmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_set_alarm);

        acceptButton = findViewById(R.id.dialog_accept_button);
        cancelButton = findViewById(R.id.dialog_cancel_button);
        mNumberPicker = findViewById(R.id.dialog_numberpicker);


        acceptButton.setOnClickListener(v -> {
            if(mAcceptButtonListener != null){
                if(mAcceptButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> {
            if(mCancelButtonListener != null){
                if(mCancelButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        mNumberPicker.setMinValue(mMinNumber);
        mNumberPicker.setMaxValue(mMaxNumber);

        try{
            this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setCancelable(true);
            this.setCanceledOnTouchOutside(true);
        }catch(Exception ignore){}



    }

    public CustomSetAlarmDialog setMinNumber(int minNumber){
        mMinNumber = minNumber;

        if(mNumberPicker != null){
            mNumberPicker.setMinValue(mMinNumber);
        }

        return this;
    }

    public CustomSetAlarmDialog setMaxNumber(int maxNumber){
        mMaxNumber = maxNumber;

        if(mNumberPicker != null){
            mNumberPicker.setMaxValue(mMaxNumber);
        }

        return this;
    }

    public CustomSetAlarmDialog setAcceptButtonListener(OnDialogClickListener listener){
        mAcceptButtonListener = listener;
        return this;
    }

    public int getSelectedNumber(){
        return mNumberPicker.getValue();
    }



}


