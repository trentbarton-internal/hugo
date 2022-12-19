package uk.co.trentbarton.hugo.customadapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.FinishJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.JourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.RealtimeJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.ScheduledJourneyStep;
import uk.co.trentbarton.hugo.dataholders.JourneyItems.WalkingJourneyStep;
import uk.co.trentbarton.hugo.datapersistence.GlobalData;
import uk.co.trentbarton.hugo.tools.Metrics;
import uk.co.trentbarton.hugo.R;

public class JourneyResultsListAdapter extends ArrayAdapter<Journey> {

    Context mContext;
    Fragment mFragment;

    private class ViewHolder {
        LinearLayout journeyDetailsHolder;
        TextView fastestBanner, dueInMIns, travelTime, disruptionHeader, arriveAtTime, leaveInMinsText, dueInHeader;
        RelativeLayout mangoHolder;
        ImageView disruptionIcon, liveSymbol;
        LinearLayout disruptionBanner;
    }

    public JourneyResultsListAdapter(Context context, Fragment fragment) {
        super(context, 0, GlobalData.getInstance().getJourneyData());
        mContext = context;
        mFragment = fragment;
    }

    @Nullable
    @Override
    public Journey getItem(int position) {
        return super.getItem(position);
    }

    public void refreshData(){
        this.clear();
        this.addAll(GlobalData.getInstance().getJourneyData());
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Journey journey = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.journey_custom_list_object, parent, false);

            viewHolder.journeyDetailsHolder = convertView.findViewById(R.id.journeyDetailsHolder);
            viewHolder.fastestBanner = convertView.findViewById(R.id.fastestBanner);
            viewHolder.travelTime = convertView.findViewById(R.id.travelTimeInMins);
            viewHolder.dueInMIns = convertView.findViewById(R.id.DueInMins);
            viewHolder.dueInHeader = convertView.findViewById(R.id.dueInHeader);
            viewHolder.journeyDetailsHolder = convertView.findViewById(R.id.journeyDetailsHolder);
            viewHolder.disruptionHeader = convertView.findViewById(R.id.disruptionTitle);
            viewHolder.disruptionIcon = convertView.findViewById(R.id.disruptionIcon);
            viewHolder.disruptionBanner = convertView.findViewById(R.id.disruptionBanner);
            viewHolder.arriveAtTime = convertView.findViewById(R.id.arriveAtTime);
            viewHolder.leaveInMinsText = convertView.findViewById(R.id.leaveInMins);
            viewHolder.mangoHolder = convertView.findViewById(R.id.mangoHolder);
            viewHolder.liveSymbol = convertView.findViewById(R.id.liveSymbol);
            addJourneyDetailsToHolder(viewHolder.journeyDetailsHolder, journey);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.journeyDetailsHolder.removeAllViewsInLayout();
            addJourneyDetailsToHolder(viewHolder.journeyDetailsHolder, journey);
        }

        if(journey.isFastest()){
            viewHolder.fastestBanner.setVisibility(View.VISIBLE);
        }else{
            viewHolder.fastestBanner.setVisibility(View.GONE);
        }

        if(journey.hasDisruptions()){
            viewHolder.disruptionBanner.setVisibility(View.VISIBLE);
            //viewHolder.disruptionBanner.setBackgroundColor(ContextCompat.getColor(mContext,journey.getSeverityBackgroundColour()));
            //viewHolder.disruptionHeader.setTextColor(ContextCompat.getColor(mContext,journey.getSeverityForegroundColour()));
            viewHolder.disruptionHeader.setText(journey.getFormattedDisruptionText());
            //viewHolder.disruptionIcon.setImageResource(journey.getSeverityIcon());
            viewHolder.disruptionBanner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(viewHolder.disruptionHeader.getMaxLines() == 1){
                        //Holder is closed so open
                        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        viewHolder.disruptionHeader.setMaxLines(200);
                        viewHolder.disruptionBanner.setLayoutParams(bannerParams);
                    }else{
                        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Metrics.densityPixelsToPixels(30));
                        viewHolder.disruptionHeader.setMaxLines(1);
                        viewHolder.disruptionBanner.setLayoutParams(bannerParams);
                    }
                }
            });

        }else{
            viewHolder.disruptionBanner.setVisibility(View.GONE);
        }

        viewHolder.travelTime.setText(journey.getFormattedTravelTimeInMins());
        viewHolder.arriveAtTime.setText("arrive at " + journey.getArrivalTimeText());
        viewHolder.leaveInMinsText.setText("leave at " + journey.getDepartureTimeText());

        if(journey.hasRealtime()){
            viewHolder.dueInMIns.setText(journey.getFormattedFirstDepartureDueInMinsText());
            viewHolder.dueInHeader.setText("DUE IN");
            viewHolder.liveSymbol.setVisibility(View.VISIBLE);
        }else{
            viewHolder.dueInMIns.setText(journey.getFormattedFirstDepartureTimeText());
            viewHolder.dueInHeader.setText("SCHEDULED AT");
            viewHolder.liveSymbol.setVisibility(View.INVISIBLE);
        }

        if(journey.isMangoApplicable()){
            viewHolder.mangoHolder.setVisibility(View.VISIBLE);
        }else{
            viewHolder.mangoHolder.setVisibility(View.GONE);
        }

        // Return the completed view to render on screen
        return convertView;
    }

    private void addJourneyDetailsToHolder(LinearLayout holder, Journey journey){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int maxWidthRemaining = displayMetrics.widthPixels - Metrics.densityPixelsToPixels(80); //Allows for padding etc

        boolean reachedTheEnd = false;
        int numberOfJourneys = journey.getNumberOfChanges() + 1;


        for(int i = 0; i < journey.getSteps().size(); i++) {

            JourneyStep step = journey.getSteps().get(i);

            if(!reachedTheEnd){

                if (step instanceof WalkingJourneyStep) {
                    if (calculateWidth(maxWidthRemaining, Metrics.densityPixelsToPixels(30), numberOfJourneys, false)) {
                        ImageView imv = new ImageView(mContext);
                        imv.setImageResource(R.drawable.walking_man);
                        imv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Metrics.densityPixelsToPixels(30), Metrics.densityPixelsToPixels(30));
                        imv.setLayoutParams(layoutParams);
                        holder.addView(imv);
                        maxWidthRemaining -= Metrics.densityPixelsToPixels(30);
                    }else {
                        reachedTheEnd = true;
                    }
                }

                if (step instanceof RealtimeJourneyStep || step instanceof ScheduledJourneyStep) {

                    TextView tv = new TextView(mContext);


                    if(step instanceof  RealtimeJourneyStep){
                        tv.setText(" " + ((RealtimeJourneyStep)step).getServiceName());
                    }else{
                        tv.setText(" " + ((ScheduledJourneyStep)step).getServiceName());
                    }

                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
                    LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Metrics.densityPixelsToPixels(30));
                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    tv.setLayoutParams(tvParams);
                    tv.measure(0, 0);
                    int tvWidth = tv.getMeasuredWidth();

                    if (calculateWidth(maxWidthRemaining, (tvWidth + Metrics.densityPixelsToPixels(30)), numberOfJourneys, true)) {

                        ImageView imv = new ImageView(mContext);
                        imv.setImageResource(R.drawable.circle_cover);
                        imv.setScaleType(ImageView.ScaleType.FIT_CENTER);

                        try{
                            if(step instanceof  RealtimeJourneyStep){
                                imv.setBackgroundColor(((RealtimeJourneyStep)step).getServiceColour());
                            }else{
                                imv.setBackgroundColor(((ScheduledJourneyStep)step).getServiceColour());
                            }
                        }catch(Exception e){
                            imv.setBackgroundColor(Color.parseColor("#9e9e9e"));
                        }

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Metrics.densityPixelsToPixels(30), Metrics.densityPixelsToPixels(30));
                        imv.setLayoutParams(layoutParams);
                        holder.addView(imv);
                        holder.addView(tv);
                        numberOfJourneys--;
                        maxWidthRemaining -= (tvWidth + Metrics.densityPixelsToPixels(30));
                    } else {
                        reachedTheEnd = true;
                    }
                }

                if ((i < journey.getSteps().size() - 2) && !reachedTheEnd && (step instanceof ScheduledJourneyStep ||
                        step instanceof RealtimeJourneyStep || step instanceof WalkingJourneyStep)) {
                    if (calculateWidth(maxWidthRemaining, Metrics.densityPixelsToPixels(30), numberOfJourneys, false) && notEndJourney(journey, i)) {
                        ImageView imv = new ImageView(mContext);
                        imv.setImageResource(R.drawable.right_arrow);
                        imv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Metrics.densityPixelsToPixels(30), Metrics.densityPixelsToPixels(30));
                        imv.setPadding(Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10),Metrics.densityPixelsToPixels(10));
                        imv.setLayoutParams(layoutParams);
                        holder.addView(imv);
                        maxWidthRemaining -= Metrics.densityPixelsToPixels(30);
                    } else {
                        reachedTheEnd = true;
                    }
                }
            }

            if(i == (journey.getSteps().size() - 1) && numberOfJourneys > 0){
                //The last entry but there are still journeys to show

                TextView tv = new TextView(mContext);
                tv.setText("+" + numberOfJourneys);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f);
                LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(Metrics.densityPixelsToPixels(30), Metrics.densityPixelsToPixels(30));
                tvParams.setMargins((maxWidthRemaining - Metrics.densityPixelsToPixels(30)),0,0,0);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundResource(R.drawable.grey_background_rounded);
                tv.setLayoutParams(tvParams);
                holder.addView(tv);
            }
        }
    }

    private boolean notEndJourney(Journey journey, int position) {

        try{
            JourneyStep step = journey.getSteps().get(position + 2);
            if(step instanceof FinishJourneyStep){
                return false;
            }else{
                return true;
            }
        }catch(Exception e){
            return false;
        }

    }

    private boolean calculateWidth(int maxWidth, int WidthToDeduct, int numberOfStepsRemaining, boolean isTravel){

        if(isTravel){
            numberOfStepsRemaining =- 1;
        }

        if(numberOfStepsRemaining == 0){
            //This is the last step so as long as the width will fit were all good
            return maxWidth > WidthToDeduct;

        }else{
            //This is not the last step so we need to remove 30 dp to possible allow for extra textView
            maxWidth -= Metrics.densityPixelsToPixels(30);
            return maxWidth > WidthToDeduct;

        }
    }
}