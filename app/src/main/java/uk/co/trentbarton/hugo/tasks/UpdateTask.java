package uk.co.trentbarton.hugo.tasks;

import android.content.Context;
import android.os.AsyncTask;
import java.util.ArrayList;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.GetStopsParams;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.BuildConfig;

/**
 * This task should be started when the app is first launched and while launched Asynchronously
 * it should block the app from continuing until the processes are finished
 *
 * When the process is finished the OnTaskCompletedListener passed in will be called
 * @see  OnTaskCompletedListener#onCompleted(Boolean)
 *
 **/
public class UpdateTask extends AsyncTask<Context, Void, Void> {

    private OnTaskCompletedListener mListener;
    private static final int MINIMUM_BUILD_VERSION_TO_SKIP_RESET = 22;

    public UpdateTask(OnTaskCompletedListener listener){
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Context... contexts) {

        Context context = contexts[0];

        if(context == null){
            //If context is null we can't really do a deal so return null and exit the update task
            if(mListener != null) {
                mListener.onCompleted(false);
            }
        }

        int versionCode = BuildConfig.VERSION_CODE;
        int installedVersion = HugoPreferences.getHugoVersionInstalled(context);
        if(versionCode != installedVersion && installedVersion < MINIMUM_BUILD_VERSION_TO_SKIP_RESET) {
            //The current version is both old and not the same as the current one

            //Delete all of the unused SharedPreferences
            HugoPreferences.resetAll(context);

            //Call the database to move all the data across as necessary and delete
            Database db = new Database(context);

            //Setup all of the new files
            //This is handled in the Database class

            //If everything goes okay, set the installed version preference to the new version number to stop this process happening everytime.
            HugoPreferences.setHugoVersionInstalled(context, versionCode);
        }

        //Now we have the correct database installed by the app and the data required like a new install

        //Make a call to the API and see if there is any new stops been added if so update the database and move the new

        GetStopsParams params = new GetStopsParams(context);
        DataRequestTask task = new DataRequestTask(params);

        task.setOnTaskCompletedListener(bool -> {

            if(!bool){
                //task failed for some reason
                if(mListener != null) {
                    mListener.onCompleted(false);
                }
            }

            Object o = task.getResponse();

            if(o instanceof ArrayList){

                if(((ArrayList) o).size() == 0){

                    if(mListener != null){
                        mListener.onCompleted(true);
                    }
                }

                ArrayList<Stop> stops = new ArrayList<>();
                int largestVersion = HugoPreferences.getCurrentStopsVersion(context);

                for(Object obj : (ArrayList)o) {
                    if(obj instanceof Stop) {
                        stops.add((Stop)obj);

                        if(((Stop) obj).getVersion() > largestVersion){
                            largestVersion = ((Stop) obj).getVersion();
                        }
                    }
                }

                Database db = new Database(context);
                final int largestVersionNew = largestVersion;

                Thread t = new Thread();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(stops.size() <= 10){
                            db.updateStops(stops);
                            HugoPreferences.setCurrentStopsVersion(context, largestVersionNew);
                        }else{

                            ArrayList<Stop> tempList = new ArrayList<>();

                            for(int i = 0; i < stops.size(); i++){

                                tempList.add(stops.get(i));

                                if(tempList.size() == 10 || i == (stops.size() - 1)){
                                    db.updateStops(tempList);
                                    try {
                                        t.wait(100); //make the thread wait 100 m/s inbetween adding 10 stops so it doesn't die
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    tempList.clear();
                                }
                            }

                            HugoPreferences.setCurrentStopsVersion(context, largestVersionNew);
                        }
                    }
                };
                t.start();
            }

            if(mListener != null){
                mListener.onCompleted(true);
            }

        });


        task.execute(context);
        return null;

    }

}
