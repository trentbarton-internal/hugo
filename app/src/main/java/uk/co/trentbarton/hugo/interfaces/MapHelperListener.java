package uk.co.trentbarton.hugo.interfaces;

import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.Stop;

public interface MapHelperListener {

    void onMapStateChanged();
    void onBusClicked(RealtimePrediction prediction);
    void onStopClicked(Stop stop);




}
