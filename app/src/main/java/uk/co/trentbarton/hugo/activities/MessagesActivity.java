package uk.co.trentbarton.hugo.activities;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.MessageDeleteParams;
import uk.co.trentbarton.hugo.dataholders.Message;
import uk.co.trentbarton.hugo.datapersistence.AlertsStatus;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;


public class MessagesActivity extends AppCompatActivity {

    Message mMessage;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        try{
            String messageJson = getIntent().getStringExtra("message");
            JSONObject obj = new JSONObject(messageJson);
            mMessage = new Message(obj);

        }catch(Exception e){
            Log.e(TAG,"Message could not be identified from Intent",e);
            finish();
        }

        init();
        setupActionBar();

    }

    private void setupActionBar() {

        ActionBar actionbar = getSupportActionBar();
        if(actionbar == null){
            return;
        }

        if(mMessage.getmType() == Message.MessageType.TRAVEL_ALERT) {
            actionbar.setTitle("Traffic alert");
        }else{
            actionbar.setTitle("A message from hugo");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messages_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_delete_message:
                deleteMessage();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteMessage() {

        CustomYesNoDialog dialog = new CustomYesNoDialog(this)
                .setTitle("Delete this Message?")
                .setContentText("Are you sure you wish to delete this message, it can't be undone?")
                .setAcceptButtonListener(() -> {

                    if(mMessage.getmType() == Message.MessageType.TRAVEL_ALERT){
                        AlertsStatus.getInstance(this).registerAlertDeleted(mMessage.getMessageId());
                        AlertsStatus.getInstance(this).saveDetails();
                        Toast.makeText(this, "Alert Deleted...", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        MessageDeleteParams params = new MessageDeleteParams(this);
                        params.addMessage(mMessage);
                        DataRequestTask task = new DataRequestTask(params);
                        task.execute(this);
                        Toast.makeText(this, "Message Deleted...", Toast.LENGTH_SHORT).show();
                        GlobalData.getInstance().addNewDeletedMessage(mMessage);
                        finish();
                    }

                    return true;
                });
        dialog.show();

    }

    private void init(){

        TextView dateText, titleText, contextText, urlTextLink;
        ImageView image;

        dateText = findViewById(R.id.activity_messages_date);
        titleText = findViewById(R.id.activity_messages_title);
        contextText = findViewById(R.id.activity_messages_content);
        urlTextLink = findViewById(R.id.activity_messages_url_link);
        image = findViewById(R.id.activity_messages_image);

        dateText.setText(mMessage.getFormattedDate());
        titleText.setText(mMessage.getmTitle());
        contextText.setText(mMessage.getmContentText());

        if(mMessage.getUrlLink() == null || mMessage.getUrlLink().isEmpty()){
            urlTextLink.setVisibility(View.GONE);
        }else{
            urlTextLink.setOnClickListener(v -> followLink());
        }

        if(mMessage.getImageUrl() == null || mMessage.getImageUrl().isEmpty()){
            image.setVisibility(View.GONE);
        }else{
            if(mMessage.getImageUrl().contains(".gif")) {
                Glide.with(this).asDrawable().load(mMessage.getImageUrl()).into(image);
            }else {
                Glide.with(this).load(mMessage.getImageUrl()).into(image);
            }
        }
    }

    private void followLink() {
        try{
            String url;
            if(mMessage.getUrlLink().contains("http")){
                url = mMessage.getUrlLink();
            }else{
                url =  "http://" + mMessage.getUrlLink();
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }catch(Exception e){
            Log.e(TAG, "URL parse error", e);
            Toast.makeText(this, "Oops something hasn't worked right.... the techie guys have been made aware.", Toast.LENGTH_LONG).show();
        }
    }
}
