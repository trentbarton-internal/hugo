package uk.co.trentbarton.hugo.dataholders;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.Locale;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.interfaces.OnMessageClickedListener;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.tools.Constants;


public class Message implements Serializable {

    private MessageType mType;
    private String mTitle;
    private String mContentText;
    private LocalDate mDateCreated;
    private LocalDate mDateActiveFrom;
    private LocalDate mDateActiveTo;
    private boolean mHidden;
    private String urlLink;
    private int severity;
    private String imageUrl;
    private boolean mBeenRead;
    private boolean mBeenDeleted;
    private int messageId;
    private View mView;
    private OnMessageClickedListener mListener;

    public enum MessageType{
        TRAVEL_ALERT("travel_alert"),
        BUS(Constants.TOPIC_BUS),
        OFFERS(Constants.TOPIC_OFFERS),
        TEST(Constants.TOPIC_TEST),
        TIMETABLE(Constants.TOPIC_TIMETABLE),
        TRAFFIC(Constants.TOPIC_TRAFFIC),
        APP(Constants.TOPIC_APP),
        UNKNOWN("unknown"),
        DIRECT("direct");

        public final String compareName;

        MessageType(String s){
            this.compareName = s;
        }

    }

    public Message(MessageType type){
        mType = type;
        mBeenRead = false;
        mHidden = false;
    }

    public Message(JSONObject obj){

        mBeenDeleted = false;
        mBeenRead = false;
        mHidden = false;

        try{
            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
            setTypeFromString(obj.getString("type"));
            mTitle = obj.getString("title");
            mContentText = obj.getString("contentText");
            mDateCreated = LocalDate.parse(obj.getString("dateCreated"), dtf);
            mDateActiveFrom = LocalDate.parse(obj.getString("activeFrom"), dtf);
            mDateActiveTo = LocalDate.parse(obj.getString("activeTo"), dtf);
            mHidden = obj.getBoolean("hidden");
            if(!obj.isNull("urlLink")) urlLink = obj.getString("urlLink");
            severity = obj.getInt("severity");
            if(!obj.isNull("imageUrl")) imageUrl = obj.getString("imageUrl");
            mBeenRead = obj.getBoolean("beenRead");
            mBeenDeleted = obj.getBoolean("beenDeleted");
            messageId = obj.getInt("messageId");

        }catch(Exception e){
            mType = MessageType.UNKNOWN;
        }
    }

    public Message(String type){
        setTypeFromString(type);
        mBeenRead = false;
        mBeenDeleted = false;
        mHidden = false;
    }

    private void setTypeFromString(String type) {

        switch(type.toLowerCase(Locale.ENGLISH)) {
            case "travel_alert":
                mType = MessageType.TRAVEL_ALERT;
                break;
            case "bus":
                mType = MessageType.BUS;
                break;
            case "offers":
                mType = MessageType.OFFERS;
                break;
            case "test":
                mType = MessageType.TEST;
                break;
            case "timetable":
                mType = MessageType.TIMETABLE;
                break;
            case "traffic":
                mType = MessageType.TRAFFIC;
                break;
            case "app":
                mType = MessageType.APP;
                break;
            case "direct":
                mType = MessageType.DIRECT;
                break;
            default:
                mType = MessageType.UNKNOWN;
        }
    }

    public String getTypeCompareName(Context context){

        if(context == null){
            return "";
        }

        switch(mType.compareName) {
            case Constants.TOPIC_APP:
                return context.getResources().getString(R.string.notification_app_title);
            case Constants.TOPIC_BUS:
                return context.getResources().getString(R.string.notification_bus_title);
            case Constants.TOPIC_OFFERS:
                return context.getResources().getString(R.string.notification_offers_title);
            case Constants.TOPIC_TEST:
                return "test";
            case Constants.TOPIC_TIMETABLE:
                return context.getResources().getString(R.string.notification_timetable_title);
            case Constants.TOPIC_TRAFFIC:
                return context.getResources().getString(R.string.notification_traffic_title);
            default:
                return "direct";
        }

    }

    public boolean isBeenDeleted() {

        boolean tempDeleted = GlobalData.getInstance().isMessageTempDeleted(this);
        return mBeenDeleted || tempDeleted;
    }

    public void setBeenDeleted(boolean mBeenDeleted) {
        this.mBeenDeleted = mBeenDeleted;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public MessageType getmType() {
        return mType;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getmContentText() {
        return mContentText;
    }

    public void setmContentText(String mContentText) {
        this.mContentText = mContentText;
    }

    public LocalDate getDateCreated() {
        return mDateCreated;
    }

    public void setDateCreated(LocalDate mDateCreated) {
        this.mDateCreated = mDateCreated;
    }

    public LocalDate getDateActiveFrom() {
        return mDateActiveFrom;
    }

    public void setmDateActiveFrom(LocalDate mDateActiveFrom) {
        this.mDateActiveFrom = mDateActiveFrom;
    }

    public LocalDate getmDateActiveTo() {
        return mDateActiveTo;
    }

    public void setmDateActiveTo(LocalDate mDateActiveTo) {
        this.mDateActiveTo = mDateActiveTo;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public void setHidden(boolean hidden) {
        this.mHidden = hidden;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFormattedDate(){

        LocalDate today = LocalDate.now();

        if(today.equals(getDateCreated())){
            return "Today";
        }

        if(getDateCreated().plusWeeks(1).isAfter(today)){
            //We send back the formatted day of the week
            return getDateCreated().dayOfWeek().getAsText(Locale.ENGLISH);
        }

        return getDateCreated().toString("dd/MM/yyyy");

    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasBeenRead() {
        return mBeenRead;
    }

    public void setmBeenRead(boolean mBeenRead) {
        this.mBeenRead = mBeenRead;
    }

    public void setOnMessageClickListener(OnMessageClickedListener l){
        mListener = l;
    }

    private void viewClicked(){

        setViewToOpened();

        if(mListener != null){
            mListener.onMessageClick(this);
        }
    }

    public void setViewToOpened(){

        if(mView == null) return;

        this.mBeenRead = true;
        mView.findViewById(R.id.obj_message_new_dot).setVisibility(View.INVISIBLE);
        ((TextView)mView.findViewById(R.id.obj_message_title)).setTextColor(Color.GRAY);
        ((TextView)mView.findViewById(R.id.obj_message_date_holder)).setTextColor(Color.GRAY);
        ((TextView)mView.findViewById(R.id.obj_message_content_text)).setTextColor(Color.GRAY);
    }

    public String toJsonString() throws Exception{

        JSONObject obj = new JSONObject();
        obj.put("type", this.mType.compareName);
        obj.put("title", mTitle);
        obj.put("contentText", mContentText);
        obj.put("dateCreated", mDateCreated.toString("dd/MM/yyyy"));
        obj.put("activeFrom", mDateActiveFrom.toString("dd/MM/yyyy"));
        obj.put("activeTo", mDateActiveTo.toString("dd/MM/yyyy"));
        obj.put("hidden", mHidden);
        obj.put("urlLink", urlLink);
        obj.put("severity", severity);
        obj.put("imageUrl", imageUrl);
        obj.put("beenRead", mBeenRead);
        obj.put("beenDeleted", mBeenDeleted);
        obj.put("messageId", messageId);
        return obj.toString();

    }



    public View getView(Context context){

        if(context == null){
            return null;
        }

        //inflate the view and return the inflated view with the new params
        View view = View.inflate(context, R.layout.object_travel_message, null);
        TextView title = view.findViewById(R.id.obj_message_title);
        title.setTextColor(Color.BLACK);
        TextView date = view.findViewById(R.id.obj_message_date_holder);
        date.setTextColor(Color.BLACK);
        TextView contentArea = view.findViewById(R.id.obj_message_content_text);
        contentArea.setTextColor(Color.BLACK);
        ImageView icon = view.findViewById(R.id.obj_message_icon);
        TextView dot = view.findViewById(R.id.obj_message_new_dot);

        if(hasBeenRead()){
            dot.setVisibility(View.INVISIBLE);
            title.setTextColor(Color.GRAY);
            date.setTextColor(Color.GRAY);
            contentArea.setTextColor(Color.GRAY);
        }

        switch (this.mType){
            case TRAVEL_ALERT:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case APP:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case BUS:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case OFFERS:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case TEST:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case TIMETABLE:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case TRAFFIC:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case DIRECT:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
            case UNKNOWN:
                icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.alerts_icon_red_background));
                break;
        }

        contentArea.setText(getmContentText());
        date.setText(getFormattedDate());
        title.setText(getmTitle());
        view.setOnClickListener(v -> viewClicked());
        mView = view;
        return view;

    }



}
