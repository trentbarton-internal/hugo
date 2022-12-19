package uk.co.trentbarton.hugo.datapersistence;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import uk.co.trentbarton.hugo.dataholders.Stop;
import au.com.bytecode.opencsv.CSVReader;
import uk.co.trentbarton.hugo.R;

public class FileHandler {

    public static ArrayList<Stop> readInStopList(Context context){

        ArrayList<Stop> stopList = new ArrayList<>();

        try{
            InputStream stream = context.getResources().openRawResource(R.raw.stops);
            InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"));
            List<String[]> csv = new CSVReader(reader).readAll();

            for(String[] stringArray : csv){
                Stop stop = new Stop();
                stop.setAtcoCode(stringArray[0]);
                stop.setEnabled(stringArray[1].equals("1"));
                stop.setPosition(new LatLng(Double.parseDouble(stringArray[2]),Double.parseDouble(stringArray[3])));
                stop.setStopName(stringArray[4]);
                stop.setLocality(stringArray[5]);
                stop.setDistance(0);
                stopList.add(stop);
            }

        }catch(Exception e){
            return new ArrayList<Stop>();
        }

        return stopList;
    }
}
