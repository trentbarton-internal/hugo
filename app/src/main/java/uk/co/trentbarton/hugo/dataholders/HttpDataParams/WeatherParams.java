package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class WeatherParams extends DataRequestParams {

    private boolean positionAdded = false;

    public WeatherParams(Context context) {
        super(ApiCalls.GET_WEATHER, context);
    }

    public WeatherParams assignLocation(LatLng position){
        try{
            add("lat",position.latitude);
            add("lng", position.longitude);
            positionAdded = true;
        }catch(Exception ignore){}

        return this;
    }

    @Override
    public boolean validate() {
        return super.validate() && positionAdded;
    }

}
