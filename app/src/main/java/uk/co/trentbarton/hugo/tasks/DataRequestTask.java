package uk.co.trentbarton.hugo.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DataRequestParams;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.interfaces.OnTaskCompletedListener;
import uk.co.trentbarton.hugo.tools.Constants;
import uk.co.trentbarton.hugo.tools.HttpResponseParser;


public class DataRequestTask extends AsyncTask<Context, Void, Boolean> {

    private final DataRequestParams mParams;
    private String errorMessage;
    private String successMessage;
    private Object mResponse;
    private OnTaskCompletedListener mListener;

    public DataRequestTask(DataRequestParams params){
        mParams = params;
    }

    public Object getResponse(){
        return this.mResponse;
    }

    public void setOnTaskCompletedListener(OnTaskCompletedListener l){
        mListener = l;
    }

    @Override
    protected Boolean doInBackground(Context... contexts) {

        try{

            RegisterUserSynchronously.run(contexts[0]);

            if(mParams == null || !mParams.validate()) {
                errorMessage = "Incorrect parameters used, please try again";
                return false;
            }

            URL url = new URL(Constants.HUGO_API_URL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(10 * 1000); //10 seconds
            urlConnection.connect();

            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(mParams.build());
            writer.close();
            outputStream.close();

            //Read
            if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                errorMessage = "Internal server error, please contact customer services to report a fault";
                return false;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            urlConnection.disconnect();

            HttpResponseParser parser = new HttpResponseParser(mParams.getCall(), sb.toString());
            mResponse = parser.getResponseObject();

            if(mParams.getCall() == DataRequestParams.ApiCalls.GET_JOURNEY && (mResponse != null)){
                HugoPreferences.setLastJourneyData(contexts[0], sb.toString());
                HugoPreferences.setLastJourneyItemChosen(contexts[0], -1);
            }


            errorMessage = parser.getErrorMessage();
            successMessage = parser.getCustomerMessage();
            return true;

        } catch (Exception e) {
            errorMessage = "An error occurred while contacting the sever please try again.";
            return false;
        }
    }

    public String getErrorMessage(){
        return this.errorMessage;
    }

    public String getSuccessMessage(){
        return this.successMessage;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(mListener != null){
            mListener.onCompleted(aBoolean);
        }
    }
}
