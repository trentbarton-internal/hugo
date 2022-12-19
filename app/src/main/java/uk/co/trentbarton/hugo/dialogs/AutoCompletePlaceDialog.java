package uk.co.trentbarton.hugo.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.customadapters.TomTomAutoCompleteDialogAdapter;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.GetPlacesParams;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.interfaces.OnTomTomPlaceSelectedListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;

public class AutoCompletePlaceDialog extends Dialog {

    private final String TAG = AutoCompletePlaceDialog.this.getClass().getSimpleName();
    private EditText userTextEntry;
    private ListView autoCompleteList;
    private Button cancelButton;
    private OnDialogClickListener mCancelButtonListener;
    private ArrayList<TomTomPlace> mPlaces;
    private TomTomAutoCompleteDialogAdapter mAdapter;
    private OnTomTomPlaceSelectedListener mPlaceSelectedListener;
    private LatLng mCurrentLocation;
    private ProgressBar mProgress;
    private DataRequestTask mTask;
    private boolean IS_TASK_RUNNING = false;

    public AutoCompletePlaceDialog(@NonNull Context context) {
        super(context);
    }

    public AutoCompletePlaceDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AutoCompletePlaceDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_autocomplete);

        mPlaces = new ArrayList<>();
        mAdapter = new TomTomAutoCompleteDialogAdapter(getContext(), mPlaces);

        //Set up all the views
        cancelButton = findViewById(R.id.dialog_accept_button);
        userTextEntry = findViewById(R.id.dialog_user_input_text);
        autoCompleteList = findViewById(R.id.dialog_autocomplete_places);
        mProgress = findViewById(R.id.dialog_progress_autocomplete);

        autoCompleteList.setAdapter(mAdapter);

        setListeners();

        userTextEntry.requestFocus();
    }

    private void getCurrentLocation() {

        if(mCurrentLocation != null){
            return;
        }

        try{

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

            fusedLocationClient.getLastLocation().addOnSuccessListener(getOwnerActivity(), location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
            });
        }catch(Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private void setListeners() {

        cancelButton.setOnClickListener(v -> {
            if(mCancelButtonListener != null){
                if(mCancelButtonListener.onClick()){
                    this.dismiss();
                }
            }else{
                this.dismiss();
            }
        });

        autoCompleteList.setOnItemClickListener((parent, view, position, id) -> {
            if(mPlaceSelectedListener != null){
                mPlaceSelectedListener.onSelected(mAdapter.getItem(position));
            }
            AutoCompletePlaceDialog.this.dismiss();
        });

        userTextEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                getCurrentLocation();

                if(s.length() > 2){
                    //Run the procedure to get the the new places

                    if(IS_TASK_RUNNING){
                        try{
                            mTask.cancel(true);
                        }catch(Exception ignore){

                        }

                    }

                    showSpinner();

                    GetPlacesParams mParams = new GetPlacesParams(getContext());
                    mParams.addStringSearch(s.toString());
                    mParams.addCurrentLocation(mCurrentLocation);
                    mTask = new DataRequestTask(mParams);
                    mTask.setOnTaskCompletedListener(successful -> {

                        IS_TASK_RUNNING = false;

                        try{
                            if(successful){
                                mPlaces.clear();
                                mPlaces.addAll((ArrayList<TomTomPlace>)mTask.getResponse());
                                mAdapter.notifyDataSetChanged();
                                hideSpinner();
                            }
                        }catch(Exception ignore){

                        }
                    });

                    mTask.execute(getContext());
                    IS_TASK_RUNNING = true;

                }else{
                    //Clear the list of places
                    mPlaces.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void hideSpinner() {

        mProgress.setVisibility(View.GONE);
        autoCompleteList.setVisibility(View.VISIBLE);

    }

    private void showSpinner(){

        mProgress.setVisibility(View.VISIBLE);
        autoCompleteList.setVisibility(View.INVISIBLE);

    }

    public void setCancelButtonListener(OnDialogClickListener listener) {
        this.mCancelButtonListener = listener;
    }

    public void setOnSelectedListener(OnTomTomPlaceSelectedListener listener){
        mPlaceSelectedListener = listener;
    }
}
