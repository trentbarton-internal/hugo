package uk.co.trentbarton.hugo.datapersistence;

import java.util.ArrayList;

import uk.co.trentbarton.hugo.dataholders.Journey;
import uk.co.trentbarton.hugo.dataholders.Message;

public class GlobalData {

    private int lastPosition;
    private static GlobalData instance;
    private ArrayList<Journey> journeyData;
    private ArrayList<Message> deletedmessages;

    public enum Fragments{
        JOURNEY_PLANNER, LIVE, MESSAGES, SETTINGS
    }

    private GlobalData() {
        deletedmessages = new ArrayList<>();
    }

    public static GlobalData getInstance(){
        if(instance == null){
            instance = new GlobalData();
            instance.lastPosition = 0;
        }

        return instance;
    }

    public void setNavigateToFragmentPosition(int position){this.lastPosition = position;}

    /**
     * Returns the Fragment enum depicting which fragment should be loaded first {@link Fragments}
     * and then removes this from memory by setting the value to the default
     * which is (@Link {@link Fragments#LIVE}}
     * @return The fragment enum which should be loaded when MainNavigation is started
     **/
    public int getNavigateToFragmentPosition(){
        return lastPosition;
    }

    public void setJourneyData(ArrayList<Journey> journeyData){
        this.journeyData = journeyData;
    }

    public ArrayList<Journey> getJourneyData(){
        return this.journeyData;
    }

    public void addNewDeletedMessage(Message message){
        deletedmessages.add(message);
    }

    public boolean isMessageTempDeleted(Message message){

        for(Message msg : deletedmessages){
            if(msg.getMessageId() == message.getMessageId()){
                return true;
            }
        }

        return false;

    }
}
