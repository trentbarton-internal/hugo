package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import uk.co.trentbarton.hugo.R;

public class ScheduledJourneyStep extends JourneyStep {

    private String boardingStopName, alightingStopName, serviceName, serviceDestination, departureTimeText;
    private int busColour, serviceColour, textColour, numberOfStops;
    private long departureTimeValue;
    private boolean busHasWifi, busHasUsb, busHasMango;

    public ScheduledJourneyStep(JSONObject object) throws Exception {
        super(object);
        this.boardingStopName = object.getString("boarding_stop");
        this.alightingStopName = object.getString("alighting_stop");
        this.serviceName = object.getString("service_name");
        this.serviceDestination = object.getString("service_destination");
        this.departureTimeText = object.getString("departure_time_text");
        this.departureTimeValue = object.getLong("departure_time_value");
        this.setBusColour(object.getString("bus_colour"));
        this.setServiceColour(object.getString("service_colour"));
        this.setTextColour(object.getString("text_colour"));
        this.setNumberOfStops(object.getInt("number_of_stops"));
        this.setBusHasUsb(object.getInt("bus_has_usb") != 0);
        this.setBusHasWifi(object.getInt("bus_has_wifi") != 0);
        this.setBusHasMango(object.getInt("bus_has_mango") != 0);
    }

    @Override
    public View getView(Context context) {
        mContext = context;
        if(mView == null){
            mView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.journey_step_scheduled, null);
            init();
        }

        return mView;
    }


    public String getDepartureTimeText() {
        return departureTimeText;
    }

    public void setDepartureTimeText(String departureTimeText) {
        this.departureTimeText = departureTimeText;
    }

    public void setBusColour(int busColour) {
        this.busColour = busColour;
    }

    public void setServiceColour(int serviceColour) {
        this.serviceColour = serviceColour;
    }

    public void setTextColour(int textColour) {
        this.textColour = textColour;
    }

    public long getDepartureTimeValue() {
        return departureTimeValue;
    }

    public void setDepartureTimeValue(long departureTimeValue) {
        this.departureTimeValue = departureTimeValue;
    }

    private void init() {

        ImageView busColourRing = mView.findViewById(R.id.busColourIndicator);
        TextView serviceName = mView.findViewById(R.id.service_name_text);
        TextView destinationText = mView.findViewById(R.id.destination_text);
        LinearLayout mangoHolder = mView.findViewById(R.id.mangoHolder);
        TextView sitBackText = mView.findViewById(R.id.sit_back_text);
        TextView scheduledTimeText = mView.findViewById(R.id.scheduledTimeText);

        busColourRing.setBackgroundColor(this.getServiceColour());
        serviceName.setText(this.getServiceName());
        destinationText.setText("Towards " + this.getServiceDestination());

        if(this.isBusHasMango()){
            mangoHolder.setVisibility(View.VISIBLE);
        }else{
            mangoHolder.setVisibility(View.GONE);
        }

        sitBackText.setText("sit back and relax for around " + this.getDurationText());

        scheduledTimeText.setText(this.departureTimeText);

        RelativeLayout holder = mView.findViewById(R.id.holder);
        holder.setOnClickListener(v -> {
            this.itemClicked();
        });

    }

    public int getBusColour() {
        return busColour;
    }

    public void setBusColour(String busColour) {
        try{
            this.busColour = Color.parseColor(busColour);
        }catch(Exception e){
            this.busColour = Color.parseColor("e3e3e3");
        }

    }

    public String getBoardingStopName() {
        return boardingStopName;
    }

    public void setBoardingStopName(String boardingStopName) {
        this.boardingStopName = boardingStopName;
    }

    public String getAlightingStopName() {
        return alightingStopName;
    }

    public void setAlightingStopName(String alightingStopName) {
        this.alightingStopName = alightingStopName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDestination() {
        return serviceDestination;
    }

    public void setServiceDestination(String serviceDestination) {
        this.serviceDestination = serviceDestination;
    }

    public int getServiceColour() {
        return serviceColour;
    }

    public void setServiceColour(String serviceColour) {
        try{
            this.serviceColour = Color.parseColor(serviceColour);
        }catch(Exception e){
            this.serviceColour = Color.parseColor("e3e3e3");
        }
    }

    public int getTextColour() {
        return textColour;
    }

    public void setTextColour(String textColour) {
        try{
            this.textColour = Color.parseColor(textColour);
        }catch(Exception e){
            this.textColour = Color.BLACK;
        }
    }

    public int getNumberOfStops() {
        return numberOfStops;
    }

    public void setNumberOfStops(int numberOfStops) {
        this.numberOfStops = numberOfStops;
    }

    public boolean isBusHasWifi() {
        return busHasWifi;
    }

    public void setBusHasWifi(boolean busHasWifi) {
        this.busHasWifi = busHasWifi;
    }

    public boolean isBusHasUsb() {
        return busHasUsb;
    }

    public void setBusHasUsb(boolean busHasUsb) {
        this.busHasUsb = busHasUsb;
    }

    public boolean isBusHasMango() {
        return busHasMango;
    }

    public void setBusHasMango(boolean busHasMango) {
        this.busHasMango = busHasMango;
    }
}
