package uk.co.trentbarton.hugo.dataholders;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;

public class Stop implements Parcelable{

    private String AtcoCode, stopName, overrideName, locality;
    private long stopId;
    private boolean enabled;
    private LatLng position;
    private int distance, version, orderNumber;
    private ServiceFilter mFilter;
    private boolean searchedForFilter = false;
    private ArrayList<RealtimePrediction> predictions;

    public Stop(){
        this.predictions = new ArrayList<>();
    }

    protected Stop(Parcel in) {
        predictions = new ArrayList<>();
        AtcoCode = in.readString();
        stopName = in.readString();
        overrideName = in.readString();
        locality = in.readString();
        stopId = in.readLong();
        enabled = in.readByte() != 0;
        position = in.readParcelable(LatLng.class.getClassLoader());
        distance = in.readInt();
        version = in.readInt();
        orderNumber = in.readInt();
        in.readTypedList(predictions, RealtimePrediction.CREATOR);
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    public long getStopId() {
        return stopId;
    }

    public void setStopId(long stopId) {
        this.stopId = stopId;
    }

    public String getAtcoCode() {
        return AtcoCode;
    }

    public void setAtcoCode(String atcoCode) {
        AtcoCode = atcoCode;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String name) {
        this.stopName = name;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ArrayList<RealtimePrediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(ArrayList<RealtimePrediction> predictions) {
        this.predictions = predictions;
    }

    public String getOverrideName() {

        if(overrideName == null || overrideName.isEmpty()){
            return this.getStopName();
        }else{
            return overrideName;
        }
    }

    public boolean isStopFavourite(Context context){
        return new Database(context).isStopFavourite(this.getAtcoCode());
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setOverrideName(String overrideName) {
        this.overrideName = overrideName;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Stop){
            return false;
        }else{
            return ((Stop) other).getAtcoCode().equals(this.getAtcoCode());
        }
    }

    public void resetFilter(Context context){
        applyFilterFromPreferences(context);
    }

    public void applyFilterToStop(Context context, ServiceFilter filter){

        mFilter = filter;
        searchedForFilter = false;

        if(mFilter.getAllServiceNames().length == 0){
            HugoPreferences.removeStopFilter(context, getAtcoCode());
            mFilter = null;
        }else{
            HugoPreferences.saveStopFilter(context, getAtcoCode(), filter);
        }

    }

    public ArrayList<RealtimePrediction> getFilteredPredictions(Context context){

        if(!searchedForFilter){applyFilterFromPreferences(context);}

        if(mFilter == null){
            return this.getPredictions();
        }

        ArrayList<RealtimePrediction> predictions = new ArrayList<>();

        for(RealtimePrediction prediction : this.getPredictions()){

            if(mFilter.passesFilter(prediction)){
                predictions.add(prediction);
            }
        }

        return predictions;

    }

    public void applyFilterFromPreferences(Context context) {
        mFilter = HugoPreferences.getFilterForStop(context, getAtcoCode());
        searchedForFilter = true;
    }

    public boolean stopHasActiveFilter(Context context){
        if(!searchedForFilter){applyFilterFromPreferences(context);}
        return (mFilter != null && mFilter.getAllServiceNames().length > 0);
    }

    public Set<String> getAllUniqueServiceNames(){

        Set<String> names = new HashSet<>();
        for(RealtimePrediction prediction : getPredictions()){
            names.add(prediction.getServiceName());
        }

        return names;

    }

    public Map<String, Integer> getAllUniqueServiceNamesWithColourCode(){

        Map<String, Integer> names = new HashMap<>();
        for(RealtimePrediction prediction : getPredictions()){
            names.put(prediction.getServiceName(), prediction.getServiceColour());
        }

        return names;
    }

    public Map<String, Integer> getAllFilteredServicesWithColourCode(){

        //These are all the services which are being filtered out with colour code
        if(mFilter == null){
            return new HashMap<>();
        }else{

            Map<String, Integer> names = getAllUniqueServiceNamesWithColourCode();
            Map<String, Integer> filteredNames = new HashMap<>();
            for(String name : names.keySet()){
                if(!mFilter.passesFilter(name)){
                    filteredNames.put(name, names.get(name));
                }
            }

            return filteredNames;
        }
    }

    public Map<String, Integer> getAllUnFilteredServicesWithColourCode(){

        //These are all the services which are being filtered out with colour code
        if(mFilter == null){
            return getAllUniqueServiceNamesWithColourCode();
        }else{
            Map<String, Integer> names = getAllUniqueServiceNamesWithColourCode();
            Map<String, Integer> filteredNames = new HashMap<>();
            for(String name : names.keySet()){
                if(mFilter.passesFilter(name)){
                    filteredNames.put(name, names.get(name));
                }
            }

            return filteredNames;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(AtcoCode);
        dest.writeString(stopName);
        dest.writeString(overrideName);
        dest.writeString(locality);
        dest.writeLong(stopId);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeParcelable(position, flags);
        dest.writeInt(distance);
        dest.writeInt(version);
        dest.writeInt(orderNumber);
        dest.writeTypedList(predictions);
    }
}
