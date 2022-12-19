package uk.co.trentbarton.hugo.dataholders;

import android.content.Context;
import android.view.View;

import org.joda.time.LocalDateTime;

public class JourneyDisruption {

    private LocalDateTime startTime, endTime;
    private String message;
    private int severity;
    private String serviceImpacted;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getServiceImpacted() {
        return serviceImpacted;
    }

    public void setServiceImpacted(String serviceImpacted) {
        this.serviceImpacted = serviceImpacted;
    }

}
