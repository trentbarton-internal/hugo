package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

public class GetServicesParams extends DataRequestParams {

    public GetServicesParams(Context context) {
        super(ApiCalls.GET_SERVICES, context);
    }

    @Override
    public boolean validate() {
        return super.validate() ;
    }
}
