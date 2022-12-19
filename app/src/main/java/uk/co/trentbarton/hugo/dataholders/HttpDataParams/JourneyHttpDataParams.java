package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import org.joda.time.LocalDateTime;

import uk.co.trentbarton.hugo.dataholders.TomTomPlace;

public class JourneyHttpDataParams extends DataRequestParams {

    private boolean toAdded, fromAdded;

    public JourneyHttpDataParams(Context context) {
        super(ApiCalls.GET_JOURNEY, context);
        toAdded = false;
        fromAdded = false;
    }

    public JourneyHttpDataParams addFromPlace(TomTomPlace place){

        if(place == null){
            throw new RuntimeException("Invalid params, place can not be null");
        }

        if(place.getLat() != 0 && place.getName() == null){
            throw new RuntimeException("Invalid params, place details can not be null");
        }

        if(place.getLat() != 0){
            try{
                add("fromLat", place.getLat());
                add("fromLng", place.getLng());
            }catch(Exception e){

            }
        }

        if(!place.getName().equals("")){
            try{
                add("fromText", place.getName());
            }catch(Exception e){}
        }

        fromAdded = true;
        return this;
    }

    public JourneyHttpDataParams addToPlace(TomTomPlace place){

        if(place == null){
            throw new RuntimeException("Invalid params, place can not be null");
        }

        if(place.getLat() != 0 && place.getName() == null){
            throw new RuntimeException("Invalid params, place details can not be null");
        }

        if(place.getLat() != 0){
            try{
                add("toLat", place.getLat());
                add("toLng", place.getLng());
            }catch(Exception e){

            }
        }

        if(!place.getName().equals("")){
            try{
                add("toText", place.getName());
            }catch(Exception e){}
        }

        toAdded = true;
        return this;
    }

    public JourneyHttpDataParams addDepartureTime(LocalDateTime ldt){

        remove("departAt");
        remove("arriveAt");

        if(ldt == null){
            return this;
        }

        try{
            add("departAt", ldt.toString("dd-MM-yyyy HH:mm:ss"));
        }catch(Exception ignore){}

        return this;

    }

    public JourneyHttpDataParams addArrivalTime(LocalDateTime ldt){

        remove("departAt");
        remove("arriveAt");

        if(ldt == null){
            return this;
        }

        try{
            add("arriveAt", ldt.toString("dd-MM-yyyy HH:mm:ss"));
        }catch(Exception ignore){}

        return this;

    }

    @Override
    public boolean validate() {
        return super.validate() && fromAdded && toAdded;
    }


}
