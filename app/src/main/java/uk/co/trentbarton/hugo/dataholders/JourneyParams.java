package uk.co.trentbarton.hugo.dataholders;

import org.joda.time.DateTime;

public class JourneyParams {

    private static JourneyParams mInstance;
    private TomTomPlace fromPlace, toPlace;
    private DateTime arrivalTime, leavingTime;

    private JourneyParams(){

    }

    public static JourneyParams getInstance(){
        if(mInstance == null){
            mInstance = new JourneyParams();
        }

        return mInstance;
    }

    public TomTomPlace getFromPlace() {
        return fromPlace;
    }

    public void setFromPlace(TomTomPlace fromPlace) {
        this.fromPlace = fromPlace;
    }

    public TomTomPlace getToPlace() {
        return toPlace;
    }

    public void setToPlace(TomTomPlace toPlace) {
        this.toPlace = toPlace;
    }

    public DateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(DateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public DateTime getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(DateTime leavingTime) {
        this.leavingTime = leavingTime;
    }

    public boolean areBothLocationsSet() {
        return fromPlace != null && toPlace != null;
    }
}
