package uk.co.trentbarton.hugo.customadapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;

public class TomTomAutoCompleteDialogAdapter extends ArrayAdapter<TomTomPlace> {

    Context mContext;
    private int mSelectedPosition = 0;

    public TomTomAutoCompleteDialogAdapter(Context context, ArrayList<TomTomPlace> places) {
        super(context, 0, places);
        mContext = context;
    }

    private class ViewHolder {
        TextView placeName, placeLocality;
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
            convertView = inflater.inflate(R.layout.stop_list_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.placeName = convertView.findViewById(R.id.stopNameView);
            viewHolder.placeLocality = convertView.findViewById(R.id.localityView);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.placeName.setText(place.getName());
        viewHolder.placeLocality.setText(place.getLocality());

        return convertView;
    }




}
