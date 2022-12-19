package uk.co.trentbarton.hugo.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

import uk.co.trentbarton.hugo.datapersistence.HugoPreferences;


public class RealtimeWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        RealtimeWidgetRemoteViewFactory viewsFactory = new RealtimeWidgetRemoteViewFactory(this, intent);
        return viewsFactory;
    }
}
