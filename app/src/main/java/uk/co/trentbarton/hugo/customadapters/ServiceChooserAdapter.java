package uk.co.trentbarton.hugo.customadapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.List;

import uk.co.trentbarton.hugo.dataholders.Service;
import uk.co.trentbarton.hugo.interfaces.OnServiceItemClickListener;
import uk.co.trentbarton.hugo.R;

public class ServiceChooserAdapter extends RecyclerView.Adapter<ServiceChooserAdapter.MyViewHolder>  implements SectionTitleProvider {

    private List<Service> serviceList;
    private OnServiceItemClickListener mListener;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView serviceName, operatorName;
        public ImageView serviceColour, selectedIcon;

        public MyViewHolder(View view) {
            super(view);
            serviceName = view.findViewById(R.id.serviceName);
            serviceColour = view.findViewById(R.id.serviceColour);
            selectedIcon = view.findViewById(R.id.selectedIcon);
            operatorName = view.findViewById(R.id.operatorName);
        }

        public void bind(final Service item, final int position, final OnServiceItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                if(listener != null){
                    listener.onItemClicked(position, item);
                }
            });
        }

    }

    public ServiceChooserAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public void setOnServiceClickedListener(OnServiceItemClickListener listener){
        mListener = listener;
    }

    @Override
    public String getSectionTitle(int position) {
        return serviceList.get(position).getServiceName().substring(0,1).toUpperCase();
    }


    @NonNull
    @Override
    public ServiceChooserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.choose_service_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceChooserAdapter.MyViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.serviceName.setText(service.getServiceName());
        holder.operatorName.setText(service.getOperator());
        holder.serviceColour.setBackgroundColor(service.getServiceColour());

        if(service.isSubscribed()){
            holder.selectedIcon.setImageResource(R.drawable.green_tick);
        }else{
            holder.selectedIcon.setImageResource(R.drawable.unselected_icon);
        }

        holder.bind(serviceList.get(position), position, mListener);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }



}
