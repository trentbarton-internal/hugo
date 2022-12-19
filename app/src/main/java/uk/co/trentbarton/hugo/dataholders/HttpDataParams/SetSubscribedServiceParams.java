package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

public class SetSubscribedServiceParams extends DataRequestParams {

    private boolean addedServices = false;

    public SetSubscribedServiceParams(Context context) {
        super(ApiCalls.SET_USER_PREFERENCES, context);
    }

    public void addServicesArray(String[] services) {
        try{
            add("services", services);
            addedServices = true;
        }catch(Exception e){
            addedServices = false;
        }
    }


}
