package uk.co.trentbarton.hugo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;
import uk.co.trentbarton.hugo.R;

public class CustomTextDialog extends Dialog {

    protected TextView titleTextView, contentText;
    protected Button acceptButton;
    protected OnDialogClickListener mCancelButtonListener, mAcceptButtonListener;
    private String mTitle = "", mUserText = "";

    public CustomTextDialog(@NonNull Context context) {
        super(context);
    }

    public CustomTextDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomTextDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_text);

        titleTextView = findViewById(R.id.dialog_title_text);
        acceptButton = findViewById(R.id.dialog_accept_button);

        contentText = findViewById(R.id.dialog_user_input_text);

        acceptButton.setOnClickListener(v -> {
            if(mAcceptButtonListener != null){
                if(mAcceptButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        contentText.setText(mUserText);
        titleTextView.setText(mTitle);

        try{
            this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setCancelable(true);
            this.setCanceledOnTouchOutside(true);
        }catch(Exception ignore){}



    }

    public CustomTextDialog setTitle(String title){
        mTitle = title;

        if(titleTextView != null){
            titleTextView.setText(title);
        }

        return this;
    }

    public CustomTextDialog setContentText(String text){

        mUserText = text;

        if(contentText != null){
            contentText.setText(text);
        }

        return this;
    }

    public CustomTextDialog setAcceptButtonListener(OnDialogClickListener listener){

        mAcceptButtonListener = listener;
        return this;
    }



}
