package uk.co.trentbarton.hugo.tasks;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.activities.StopDetailsActivity;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.RealTimeFullParams;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.interfaces.RealtimeTrackerUpdateListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTask;

public class RealtimeTracker {

    private int mFailedAttempts = 0;
    private String lastSuccessfulTime;
    private final String mServiceName;
    private final long mVehicleNumber;
    private final String mAtcoCode;
    private RealtimeTrackerUpdateListener mListener;
    private Runnable mRunnable;
    private Handler mHandler;
    private DataRequestTask mTask;

    public RealtimeTracker(String serviceName, long vehicleNumber, String atcoCode){
        mServiceName = serviceName;
        mVehicleNumber = vehicleNumber;
        mAtcoCode = atcoCode;
        mHandler = new Handler();
    }

    public void setRealtimeTrackerUpdateListener(RealtimeTrackerUpdateListener listener){
        mListener = listener;
    }

    private void sendErrorMessage(){
        if(mListener != null){
            mListener.onErrorReceived();
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void incrementError(){
        mFailedAttempts++;
        if(mFailedAttempts > 2){
            sendErrorMessage();
            stopRefreshing();
        }
    }

    private void sendUpdateMessage(RealtimePrediction prediction){
        if(mListener != null){
            mListener.onPredictionUpdated(prediction);
        }
    }

    public void stopRefreshing() {

        mHandler.removeCallbacks(mRunnable);

    }

    public void startRefreshing(Context context){

        if(mHandler == null){
            mHandler = new Handler();
        }

        mHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mRunnable = this;
                try{
                    mTask.cancel(true);
                }catch(Exception e){}

                RealTimeFullParams params = new RealTimeFullParams(false, context);
                params.addAtcoCode(mAtcoCode);
                mTask = new DataRequestTask(params);
                mTask.setOnTaskCompletedListener(bool -> {

                    if(bool){
                        if(mTask.getResponse() instanceof ArrayList){
                            try{
                                searchResults((ArrayList<RealtimePrediction>) mTask.getResponse());
                            }catch(ClassCastException ex){
                                incrementError();
                            }
                        }else{
                            incrementError();
                        }
                    }else{
                        incrementError();
                    }
                });
                mTask.execute(context);
                mHandler.postDelayed(mRunnable, 30 * 1000);
            }
        }, 0);

    }

    private void searchResults(ArrayList<RealtimePrediction> predictions) {

        boolean found = false;

        for(RealtimePrediction prediction : predictions){
            if(prediction.getVehicleNumber() == mVehicleNumber && prediction.getServiceName().equalsIgnoreCase(mServiceName)){
                found = true;
                sendUpdateMessage(prediction);
                break;
            }
        }

        if(!found){
            incrementError();
        }

    }


}
