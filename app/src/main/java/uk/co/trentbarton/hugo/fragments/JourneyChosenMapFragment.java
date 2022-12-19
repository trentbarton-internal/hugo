package uk.co.trentbarton.hugo.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.FinishJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.HopOffStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.JourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.RealtimeJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.ScheduledJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.WalkingJourneyStep;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.fragments.journeychosenmapitems.FinishJourneyFragment;
import uk.co.trentbarton.hugo.fragments.journeychosenmapitems.HopOffJourneyFragment;
import uk.co.trentbarton.hugo.fragments.journeychosenmapitems.RealtimeJourneyFragment;
import uk.co.trentbarton.hugo.fragments.journeychosenmapitems.ScheduledJourneyFragment;
import uk.co.trentbarton.hugo.fragments.journeychosenmapitems.WalkingJourneyFragment;
import uk.co.trentbarton.hugo.tools.Constants;
import uk.co.trentbarton.hugo.tools.Metrics;

import static uk.co.trentbarton.hugo.fragments.MapFragment.STANDARD_ZOOM_LEVEL;

public class JourneyChosenMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = JourneyChosenMapFragment.class.getSimpleName();
    private int index = -1;
    private ViewPager mViewPager;
    private MapView mMapView;
    private GoogleMap mMap;
    private Journey mJourney;
    private ScreenSlidePagerAdapter mAdapter;
    private Button backToListButton;
    private Polyline mPolyLine;
    private List<Marker> mMarkerList;
    private Bitmap mStartIcon, mEndIcon, mBusStopIcon, mFinishIcon;

    public static JourneyChosenMapFragment newInstance(int index) {

        Bundle args = new Bundle();
        args.putInt("index", index);

        JourneyChosenMapFragment fragment = new JourneyChosenMapFragment();
        fragment.setArguments(args);
        fragment.index = index;
        return fragment;
    }

    private void readBundle(Bundle bundle){
        if (bundle != null) {
            index = bundle.getInt("index");
        }
    }

    public JourneyChosenMapFragment(){
        mMarkerList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey_map_details, container, false);
        readBundle(getArguments());

        if(index != -1){
            try{
                mJourney = GlobalData.getInstance().getJourneyData().get(index);
            }catch(Exception e){

            }
        }

        mMapView = view.findViewById(R.id.fragment_journey_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mViewPager = view.findViewById(R.id.fragment_journey_map_viewpager);
        mAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setClipToPadding(false);
        mViewPager.setPadding(Metrics.densityPixelsToPixels(25), 0, Metrics.densityPixelsToPixels(25), 0);
        backToListButton = view.findViewById(R.id.fragment_journey_map_back_to_list_button);

        backToListButton.setOnClickListener(v -> {
            goBackToList();
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                moveMap(mJourney.getSteps().get(position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        return view;
    }

    private void goBackToList() {

        JourneyChosenFragment fragment = ((JourneyChosenFragment) getParentFragment());
        if(fragment != null){
            fragment.closeMap();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setIndoorEnabled(false);
    }

    public void moveMap(JourneyStep step){

        if(step.getPolyLine() != null && !step.getPolyLine().equalsIgnoreCase("")){
            drawPostionOnToMap(step);
        }


    }

    private BitmapDescriptor getStartStopIcon(){

        if(mStartIcon == null){

            Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.start_marker);
            float scale = (float) bmp.getWidth() / (float) bmp.getHeight();

            mStartIcon = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getContext().getResources(), R.drawable.start_marker),
                    (int) (Metrics.densityPixelsToPixels(40) * scale),
                    Metrics.densityPixelsToPixels(40),
                    false);
        }

        if(mStartIcon !=  null){
            return BitmapDescriptorFactory.fromBitmap(mStartIcon);
        }else{
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        }

    }

    private BitmapDescriptor getBusStopIcon(){

        if(mBusStopIcon == null){
            mBusStopIcon = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bus_stop_icon),
                    Metrics.densityPixelsToPixels(40),
                    Metrics.densityPixelsToPixels(40),
                    false);
        }

        if(mBusStopIcon !=  null){
            return BitmapDescriptorFactory.fromBitmap(mBusStopIcon);
        }else{
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }

    }

    private BitmapDescriptor getEndStopIcon(){

        if(mEndIcon == null){

            Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.end_marker);
            float scale = (float) bmp.getWidth() / (float) bmp.getHeight();

            mEndIcon = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getContext().getResources(), R.drawable.end_marker),
                    (int) (Metrics.densityPixelsToPixels(40) * scale),
                    Metrics.densityPixelsToPixels(40),
                    false);
        }

        if(mEndIcon !=  null){
            return BitmapDescriptorFactory.fromBitmap(mEndIcon);
        }else{
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }

    }

    private BitmapDescriptor getFinishIcon() {
        if(mFinishIcon == null){
            mFinishIcon = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getContext().getResources(), R.drawable.finish_journey_icon),
                    Metrics.densityPixelsToPixels(40),
                    Metrics.densityPixelsToPixels(40),
                    false);
        }

        if(mFinishIcon !=  null){
            return BitmapDescriptorFactory.fromBitmap(mFinishIcon);
        }else{
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }
    }


    private void drawPostionOnToMap(JourneyStep step) {

        clearMap();

        if(step instanceof WalkingJourneyStep || step instanceof RealtimeJourneyStep || step instanceof ScheduledJourneyStep){
            if(mMap != null){
                List<LatLng> points = PolyUtil.decode(step.getPolyLine());
                mPolyLine = mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .color(Color.DKGRAY));

                mMarkerList.add(
                    mMap.addMarker(new MarkerOptions()
                        .draggable(false)
                        .position(points.get(0))
                        .icon(getStartStopIcon()))
                );

                mMarkerList.add(
                    mMap.addMarker(new MarkerOptions()
                            .draggable(false)
                            .position(points.get(points.size() - 1))
                            .icon(getEndStopIcon()))
                );

                moveMapToBestLocation();
            }
        }else{
            if(step instanceof FinishJourneyStep){

                mMarkerList.add(
                        mMap.addMarker(new MarkerOptions()
                                .draggable(false)
                                .position(step.getStartPosition())
                                .icon(getFinishIcon()))
                );

            }else{
                //Hop off step
                mMarkerList.add(
                        mMap.addMarker(new MarkerOptions()
                                .draggable(false)
                                .position(step.getStartPosition())
                                .icon(getBusStopIcon()))
                );
            }

            moveMapToBestLocation();
        }
    }


    public void moveMapToBestLocation(){

        if(mMap == null){
            return;
        }

        if(mMarkerList == null || mMarkerList.size() <= 0){
            //Zoom to fixed position above the east midlands
            CameraPosition pos = new CameraPosition.Builder()
                    .target(new LatLngBounds(Constants.SOUTH_WEST_MAP_CORNER, Constants.NORTH_EAST_MAP_CORNER).getCenter())
                    .bearing(0.0f)
                    .zoom(10.0f)
                    .tilt(50.0f)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            return;
        }

        if(mMarkerList.size() == 1){
            //Zoom to this single location then
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mMarkerList.get(0).getPosition())      // Sets the center of the map to the clicked marker
                    .zoom(STANDARD_ZOOM_LEVEL)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to north
                    .tilt(0)
                    .build();
            // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            return;
        }

        //Try and get any selectedStops first, then use the current location if available, if not fall back to East Midland hardcoded
        CameraUpdate cu;

        //There are multiple markers so zoom to fit
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;

        //There are a number of stops that the user has selected so pan to these as a group
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker: mMarkerList) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        int padding = Metrics.densityPixelsToPixels(80) ; // offset from edges of the map in pixels
        try{
            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
            return;
        }catch(Exception ignore){
            cu = CameraUpdateFactory.newLatLngBounds(bounds, (int)(deviceWidth * 0.8), (int)(deviceHeight * 0.8), padding);
            mMap.moveCamera(cu);
            return;
        }
    }

    private void clearMap() {

        if(mPolyLine != null){
            mPolyLine.remove();
            mPolyLine = null;
        }

        if(mMarkerList != null){

            if(mMarkerList.size() > 0){
                for(Marker marker : mMarkerList){
                    marker.remove();
                }

                mMarkerList.clear();

            }
        }
    }

    public void loadChild(int index){
        mViewPager.setCurrentItem(index - 1);
        moveMap(mJourney.getSteps().get(index));
    }

    @Override
    public void onResume() {
        super.onResume();

        if(GlobalData.getInstance().getJourneyData() == null || GlobalData.getInstance().getJourneyData().size() == 0){
            getActivity().finish();
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
        if(mMapView != null) mMapView.onStop();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView != null) mMapView.onLowMemory();
    }



    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            try{
                int newPosition = position + 1; // this makes the first index impossible
                JourneyStep step = mJourney.getSteps().get(newPosition);

                if(step instanceof WalkingJourneyStep){
                    WalkingJourneyFragment fragment = WalkingJourneyFragment.newInstance(index, newPosition);
                    return fragment;
                }else if(step instanceof RealtimeJourneyStep) {
                    RealtimeJourneyFragment fragment = RealtimeJourneyFragment.newInstance(index, newPosition);
                    return fragment;
                }else if(step instanceof ScheduledJourneyStep) {
                    ScheduledJourneyFragment fragment = ScheduledJourneyFragment.newInstance(index, newPosition);
                    return fragment;
                }else if(step instanceof HopOffStep){
                    HopOffJourneyFragment fragment = HopOffJourneyFragment.newInstance(index, newPosition);
                    return fragment;
                }else if(step instanceof FinishJourneyStep){
                    FinishJourneyFragment fragment = FinishJourneyFragment.newInstance(index, newPosition);
                    return fragment;
                }else{
                    throw new RuntimeException("Invalid type of fragment identified at journeyIndex " + index + " and step position " + newPosition);
                }
            }catch(Exception e){
                try{
                    Toast.makeText(getContext(), "Oops! something has gone wrong, let's try that again",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    return null;
                }catch(Exception ex){
                    //ignore
                    return null;
                }
            }
        }

        @Override
        public int getCount() {

            if(mJourney == null){
                return 0;
            }else{
                return (mJourney.getSteps().size() - 1);
            }

        }
    }
}
