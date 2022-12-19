package uk.co.trentbarton.hugo.dataholders;

import java.util.HashSet;
import java.util.Set;

public class ServiceFilter {

    private Set<String> serviceNames;

    public ServiceFilter(){
        serviceNames = new HashSet<>();
    }

    public void addServiceName(String name){
        serviceNames.add(name);
    }

    public void removeServiceName(String name){
        serviceNames.remove(name);
    }

    public void clearAllServiceNames(){
        serviceNames.clear();
    }

    public boolean passesFilter(RealtimePrediction prediction) {
        return !serviceNames.contains(prediction.getServiceName());
    }

    public boolean passesFilter(String serviceName) {
        return !serviceNames.contains(serviceName);
    }

    public String[] getAllServiceNames(){
        return serviceNames.toArray(new String[serviceNames.size()]);
    }
}
