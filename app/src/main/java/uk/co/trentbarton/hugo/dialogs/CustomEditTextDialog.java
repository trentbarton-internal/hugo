package uk.co.trentbarton.hugo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;
import uk.co.trentbarton.hugo.R;

public class CustomEditTextDialog extends Dialog {

    protected TextView titleTextView;
    protected Button acceptButton, cancelButton;
    protected EditText userInputText;
    protected OnDialogClickListener mCancelButtonListener, mAcceptButtonListener;
    private String mTitle = "", mUserText = "", mHintText = "Please give the stop a name";

    public CustomEditTextDialog(@NonNull Context context) {
        super(context);
    }

    public CustomEditTextDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomEditTextDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_text);

        titleTextView = findViewById(R.id.dialog_title_text);
        acceptButton = findViewById(R.id.dialog_accept_button);
        cancelButton = findViewById(R.id.dialog_cancel_button);
        userInputText = findViewById(R.id.dialog_user_input_text);

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
            }
        });

        userInputText.setText(mUserText);
        titleTextView.setText(mTitle);
        userInputText.setHint(mHintText);

        try{
            this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setCancelable(true);
            this.setCanceledOnTouchOutside(true);
        }catch(Exception ignore){}



    }

    public CustomEditTextDialog setTitle(String title){
        mTitle = title;

        if(titleTextView != null){
            titleTextView.setText(title);
        }

        return this;
    }

    public CustomEditTextDialog setUserInputText(String text){

        mUserText = text;

        if(userInputText != null){
            userInputText.setText(text);
        }

        return this;
    }

    public CustomEditTextDialog setCancelButtonListener(OnDialogClickListener listener){
        mCancelButtonListener = listener;
        return this;
    }

    public CustomEditTextDialog setAcceptButtonListener(OnDialogClickListener listener){
        mAcceptButtonListener = listener;
        return this;
    }

    public String getEnteredText(){
        return this.userInputText.getText().toString();
    }


    public CustomEditTextDialog setHintText(String hintText) {
        mHintText = hintText;

        if(userInputText != null){
            userInputText.setHint(hintText);
        }

        return this;
    }
}
