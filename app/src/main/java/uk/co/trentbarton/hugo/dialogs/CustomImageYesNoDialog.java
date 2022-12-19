package uk.co.trentbarton.hugo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;

public class CustomImageYesNoDialog extends Dialog {

    protected TextView titleTextView, contentText;
    private ImageView imageView;
    protected Button acceptButton, cancelButton;
    protected OnDialogClickListener mCancelButtonListener, mAcceptButtonListener;
    private String mTitle = "", mUserText = "";
    private int mImageResource = 0;

    public CustomImageYesNoDialog(@NonNull Context context) {
        super(context);
    }

    public CustomImageYesNoDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomImageYesNoDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_image_yes_no);

        titleTextView = findViewById(R.id.dialog_title_text);
        acceptButton = findViewById(R.id.dialog_accept_button);
        imageView = findViewById(R.id.dialog_image);
        cancelButton = findViewById(R.id.dialog_cancel_button);
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

        cancelButton.setOnClickListener(v -> {
            if(mCancelButtonListener != null){
                if(mCancelButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        contentText.setText(mUserText);
        titleTextView.setText(mTitle);

        if(mImageResource != 0){
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(mImageResource);
        }else{
            imageView.setVisibility(View.GONE);
        }

        try{
            this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setCancelable(true);
            this.setCanceledOnTouchOutside(true);
        }catch(Exception ignore){}
    }

    public CustomImageYesNoDialog setTitle(String title){
        mTitle = title;

        if(titleTextView != null){
            titleTextView.setText(title);
        }

        return this;
    }

    public CustomImageYesNoDialog setContentText(String text){

        mUserText = text;

        if(contentText != null){
            contentText.setText(text);
        }

        return this;
    }

    public CustomImageYesNoDialog setAcceptButtonListener(OnDialogClickListener listener){

        mAcceptButtonListener = listener;
        return this;
    }

    public CustomImageYesNoDialog setCancelButtonListener(OnDialogClickListener listener){

        mCancelButtonListener = listener;
        return this;
    }

    public CustomImageYesNoDialog setImage(int resourceId){
        mImageResource = resourceId;

        if(imageView != null){
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(mImageResource);
        }

        return this;
    }



}