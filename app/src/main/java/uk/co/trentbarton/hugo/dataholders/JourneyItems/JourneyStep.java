package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import uk.co.trentbarton.hugo.interfaces.OnStepClickedListener;

public abstract class JourneyStep{

    private String polyLine;
    private LocalDateTime startTime, endTime;
    private LatLng startPosition, endPosition;
    private long distanceValue, durationValue;
    private String distanceText, durationText, instructions;
    protected Context mContext;
    protected View mView;
    protected OnStepClickedListener mListener;

    abstract public View getView(Context context);

    protected JourneyStep(JSONObject object) throws Exception{

        this.setPolyLine(object.getString("polyline"));
        this.setStartPosition(new LatLng(object.getDouble("start_lat"), object.getDouble("start_lng")));
        this.setEndPosition(new LatLng(object.getDouble("end_lat"), object.getDouble("end_lng")));
        this.setDistanceValue(object.getLong("distance_value"));
        this.setDurationValue(object.getLong("duration_value"));
        this.setDistanceText(object.getString("distance_text"));
        this.setDurationText(object.getString("duration_text"));
        this.setInstructions(object.getString("instructions"));
        this.startTime = LocalDateTime.fromDateFields(new Date(object.getLong("departure_time_value") * 1000L));
        this.endTime = LocalDateTime.fromDateFields(new Date(object.getLong("arrival_time_value") * 1000L));
    }

    public String getPolyLine() {
        return polyLine;
    }

    public void setPolyLine(String polyLine) {
        this.polyLine = polyLine;
    }

    public LatLng getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(LatLng startPosition) {
        this.startPosition = startPosition;
    }

    public LatLng getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(LatLng endPosition) {
        this.endPosition = endPosition;
    }

    public long getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(long distanceValue) {
        this.distanceValue = distanceValue;
    }

    public long getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(long durationValue) {
        this.durationValue = durationValue;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public String getDurationText() {
        return durationText;
    }

    /*public Spanned getFormattedDurationText(){
        try{
            String[] parts = this.getDurationText().split(" ");
            return Html.fromHtml("<b>" + parts[0] + "</b>" + " " + parts[1]);
        }catch(Exception e){
            return Html.fromHtml(this.getDurationText());
        }
    }*/

    public SpannableString getFormattedDurationText(){

        try{
            String[] parts = this.getDurationText().split(" ");
            SpannableString text = new SpannableString(this.getDurationText());
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, parts[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new RelativeSizeSpan(0.7f), parts[0].length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(Color.parseColor("#444444")), parts[0].length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return text;
        }catch(Exception e){
            return new SpannableString(this.getDurationText());
        }
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    protected void itemClicked(){
        if(mListener != null){
            mListener.onStepClicked(this);
        }
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setOnStepClickedListener(OnStepClickedListener l){
        mListener = l;
    }
}
