package uk.co.trentbarton.hugo.customviewcontrollers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.customview.CustomBusMarker;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.interfaces.MapHelperListener;
import uk.co.trentbarton.hugo.tools.Constants;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

import static uk.co.trentbarton.hugo.fragments.MapFragment.STANDARD_ZOOM_LEVEL;

public class MapHelper implements GoogleMap.OnMarkerClickListener{

    private static MapHelper mInstance;
    private ArrayList<Stop> mNormalStops;
    private ArrayList<Stop> mSelectedStops;
    private ArrayList<Marker> mNormalMarkers;
    private ArrayList<Marker> mSelectedMarkers;
    private ArrayList<Marker> mSelectedVehicleMarkers;
    private ArrayList<RealtimePrediction> mTrackedVehicles;
    private DrawLocalStopTask mLocalStopTask;
    private Database mDatabase;
    private GoogleMap mMap;
    private MapHelperListener mListener;
    private MapState mCurrentState;
    private Bitmap mNormalStopIcon, mSelectedStopIcon;
    private Context mContext;
    private final int MIN_MOVE_DISTANCE = 500;
    private LatLng mLastPosition;
    private float mLastMovedToZoomLevel;
    private LatLng mLastMovedToPosition;
    private Stop mLastStopClicked;
    private final static float MIN_ZOOM_OUT_LEVEL_FOR_STOPS = 15.5f;
    private boolean onResumeCalled = false;

    public void setMonitoredVehicles(ArrayList<RealtimePrediction> predictions) {
        if(predictions != null){
            this.mTrackedVehicles = predictions;
        }
    }

    public Stop getLastStopClicked() {
        return mLastStopClicked;
    }

    public void trackingRefreshed(ArrayList<RealtimePrediction> predictions) {

        ArrayList<RealtimePrediction> predictionsBeingTracked = new ArrayList<>();

        if(predictions != null){
            for(RealtimePrediction prediction : predictions){

                if(isBeingTracked(prediction)){

                    predictionsBeingTracked.add(prediction);

                }
            }
        }

        removeAllNormalMarkers();
        removeAllVehicleMarkers();
        this.mTrackedVehicles = predictionsBeingTracked;
        drawMarkers();

    }

    private boolean isBeingTracked(RealtimePrediction prediction) {

        for(RealtimePrediction trackedPredictions : mTrackedVehicles){

            if(prediction.equals(trackedPredictions)){
                return true;
            }

        }

        return false;

    }

    public void onPause(){
        Log.d("MapHelper", "On pause called");
        onResumeCalled = false;
    }

    public void onResume() {

        if(onResumeCalled){
            return;
        }

        onResumeCalled = true;

        Log.d("MapHelper", "On resume called");
        //Here we need to determine the state based on the data we have and draw markers as appropriate
        if(mTrackedVehicles.size() == 0 && mSelectedStops.size() == 0){
            //No data so just go to normal state
            changeState(MapState.SEARCHING);
            mLastPosition = new LatLng(0,0);
            drawMarkers();

        }else{
            if(mTrackedVehicles.size() > 0){
                changeState(MapState.TRACKING_VEHICLES);
                mListener.onMapStateChanged();
                drawMarkers();
            }else{
                //We are still in searching mode only the markers that have been selected will always be visible
                if(mMap == null || mMap.getCameraPosition().zoom < MIN_ZOOM_OUT_LEVEL_FOR_STOPS){
                    changeState(MapState.ZOOMED_OUT);
                }else{
                    changeState(MapState.SEARCHING);
                }

                drawMarkers();
            }
        }

    }

    public RealtimePrediction getTrackedPrediction() {

        if(mTrackedVehicles != null && !mTrackedVehicles.isEmpty()){
            return mTrackedVehicles.get(0);
        }else{
            return null;
        }
    }

    public Stop getTrackedStop() {
        if(this.mSelectedMarkers != null && !this.mSelectedMarkers.isEmpty()){
            return (Stop) mSelectedMarkers.get(0).getTag();
        }else{
            return null;
        }
    }

    public enum MapState{
        SEARCHING, TRACKING_VEHICLES, ZOOMED_OUT;
    }

    private MapHelper(){

        mNormalStops = new ArrayList<>();
        mSelectedStops = new ArrayList<>();
        mTrackedVehicles = new ArrayList<>();
        mNormalMarkers = new ArrayList<>();
        mSelectedMarkers = new ArrayList<>();
        mSelectedVehicleMarkers = new ArrayList<>();
        changeState(MapState.SEARCHING);
        mLocalStopTask = new DrawLocalStopTask();
        mLastPosition = new LatLng(0,0);

    }

    public static MapHelper getInstance(){

        if(mInstance == null){
            mInstance = new MapHelper();
        }

        return mInstance;

    }

    public MapState getMapState(){
        return this.mCurrentState;
    }

    public void setmListener(MapHelperListener l){
        this.mListener = l;
    }

    public void clearInstance(){
        mInstance = null;
    }

    public void assignMonitoredVehicle(RealtimePrediction prediction){

        removeAllNormalMarkers();
        removeAllSelectedStopMarkers();
        removeAllVehicleMarkers();
        this.mTrackedVehicles.clear();
        this.mTrackedVehicles.add(prediction);
        changeState(MapState.TRACKING_VEHICLES);
        drawMarkers();

    }

    private void removeAllSelectedStopMarkers() {
        for(Marker marker : mSelectedMarkers){
            marker.remove();
        }
        mSelectedMarkers.clear();
        mSelectedStops.clear();
    }

    private void removeAllVehicleMarkers() {

        for(Marker marker : mSelectedVehicleMarkers){
            marker.remove();
        }
        mSelectedVehicleMarkers.clear();
        mTrackedVehicles.clear();
    }

    public void assignMap(GoogleMap map){
        mMap = map;
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTag() instanceof Stop){
            panToMarker((Stop) marker.getTag());
            mLastStopClicked = (Stop) marker.getTag();
            mListener.onStopClicked((Stop) marker.getTag());
        }else{
            mListener.onBusClicked((RealtimePrediction) marker.getTag());
        }

        return true;
    }

    public void setLastStopClicked(Stop stop){
        mLastStopClicked = stop;
    }

    public void assignContext(Context context){
        mContext = context;
    }

    private BitmapDescriptor getNormalStopIcon(){

        if(mNormalStopIcon == null && mContext != null){
            mNormalStopIcon = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bus_stop_icon),
                    Metrics.densityPixelsToPixels(40),
                    Metrics.densityPixelsToPixels(40),
                    false);
        }

        if(mNormalStopIcon !=  null){
            return BitmapDescriptorFactory.fromBitmap(mNormalStopIcon);
        }else{
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }

    }

    public void endTracking(){

        changeState(MapState.SEARCHING);
        removeAllSelectedStopMarkers();
        removeAllNormalMarkers();
        removeAllVehicleMarkers();

        mLastPosition = new LatLng(0,0);
        mapMoved();

    }

    private void panToLocation(LatLng location, float zoomLevel){

        Log.d("MapHelper", "Pan to location: " + location.toString() + " at zoom level: " + zoomLevel);


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to the clicked marker
                .zoom(zoomLevel)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(0)
                .build();
        // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void panToLocation(LatLng location){
        panToLocation(location, STANDARD_ZOOM_LEVEL);
    }

    private void panToMarker(Stop stop){
        panToMarker(stop.getPosition());
    }

    private void panToMarker(Marker marker) {panToMarker(marker.getPosition());}

    private void panToMarker(LatLng location){
        panToMarker(location, STANDARD_ZOOM_LEVEL);
    }

    private void panToMarker(LatLng location, float zoomLevel){

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)     // Sets the center of the map to the clicked marker
                .zoom(zoomLevel)      // Sets the zoom
                .bearing(0)           // Sets the orientation of the camera to north
                .tilt(0)
                .build();
        // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        double center = mMap.getCameraPosition().target.latitude;
        double southMap = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;
        double newLat = ((center-southMap)/2) + center;

        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(newLat, location.longitude))      // Sets the center of the map to the clicked marker
                .zoom(zoomLevel)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(0)
                .build();
        // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public boolean moveMapToBestLocation(){

        if(mMap == null){
            return false;
        }

        //Try and get any selectedStops first, then use the current location if available, if not fall back to East Midland hardcoded
        CameraUpdate cu;

        if((mSelectedMarkers.size() + mSelectedVehicleMarkers.size()) > 0){
            if((mSelectedMarkers.size() + mSelectedVehicleMarkers.size()) == 1){

                //If there is only a single marker then zoom into this stop
                if(mSelectedMarkers.isEmpty()){
                    panToMarker(mSelectedVehicleMarkers.get(0));
                }else{
                    panToMarker(mSelectedMarkers.get(0));
                }
                return true;
            }else {

                //There are multiple markers so zoom to fit
                DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                int deviceWidth = displayMetrics.widthPixels;
                int deviceHeight = displayMetrics.heightPixels;

                //There are a number of stops that the user has selected so pan to these as a group
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker: mSelectedMarkers) {
                    builder.include(marker.getPosition());
                }

                for (Marker marker: mSelectedVehicleMarkers) {
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                int padding = Metrics.densityPixelsToPixels(80) ; // offset from edges of the map in pixels
                try{
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                    return true;
                }catch(Exception ignore){
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, deviceWidth, deviceHeight, padding);
                    mMap.moveCamera(cu);
                    return true;
                }
            }
        }else if(mLastMovedToPosition != null){
            //What i want to achieve here is that of we have already moved to a place I want to go back there not my current location although we should still look for a current location
            panToLocation(mLastMovedToPosition, mLastMovedToZoomLevel);
            return true;
        }

        return false;
    }

    private BitmapDescriptor getSelectedStopIcon(){

        if(mSelectedStopIcon == null && mContext != null){
            mSelectedStopIcon = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bus_stop_selected_icon),
                    Metrics.densityPixelsToPixels(50),
                    Metrics.densityPixelsToPixels(50),
                    false);
        }

        if(mSelectedStopIcon !=  null){
            return BitmapDescriptorFactory.fromBitmap(mSelectedStopIcon);
        }else{
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        }

    }

    private boolean changeState(MapState newState){
        if(newState != mCurrentState){
            mCurrentState = newState;
            if(mListener != null){
                mListener.onMapStateChanged();
            }

            return true;
        }

        mCurrentState = newState;
        return false;

    }

    public void forceFakeMapMove(){

        if(mMap == null){
            return;
        }

        if(mCurrentState == MapState.TRACKING_VEHICLES){
            //Do Nothing as the drawing will be handled in the map class
            return;
        }

        if(mMap.getCameraPosition().zoom < MIN_ZOOM_OUT_LEVEL_FOR_STOPS){
            removeAllNormalMarkers();
            changeState(MapState.ZOOMED_OUT);
            mLastPosition = new LatLng(0,0);
            return;
        }else{
            changeState(MapState.SEARCHING);
        }

        //we have moved more than 500m so run the task to populate stops
        mLastPosition = mMap.getCameraPosition().target;
        mLocalStopTask.cancel(true);
        mLocalStopTask = new DrawLocalStopTask();
        mLocalStopTask.execute(mMap.getCameraPosition().target);

    }

    public void mapMoved(){

        if(mMap == null) return;

        if(mCurrentState == MapState.TRACKING_VEHICLES){
            //Do Nothing as the drawing will be handled in the map class
            return;
        }

        if(mMap.getCameraPosition().zoom < MIN_ZOOM_OUT_LEVEL_FOR_STOPS){
            removeAllNormalMarkers();
            changeState(MapState.ZOOMED_OUT);
            mLastPosition = new LatLng(0,0);
            return;
        }else{
            changeState(MapState.SEARCHING);
        }

        if(calculateDistance(mLastPosition,mMap.getCameraPosition().target) > MIN_MOVE_DISTANCE){
            //we have moved more than 500m so run the task to populate stops
            mLastPosition = mMap.getCameraPosition().target;
            mLocalStopTask.cancel(true);
            mLocalStopTask = new DrawLocalStopTask();
            mLocalStopTask.execute(mMap.getCameraPosition().target);
        }

        mLastMovedToPosition = mMap.getCameraPosition().target;
        mLastMovedToZoomLevel = mMap.getCameraPosition().zoom;
    }

    private void removeAllNormalMarkers() {

        for(Marker marker : mNormalMarkers){
            marker.remove();
        }

        mNormalMarkers.clear();

    }

    private double calculateDistance (LatLng positionA, LatLng positionB){
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(positionB.latitude-positionA.latitude);
        double lngDiff = Math.toRadians(positionB.longitude-positionA.longitude);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(positionA.latitude)) * Math.cos(Math.toRadians(positionB.latitude)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }

    private void drawMarkers(){

        if(mMap == null){
            return;
        }

        if(getMapState() == MapState.SEARCHING) {
            drawNormalMarkers();
            drawSelectedMarkers();
        }else if(getMapState() == MapState.ZOOMED_OUT){
            drawSelectedMarkers();
        }else if(getMapState() == MapState.TRACKING_VEHICLES){
            drawLastClickedStop();
            drawBusMarkers();
        }

        moveMapToBestLocation();
    }

    private void drawBusMarkers() {

        for(RealtimePrediction prediction : this.mTrackedVehicles){

            if(!prediction.isWorking() || (prediction.getVehiclePosition().latitude == 0 && prediction.getVehiclePosition().longitude == 0)){
                continue;
            }

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .draggable(false)
                    .position(prediction.getVehiclePosition())
                    .title(prediction.getServiceName())
                    .icon(BitmapDescriptorFactory.fromBitmap(new CustomBusMarker(prediction.getVehicleColour()).getBitmap(mContext))));
            marker.setTag(prediction);
            mSelectedVehicleMarkers.add(marker);

        }

    }

    private void drawLastClickedStop() {

        if(mLastStopClicked != null){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .draggable(false)
                    .position(mLastStopClicked.getPosition())
                    .title(mLastStopClicked.getStopName())
                    .icon(getSelectedStopIcon()));
            marker.setTag(mLastStopClicked);
            mSelectedMarkers.add(marker);

        }

    }

    private void drawNormalMarkers(){

        for(Stop stop : mNormalStops){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .draggable(false)
                    .position(stop.getPosition())
                    .title(stop.getStopName())
                    .icon(getNormalStopIcon()));
            marker.setTag(stop);
            mNormalMarkers.add(marker);
        }
    }

    private void drawSelectedMarkers(){

        for(Stop stop : mSelectedStops){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .draggable(false)
                    .position(stop.getPosition())
                    .title(stop.getStopName())
                    .icon(getSelectedStopIcon()));
            marker.setTag(stop);
            mSelectedMarkers.add(marker);
        }

    }

    public void setSelectedStops(ArrayList<Stop> stops) {

        if(stops != null){
            removeAllSelectedStopMarkers();
            this.mSelectedStops = stops;
        }
    }

    private class DrawLocalStopTask extends AsyncTask<LatLng, Void, ArrayList<Stop>> {

        @Override
        protected ArrayList<Stop> doInBackground(LatLng... positions) {

            LatLng centrePosition = positions[0];

            if(mDatabase == null){
                if(mContext == null){
                    return null;
                }
                mDatabase = new Database(mContext);
            }

            ArrayList<Stop> stops = mDatabase.getAllStopsInArea(centrePosition);
            return stops;
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops) {

            ArrayList<Stop> stopsToAdd = new ArrayList<>();

            if(stops == null){
                return;
            }

            for(Stop stop : stops) {

                boolean addToMap = true;

                for (Marker pickedMarker : mSelectedMarkers) {

                    Stop pickedStop = (Stop) pickedMarker.getTag();

                    if (stop.getStopId() == pickedStop.getStopId()) {
                        addToMap = false;
                        break;
                    }
                }

                if (addToMap) {
                    stopsToAdd.add(stop);
                }
            }


            for (Stop stop : stopsToAdd){

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .draggable(false)
                        .position(stop.getPosition())
                        .title(stop.getStopName())
                        .icon(getNormalStopIcon()));
                marker.setTag(stop);
                mNormalMarkers.add(marker);

            }
        }
    }
}
