package uk.co.trentbarton.hugo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.activities.JourneyResults;
import uk.co.trentbarton.hugo.customadapters.TomTomPlaceAdapter;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpImageItem;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenu;
import uk.co.trentbarton.hugo.customview.SlideUpMenu.SlideUpMenuItem;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.GetPlacesParams;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.JourneyHttpDataParams;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.JourneyParams;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.dialogs.CustomEditTextDialog;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.interfaces.OnDataReceivedListener;
import uk.co.trentbarton.hugo.interfaces.OnFavouritePlaceClickListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.tasks.OnInfoReceivedListener;
import uk.co.trentbarton.hugo.tools.Metrics;

public class JourneyFragment extends MapFragment implements OnFavouritePlaceClickListener, OnInfoReceivedListener, GoogleMap.OnMarkerClickListener, OnDataReceivedListener {

    private EditText fromEditText, toEditText;
    private RelativeLayout loadingScreen;
    private Marker fromMarker, toMarker;
    private Polyline journeyLine;
    private LinearLayout fromLocationHolder, toLocationHolder, resultsListHolder;
    private ImageView switchButton, stateChangeButton, loadingGifHolder, toPlaceTextDelete, fromPlaceTextDelete;
    private Button goButton;
    private SlideUpMenu mMenu;
    private ListView resultsList;
    private TomTomPlaceAdapter mAdapter;
    private TextView mHintText;
    private Database mDatabase;
    private static final String TAG = JourneyFragment.class.getSimpleName();
    private LatLngBounds BOUNDS_LOCAL;
    private boolean USER_TAPPED_PLACE = false;
    private boolean PAUSE_AUTO_POPULATE_LIST = false;
    private boolean IS_MAP_SHOWING = false;
    private Bitmap fromMarkerBitmap, toMarkerBitmap, tempMarkerBitmap;
    private Marker tempMarker;
    private ArrayList<TomTomPlace> mFavourites, mPlaces;
    private boolean IS_TASK_RUNNING = false;
    DataRequestTask mTask;

    public static JourneyFragment newInstance() {
        JourneyFragment fragment = new JourneyFragment();
        Bundle bundle = fragment.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(LAYOUT_REFERENCE, R.layout.fragment_journey_main);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Fragment started");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "Fragemnt attached");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = getBaseView();
        setupViews(view);
        init();
        assignListeners();
        return view;
    }

    private void init() {

        try {
            setLocalBounds();
        } catch (Exception e) {
            Log.e("Act PlaceSearch", "An error Occurred when setting local bounds", e);
            BOUNDS_LOCAL = LatLngBounds.builder()
                    .include(new LatLng(53.464784, -2.219252))
                    .include(new LatLng(52.610950, -0.578948))
                    .build();
        }

        mFavourites = new ArrayList<>();
        mPlaces = new ArrayList<>();
        mDatabase = new Database(getContext());

        mFavourites.addAll(mDatabase.getFavouritePlaces());

        mAdapter = new TomTomPlaceAdapter(getContext(), mPlaces);
        mAdapter.setOnFavouritePlaceClicked(this);

        resultsList.setTextFilterEnabled(true);
        resultsList.setAdapter(mAdapter);

        Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.start_marker);
        float scale = (float) bmp.getWidth() / (float) bmp.getHeight();

        if (currentLocation != null) {

            String name = "Use current location";
            String locality = "";
            double lat = currentLocation.getLatitude();
            double lng = currentLocation.getLongitude();
            mPlaces.add(new TomTomPlace(lat, lng, name, locality));
            mAdapter.notifyDataSetChanged();
        }


        fromMarkerBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getContext().getResources(), R.drawable.start_marker),
                (int) (Metrics.densityPixelsToPixels(40) * scale),
                Metrics.densityPixelsToPixels(40),
                false);

        toMarkerBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getContext().getResources(), R.drawable.end_marker),
                (int) (Metrics.densityPixelsToPixels(40) * scale),
                Metrics.densityPixelsToPixels(40),
                false);

        tempMarkerBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getContext().getResources(), R.drawable.temp_marker),
                (int) (Metrics.densityPixelsToPixels(40) * scale),
                Metrics.densityPixelsToPixels(40),
                false);

    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
    }

    private void setupViews(View view) {

        fromEditText = view.findViewById(R.id.fromPlaceText);
        toEditText = view.findViewById(R.id.toPlaceText);
        toLocationHolder = view.findViewById(R.id.toLocationHolder);
        fromLocationHolder = view.findViewById(R.id.fromLocationHolder);
        stateChangeButton = view.findViewById(R.id.place_enter_switch_state_button);
        switchButton = view.findViewById(R.id.switch_icon);
        resultsList = view.findViewById(R.id.placeSearchResultsList);
        goButton = view.findViewById(R.id.journeyGoButton);
        mMenu = view.findViewById(R.id.fragment_journey_slide_up_menu);
        goButton.setVisibility(View.GONE);
        loadingGifHolder = view.findViewById(R.id.loadingGifHolder);
        loadingScreen = view.findViewById(R.id.loading_screen);
        resultsListHolder = view.findViewById(R.id.placeSearchResultsListHolder);
        mHintText = view.findViewById(R.id.journey_main_list_hint_text);
        toPlaceTextDelete = view.findViewById(R.id.toPlaceTextDelete);
        fromPlaceTextDelete = view.findViewById(R.id.fromPlaceTextDelete);
        resultsList.setVisibility(View.INVISIBLE);

    }

    private void assignListeners() {

        fromEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                switchButton.setVisibility(View.GONE);
                fromPlaceTextDelete.setVisibility(View.VISIBLE);
                mHintText.setVisibility(View.GONE);
                if(IS_MAP_SHOWING){
                    switchStates();
                }
                resultsList.setVisibility(View.VISIBLE);
                fromLocationHolder.setBackgroundResource(R.drawable.selected_background_10dp_corners);
                populateNewPlaces(fromEditText.getText().toString());
            } else {
                fromPlaceTextDelete.setVisibility(View.GONE);
                if(!toEditText.hasFocus()){
                    switchButton.setVisibility(View.VISIBLE);
                    mHintText.setVisibility(View.VISIBLE);
                    resultsList.setVisibility(View.INVISIBLE);
                }
                mHintText.setVisibility(View.GONE);
                clearAllListData();
                fromLocationHolder.setBackgroundResource(R.drawable.light_grey_background_10dp_corners);
            }
        });

        loadingScreen.setOnClickListener(v -> {
            //Blank onclick listener to block taps while this works
        });

        toEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if(IS_MAP_SHOWING){
                switchStates();
            }
            if (hasFocus) {
                toPlaceTextDelete.setVisibility(View.VISIBLE);
                switchButton.setVisibility(View.GONE);
                mHintText.setVisibility(View.GONE);
                resultsList.setVisibility(View.VISIBLE);
                toLocationHolder.setBackgroundResource(R.drawable.selected_background_10dp_corners);
                populateNewPlaces(toEditText.getText().toString());
            } else {
                toPlaceTextDelete.setVisibility(View.GONE);
                if(!fromEditText.hasFocus()){
                    switchButton.setVisibility(View.VISIBLE);
                    mHintText.setVisibility(View.VISIBLE);
                    resultsList.setVisibility(View.INVISIBLE);
                }
                mHintText.setVisibility(View.GONE);
                clearAllListData();
                toLocationHolder.setBackgroundResource(R.drawable.light_grey_background_10dp_corners);
            }
        });

        toPlaceTextDelete.setOnClickListener(v -> {

            if(IS_TASK_RUNNING){
                try{
                    mTask.cancel(true);
                    IS_TASK_RUNNING = false;
                }catch(Exception ignore){

                }
            }

            toEditText.setText("");
            JourneyParams.getInstance().setToPlace(null);
            goButton.setVisibility(View.GONE);
            populateNewPlaces("");
        });


        fromPlaceTextDelete.setOnClickListener(v -> {

            if(IS_TASK_RUNNING){
                try{
                    mTask.cancel(true);
                    IS_TASK_RUNNING = false;
                }catch(Exception ignore){

                }
            }

            fromEditText.setText("");
            JourneyParams.getInstance().setFromPlace(null);
            goButton.setVisibility(View.GONE);
            populateNewPlaces("");
        });


        stateChangeButton.setOnClickListener(v -> {
            switchStates();
        });

        fromEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(fromEditText.hasFocus()) populateNewPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });

        toEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(toEditText.hasFocus()) populateNewPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable text) {

            }
        });

        resultsList.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        resultsList.setOnItemClickListener((parent, view, position, id) -> {
            TomTomPlace place = mAdapter.getItem(position);
            USER_TAPPED_PLACE = true;

            if (place != null) {

                Log.d(TAG, "User tapped on place: " + place.toString());

                place.setOnInfoReceivedListener(JourneyFragment.this);
                //Google logo will return null
                if (fromEditText.hasFocus()) {
                    fromEditText.setText(place.getName());
                    JourneyParams.getInstance().setFromPlace(place);
                    fromEditText.clearFocus();
                    placeFromMarker();
                    hideKeyboard();
                } else if (toEditText.hasFocus()) {
                    toEditText.setText(place.getName());
                    JourneyParams.getInstance().setToPlace(place);
                    toEditText.clearFocus();
                    placeToMarker();
                    hideKeyboard();
                }
            }

            USER_TAPPED_PLACE = false;
            checkLocationsSet();

        });

        resultsListHolder.setOnClickListener(v -> {
            //Do nothing on purpose to consume the click event
        });

        switchButton.setOnClickListener(v -> switchLocations());

        goButton.setOnClickListener(v -> {
            runJourneyRequest();
        });

    }

    private void showLoadingScreen() {
        loadingScreen.setVisibility(View.VISIBLE);
        Glide.with(this).asGif().load(R.raw.tetris).into(loadingGifHolder);
    }

    private void runJourneyRequest() {
        showLoadingScreen();

        JourneyHttpDataParams params = new JourneyHttpDataParams(getContext());
        params.addFromPlace(JourneyParams.getInstance().getFromPlace());
        params.addToPlace(JourneyParams.getInstance().getToPlace());

        DataRequestTask task = new DataRequestTask(params);
        task.setOnTaskCompletedListener(bool -> {
            hideLoading();
            if (bool) {
                //Task worked so get array of results
                if(task.getResponse() == null){
                    Context context = getContext();
                    if(context != null) Toast.makeText(getContext(), "Search returned 0 results", Toast.LENGTH_SHORT).show();
                }else{
                    if(task.getResponse() instanceof  ArrayList){
                        ArrayList<Journey> list = (ArrayList<Journey>) task.getResponse();
                        //Launch the new Activity with this data
                        if(getActivity() != null){
                            GlobalData.getInstance().setJourneyData(list);
                            Intent intent = new Intent(getActivity(), JourneyResults.class);
                            startActivity(intent);
                        }
                    }
                    else{
                        Context context = getContext();
                        if(context != null) Toast.makeText(getContext(), "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Context context = getContext();
                if(context != null) Toast.makeText(getContext(), task.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });
        Context context = getContext();
        if(context != null) task.execute(getContext());
    }

    private void hideLoading() {
        loadingScreen.setVisibility(View.GONE);
    }

    private void placeFromMarker(){

        if(getMap() == null){
            return;
        }

        if(fromMarker != null){
            fromMarker.remove();
        }

        if(JourneyParams.getInstance().getFromPlace() == null || JourneyParams.getInstance().getFromPlace().getPosition() == null){
            return;
        }

        fromMarker = getMap().addMarker(new MarkerOptions()
                .draggable(false)
                .position(JourneyParams.getInstance().getFromPlace().getPosition())
                .title(JourneyParams.getInstance().getFromPlace().getName())
                .icon(BitmapDescriptorFactory.fromBitmap(fromMarkerBitmap)));
        fromMarker.setTag(JourneyParams.getInstance().getFromPlace());

        drawPolyline();
        checkLocationsSet();


    }

    private void placeToMarker(){

        if(getMap() == null){
            return;
        }

        if(toMarker != null){
            toMarker.remove();
        }

        if(JourneyParams.getInstance().getToPlace() == null || JourneyParams.getInstance().getToPlace().getPosition() == null){
            return;
        }


        toMarker = getMap().addMarker(new MarkerOptions()
                .draggable(false)
                .position(JourneyParams.getInstance().getToPlace().getPosition())
                .title(JourneyParams.getInstance().getToPlace().getName())
                .icon(BitmapDescriptorFactory.fromBitmap(toMarkerBitmap)));
        toMarker.setTag(JourneyParams.getInstance().getToPlace());

        drawPolyline();
        checkLocationsSet();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        googleMap.setOnMapLongClickListener(this::showDropPinMenu);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this::showDropPinMenu);

       moveLocationButton(mMapView);

    }

    private void drawPolyline(){

        if(toMarker == null || fromMarker == null){
            return;
        }

        if(journeyLine != null){
            journeyLine.remove();
        }

        List<PatternItem> pattern = new ArrayList<>();
        pattern.add(new Dash(Metrics.densityPixelsToPixels(10)));
        pattern.add(new Gap(Metrics.densityPixelsToPixels(2)));

        journeyLine = getMap().addPolyline(new PolylineOptions()
                .add(toMarker.getPosition())
                .add(fromMarker.getPosition())
                .jointType(JointType.ROUND)
                .pattern(pattern)
                .color(Color.BLACK));

        moveMapToPolyline();

    }

    private void removeTempMarker(){
        if(tempMarker != null){
            tempMarker.remove();
            tempMarker = null;
        }
    }

    private void showDropPinMenu(LatLng position){

        tempMarker = getMap().addMarker(new MarkerOptions()
                .draggable(false)
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(tempMarkerBitmap)));

        ArrayList<SlideUpMenuItem> menuItems = new ArrayList<>();

        SlideUpImageItem startItem = new SlideUpImageItem(getContext(), "Set as start", R.drawable.start_marker);
        startItem.setOnClickListener(v -> {

            String name = "start marker";
            String locality = "";
            double lat = position.latitude;
            double lng = position.longitude;
            TomTomPlace place = new TomTomPlace(lat, lng, name, locality);
            fromEditText.setText(place.getName());
            place.setOnInfoReceivedListener(JourneyFragment.this);
            JourneyParams.getInstance().setFromPlace(place);
            place.getNameFromGoogle();
            placeFromMarker();
            removeTempMarker();
            mMenu.slideDown();
        });

        menuItems.add(startItem);

        SlideUpImageItem endItem = new SlideUpImageItem(getContext(), "Set as end", R.drawable.end_marker);
        endItem.setOnClickListener(v -> {
            String name = "end marker";
            String locality = "";
            double lat = position.latitude;
            double lng = position.longitude;
            TomTomPlace place = new TomTomPlace(lat, lng, name, locality);
            toEditText.setText(place.getName());
            place.setOnInfoReceivedListener(JourneyFragment.this);
            JourneyParams.getInstance().setToPlace(place);
            place.getNameFromGoogle();
            placeToMarker();
            removeTempMarker();
            mMenu.slideDown();
        });

        menuItems.add(endItem);

        SlideUpImageItem saveItem = new SlideUpImageItem(getContext(), "Save as favourite", R.drawable.favourite_star_selected);
        saveItem.setOnClickListener(v -> {

            TomTomPlace place = new TomTomPlace(position.latitude, position.longitude, "favourite", "");
            saveFavouriteFromMapClick(place);
            removeTempMarker();
            mMenu.slideDown();
        });

        mMenu.setOnCloseListener(v -> {
            removeTempMarker();
        });

        menuItems.add(saveItem);
        mMenu.assignItems(menuItems);
        mMenu.slideUp();

    }

    private void saveFavouriteFromMapClick(TomTomPlace place){
        CustomEditTextDialog dialog = new CustomEditTextDialog(getContext());
        dialog.setTitle("Add selected marker location as a favourite place?");
        dialog.setUserInputText("");
        dialog.setAcceptButtonListener(() -> {
            if(dialog.getEnteredText().equalsIgnoreCase("")){
                Toast.makeText(getContext(), "You must provide a name", Toast.LENGTH_LONG).show();
                return false;
            }

            place.setName(dialog.getEnteredText());
            place.setFavourite(true);
            mFavourites.add(place);
            mDatabase.saveFavouritePlace(place, false);
            Toast.makeText(getContext(), "Place saved as a favourite", Toast.LENGTH_SHORT).show();
            refreshPlaces();
            return true;
        });
        dialog.show();
    }

    private void switchStates() {

        if(IS_MAP_SHOWING){
            stateChangeButton.setImageResource(R.drawable.map_icon);
            resultsListHolder.animate().setDuration(200).translationX(0).start();
        }else{
            clearAllFocuses();
            hideKeyboard();
            stateChangeButton.setImageResource(R.drawable.list_icon);
            resultsListHolder.animate().setDuration(200).translationX(-resultsList.getMeasuredWidth()).start();
        }

        IS_MAP_SHOWING = !IS_MAP_SHOWING;

    }

    private void clearAllFocuses() {
        if(fromEditText.hasFocus()){
            fromEditText.clearFocus();
        }
        if(toEditText.hasFocus()){
            toEditText.clearFocus();
        }

        clearAllListData();
    }

    private void switchLocations(){

        PAUSE_AUTO_POPULATE_LIST = true;
        TomTomPlace from = JourneyParams.getInstance().getFromPlace();
        TomTomPlace to = JourneyParams.getInstance().getToPlace();
        String fromName = fromEditText.getText().toString();
        String toName = toEditText.getText().toString();

        JourneyParams.getInstance().setToPlace(from);
        JourneyParams.getInstance().setFromPlace(to);
        fromEditText.setText(toName);
        toEditText.setText(fromName);

        placeFromMarker();
        placeToMarker();

        PAUSE_AUTO_POPULATE_LIST = false;
    }

    private void clearAllListData(){

        mPlaces.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void hideKeyboard(){

        Log.d(TAG, "hideKeyboard called");

        try{
            Activity activity = getActivity();

            if(activity == null){
                throw new Exception("Couldn't get a reference to Activity");
            }

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = fromEditText;
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                throw new Exception("No view's have been drawn yet!");
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }catch(Exception ignore){
            Log.e(TAG, "Couldn't get reference to activity line 636");
        }
    }

    @Override
    protected void moveToCurrentLocation() {
        super.moveToCurrentLocation();

        if(currentLocation != null){
            if(JourneyParams.getInstance().getFromPlace() == null){

                TomTomPlace place = new TomTomPlace(currentLocation.getLatitude(), currentLocation.getLongitude(), "current location", "");
                place.setOnInfoReceivedListener(JourneyFragment.this);
                JourneyParams.getInstance().setFromPlace(place);
                place.getNameFromGoogle();
                placeFromMarker();
            }
        }

    }

    public void moveLocationButton(View mapView) {
        try {
            assert mapView != null; // skip this if the mapView has not been set yet

            Log.d(TAG, "moveLocationButton()");

            // View view = mapView.findViewWithTag("GoogleMapCompass");
            View view = mapView.findViewWithTag("GoogleMapMyLocationButton");

            // move the compass button to the right side, centered
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.setMargins(15,15,15,15);
            view.setLayoutParams(layoutParams);

            view = mapView.findViewWithTag("GoogleMapCompass");
            view.setVisibility(View.GONE);

        } catch (Exception ex) {
            Log.e(TAG, "moveLocationButton() - failed: " + ex.getLocalizedMessage());
        }
    }

    private void checkLocationsSet(){

        try{
            if(JourneyParams.getInstance().areBothLocationsSet()){
                this.goButton.setVisibility(View.VISIBLE);
            }else{
                this.goButton.setVisibility(View.GONE);
            }
        }catch(Exception ignore){}

    }

    private void populateNewPlaces(String toSearchOn){

        if(PAUSE_AUTO_POPULATE_LIST){return;}

        if(toSearchOn.length() < 3 || USER_TAPPED_PLACE){

            if(IS_TASK_RUNNING){
                try{
                    mTask.cancel(true);
                    IS_TASK_RUNNING = false;
                }catch(Exception ignore){

                }
            }

            clearAllListData();
            showFavourites();
            return;
        }

        ArrayList<TomTomPlace> placeList;

        if(currentLocation != null){
            placeList = mDatabase.getPlaceReferences(toSearchOn,new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }else{
            placeList = new ArrayList<>();
        }

        if(placeList == null || placeList.isEmpty()){
            Log.d(TAG, "Database returned 0 results");

            if(IS_TASK_RUNNING){
                try{
                    mTask.cancel(true);
                    IS_TASK_RUNNING = false;
                }catch(Exception ignore){

                }
            }

            GetPlacesParams params = new GetPlacesParams(getContext());
            if(currentLocation != null){
                params.addCurrentLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            params.addStringSearch(toSearchOn);
            mTask = new DataRequestTask(params);
            mTask.setOnTaskCompletedListener(worked -> {
                if(worked){
                    mPlaces.clear();

                    if((mTask.getResponse() == null || !((ArrayList<TomTomPlace>)mTask.getResponse()).isEmpty()) && currentLocation != null){
                        mDatabase.addPlaceReferences(toSearchOn, (ArrayList<TomTomPlace>)mTask.getResponse(), new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    }

                    if(mTask.getResponse() != null){
                        mPlaces.addAll((ArrayList<TomTomPlace>)mTask.getResponse());
                    }else{
                        mPlaces.clear();
                    }

                    mAdapter.notifyDataSetChanged();
                    IS_TASK_RUNNING = false;
                }
            });
            IS_TASK_RUNNING = true;
            mTask.execute(getContext());

        }else{

            if(IS_TASK_RUNNING){
                try{
                    mTask.cancel(true);
                    IS_TASK_RUNNING = false;
                }catch(Exception ignore){

                }
            }

            Log.d(TAG, "Database returned " + placeList.size() + " results");
            mPlaces.clear();
            mPlaces.addAll(placeList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showFavourites() {

        if (currentLocation != null) {

            String name = "Use current location";
            String locality = "";
            double lat = currentLocation.getLatitude();
            double lng = currentLocation.getLongitude();
            mPlaces.add(new TomTomPlace(lat, lng, name, locality));
            mAdapter.notifyDataSetChanged();
        }

        mPlaces.addAll(mFavourites);
        mAdapter.notifyDataSetChanged();


    }

    public void moveMapToPolyline(){

        if(getMap() == null || journeyLine == null){
            return;
        }

        //Try and get any selectedStops first, then use the current location if available, if not fall back to East Midland hardcoded
        CameraUpdate cu;

        if(getContext() == null){
            return;
        }

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;

        //There are a number of stops that the user has selected so pan to these as a group
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : journeyLine.getPoints()) {
            builder.include(position);
        }

        LatLngBounds bounds = builder.build();
        int padding = Metrics.densityPixelsToPixels(80) ; // offset from edges of the map in pixels
        try{
            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            getMap().moveCamera(cu);
        }catch(Exception ignore){
            cu = CameraUpdateFactory.newLatLngBounds(bounds, deviceWidth, deviceHeight, padding);
            getMap().moveCamera(cu);

        }
    }

    private void setLocalBounds() {

        if (currentLocation != null) {
            double radiusDegrees = 0.02;
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            LatLng northEast = new LatLng(latitude + radiusDegrees, longitude + radiusDegrees);
            LatLng southWest = new LatLng(latitude - radiusDegrees, longitude - radiusDegrees);
            BOUNDS_LOCAL = LatLngBounds.builder()
                    .include(northEast)
                    .include(southWest)
                    .build();
        } else {
            BOUNDS_LOCAL = LatLngBounds.builder()
                    .include(new LatLng(53.464784, -2.219252))
                    .include(new LatLng(52.610950, -0.578948))
                    .build();
        }

    }

    @Override
    public void onResume() {
        Log.d("JourneyFrag","GMap Resumed");
        refreshPlaces();
        super.onResume();
    }

    @Override
    protected boolean setCurrentLocation(Location l) {
        return super.setCurrentLocation(l);
    }

    @Override
    public void onPause() {

        Log.d(TAG,"GMap paused");
        super.onPause();
    }

    public boolean saveFavouritePlace(TomTomPlace place){

        if(place.getName().equalsIgnoreCase("")){
            return false;
        }

        Database db = new Database(getContext());
        return db.saveFavouritePlace(place, false);

    }

    @Override
    public void onInfoReceived(TomTomPlace place) {
        if(place != null){
            Log.d(TAG, "Info received: " + place.toString());
            refreshPlaces();
        }
    }

    private void refreshPlaces() {
        refreshToPlace();
        refreshFromPlace();
        checkLocationsSet();
    }

    private void refreshToPlace(){

        if(JourneyParams.getInstance().getToPlace() == null){
            return;
        }

        if(JourneyParams.getInstance().getToPlace().getPosition() != null){
            placeToMarker();
        }

        if(!JourneyParams.getInstance().getToPlace().getName().equalsIgnoreCase("")){
            if(toEditText != null) toEditText.setText(JourneyParams.getInstance().getToPlace().getName());
        }

    }

    private void refreshFromPlace(){

        if(JourneyParams.getInstance().getFromPlace() == null){
            return;
        }

        if(JourneyParams.getInstance().getFromPlace().getPosition() != null){
            placeFromMarker();
        }

        if(!JourneyParams.getInstance().getFromPlace().getName().equalsIgnoreCase("")){
            if(fromEditText != null) fromEditText.setText(JourneyParams.getInstance().getFromPlace().getName());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(marker.getTag() instanceof TomTomPlace){
            saveFavouriteFromMapClick((TomTomPlace) marker.getTag());
        }

        return true;
    }

    @Override
    public void onDataRecieved(Object object) {

        try{

            ArrayList<TomTomPlace> places = (ArrayList<TomTomPlace>) object;
            mPlaces.clear();
            mPlaces.addAll(places);
            mAdapter.notifyDataSetChanged();

        }catch(ClassCastException exception){

            mPlaces.clear();
            mAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onMakeFavouriteClicked(TomTomPlace place) {

        CustomEditTextDialog dialog = new CustomEditTextDialog(getContext());
        dialog.setTitle(String.format("Add %s as a favourite place?",place.getName()));
        dialog.setUserInputText(place.getName());
        dialog.setAcceptButtonListener(() -> {
            place.setName(dialog.getEnteredText());
            if(saveFavouritePlace(place)){
                PAUSE_AUTO_POPULATE_LIST = true;
                if(toEditText.hasFocus()){
                    JourneyParams.getInstance().setToPlace(place);
                    toEditText.clearFocus();
                    hideKeyboard();
                    toEditText.setText(place.getName());
                }else if(fromEditText.hasFocus()){
                    fromEditText.setText(place.getName());
                    JourneyParams.getInstance().setFromPlace(place);
                    fromEditText.clearFocus();
                    hideKeyboard();
                }

                PAUSE_AUTO_POPULATE_LIST = false;
                place.setFavourite(true);
                mFavourites.add(place);
                refreshPlaces();
                Toast.makeText(getContext(), "Place saved as a favourite", Toast.LENGTH_SHORT).show();
                return true;
            }else{
                Toast.makeText(getContext(), "An error occurred while saving", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        dialog.show();

    }

    @Override
    public void onRemoveFavouriteClicked(TomTomPlace place) {

        CustomYesNoDialog dialog = new CustomYesNoDialog(getContext());
        dialog.setTitle("Really delete?");
        dialog.setContentText(String.format("Remove %s as a favourite place?",place.getName()));
        dialog.setAcceptButtonListener(() -> {
            if(mDatabase.deleteFavouritePlace(place)){
                Toast.makeText(getContext(), "Place removed from favourites", Toast.LENGTH_SHORT).show();
                mFavourites.remove(place);
                mPlaces.remove(place);
                mAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getContext(), "An error occurred while trying to remove from favourites", Toast.LENGTH_LONG).show();
            }
            return true;
        });
        dialog.show();



    }
}
