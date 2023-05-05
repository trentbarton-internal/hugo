package uk.co.trentbarton.hugo.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import uk.co.trentbarton.hugo.R;
import uk.co.trentbarton.hugo.dataholders.Service;
import uk.co.trentbarton.hugo.interfaces.OnServiceItemClickListener;

public class ServiceChooserAdapter extends ArrayAdapter<Service> {

    Context mContext;
    List<Service> mServices;
    private OnServiceItemClickListener mListener;

    public ServiceChooserAdapter(@NonNull Context context, @NonNull List<Service> objects) {
        super(context, 0, objects);
        mServices = objects;
        mContext = context;
    }

    @Nullable
    @Override
    public Service getItem(int position) {
        return super.getItem(position);
    }

    public void setOnServiceClickedListener(OnServiceItemClickListener listener){
        mListener = listener;
    }

    static class ViewHolder {
        public TextView serviceName, operatorName;
        public ImageView serviceColour, selectedIcon;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        Service service = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ServiceChooserAdapter.ViewHolder viewHolder; // view lookup cache stored in tag
        if (view == null) {
            // If there's no view to re-use, inflate a brand new view for row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.choose_service_item, parent, false);

            viewHolder = new ServiceChooserAdapter.ViewHolder();
            viewHolder.serviceName = view.findViewById(R.id.serviceName);
            viewHolder.serviceColour = view.findViewById(R.id.serviceColour);
            viewHolder.selectedIcon = view.findViewById(R.id.selectedIcon);
            viewHolder.operatorName = view.findViewById(R.id.operatorName);
            // Cache the viewHolder object inside the fresh view
            view.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ServiceChooserAdapter.ViewHolder) view.getTag();
        }
        assert service != null;
        viewHolder.serviceName.setText(service.getServiceName());
        viewHolder.operatorName.setText(service.getOperator());
        viewHolder.serviceColour.setBackgroundColor(service.getServiceColour());

        if(service.isSubscribed()){
            viewHolder.selectedIcon.setImageResource(R.drawable.green_tick);
        }else{
            viewHolder.selectedIcon.setImageResource(R.drawable.unselected_icon);
        }

        view.setOnClickListener(view1 -> mListener.onItemClicked(position, getItem(position)));

        return view;
    }
}
