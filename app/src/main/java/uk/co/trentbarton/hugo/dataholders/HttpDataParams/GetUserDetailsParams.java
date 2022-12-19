package uk.co.trentbarton.hugo.dataholders.HttpDataParams;

import android.content.Context;

public class GetUserDetailsParams extends DataRequestParams {

    public GetUserDetailsParams(Context context) {
        super(ApiCalls.GET_ALL_USER_DETAILS, context);
    }

    @Override
    public boolean validate() {
        return super.validate();
    }
}
