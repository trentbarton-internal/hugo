package uk.co.trentbarton.hugo.customviewcontrollers;

import org.joda.time.DateTime;

import uk.co.trentbarton.hugo.customview.RefreshView;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DataRequestParams;
import uk.co.trentbarton.hugo.interfaces.OnRefreshDataListener;
import uk.co.trentbarton.hugo.tasks.DataRequestTaskSynchronous;

public class RefreshViewController {

    private RefreshView mView;
    private DataRequestParams mParams;
    private OnRefreshDataListener mListener;
    private Thread refreshThread;
    private DateTime lastTimeDataReceived;
    private boolean isRunning = false;
    private boolean isTaskRunning = false;
    private static final int REFRESH_MILLISECONDS = 30 * 1000;
    private static final int REFRESH_SLEEP = 50;
    private Runnable mRunnable;


    public RefreshViewController(){
        lastTimeDataReceived = DateTime.now();

        mRunnable = () -> {
            while(isRunning){
                //Standard looper every 50 m/s call this
                try{
                    refreshView();
                    Thread.sleep(REFRESH_SLEEP);
                }catch(InterruptedException e){
                    break;
                }
            }
        };
    }

    public void setDataRequestParams(DataRequestParams params){
        mParams = params;
    }

    public void setRefreshView(RefreshView v){
        mView = v;
    }

    public void setListener(OnRefreshDataListener l){
        mListener = l;
    }

    public void startRefreshing(){

        if(refreshThread != null && isRunning){
            //The thread is already running we don't need to do anything
            return;
        }

        if(mParams == null){
            //there is no point in starting without any parameters
            return;
        }

        //Now set the variable to true to keep the loop running
        isRunning = true;

        //Make an initial call to get data
        //Start the indeterminate spinner and set last startTime to now
        //while startTime + timeout is less than now keep spinning the spinner

        if(mView == null){
            throw new RuntimeException("View is not set");
        }

        callTask();

        refreshThread = new Thread(mRunnable,"RefreshViewController");
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    private void refreshView(){

        //Called every 50m/s
        //Determine the state of the loop i.e. are we currently calling the task or are we waiting
        //if we are calling the task let the view handle rotation

        if(isTaskRunning){
            //If true we simply need to invalidate the view to let it be redrawn
            mView.postInvalidate();

        }else{
            //We need to work out how many milliseconds have passed since the last time we got data
            int millsElapsed = (int) (DateTime.now().getMillis() - lastTimeDataReceived.getMillis());

            //if this time is greater than the refreshtime we should call the task otherwise alter the angle and redraw the view
            if(millsElapsed < REFRESH_MILLISECONDS){
                int newAngle = -((int)(360 * ((double) millsElapsed / (double) REFRESH_MILLISECONDS)));
                mView.updateSweepAngle(newAngle);
                mView.postInvalidate();
            }else{
                callTask();
            }
        }
    }

    public void stopRefreshing(){

        isRunning = false;
        isTaskRunning = false;
        if(refreshThread!= null && refreshThread.isAlive()){
            refreshThread.interrupt();
        }
    }

    public boolean isParamsSet(){
        return mParams != null;
    }

    public void refreshNow(){
        if(isTaskRunning){
            //Task is already running so ignore
            return;
        }

        callTask();

    }

    private void callTask(){

        isTaskRunning = true;
        mView.setIndeterminate(true);

        Thread t = new Thread(() -> {

            DataRequestTaskSynchronous task = new DataRequestTaskSynchronous(mParams);
            if(task.run(mView.getContext())){
                isTaskRunning = false;
                mView.setIndeterminate(false);
                lastTimeDataReceived = DateTime.now();
                mListener.onDataRefreshed(task.getResponse());
            }else{
                isTaskRunning = false;
                mView.setIndeterminate(false);
                lastTimeDataReceived = DateTime.now();
            };

        });
        t.setName("Synchronous data thread");
        t.setDaemon(true);
        t.start();

    }
}
