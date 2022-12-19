package uk.co.trentbarton.hugo.interfaces;

import uk.co.trentbarton.hugo.customview.BusStopItem;

public interface OnStopItemStatusChangedListener {

    void onStopClosed(BusStopItem stop);
    void onStopOpened(BusStopItem stop);

}
