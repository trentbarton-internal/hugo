package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import java.util.ArrayList;

public class MultipleRealTimeFullParams extends DataRequestParams {

    ArrayList<String> atcoCodes;

    public MultipleRealTimeFullParams(Context context) {
        super(ApiCalls.GET_MULTIPLE_REALTIME_FULL, context);
        atcoCodes = new ArrayList<>();
    }

    public MultipleRealTimeFullParams addAtcoCode(String atcoCode){
        atcoCodes.add(atcoCode);
        return this;
    }

    public MultipleRealTimeFullParams includeDriverNames(boolean b){

        try{
            add("searchDriverName",b);
        }catch(Exception ignore){}

        return this;
    }

    @Override
    public boolean validate() {
        return super.validate() && (this.atcoCodes.size() > 0);
    }


    @Override
    public String build() {
        String[] codesAsArray = new String[atcoCodes.size()];
        int counter = 0;
        for(String code : atcoCodes){
            codesAsArray[counter] = code;
            counter++;
        }
        try{
            add("atcoCode", codesAsArray);
        }catch(Exception e){}

        return super.build();
    }
}
