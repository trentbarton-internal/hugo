package uk.co.trentbarton.hugo.dataholders.JourneyItems;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.dialogs.CustomImageYesNoDialog;
import uk.co.trentbarton.hugo.dialogs.CustomTextDialog;
import uk.co.trentbarton.hugo.dialogs.CustomYesNoDialog;
import uk.co.trentbarton.hugo.tasks.LocationForegroundService;
import uk.co.trentbarton.hugo.tasks.OnDialogClickListener;
import uk.co.trentbarton.hugo.tools.Constants;

public class HopOffStep extends JourneyStep {

    TextView tellMeWhenButton;

    public HopOffStep(JSONObject object) throws Exception{
        super(object);
        setInstructions("Hop off at " + object.getString("alighting_stop"));
        this.setStartPosition(this.getEndPosition());
    }

    public LatLng getStopLocation(){
        return this.getEndPosition();
    }

    @Override
    public View getView(Context context) {

        mContext = context;

        if (mView == null) {
            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.journey_step_hop_off, null);
            tellMeWhenButton = mView.findViewById(R.id.tellMeWhenToGetOffButton);
            init();
        }

        return mView;
    }

    private void init() {

        TextView instructionHolder = mView.findViewById(R.id.instructions_holder);
        instructionHolder.setText(this.getInstructions());
        RelativeLayout holder = mView.findViewById(R.id.holder);
        tellMeWhenButton.setOnClickListener(v -> {
            askAboutSettingAlarm();
        });
        holder.setOnClickListener(v -> {
           this.itemClicked();
        });

    }

    private void askAboutSettingAlarm() {

        PackageManager packageManager = mContext.getPackageManager();
        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        if(!hasGPS){
            CustomTextDialog dialog = new CustomTextDialog(mContext)
                .setTitle("Sorry....")
                .setContentText("Your device doesn't have GPS so we're unable to track your location");
            dialog.show();
            return;
        }

        CustomImageYesNoDialog dialog = new CustomImageYesNoDialog(mContext)
            .setTitle("Set an alarm?")
            .setImage(R.drawable.bus_tracking_icon)
            .setContentText(mContext.getString(R.string.setting_get_off_alarm_text))
            .setAcceptButtonListener(() -> {
                Intent intent = new Intent(mContext, LocationForegroundService.class);
                intent.putExtra("polyLine", getPolyLine());
                intent.putExtra("lat", getEndPosition().latitude);
                intent.putExtra("lng", getEndPosition().longitude);
                if (!LocationForegroundService.IS_SERVICE_RUNNING) {

                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },0);
                    }else{
                        intent.setAction(LocationForegroundService.START_FOREGROUND_ACTION);
                        mContext.startService(intent);
                        HugoPreferences.setAlightingAlarmStopName(mContext, getInstructions());
                    }

                } else {
                    Toast.makeText(mContext, "There is already an alarm running, you can't set multiple alarms at this time", Toast.LENGTH_LONG).show();
                }

                return true;
            });
        dialog.show();

    }
}
