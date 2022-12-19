package uk.co.trentbarton.hugo.interfaces;

import uk.co.trentbarton.hugo.dataholders.TomTomPlace;

public interface OnFavouritePlaceClickListener {

    void onMakeFavouriteClicked(TomTomPlace place);
    void onRemoveFavouriteClicked(TomTomPlace place);

}
