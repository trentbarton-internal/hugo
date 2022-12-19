package uk.co.trentbarton.hugo.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import uk.co.trentbarton.hugo.dataholders.HttpDataParams.PushTokenParams;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tools.Constants;


public class SendPushTokenSynchronously {

    public static void run(Context context) throws Exception {

        if(context == null){
            return;
        }

        if(!isNetworkAvailable(context)){
            return; //No network available
        }

        if(HugoPreferences.getPushToken(context).equalsIgnoreCase("") || HugoPreferences.isPushSent(context)){
            //If push token equals an empty string or we have already sent it then ignore
            return;
        }

        PushTokenParams params = new PushTokenParams(context);
        if(!params.validate()){
            //Invalid params
            return;
        }

        URL url = new URL(Constants.HUGO_API_URL);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        urlConnection.setRequestMethod("POST");
        urlConnection.connect();

        OutputStream outputStream = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        writer.write(params.build());
        writer.close();
        outputStream.close();

        //Read
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        bufferedReader.close();
        parseResponse(sb.toString(), context);

    }

    private static void parseResponse(String result, Context context) throws Exception{

        JSONObject object = new JSONObject(result);
        String debugMessage = object.getString("debug_message");

        if(debugMessage.equalsIgnoreCase("Success")){
            HugoPreferences.setPushSent(context, true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
