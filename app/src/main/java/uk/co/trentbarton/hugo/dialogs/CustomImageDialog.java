package uk.co.trentbarton.hugo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;

public class CustomImageDialog extends Dialog {

    protected TextView titleTextView, contentText;
    private ImageView imageView;
    protected Button acceptButton;
    protected OnDialogClickListener mCancelButtonListener, mAcceptButtonListener;
    private String mTitle = "", mUserText = "";
    private int mImageResource = 0;

    public CustomImageDialog(@NonNull Context context) {
        super(context);
    }

    public CustomImageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomImageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_image);

        titleTextView = findViewById(R.id.dialog_title_text);
        acceptButton = findViewById(R.id.dialog_accept_button);
        imageView = findViewById(R.id.dialog_image);

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

    public CustomImageDialog setTitle(String title){
        mTitle = title;

        if(titleTextView != null){
            titleTextView.setText(title);
        }

        return this;
    }

    public CustomImageDialog setContentText(String text){

        mUserText = text;

        if(contentText != null){
            contentText.setText(text);
        }

        return this;
    }

    public CustomImageDialog setAcceptButtonListener(OnDialogClickListener listener){

        mAcceptButtonListener = listener;
        return this;
    }

    public CustomImageDialog setImage(int resourceId){
        mImageResource = resourceId;

        if(imageView != null){
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(mImageResource);
        }

        return this;
    }



}