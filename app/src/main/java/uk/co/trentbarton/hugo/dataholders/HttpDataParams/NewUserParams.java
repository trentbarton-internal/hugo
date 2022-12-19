package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

import uk.co.trentbarton.hugo.tools.Constants;

public class NewUserParams extends DataRequestParams {

    private boolean addedOperatingSystem = false;

    public NewUserParams(Context context) {
        super(ApiCalls.REGISTER_USER, context);
        addOperatingSystem();
    }



    @Override
    public boolean validate() {
        return super.validate() && this.addedOperatingSystem;
    }

    private void addOperatingSystem(){

        try{
            add("operatingSystem", Constants.OPERATING_SYSTEM);
            addedOperatingSystem = true;
        }catch(Exception ignore){}

    }
}
