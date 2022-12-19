package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class GetPlacesParams extends DataRequestParams {

    public GetPlacesParams(Context context) {
        super(ApiCalls.GET_PLACES, context);
    }

    public void addStringSearch(String search){
        try{
            add("search", search);
        }catch(Exception e){

        }
    }

    public void addCurrentLocation(LatLng position){

        if(position == null){
            return;
        }

        if(position.latitude == 0 && position.longitude == 0){
            return;
        }

        try{
            add("lat", position.latitude);
            add("lng", position.longitude);
        }catch(Exception e){

        }
    }

    public void addRadius(int radius){
        try{
            add("radius", radius);
        }catch(Exception e){

        }
    }

    @Override
    public boolean validate() {
        return super.validate() ;
    }
}
