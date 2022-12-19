package uk.co.trentbarton.hugo.dataholders;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import uk.co.trentbarton.hugo.datapersistence.Database;
import uk.co.trentbarton.hugo.tasks.OnInfoReceivedListener;

public class TomTomPlace {

    private double mLat, mLng;
    private String mName, mLocality;
    private OnInfoReceivedListener mListener;
    private boolean isFavourite;

    public TomTomPlace(double lat, double lng, String name, String locality) {

        mLat = lat;
        mLng = lng;
        mName = name;
        mLocality = locality;

    }

    public boolean isFavourite(){
        return this.isFavourite;
    }

    public void setFavourite(boolean isFavourite){
        this.isFavourite = isFavourite;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    public void setLat(long lat) {
        this.mLat = (double) (lat / (double)Database.MULTIPLIER);
    }

    public void setLng(long lng) {
        this.mLng = (double) (lng / (double)Database.MULTIPLIER);
    }

    public LatLng getPosition(){
        return new LatLng(getLat(), getLng());
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        this.mLng = lng;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getLocality() {
        return mLocality;
    }

    public void setLocality(String locality) {
        this.mLocality = locality;
    }

    public long getDatabaseLat() {
        return (long) (this.getLat() * Database.MULTIPLIER);
    }

    public long getDatabaseLng() {
        return (long) (this.getLng() * Database.MULTIPLIER);
    }

    public void setOnInfoReceivedListener(OnInfoReceivedListener l){
        this.mListener = l;
    }

    public void getNameFromGoogle(){

        PlaceNameTask task = new PlaceNameTask();
        task.execute();

    }

    private class PlaceNameTask extends AsyncTask<Void, Void, Void> {

        private final String KEY = "AIzaSyApVO9feOwud1gnI3OShqXyeu2qgKQRFlE";
        private String urlText = "https://maps.googleapis.com/maps/api/geocode/json?";

        @Override
        protected Void doInBackground(Void... voids) {

            try{

                TomTomPlace place = TomTomPlace.this;
                String response = "";

                DecimalFormat df = new DecimalFormat("#.#######");
                df.setRoundingMode(RoundingMode.DOWN);
                urlText = urlText + String.format("latlng=%s,%s&key=%s",df.format(place.getPosition().latitude),df.format(place.getPosition().longitude),KEY);

                URL url = new URL(urlText);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    byte[] contents = new byte[1024];
                    int bytesRead = 0;

                    while((bytesRead = in.read(contents)) != -1) {
                        response += new String(contents, 0, bytesRead);
                    }

                    parseResponse(response);

                } catch(Exception e) {
                    return null;
                } finally{
                    urlConnection.disconnect();
                }

            }catch(Exception e){

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(mListener != null){
                mListener.onInfoReceived(TomTomPlace.this);
            }

        }

        @Override
        protected void onCancelled() {

            if(mListener != null){
                mListener.onInfoReceived(TomTomPlace.this);
            }

            super.onCancelled();

        }

        private void parseResponse(String jsonResponse) throws Exception{

            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray results = obj.getJSONArray("results");
            JSONObject placeObject = (JSONObject) results.get(0);

            String locality = "";
            String placeName = placeObject.getString("formatted_address");
            String formattedPlaceName = "";
            boolean pass = true;

            for(char c : placeName.toCharArray()){

                if(formattedPlaceName.length() >= 45){
                    break;
                }

                if(c == ','){
                    if(!pass){
                        break;
                    }else{
                        pass = false;
                    }
                }
                formattedPlaceName += c;
            }

            JSONArray placesArray = placeObject.getJSONArray("address_components");
            outerloop:
            for(int i = 0; i < placesArray.length(); i++){

                for(int x = 0; x < placesArray.getJSONObject(i).getJSONArray("types").length(); x++){
                    String type = placesArray.getJSONObject(i).getJSONArray("types").getString(x);

                    if(type.equalsIgnoreCase("locality")){
                        if(placesArray.getJSONObject(i).has("long_name")){
                            locality = placesArray.getJSONObject(i).getString("long_name");
                        }
                        break outerloop;
                    }
                }
            }

            TomTomPlace.this.setName(formattedPlaceName);
            TomTomPlace.this.setLocality(locality);

        }
    }


}
