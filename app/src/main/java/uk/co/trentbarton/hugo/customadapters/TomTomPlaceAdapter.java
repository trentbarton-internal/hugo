package uk.co.trentbarton.hugo.customadapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.interfaces.OnFavouritePlaceClickListener;

public class TomTomPlaceAdapter extends ArrayAdapter<TomTomPlace> {

    Context mContext;
    private int mSelectedPosition = 0;
    private OnFavouritePlaceClickListener mFavouriteClickedListener;

    public TomTomPlaceAdapter(Context context, ArrayList<TomTomPlace> places) {
        super(context, 0, places);
        mContext = context;
    }

    public void setOnFavouritePlaceClicked(OnFavouritePlaceClickListener l) {
        mFavouriteClickedListener = l;
    }

    private class ViewHolder {
        TextView placeName, placeLocality;
        ImageView selectedStar;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TomTomPlace place = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.matched_place_adapter_view, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.placeName = convertView.findViewById(R.id.matchedPlaceName);
            viewHolder.placeLocality = convertView.findViewById(R.id.matchedPlaceLocality);
            viewHolder.selectedStar = convertView.findViewById(R.id.favouriteIndicator);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.placeName.setText(place.getName());
        viewHolder.placeLocality.setText(place.getLocality());

        if(place.getName().equalsIgnoreCase("Use current location")){
            viewHolder.selectedStar.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.selectedStar.setVisibility(View.VISIBLE);
        }

        if(place.isFavourite()){
            viewHolder.selectedStar.setImageResource(R.drawable.favourite_star_selected);
        }else{
            viewHolder.selectedStar.setImageResource(R.drawable.favourite_star_unselected);
        }

        viewHolder.selectedStar.setOnClickListener(v -> {
            if(mFavouriteClickedListener != null) {

                if(place.isFavourite()){
                    mFavouriteClickedListener.onRemoveFavouriteClicked(place);
                }else{
                    mFavouriteClickedListener.onMakeFavouriteClicked(place);
                }
            }
        });

        return convertView;
    }
}
