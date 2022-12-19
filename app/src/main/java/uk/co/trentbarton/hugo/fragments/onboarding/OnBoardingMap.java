package uk.co.trentbarton.hugo.fragments.onboarding;

import android.content.Context;
import androidx.fragment.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.OnBoardingActivity;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.dialogs.CustomEditTextDialog;
import uk.co.trentbarton.hugo.tools.Metrics;

import static uk.co.trentbarton.hugo.fragments.MapFragment.STANDARD_ZOOM_LEVEL;

public class OnBoardingMap extends Fragment implements OnMapReadyCallback {

    private TomTomPlace mPlace;
    private Database mDatabase;
    private GoogleMap mMap;
    private MapView mMapView;
    public LatLng mCurrentCentreLocation;
    private final int MIN_MOVE_DISTANCE = 500;
    private DrawLocalStopTask mLocalStopTask;
    ArrayList<Marker> mPlacedMarkers;
    private Bitmap mNormalStopIcon;
    private RelativeLayout mStopDialog;
    private LinearLayout mHintText;
    private ImageView mNextButton, mStopFavouriteIcon;
    private TextView mStopNameText;
    private Stop mSelectedStop;
    private boolean MOVING_MANUALLY = true;
    private Context mContext;

    public OnBoardingMap(){
        mPlacedMarkers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_on_boarding_map, container, false);
        mDatabase = new Database(getActivity());
        mMapView = view.findViewById(R.id.onBoardingMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mStopDialog = view.findViewById(R.id.onBoardingMapStopMarkerDialog);
        mHintText = view.findViewById(R.id.onBoardingMapInstructionsView);
        mNextButton = view.findViewById(R.id.onBoardingNextArrow);
        mStopFavouriteIcon = view.findViewById(R.id.onBoardingStopFavouriteIcon);
        mStopNameText = view.findViewById(R.id.onBoardingStopNameText);

        mStopFavouriteIcon.setOnClickListener(v -> saveStop());

        mNextButton.setOnClickListener(v -> {
            ((OnBoardingActivity)getActivity()).changeFragment(OnBoardingActivity.OnBoardingFragmentType.END);
        });

        mContext = getActivity();

        return view;

    }

    private void saveStop() {

        CustomEditTextDialog dialog = new CustomEditTextDialog(mContext);
        dialog.setTitle("Feel free to change the name of (" + mSelectedStop.getStopName() + ") to a name more personal to you")
                .setUserInputText("")
                .setHintText(mSelectedStop.getStopName())
                .setAcceptButtonListener(() -> {
                    String newName = dialog.getEnteredText();
                    if(dialog.getEnteredText().isEmpty()){
                        newName = mSelectedStop.getStopName();
                    }
                    mSelectedStop.setOverrideName(newName);
                    mDatabase.saveFavouriteStop(mSelectedStop, false);
                    mStopFavouriteIcon.setImageResource(R.drawable.favourite_star_selected);
                    Toast.makeText(mContext, "Saved as a favourite", Toast.LENGTH_SHORT).show();
                    mNextButton.setVisibility(View.VISIBLE);
                    return true;
                })
                .show();
    }

    public void setPlace(TomTomPlace place){

        if(place == null){
            if(mContext != null){
                Toast.makeText(mContext,"Sorry something has gone wrong with this process, we've defaulted to trentbarton HQ", Toast.LENGTH_LONG).show();
            }
            mPlace = new TomTomPlace(-1.339628,53.018082,"trentbarton HQ","Heanor");
        }else{
            mPlace = place;
        }

        mCurrentCentreLocation = new LatLng(mPlace.getLat(), mPlace.getLng());
        moveToLocation();
        mLocalStopTask = new DrawLocalStopTask();
        mLocalStopTask.execute(mCurrentCentreLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setIndoorEnabled(false);
        mMap.setOnCameraMoveListener(() -> {

            if(MOVING_MANUALLY){
                mHintText.setVisibility(View.VISIBLE);
                mStopDialog.setVisibility(View.GONE);
            }

            if(calculateDistance(mCurrentCentreLocation,mMap.getCameraPosition().target) > MIN_MOVE_DISTANCE){
                //we have moved more than 500m so run the task to populate stops
                mCurrentCentreLocation = mMap.getCameraPosition().target;
                mLocalStopTask.cancel(true);
                mLocalStopTask = new DrawLocalStopTask();
                mLocalStopTask.execute(mMap.getCameraPosition().target);
            }
        });

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                //User gesture
                MOVING_MANUALLY = true;
            } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
                MOVING_MANUALLY = false;
            } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
                MOVING_MANUALLY = false;
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            //Show a pop-up box with the star to save
            panToMarker(marker.getPosition());
            Stop stop = (Stop) marker.getTag();
            mSelectedStop = stop;
            mStopNameText.setText(stop.getOverrideName());
            if(mDatabase.isStopFavourite(stop)){
                mStopFavouriteIcon.setImageResource(R.drawable.favourite_star_selected);
            }else{
                mStopFavouriteIcon.setImageResource(R.drawable.favourite_star_unselected);
            }
            mStopDialog.setVisibility(View.VISIBLE);
            mHintText.setVisibility(View.GONE);
            return true;
        });


        moveToLocation();

        if(mCurrentCentreLocation != null){
            mLocalStopTask = new DrawLocalStopTask();
            mLocalStopTask.execute(mCurrentCentreLocation);
        }
    }

    private void panToMarker(LatLng location){

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to the clicked marker
                .zoom(STANDARD_ZOOM_LEVEL)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(0)
                .build();
        // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        double center = mMap.getCameraPosition().target.latitude;
        double southMap = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;
        double newLat = ((center-southMap) * 0.2) + center;

        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(newLat, location.longitude))      // Sets the center of the map to the clicked marker
                .zoom(STANDARD_ZOOM_LEVEL)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(0)
                .build();
        // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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

    private void moveToLocation(){

        if(mMap == null){
            return;
        }

        CameraPosition pos;

        if(mPlace != null){
            pos = new CameraPosition.Builder()
                    .target(new LatLng(mPlace.getLat(), mPlace.getLng()))
                    .zoom(STANDARD_ZOOM_LEVEL)
                    .tilt(35.0f)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        if(mMapView != null) mMapView.onStop();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView != null) mMapView.onLowMemory();
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

            return mDatabase.getAllStopsInArea(centrePosition);
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops) {

            if(stops == null || mMap == null){
                return;
            }

            for(Marker marker : mPlacedMarkers){
                marker.remove();
            }
            mPlacedMarkers.clear();

            for (Stop stop : stops){

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .draggable(false)
                        .position(stop.getPosition())
                        .title(stop.getStopName())
                        .icon(getNormalStopIcon()));
                marker.setTag(stop);
                mPlacedMarkers.add(marker);

            }
        }
    }
}
