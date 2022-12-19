package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class RealTimeAreaParams extends DataRequestParams{

    private LatLng mCurrentPosition;

    public RealTimeAreaParams(boolean includeDriverNames, Context c) {
        super(ApiCalls.GET_REALTIME_IN_AREA, c);
    }

    public RealTimeAreaParams setPosition(LatLng pos){
        try{
            add("lat",pos.latitude);
            add("lng", pos.longitude);
            this.mCurrentPosition = pos;
        }catch(Exception ignore){}

        return this;
    }

    public RealTimeAreaParams includeDriverNames(boolean b){

        try{
            add("searchDriverName",b);
        }catch(Exception ignore){}

        return this;
    }

    public RealTimeAreaParams setMaxDistance(int distance){

        try{
            add("max_distance",distance);
        }catch(Exception ignore){}

        return this;
    }

    public RealTimeAreaParams setMaxResults(int results){

        try{
            add("max_results",results);
        }catch(Exception ignore){}

        return this;
    }



    @Override
    public boolean validate() {
        return super.validate() && !(this.mCurrentPosition == null);
    }

   }
