package uk.co.trentbarton.hugo.tools;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;

import uk.co.trentbarton.hugo.dataholders.Alarm;
import uk.co.trentbarton.hugo.dataholders.Message;
import uk.co.trentbarton.hugo.dataholders.Service;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.dataholders.HttpDataParams.DataRequestParams;
import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.RealtimePrediction;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;
import uk.co.trentbarton.hugo.dataholders.UserDetails;
import uk.co.trentbarton.hugo.dataholders.WeatherHolder;

public class HttpResponseParser {

    private static final String TAG = HttpResponseParser.class.getSimpleName();
    private static final String SUCCESS_MESSAGE = "Success";
    private final String fullResponse;
    private String customerMessage;
    private String debugMessage;
    private final DataRequestParams.ApiCalls mCall;
    private Object responseObject;

    public HttpResponseParser(DataRequestParams.ApiCalls call, String response){
        fullResponse = response;
        mCall = call;

        try{
            parseObject();
        }catch(Exception e){
            responseObject = null;
            Log.e(TAG, e.getMessage(), e);
            debugMessage = "Exception occurred while parsing the JSON response: " + e.getMessage();
            customerMessage = "An internal error with hugo occurred, the developer has been made aware";
        }

    }

    private void parseObject() throws Exception{

        JSONObject obj = new JSONObject(fullResponse);
        debugMessage = obj.getString("debug_message");
        customerMessage = obj.getString("customer_message");

        switch(mCall){
            case REGISTER_USER:
                responseObject = parseNewUser();
                break;
            case GET_REALTIME_BASIC:
            case GET_REALTIME_FULL:
                responseObject = parseRealTime();
                break;
            case GET_REALTIME_IN_AREA:
                responseObject = parseStopWithRealTime();
                break;
            case GET_MULTIPLE_REALTIME_FULL:
                responseObject = parseMultipleStops();
                break;
            case GET_JOURNEY:
                responseObject = parseJourney();
                break;
            case GET_WEATHER:
                responseObject = parseWeather();
                break;
            case GET_STOPS:
                responseObject = parseStops();
                break;
            case GET_SERVICES:
                responseObject = parseServices();
                break;
            case CREATE_ALARM:
                responseObject = parseAlarmIdentifier();
                break;
            case GET_ALL_USER_DETAILS:
                responseObject = parseUserDetails();
                break;
            case GET_PLACES:
                responseObject = parseTomTomPlaces();
                break;
        }
    }

    private ArrayList<TomTomPlace> parseTomTomPlaces() throws Exception {

        ArrayList<TomTomPlace> places = new ArrayList<>();
        JSONArray placesArray = new JSONObject(fullResponse).getJSONArray("data");

        for(int i = 0; i < placesArray.length(); i++){

            JSONObject placeObj = placesArray.getJSONObject(i);

            String name = placeObj.getString("name");
            String locality = placeObj.getString("locality");
            double lat = placeObj.getDouble("lat");
            double lng = placeObj.getDouble("lng");
            TomTomPlace place = new TomTomPlace(lat,lng,name,locality);

            places.add(place);
        }

        return places;

    }

    private UserDetails parseUserDetails() throws Exception{

        UserDetails details = new UserDetails();
        JSONObject obj = new JSONObject(fullResponse).getJSONObject("data");

        if(obj.has("push_token") && !obj.isNull("push_token")){
            details.setPushToken(obj.getString("push_token"));
        }

        if(obj.has("user_id") && !obj.isNull("user_id")){
            details.setUserID(Integer.parseInt(obj.getString("user_id")));
        }

        JSONArray alarmsArray = obj.getJSONArray("current_alarms");

        for(int i = 0; i < alarmsArray.length(); i++){

            JSONObject alarmObj = alarmsArray.getJSONObject(i);
            Alarm alarm = new Alarm();
            alarm.setAtcoCode(alarmObj.getString("atco_code"));
            alarm.setMinuteTrigger(alarmObj.getInt("minimum_time"));
            alarm.setAlarmID(alarmObj.getInt("alarm_id"));
            alarm.setServiceName(alarmObj.getString("service_name"));
            alarm.setScheduledTime(LocalDateTime.fromDateFields(new Date(Long.parseLong(alarmObj.getString("departure_time")))));
            details.addAlarm(alarm);
        }

        JSONArray messagesArray = obj.getJSONArray("current_messages");

        for(int i = 0; i < messagesArray.length(); i++){

            JSONObject messageObj = messagesArray.getJSONObject(i);
            Message message;

            if(messageObj.has("message_type") && !messageObj.isNull("message_type")){
                message = new Message(messageObj.getString("message_type"));
            }else{
                message = new Message(Message.MessageType.UNKNOWN);
            }

            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
            message.setDateCreated(LocalDate.parse(messageObj.getString("created_date"), dtf));
            message.setMessageId(Integer.parseInt(messageObj.getString("message_id")));
            message.setmTitle(messageObj.getString("title"));
            message.setmContentText(messageObj.getString("body_text"));
            if(!messageObj.isNull("url")) message.setUrlLink(messageObj.getString("url"));
            if(!messageObj.isNull("image")) message.setImageUrl(messageObj.getString("image"));
            message.setmDateActiveFrom(LocalDate.parse(messageObj.getString("valid_from"), dtf));
            message.setmDateActiveTo(LocalDate.parse(messageObj.getString("valid_to"), dtf));
            message.setmBeenRead(messageObj.getBoolean("been_read"));
            details.addMessage(message);

        }

        JSONArray alertsArray = obj.getJSONArray("current_alerts");

        for(int i = 0; i < alertsArray.length(); i++){

            JSONObject alertObj = alertsArray.getJSONObject(i);
            Message alert = new Message(Message.MessageType.TRAVEL_ALERT);

            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
            alert.setmDateActiveFrom(LocalDate.parse(alertObj.getString("start"), dtf));
            alert.setmDateActiveTo(LocalDate.parse(alertObj.getString("end"), dtf));
            alert.setmContentText(alertObj.getString("message"));
            alert.setSeverity(Integer.parseInt(alertObj.getString("severity")));
            alert.setmTitle(alertObj.getString("service_impacted"));
            alert.setmBeenRead(false);
            alert.setImageUrl(null);
            alert.setMessageId(Integer.parseInt(alertObj.getString("id")));
            alert.setDateCreated(LocalDate.parse(alertObj.getString("start"), dtf));
            details.addAlert(alert);

        }

        return details;

    }

    private Integer parseAlarmIdentifier() throws Exception{

        JSONObject obj = new JSONObject(fullResponse).getJSONObject("data");
        return Integer.parseInt(obj.getString("alarm_identifier"));
    }

    private Object parseMultipleStops() throws Exception{

        JSONObject obj = new JSONObject(fullResponse);
        JSONArray objArray = obj.getJSONArray("data");
        JSONObject dataObject;
        ArrayList<Stop> stops = new ArrayList<>();

        for(int i = 0; i < objArray.length(); i++){

            dataObject = objArray.getJSONObject(i);
            Stop stop = new Stop();
            stop.setAtcoCode(dataObject.getString("atcoCode"));

            JSONArray predictionArray = dataObject.getJSONArray("predictions");
            ArrayList<RealtimePrediction> predictions = new ArrayList<>();

            for(int x = 0; x < predictionArray.length(); x++){
                predictions.add(parsePrediction(predictionArray.getJSONObject(x)));
            }

            stop.setPredictions(predictions);
            stops.add(stop);
        }

        return stops;


    }

    private ArrayList<Service> parseServices() throws Exception {

        JSONArray obj = new JSONObject(fullResponse).getJSONArray("data");
        ArrayList<Service> serviceList = new ArrayList<>();

        for(int i = 0; i < obj.length(); i++){

            JSONObject serviceObj = obj.getJSONObject(i);
            Service service = new Service();
            service.setServiceName(serviceObj.getString("service_name"));
            service.setServiceColour(Color.parseColor(serviceObj.getString("service_colour")));
            service.setSubscribed(serviceObj.getString("subscribed").equalsIgnoreCase("1"));
            service.setOperator(serviceObj.getString("operator"));
            serviceList.add(service);
        }

        return serviceList;

    }

    private WeatherHolder parseWeather() throws Exception {

        JSONObject obj = new JSONObject(fullResponse).getJSONArray("data").getJSONObject(0);
        WeatherHolder weather = new WeatherHolder();
        weather.setCurrentTemp(obj.getString("current_temperature"));
        weather.setForecast(obj.getString("forecast"));
        weather.setCurrentWeather(obj.getString("current_weather"));
        weather.setIconUrl(obj.getString("icon"));
        return weather;

    }

    private ArrayList<Stop> parseStops() throws Exception{

        JSONArray objArray = new JSONObject(fullResponse).getJSONArray("data");
        ArrayList<Stop> stops = new ArrayList<>();

        for(int i = 0; i < objArray.length(); i++){

            JSONObject stopObj = objArray.getJSONObject(i);
            Stop stop = new Stop();
            stop.setAtcoCode(stopObj.getString("atcoCode"));
            stop.setEnabled(stopObj.getInt("enabled") != 0);
            stop.setPosition(new LatLng(stopObj.getDouble("latitude"), stopObj.getDouble("longitude")));
            stop.setStopName(stopObj.getString("name"));
            stop.setLocality(stopObj.getString("locality"));
            stop.setVersion(stopObj.getInt("version"));
            stops.add(stop);

        }

        return stops;

    }

    private ArrayList<Stop> parseStopWithRealTime() throws Exception {

        JSONObject obj = new JSONObject(fullResponse);
        JSONArray objArray = obj.getJSONArray("data");
        JSONObject dataObject;
        ArrayList<Stop> stops = new ArrayList<>();


        for(int i = 0; i < objArray.length(); i++){

            dataObject = objArray.getJSONObject(i);
            Stop stop = parseStop(dataObject.getJSONObject("stopDetails"));

            JSONArray predictionArray = dataObject.getJSONArray("predictions");
            ArrayList<RealtimePrediction> predictions = new ArrayList<>();

            for(int x = 0; x < predictionArray.length(); x++){
                predictions.add(parsePrediction(predictionArray.getJSONObject(x)));
            }

            stop.setPredictions(predictions);
            stops.add(stop);
        }

        return stops;


    }

    private Stop parseStop(JSONObject stopDetails) throws Exception{

        Stop stop = new Stop();
        stop.setAtcoCode(stopDetails.getString("atcoCode"));
        stop.setEnabled(stopDetails.getInt("enabled") == 1);
        stop.setPosition(new LatLng(stopDetails.getDouble("latitude"),stopDetails.getDouble("longitude")));
        stop.setStopName(stopDetails.getString("name"));
        stop.setLocality(stopDetails.getString("locality"));
        stop.setDistance(stopDetails.getInt("distance"));
        stop.setVersion(stopDetails.getInt("version"));
        return stop;

    }

    private RealtimePrediction parsePrediction(JSONObject item) throws Exception{

        RealtimePrediction prediction = new RealtimePrediction();

        prediction.setServiceName(item.getString("service_name"));
        prediction.setServiceColour(item.getString("service_colour"));
        prediction.setJourneyDestination(item.getString("journey_destination"));
        prediction.setPredictionDisplay(item.getString("prediction_display"));

        if(item.has("status")) prediction.setWorking(item.getBoolean("status"));
        if(item.has("stop_ref")) prediction.setStopCode(item.getString("stop_ref"));
        if(item.has("journey_origin")) prediction.setJourneyOrigin(item.getString("journey_origin"));
        if(item.has("vehicle_location_lat") && item.has("vehicle_location_lng")) prediction.setVehiclePosition(item.getDouble("vehicle_location_lat"),item.getDouble("vehicle_location_lng"));
        if(item.has("destination_arrival_time")) prediction.setDestinationArrivalTime(LocalDateTime.fromDateFields(new Date(item.getLong("destination_arrival_time") * 1000L)));
        if(item.has("origin_departure_time")) prediction.setOriginDepartureTime(LocalDateTime.fromDateFields(new Date(item.getLong("origin_departure_time") * 1000L)));
        if(item.has("in_congestion")) prediction.setInCongestion(item.getBoolean("in_congestion"));
        if(item.has("at_stop")) prediction.setAtStop(item.getBoolean("at_stop"));
        if(item.has("scheduled_departure_time")) prediction.setScheduledDepartureTime(LocalDateTime.fromDateFields(new Date(item.getLong("scheduled_departure_time") * 1000L)));
        if(item.has("actual_departure_time")) prediction.setActualDepartureTime(LocalDateTime.fromDateFields(new Date(item.getLong("actual_departure_time") * 1000L)));
        if(item.has("prediction_in_seconds")) prediction.setPredictionInSeconds(item.getInt("prediction_in_seconds"));
        if(item.has("cancelled_service")) prediction.setCancelledService(item.getBoolean("cancelled_service"));
        if(item.has("cancelled_reason")) prediction.setCancelledReason(item.getString("cancelled_reason"));
        if(item.has("driver_name")) prediction.setDriverName(item.getString("driver_name"));
        if(item.has("vehicle_colour")) prediction.setVehicleColour(item.getString("vehicle_colour"));
        if(item.has("vehicle_number")) prediction.setVehicleNumber(Integer.parseInt(item.getString("vehicle_number")));
        if(item.has("has_wifi")) prediction.setHasWifi(item.getInt("has_wifi") == 1);
        if(item.has("has_usb")) prediction.setHasUsb(item.getInt("has_usb") == 1);
        if(item.has("has_mango")) prediction.setHasMango(item.getInt("has_mango") == 1);

        return prediction;
    }

    public Object getResponseObject(){
        return this.responseObject;
    }

    public String getCustomerMessage(){
        return this.customerMessage;
    }

    public String getErrorMessage(){
        return this.debugMessage;
    }

    private String parseNewUser() throws Exception{

        JSONObject obj = new JSONObject(fullResponse);
        obj = obj.getJSONObject("data");
        return obj.getString("user_token");

    }

    private ArrayList<RealtimePrediction> parseRealTime() throws Exception{

        JSONObject obj = new JSONObject(fullResponse);
        JSONArray arr = obj.getJSONArray("data");
        ArrayList<RealtimePrediction> predictions = new ArrayList<>();

        for(int index = 0; index < arr.length(); index++){

            JSONObject item = arr.getJSONObject(index);
            predictions.add(parsePrediction(item));
        }

        return predictions;
    }

    private ArrayList<Journey> parseJourney() throws Exception{

        JSONObject obj = new JSONObject(fullResponse);
        JSONArray arr = obj.getJSONArray("data");

        ArrayList<Journey> journeys = new ArrayList<>();

        for(int index = 0; index < arr.length(); index++){

            JSONObject item = arr.getJSONObject(index);
            Journey journey = new Journey(item);
            journeys.add(journey);

        }

        return journeys;

    }

}
