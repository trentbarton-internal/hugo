package uk.co.trentbarton.hugo.customadapters;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.R;

public class StopDetailsAdapter extends ArrayAdapter<RealtimePrediction> {

    Context mContext;
    ArrayList<RealtimePrediction> mPredictions;
    private boolean mIsInSelectionMode = false;
    private int mSelectedPosition = 0;

    public void refreshData(ArrayList<RealtimePrediction> filteredPredictions) {
        this.clear();
        this.addAll(filteredPredictions);
        notifyDataSetChanged();
    }

    public RealtimePrediction getSelectedItem() {

        try{
            return getItem(mSelectedPosition);
        }catch(Exception e){
            return null;
        }

    }

    static class ViewHolder {
        TextView serviceName, serviceDestination, predictionDisplay, bottomLine;
        ImageView serviceColour, selectionImage;
    }

    public StopDetailsAdapter(Context context, ArrayList<RealtimePrediction> predictions) {
        super(context, 0, predictions);
        mPredictions = predictions;
        mContext = context;
    }

    public void setSelectionMode(boolean inSelectionMode){
        if(inSelectionMode){
            setSelectedPosition(0);
        }
        mIsInSelectionMode = inSelectionMode;
    }

    public void setSelectedPosition(int pos){
        mSelectedPosition = pos;
    }

    @Nullable
    @Override
    public RealtimePrediction getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RealtimePrediction prediction = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.object_realtime_prediction_item_stop_details, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.serviceName = convertView.findViewById(R.id.object_realtime_prediction_service_name);
            viewHolder.serviceDestination = convertView.findViewById(R.id.object_realtime_prediction_destination_name);
            viewHolder.predictionDisplay = convertView.findViewById(R.id.object_realtime_prediction_time);
            viewHolder.serviceColour = convertView.findViewById(R.id.object_realtime_prediction_service_colour);
            viewHolder.bottomLine = convertView.findViewById(R.id.object_realtime_bottom_line);
            viewHolder.selectionImage = convertView.findViewById(R.id.object_realtime_prediction_selection_image);
            viewHolder.bottomLine.setVisibility(View.GONE);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.serviceName.setText(prediction.getServiceName());
        viewHolder.serviceDestination.setText(prediction.getJourneyDestination());
        viewHolder.serviceColour.setBackgroundColor(prediction.getServiceColour());
        viewHolder.predictionDisplay.setText(prediction.getFormattedPredictionDisplay());
        // Return the completed view to render on screen

        if(mIsInSelectionMode){
            if(mSelectedPosition == position){
                viewHolder.selectionImage.setImageResource(R.drawable.green_tick);
            }else{
                viewHolder.selectionImage.setImageResource(R.drawable.unselected_icon);
            }
        }else{
            viewHolder.predictionDisplay.setTranslationX(viewHolder.selectionImage.getWidth());
            viewHolder.selectionImage.setTranslationX(viewHolder.selectionImage.getWidth());
        }

        return convertView;
    }

}