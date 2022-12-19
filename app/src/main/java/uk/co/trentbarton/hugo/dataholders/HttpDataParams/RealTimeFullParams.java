package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

public class RealTimeFullParams extends DataRequestParams{

    private String mAtcoCode;

    public RealTimeFullParams(boolean includeDriverNames, Context context) {
        super(ApiCalls.GET_REALTIME_FULL, context);
    }

    public RealTimeFullParams addAtcoCode(String atcoCode){

        try{
            add("atcoCode",atcoCode);
            this.mAtcoCode = atcoCode;
        }catch(Exception ignore){}

        return this;
    }

    public RealTimeFullParams includeDriverNames(boolean b){

        try{
            add("searchDriverName",b);
        }catch(Exception ignore){}

        return this;
    }

    @Override
    public boolean validate() {
        return super.validate() && !this.mAtcoCode.equals("");
    }


}
