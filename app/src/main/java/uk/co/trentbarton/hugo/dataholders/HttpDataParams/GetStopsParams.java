package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;
import android.location.Location;

import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;

public class GetStopsParams extends DataRequestParams {

    private boolean versionSet = false;

    public GetStopsParams(Context context) {
        super(ApiCalls.GET_STOPS, context);
        init(context);
    }

    private void init(Context context){

        int currentVersion = HugoPreferences.getCurrentStopsVersion(context);
        try{
            add("version", currentVersion);
            versionSet = true;
        }catch(Exception ignore){}
    }

    public void setVersion(int versionNumber){
        try{
            remove("version");
            add("version", versionNumber);
            versionSet = true;
        }catch(Exception ignore){}
    }

    public void addLocation(Location loc){
        try{
            add("lat", loc.getLatitude());
            add("lng", loc.getLongitude());
        }catch(Exception ignore){}
    }

    @Override
    public boolean validate() {
        return super.validate() && this.versionSet;
    }


}
