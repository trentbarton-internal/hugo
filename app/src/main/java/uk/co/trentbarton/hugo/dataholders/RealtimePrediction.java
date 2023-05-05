package uk.co.trentbarton.hugo.dataholders;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.util.Date;


public class RealtimePrediction implements Parcelable {

    private boolean isWorking, inCongestion, atStop, cancelledService, hasWifi, hasUsb, hasMango;
    private String stopCode, serviceName, journeyOrigin, journeyDestination, predictionDisplay, cancelledReason, driverName;
    private LatLng vehiclePosition;
    private LocalDateTime originDepartureTime, destinationArrivalTime, scheduledDepartureTime, actualDepartureTime;
    private int serviceColour, predictionInSeconds, vehicleColour, vehicleNumber;

    public RealtimePrediction(){}

    public RealtimePrediction(JSONObject object){

        try{
            if(object.has("isWorking")) {
                isWorking = (object.getInt("isWorking") > 0);
            }else {
                isWorking = false;
            }

            if(object.has("inCongestion")) {
                inCongestion = (object.getInt("inCongestion") > 0);
            }else {
                inCongestion = false;
            }

            if(object.has("atStop")) {
                atStop = (object.getInt("atStop") > 0);
            }else {
                atStop = false;
            }

            if(object.has("cancelled_service")) {
                cancelledService = object.getBoolean("cancelled_service");
            }else {
                cancelledService = false;
            }

            if(object.has("hasWifi")) {
                hasWifi = (object.getInt("hasWifi") > 0);
            }else {
                hasWifi = false;
            }

            if(object.has("hasUsb")) {
                hasUsb = (object.getInt("hasUsb") > 0);
            }else {
                hasUsb = false;
            }

            if(object.has("hasMango")) {
                hasMango = (object.getInt("hasMango") > 0);
            }else {
                hasMango = false;
            }

            if(object.has("stopCode")) {
                stopCode = object.getString("stopCode");
            }else {
                stopCode = "";
            }

            if(object.has("serviceName")) {
                serviceName = object.getString("serviceName");
            }else {
                serviceName = "";
            }

            if(object.has("journeyOrigin")) {
                journeyOrigin = object.getString("journeyOrigin");
            }else {
                journeyOrigin = "";
            }

            if(object.has("journeyDestination")) {
                journeyDestination = object.getString("journeyDestination");
            }else {
                journeyDestination = "";
            }

            if(object.has("predictionDisplay")) {
                predictionDisplay = object.getString("predictionDisplay");
            }else {
                predictionDisplay = "";
            }

            if(object.has("cancelledReason")) {
                cancelledReason = object.getString("cancelledReason");
            }else {
                cancelledReason = "";
            }

            if(object.has("driverName")) {
                driverName = object.getString("driverName");
            }else {
                driverName = "";
            }

            if(object.has("vehiclePositionLat") && object.has("vehiclePositionLng")){
                vehiclePosition = new LatLng(object.getDouble("vehiclePositionLat"), object.getDouble("vehiclePositionLng"));
            }else{
                vehiclePosition = new LatLng(0,0);
            }

            //serviceColour, predictionInSeconds, vehicleColour, vehicleNumber;
            if(object.has("serviceColour")) {
                serviceColour = object.getInt("serviceColour");
            }else {
                serviceColour = Color.parseColor("#939393");
            }

            if(object.has("vehicleColour")) {
                vehicleColour = object.getInt("vehicleColour");
            }else {
                vehicleColour = Color.parseColor("#939393");
            }

            if(object.has("predictionInSeconds")) {
                predictionInSeconds = object.getInt("predictionInSeconds");
            }else {
                predictionInSeconds = 0;
            }
            if(object.has("vehicleNumber")) {
                vehicleNumber = object.getInt("vehicleNumber");
            }else {
                vehicleNumber = 0;
            }

            if(object.has("originDepartureTime")){
                Date date = new Date();
                date.setTime(object.getLong("originDepartureTime"));
                originDepartureTime = LocalDateTime.fromDateFields(date);
            }

            if(object.has("destinationArrivalTime")){
                Date date = new Date();
                date.setTime(object.getLong("destinationArrivalTime"));
                originDepartureTime = LocalDateTime.fromDateFields(date);
            }

            if(object.has("scheduledDepartureTime")){
                Date date = new Date();
                date.setTime(object.getLong("scheduledDepartureTime"));
                originDepartureTime = LocalDateTime.fromDateFields(date);
            }

            if(object.has("actualDepartureTime")){
                Date date = new Date();
                date.setTime(object.getLong("actualDepartureTime"));
                originDepartureTime = LocalDateTime.fromDateFields(date);
            }

        }catch(Exception e){

        }
    }

    protected RealtimePrediction(Parcel in) {
        isWorking = in.readByte() != 0;
        inCongestion = in.readByte() != 0;
        atStop = in.readByte() != 0;
        cancelledService = in.readByte() != 0;
        hasWifi = in.readByte() != 0;
        hasUsb = in.readByte() != 0;
        hasMango = in.readByte() != 0;
        stopCode = in.readString();
        serviceName = in.readString();
        journeyOrigin = in.readString();
        journeyDestination = in.readString();
        predictionDisplay = in.readString();
        cancelledReason = in.readString();
        driverName = in.readString();
        vehiclePosition = in.readParcelable(LatLng.class.getClassLoader());
        serviceColour = in.readInt();
        predictionInSeconds = in.readInt();
        vehicleColour = in.readInt();
        vehicleNumber = in.readInt();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        scheduledDepartureTime = formatter.parseLocalDateTime(in.readString());
        originDepartureTime = formatter.parseLocalDateTime(in.readString());
        destinationArrivalTime = formatter.parseLocalDateTime(in.readString());
        actualDepartureTime = formatter.parseLocalDateTime(in.readString());
    }

    public static final Creator<RealtimePrediction> CREATOR = new Creator<RealtimePrediction>() {
        @Override
        public RealtimePrediction createFromParcel(Parcel in) {
            return new RealtimePrediction(in);
        }

        @Override
        public RealtimePrediction[] newArray(int size) {
            return new RealtimePrediction[size];
        }
    };

    public boolean isWorking() {

        if(this.getVehiclePosition() == null){
            return false;
        }

        if(this.getVehiclePosition().latitude < 0.1 && this.getVehiclePosition().latitude > -0.1){
            return false;
        }

        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    public boolean isInCongestion() {
        return inCongestion;
    }

    public void setInCongestion(boolean inCongestion) {
        this.inCongestion = inCongestion;
    }

    public boolean isAtStop() {
        return atStop;
    }

    public void setAtStop(boolean atStop) {
        this.atStop = atStop;
    }

    public boolean isCancelledService() {
        return cancelledService;
    }

    public void setCancelledService(boolean cancelledService) {
        this.cancelledService = cancelledService;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isHasUsb() {
        return hasUsb;
    }

    public void setHasUsb(boolean hasUsb) {
        this.hasUsb = hasUsb;
    }

    public boolean isHasMango() {
        return hasMango;
    }

    public void setHasMango(boolean hasMango) {
        this.hasMango = hasMango;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getJourneyOrigin() {
        return journeyOrigin;
    }

    public void setJourneyOrigin(String journeyOrigin) {
        this.journeyOrigin = journeyOrigin;
    }

    public String getJourneyDestination() {
        return journeyDestination;
    }

    public void setJourneyDestination(String journeyDestination) {
        this.journeyDestination = journeyDestination;
    }

    public String getPredictionDisplay() {
        return predictionDisplay;
    }

    public void setPredictionDisplay(String predictionDisplay) {
        this.predictionDisplay = predictionDisplay;
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public LatLng getVehiclePosition() {
        return vehiclePosition;
    }

    public void setVehiclePosition(LatLng vehiclePosition) {
        this.vehiclePosition = vehiclePosition;
    }

    public void setVehiclePosition(double lat, double lng) {
        this.vehiclePosition = new LatLng(lat,lng);
    }

    public LocalDateTime getOriginDepartureTime() {
        return originDepartureTime;
    }

    public void setOriginDepartureTime(LocalDateTime originDepartureTime) {
        this.originDepartureTime = originDepartureTime;
    }

    public LocalDateTime getDestinationArrivalTime() {
        return destinationArrivalTime;
    }

    public void setDestinationArrivalTime(LocalDateTime destinationArrivalTime) {
        this.destinationArrivalTime = destinationArrivalTime;
    }

    public LocalDateTime getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(LocalDateTime scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }

    public LocalDateTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(LocalDateTime actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public int getServiceColour() {
        return serviceColour;
    }

    public void setServiceColour(int serviceColour) {
        this.serviceColour = serviceColour;
    }

    public void setServiceColour(String htmlColour){
        try{
            this.serviceColour = Color.parseColor(htmlColour);
        }catch(Exception e){
            this.serviceColour = Color.parseColor("#939393");
        }
    }

    public int getPredictionInSeconds() {
        return predictionInSeconds;
    }

    public void setPredictionInSeconds(int predictionInSeconds) {
        this.predictionInSeconds = predictionInSeconds;
    }

    public SpannableString getFormattedPredictionDisplay(){

        try{
            String[] parts = this.getPredictionDisplay().split(" ");
            SpannableString text = new SpannableString(this.getPredictionDisplay());

            text.setSpan(new StyleSpan(Typeface.BOLD), 0, parts[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (isCancelledService()){
                text.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 0, parts[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            text.setSpan(new RelativeSizeSpan(0.7f), parts[0].length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (isCancelledService()){
                text.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), parts[0].length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else{
                text.setSpan(new ForegroundColorSpan(Color.parseColor("#444444")), parts[0].length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return text;
        }catch(Exception e){
            return new SpannableString(this.getPredictionDisplay());
        }

    }

    public int getVehicleColour() {
        return vehicleColour;
    }

    public void setVehicleColour(int vehicleColour) {
        this.vehicleColour = vehicleColour;
    }

    public void setVehicleColour(String htmlColour){
        try{
            this.vehicleColour = Color.parseColor(htmlColour);
        }catch(Exception e){
            this.vehicleColour = Color.parseColor("#939393");
        }
    }

    public int getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(int vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isWorking ? 1 : 0));
        dest.writeByte((byte) (inCongestion ? 1 : 0));
        dest.writeByte((byte) (atStop ? 1 : 0));
        dest.writeByte((byte) (cancelledService ? 1 : 0));
        dest.writeByte((byte) (hasWifi ? 1 : 0));
        dest.writeByte((byte) (hasUsb ? 1 : 0));
        dest.writeByte((byte) (hasMango ? 1 : 0));
        dest.writeString(stopCode);
        dest.writeString(serviceName);
        dest.writeString(journeyOrigin);
        dest.writeString(journeyDestination);
        dest.writeString(predictionDisplay);
        dest.writeString(cancelledReason);
        dest.writeString(driverName);
        dest.writeParcelable(vehiclePosition, flags);
        dest.writeInt(serviceColour);
        dest.writeInt(predictionInSeconds);
        dest.writeInt(vehicleColour);
        dest.writeInt(vehicleNumber);
        dest.writeString(scheduledDepartureTime.toString("dd/MM/yyyy HH:mm:ss"));
        dest.writeString(originDepartureTime.toString("dd/MM/yyyy HH:mm:ss"));
        dest.writeString(destinationArrivalTime.toString("dd/MM/yyyy HH:mm:ss"));
        dest.writeString(actualDepartureTime.toString("dd/MM/yyyy HH:mm:ss"));
    }

    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof RealtimePrediction)){
            return false;
        }else{

            String otherCompareDetails = ((RealtimePrediction) obj).getServiceName() + ((RealtimePrediction) obj).getScheduledDepartureTime() + ((RealtimePrediction) obj).getStopCode();
            String thisCompareDetails = getServiceName() + getScheduledDepartureTime() + getStopCode();

            return otherCompareDetails.equalsIgnoreCase(thisCompareDetails);

        }

    }

    public String toJsonString() throws Exception {

        /*private boolean isWorking, inCongestion, atStop, cancelledService, hasWifi, hasUsb, hasMango;
        private String stopCode, serviceName, journeyOrigin, journeyDestination, predictionDisplay, cancelledReason, driverName;
        private LatLng vehiclePosition;
        private LocalDateTime originDepartureTime, destinationArrivalTime, scheduledDepartureTime, actualDepartureTime;
        private int serviceColour, predictionInSeconds, vehicleColour, vehicleNumber;*/

        JSONObject object = new JSONObject();
        object.put("isWorking", isWorking ? 1 : 0);
        object.put("inCongestion", inCongestion ? 1 : 0);
        object.put("atStop", atStop ? 1 : 0);
        object.put("cancelledService", cancelledService ? 1 : 0);
        object.put("hasWifi", hasWifi ? 1 : 0);
        object.put("hasUsb", hasUsb ? 1 : 0);
        object.put("hasMango", hasMango ? 1 : 0);
        object.put("stopCode", stopCode);
        object.put("serviceName", serviceName);
        object.put("journeyOrigin", journeyOrigin);
        object.put("journeyDestination", journeyDestination);
        object.put("predictionDisplay", predictionDisplay);
        object.put("cancelledReason", cancelledReason);
        object.put("driverName", driverName);
        object.put("vehiclePositionLat", vehiclePosition.latitude);
        object.put("vehiclePositionLng", vehiclePosition.longitude);
        object.put("originDepartureTime", originDepartureTime.toDate().getTime());
        object.put("destinationArrivalTime", destinationArrivalTime.toDate().getTime());
        object.put("scheduledDepartureTime", scheduledDepartureTime.toDate().getTime());
        object.put("actualDepartureTime", actualDepartureTime.toDate().getTime());
        object.put("serviceColour", serviceColour);
        object.put("predictionInSeconds", predictionInSeconds);
        object.put("vehicleColor", vehicleColour);
        object.put("vehicleNumber", vehicleNumber);
        return object.toString();

    }
}
