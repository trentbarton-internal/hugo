package uk.co.trentbarton.hugo.tasks;

import android.content.Context;
import android.util.Log;

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
import uk.co.trentbarton.hugo.interfaces.OnRefreshDataListener;
import uk.co.trentbarton.hugo.tools.Constants;
import uk.co.trentbarton.hugo.tools.HttpResponseParser;

public class DataRequestTaskSynchronous {

    private static final String TAG = DataRequestTaskSynchronous.class.getSimpleName();
    private final DataRequestParams mParams;
    private String errorMessage;
    private String successMessage;
    private Object mResponse;

    public DataRequestTaskSynchronous(DataRequestParams params){
        mParams = params;
    }

    public Object getResponse(){
        return this.mResponse;
    }

    public Boolean run(Context context) {

        try{

            RegisterUserSynchronously.run(context);
            SendPushTokenSynchronously.run(context);

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

            if(mParams.getCall() == DataRequestParams.ApiCalls.GET_JOURNEY && (parser.getResponseObject() != null)){
                HugoPreferences.setLastJourneyData(context, sb.toString());
                HugoPreferences.setLastJourneyItemChosen(context, -1);
            }

            mResponse = parser.getResponseObject();
            errorMessage = parser.getErrorMessage();
            successMessage = parser.getCustomerMessage();
            return true;

        } catch (Exception e) {

            if(e.getLocalizedMessage() == null || e.getLocalizedMessage().equalsIgnoreCase("")){
                Log.e(TAG, "Unknown error");
            }else{
                Log.e(TAG, e.getMessage(), e);
            }
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

}
