package uk.co.trentbarton.hugo.dataholders;

public class WeatherHolder {

    private String currentTemp;
    private String forecast;
    private String currentWeather;
    private String iconUrl;
    private String[] shortwords = {"Sunny","Cloudy","Overcast","Mist","Rain","Snow","Sleet","Drizzle","Thundery","Blizzard","Fog","Ice"};

    public String getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(String currentTemp) {
        this.currentTemp = currentTemp + "\u2103";
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast + "\u2103";
    }

    public String getCurrentTempAndWeather() {

        if(currentWeather.length() > 10){
            String[] words = currentWeather.split(" ");
            return currentTemp + " - " + getShortDescription(words);
        }else{
            return currentTemp + " - " + currentWeather;
        }
    }

    private String getShortDescription(String[] words) {

        for(String word : words){

            for(String shortWord: shortwords){

                if(word.equalsIgnoreCase(shortWord)){
                    return shortWord;
                }
            }
        }

        return "";

    }

    public void setCurrentWeather(String currentWeather) {
        this.currentWeather = currentWeather;
    }

    public String getIconUrl() {
        if(iconUrl.contains("//")){
            return iconUrl;
        }else{
            return "//" + iconUrl;
        }

    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
