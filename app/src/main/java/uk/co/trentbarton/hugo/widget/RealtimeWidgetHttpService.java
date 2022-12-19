package uk.co.trentbarton.hugo.widget;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.dataholders.HttpDataParams.RealTimeFullParams;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RealtimeWidgetHttpService extends JobService {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job Started");
        String atcoCode = jobParameters.getExtras().getString("atcoCode");

        RealTimeFullParams mParams = new RealTimeFullParams(false, this);
        mParams.addAtcoCode(atcoCode);

        if(!isNetworkAvailable()){
            Intent intent = new Intent(this, RealtimeWidgetProvider.class);
            intent.setAction(RealtimeWidgetProvider.ACTION_NO_NETWORK);
            sendBroadcast(intent);
            return false;
        }else{
            DataRequestTask task = new DataRequestTask(mParams);
            task.setOnTaskCompletedListener(bool -> {
                try{
                    if(task.getResponse() instanceof ArrayList){
                        HugoPreferences.setWidgetRealtimePredictions(this, atcoCode, (ArrayList<RealtimePrediction>) task.getResponse());
                    }else{
                        HugoPreferences.setWidgetRealtimePredictions(this, atcoCode, null);
                    }
                    Intent intent = new Intent(this, RealtimeWidgetProvider.class);
                    intent.setAction(RealtimeWidgetProvider.ACTION_SHOW_DATA);
                    sendBroadcast(intent);
                    jobFinished(jobParameters, false);
                }catch(Exception ignore){
                    jobFinished(jobParameters, false);
                }
            });

            task.execute(this);
            //return false means job is done, true means job is still running (if it's AsyncTask etc) then call jobFinished();
            return true;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job Ended");
        Intent intent = new Intent(this, RealtimeWidgetProvider.class);
        intent.setAction(RealtimeWidgetProvider.ACTION_SHOW_DATA);
        sendBroadcast(intent);
        return false;
    }


}
