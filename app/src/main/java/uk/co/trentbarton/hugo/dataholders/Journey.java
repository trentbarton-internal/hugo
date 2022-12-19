package uk.co.trentbarton.hugo.dataholders;

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.trentbarton.hugo.dataholders.JourneyItems.FinishJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.HopOffStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.JourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.RealtimeJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.ScheduledJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.StartJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.WalkingJourneyStep;
import uk.co.trentbarton.hugo.R;

public class Journey implements Serializable {

    private String journeyPolyLine, distanceText, durationText, firstDepartureTimeText;
    private ArrayList<JourneyStep> steps;
    private String arrivalTimeText, departureTimeText, endAddress, startAddress;
    private long arrivalTime,departureTime, distance, duration, journeyId, firstDepartureTime, totalWalkingDistance;
    private LatLng endPosition, startPosition;
    private boolean isMangoApplicable, isFastest;
    private String overrideFromName, overrideToName;
    private ArrayList<JourneyDisruption> disruptions;
    private int maxSeverity = 0;
    private int numberOfChanges;

    public Journey(){

        this.disruptions = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.isFastest = true;
        this.isMangoApplicable = true;

    }

    public Journey (JSONObject item) throws Exception{

        this.disruptions = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.setArrivalTimeText(item.getString("arrival_text"));
        this.setArrivalTime(item.getLong("arrival_value"));
        this.setDepartureTimeText(item.getString("departure_text"));
        this.setDepartureTime(item.getLong("departure_value"));
        this.setDistanceText(item.getString("distance_text"));
        this.setDistance(item.getLong("distance_value"));
        this.setDurationText(item.getString("duration_text"));
        this.setDuration(item.getLong("duration_value"));
        this.setEndAddress(item.getString("end_address"));
        this.setEndPosition(new LatLng(item.getDouble("end_lat"), item.getDouble("end_lng")));
        this.setJourneyPolyLine(item.getString("polyline"));
        this.setStartAddress(item.getString("start_address"));
        this.setStartPosition(new LatLng(item.getDouble("start_lat"), item.getDouble("start_lng")));
        if(!item.isNull("journey_id")){this.setJourneyId(item.getLong("journey_id"));}
        this.setFirstDepartureTimeText(item.getString("first_departure_time_text"));
        this.setFirstDepartureTime(item.getLong("first_departure_time_value"));
        this.setTotalWalkingDistance(item.getLong("total_walking_distance"));
        this.setNumberOfChanges(item.getInt("number_of_changes"));
        this.setFastest(item.getJSONObject("journey_features").getInt("fastest") != 0);
        this.setMangoApplicable(item.getJSONObject("journey_features").getInt("mango") != 0);
        if(!item.isNull("disruptions"))this.addDisruptions(item.getJSONArray("disruptions"));
        this.addSteps(item.getJSONArray("steps"));

    }

    private void addSteps(JSONArray steps) throws Exception {

        if(steps == null){
            return;
        }


        for(int i = 0; i < steps.length(); i++){

            JSONObject stepObject = steps.getJSONObject(i);
            String stepType = stepObject.getString("travel_mode");

            if(JourneyParams.getInstance().getFromPlace() == null){
                stepObject.put("start_address", this.getStartAddress());
            }else{
                stepObject.put("start_address", JourneyParams.getInstance().getFromPlace().getName());
            }

            if(JourneyParams.getInstance().getToPlace() == null){
                stepObject.put("end_address", this.getEndAddress());
            }else{
                stepObject.put("end_address", JourneyParams.getInstance().getToPlace().getName());
            }

            if(i == 0){

                this.addStep(new StartJourneyStep(stepObject));
            }

            if(stepType.equalsIgnoreCase("WALKING")){
                this.addStep(new WalkingJourneyStep(stepObject));
            }else{
                if(stepObject.isNull("realtime_prediction")){
                    this.addStep(new ScheduledJourneyStep(stepObject));
                }else{
                    this.addStep(new RealtimeJourneyStep(stepObject));
                }

                this.addStep(new HopOffStep(stepObject));
            }

            if(i == (steps.length() - 1)){
                FinishJourneyStep journeyStep = new FinishJourneyStep(stepObject);
                journeyStep.setTotalDistance(this.getDistanceText());
                this.addStep(journeyStep);
            }
        }
    }

    private void addStep(JourneyStep journeyStep) {
        this.steps.add(journeyStep);
    }

    private void addDisruptions(JSONArray disruptions) throws Exception{

        if(disruptions == null) {
            return;
        }

        for(int i = 0; i < disruptions.length(); i++) {
            JourneyDisruption disruption = new JourneyDisruption();
            disruption.setStartTime(LocalDateTime.parse(disruptions.getJSONObject(i).getString("start"), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            disruption.setEndTime(LocalDateTime.parse(disruptions.getJSONObject(i).getString("end"), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            disruption.setMessage(disruptions.getJSONObject(i).getString("message"));
            disruption.setSeverity(Integer.parseInt(disruptions.getJSONObject(i).getString("severity")));
            this.addDisruption(disruption);
        }
    }

    public String getFirstDepartureTimeText() {
        return firstDepartureTimeText;
    }

    public Spanned getFormattedFirstDepartureTimeText() {
        try{
            String[] parts = firstDepartureTimeText.split(" ");
            return Html.fromHtml("<b>" + parts[0] + "</b> " + parts[1]);
        }catch(Exception e){
            return Html.fromHtml(firstDepartureTimeText);
        }
    }

    public void setFirstDepartureTimeText(String firstDepartureTimeText) {
        this.firstDepartureTimeText = firstDepartureTimeText;
    }

    public String getFormattedDisruptionText(){

        if(!this.hasDisruptions()) return "";

        String text = "";

        for(int i = 0; i < this.getDisruptions().size(); i++){
            JourneyDisruption dis = this.getDisruptions().get(i);
            text += dis.getMessage();

            if(i != this.getDisruptions().size() -1 ){
                text += "\n\n";
            }

        }

        return text;

    }


    public long getFirstDepartureTime() {
        return firstDepartureTime;
    }

    public void setFirstDepartureTime(long firstDepartureTime) {
        this.firstDepartureTime = firstDepartureTime;
    }

    public long getTotalWalkingDistance() {
        return totalWalkingDistance;
    }

    public void setTotalWalkingDistance(long totalWalkingDistance) {
        this.totalWalkingDistance = totalWalkingDistance;
    }

    public int getNumberOfChanges() {
        return numberOfChanges;
    }

    public void setNumberOfChanges(int numberOfChanges) {
        this.numberOfChanges = numberOfChanges;
    }

    public String getJourneyPolyLine() {
        return journeyPolyLine;
    }

    public void setJourneyPolyLine(String journeyPolyLine) {
        this.journeyPolyLine = journeyPolyLine;
    }

    public boolean hasRealtime() {

        if(this.getSteps() == null){
            return false;
        }

        for(JourneyStep step : this.getSteps()){
            if(step instanceof RealtimeJourneyStep || step instanceof ScheduledJourneyStep){
                return step instanceof RealtimeJourneyStep;
            }
        }

        return false;
    }

    private String getOverrideFromName() {
        return overrideFromName;
    }

    public void setOverrideFromName(String overrideFromName) {
        this.overrideFromName = overrideFromName;
    }

    private String getOverrideToName() {
        return overrideToName;
    }

    public void setOverrideToName(String overrideToName) {
        this.overrideToName = overrideToName;
    }

    public String getFirstDepartureDueInMinsText() {

        long millsDifference = (getFirstDepartureTime() * 1000L) - System.currentTimeMillis();

        if(millsDifference < 30 * 1000){ //less than 30 seconds
            return "due";
        }

        int seconds = (int) (millsDifference / 1000);
        int mins = (int) seconds / 60;

        if(seconds < 90){
            return "1 min";
        }

        if((seconds % 60) < 30){
            return mins + " mins";
        }else{
            return (mins + 1) + " mins";
        }
    }

    public Spanned getFormattedFirstDepartureDueInMinsText() {

        long millsDifference = (getFirstDepartureTime() * 1000L) - System.currentTimeMillis();

        if(millsDifference < 30 * 1000){ //less than 30 seconds
            return Html.fromHtml("<b>due</b>");
        }

        int seconds = (int) (millsDifference / 1000);
        int mins = (int) seconds / 60;

        if(seconds < 90){
            return Html.fromHtml("<b>1</b> min");
        }

        if((seconds % 60) < 30){
            return Html.fromHtml("<b>" + mins + "</b> mins");
        }else{
            return Html.fromHtml("<b>" + (mins+1) + "</b> mins");
        }
    }

    public ArrayList<JourneyStep> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<JourneyStep> steps) {
        this.steps = steps;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String t){
        this.distanceText = t;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public boolean isHasRealtime() {

        if(this.steps == null || this.steps.size() == 0){
            return false;
        }

        for(JourneyStep step : this.getSteps()){
            if(step instanceof RealtimeJourneyStep){
                return true;
            }
        }

        return false;
    }

    public String getArrivalTimeText() {
        return arrivalTimeText;
    }

    public void setArrivalTimeText(String arrivalTimeText) {
        this.arrivalTimeText = arrivalTimeText;
    }

    public String getDepartureTimeText() {
        return departureTimeText;
    }

    public Spanned getFormattedDepartureTimeText() {

        try{
            String[] parts = departureTimeText.split(" ");
            return Html.fromHtml("<b>" + parts[0] + "</b> " + parts[1]);
        }catch(Exception e){
            return Html.fromHtml(getDepartureTimeText());
        }

    }

    public void setDepartureTimeText(String departureTimeText) {
        this.departureTimeText = departureTimeText;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public long getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(long journeyId) {
        this.journeyId = journeyId;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LatLng getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(LatLng endPosition) {
        this.endPosition = endPosition;
    }

    public LatLng getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(LatLng startPosition) {
        this.startPosition = startPosition;
    }

    public List<LatLng> getPolyLineCoOrdinates(){
        return PolyUtil.decode(getJourneyPolyLine());
    }

    public String getTravelTimeInMins(){

        int minsTravelTime = (int) this.duration/60;
        int hours = (int) Math.floor(minsTravelTime/60.0);
        int mins = minsTravelTime % 60;

        if(hours == 0){
            return mins + " mins";
        }else{
            return String.format(Locale.ENGLISH,"%d hr %d mins",hours,mins);
        }

    }

    public Spanned getFormattedTravelTimeInMins(){

        int minsTravelTime = (int) this.duration/60;
        int hours = (int) Math.floor(minsTravelTime/60.0);
        int mins = minsTravelTime % 60;

        if(hours == 0){
            return Html.fromHtml("<b>" + mins + "</b> mins");
        }else{
            if(hours > 1){
                return Html.fromHtml("<b>" + hours + "</b> hrs <b>" + mins + "</b> mins");
            }else{
                return Html.fromHtml("<b>1</b> hr <b>" + mins + "</b> mins");
            }
        }

    }

    public boolean isMangoApplicable() {
        return isMangoApplicable;
    }

    public void setMangoApplicable(boolean mangoApplicable) {
        isMangoApplicable = mangoApplicable;
    }

    public boolean isFastest() {
        return isFastest;
    }

    public void setFastest(boolean fastest) {
        isFastest = fastest;
    }

    public String extractLocalityFromAddress(String address){

        char[] stringarray = address.toCharArray();
        boolean startRecording = false;
        String result = "";

        for(int i = (stringarray.length - 1); i >= 0 ; i--){

            if(stringarray[i] == ','){
                if(startRecording){
                    //If start recording is true then we have the first complete so we should stop the loop
                    break;
                }else{
                    //First comma encountered so ignore this char
                    i--;
                }
                startRecording = !startRecording;
            }

            if(startRecording){
                result += stringarray[i];
            }
        }

        stringarray = result.trim().toCharArray();
        result = "";

        for(int i = (stringarray.length -1); i >= 0; i--){

            if(stringarray[i] == ' '){
                break;
            }

            result += stringarray[i];

        }

        return result;

    }

    public ArrayList<JourneyDisruption> getDisruptions() {
        return disruptions;
    }

    public void addDisruption(JourneyDisruption disruption) {
        this.disruptions.add(disruption);
    }

    public int getMaxSeverity() {
        return maxSeverity;
    }

    public void setMaxSeverity(int max) {
        if(this.maxSeverity < max) {
            this.maxSeverity = max;
        }
    }

    public String getFriendlyFromName(){
        if(this.getOverrideFromName() == null || this.getOverrideFromName().equalsIgnoreCase("")){
            return trimAddress(this.getStartAddress());
        }else{
            return trimAddress(this.getOverrideFromName());
        }
    }

    public String getFriendlyToName(){
        if(this.getOverrideToName() == null || this.getOverrideToName().equalsIgnoreCase("")){
            return trimAddress(this.getEndAddress());
        }else{
            return trimAddress(this.getOverrideToName());
        }
    }

    private String trimAddress(String address) {

        String returnString = "";

        for(char c : address.toCharArray()) {
            if(c == ','){
                break;
            }else{
                returnString += c;
            }
        }

        return  returnString.trim();
    }

    public boolean hasDisruptions() {

        return disruptions.size() != 0;

    }
}
