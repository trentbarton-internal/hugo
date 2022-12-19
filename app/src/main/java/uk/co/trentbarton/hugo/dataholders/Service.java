package uk.co.trentbarton.hugo.dataholders;

import android.graphics.Color;

import java.util.Locale;

public class Service {

    private String serviceName;
    private int serviceColour;
    private boolean subscribed;
    private String operator;

    public Service(){
        this.serviceName = "";
        this.serviceColour = Color.parseColor("#939393");
        this.subscribed = false;
        this.operator = "Unknown";
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServiceColour() {
        return serviceColour;
    }

    public void setServiceColour(int serviceColour) {
        this.serviceColour = serviceColour;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getTopicName() {

        return this.serviceName.replace(" ", "").trim().toLowerCase(Locale.ENGLISH);

    }
}
