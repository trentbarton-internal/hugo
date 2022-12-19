package uk.co.trentbarton.hugo.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.dialogs.CustomImageYesNoDialog;
import uk.co.trentbarton.hugo.interfaces.RefreshInterface;
import uk.co.trentbarton.hugo.tasks.LocationForegroundService;
import uk.co.trentbarton.hugo.tools.Constants;
import uk.co.trentbarton.hugo.R;

public class MapFragment extends Fragment implements OnMapReadyCallback, RefreshInterface, LocationListener{

    protected GoogleMap mMap;
    protected MapView mMapView;
    private final int FRAGMENT_ID = 0;
    protected Location currentLocation = null;
    private int layoutRef;
    private LocationManager mLocationManager;
    private View mView;
    private boolean locationUpdatesSet = false;
    protected static final String LAYOUT_REFERENCE = "layout_ref";
    public static final int STANDARD_ZOOM_LEVEL = 16;
    protected boolean USER_MOVED_MAP = false;
    private Bundle mInstanceState;
    private boolean gettingMap = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutRef = getArguments() != null ? getArguments().getInt(LAYOUT_REFERENCE) : -1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            mView = inflater.inflate(layoutRef, container, false);
            mInstanceState = savedInstanceState;
            setLocationUpdates();
            return mView;
        }catch(Exception e){
            //This shouldn't happen but appears to be happening on live screen only for some reason
            mView = inflater.inflate(R.layout.fragment_live_main, container, false);
            mInstanceState = savedInstanceState;
            setLocationUpdates();
            return mView;
        }

    }

    public void startMapAsync(){
        if(mMap == null && !gettingMap && mView != null){
            gettingMap = true;
            mMapView = mView.findViewById(R.id.main_map);
            mMapView.onCreate(mInstanceState);
            mMapView.getMapAsync(this);
        }
    }

    protected View getBaseView(){
        return mView;
    }

    protected GoogleMap getMap(){
        return mMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Save map to static reference
        mMap = googleMap;
        googleMap.setIndoorEnabled(false);
        enableMapLocationEnabled();

        if (mMapView != null && mMapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);

            View locationCompass = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("5"));
            // and next place it, on bottom right (as Google Maps app)
            layoutParams = (RelativeLayout.LayoutParams) locationCompass.getLayoutParams();
            // position on bottom left
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,28,160);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.setMarginEnd(28);
            }

        }

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {

                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    //User gesture
                    USER_MOVED_MAP = true;
                } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
                    USER_MOVED_MAP = false;
                } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
                    USER_MOVED_MAP = false;
                }
            }
        });

        mMap.setOnCameraIdleListener(() -> cameraFinishedMoving());

        if(currentLocation != null){
            moveToCurrentLocation();
        }

        setLocationUpdates();

    }

    protected void cameraFinishedMoving(){

    }

    private void enableMapLocationEnabled(){

        if(getContext() == null || getActivity() == null){
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CustomImageYesNoDialog dialog = new CustomImageYesNoDialog(getContext())
                    .setTitle("Allow hugo to access location?")
                    .setImage(R.drawable.bus_tracking_icon)
                    .setContentText(getContext().getString(R.string.standard_location_request))
                    .setAcceptButtonListener(() -> {
                        ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },FRAGMENT_ID);
                        return true;
                    });
            dialog.show();
        }else{
            showLocationIcon();
        }
    }

    protected void askForLocationPermission(){

        if(getContext() == null || getActivity() == null){
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },FRAGMENT_ID);
        }else{
            setLocationUpdates();
            showLocationIcon();
        }
    }

    protected boolean haveAccessToLocation(){

        return getContext() != null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    public void setLocationUpdates(){

        if(locationUpdatesSet){
            return;
        }

        try{

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                String provider = mLocationManager.getBestProvider(getCriteria(), true);
                Location lastKnown = mLocationManager.getLastKnownLocation(provider);
                if (lastKnown != null && (System.currentTimeMillis() - (1000 * 60 * 5)) < lastKnown.getTime()) {
                    setCurrentLocation(lastKnown);
                }
                mLocationManager.requestLocationUpdates(provider, 0 /*60 seconds minimum time to update*/, 0 /*minimum distance to send us an update*/, this);
                locationUpdatesSet = true;
            }
        }catch(Exception ignore){}

    }

    protected boolean setCurrentLocation(Location l){

        if(l == null){
            return false;
        }

        if(currentLocation == null){
            currentLocation = l;
            moveToCurrentLocation();
            return true;
        }

        if(currentLocation.distanceTo(l) > 100.0){
            currentLocation = l;
            moveToCurrentLocation();
            return true;
        }

        return false;

    }

    protected void moveToCurrentLocation(){

        if(mMap == null){
            return;
        }

        CameraPosition pos;

        if(currentLocation != null){
            pos = new CameraPosition.Builder()
                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .bearing(currentLocation.getBearing())
                    .zoom(STANDARD_ZOOM_LEVEL)
                    .tilt(35.0f)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        }else{

            pos = new CameraPosition.Builder()
                    .target(new LatLngBounds(Constants.SOUTH_WEST_MAP_CORNER, Constants.NORTH_EAST_MAP_CORNER).getCenter())
                    .bearing(0.0f)
                    .zoom(10.0f)
                    .tilt(50.0f)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        }

    }

    @SuppressWarnings({"MissingPermission"})
    private void showLocationIcon(){
        if(mMap != null && getContext()!= null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }
    }

    private Criteria getCriteria(){

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_COARSE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mLocationManager != null){
            setLocationUpdates();
        }
        if(mMapView != null) mMapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMapView != null) mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mLocationManager != null) mLocationManager.removeUpdates(this);
        if(mMapView != null) mMapView.onStop();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView != null) mMapView.onLowMemory();
    }

    @Override
    public void RefreshUI() {
        showLocationIcon();
        setLocationUpdates();
    }



    @Override
    public void onLocationChanged(Location location) {
        setCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
