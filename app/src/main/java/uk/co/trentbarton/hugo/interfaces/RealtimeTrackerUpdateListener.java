package uk.co.trentbarton.hugo.interfaces;

import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;

public interface RealtimeTrackerUpdateListener {

    void onPredictionUpdated(RealtimePrediction prediction);
    void onErrorReceived();
}
