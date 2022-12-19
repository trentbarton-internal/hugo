package uk.co.trentbarton.hugo.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import uk.co.trentbarton.hugo.dataholders.HttpDataParams.NewUserParams;
import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;
import uk.co.trentbarton.hugo.tools.Constants;


public class RegisterUserSynchronously {

    private static final String TAG = RegisterUserSynchronously.class.getSimpleName();


    public RegisterUserSynchronously(){}

    public static void run(Context context) throws Exception {

        if(context == null){
            return;
        }

        if(!isNetworkAvailable(context)){
            return; //No network available
        }

        if(!(HugoPreferences.getUserToken(context).equalsIgnoreCase(""))){
            //We already have a token so cancel this task
            return;
        }

        NewUserParams params = new NewUserParams(context);
        if(!params.validate()){
            //Invalid params
            return;
        }

        Log.i(TAG, "New user task running");


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
        parseUser(sb.toString(), context);
        urlConnection.disconnect();

    }

    private static void parseUser(String result, Context context) throws Exception{

        JSONObject object = new JSONObject(result);
        JSONObject userObject = object.getJSONObject("data");

        String resultText = userObject.getString("message");
        if(resultText.equalsIgnoreCase("User created successfully")){

            String userToken = userObject.getString("user_token");

            if(!userToken.isEmpty()){
                HugoPreferences.setUserToken(context, userToken);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
