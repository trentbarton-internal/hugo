package uk.co.trentbarton.hugo.datapersistence;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import uk.co.trentbarton.hugo.dataholders.Stop;
import uk.co.trentbarton.hugo.dataholders.TomTomPlace;


public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hugo_db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_STOPS = "Stops";
    private static final String TABLE_FAVOURITE_STOPS = "favouriteStops";
    private static final String TABLE_FAVOURITE_PLACES = "favouritePlaces";
    private static final String TABLE_RECENT_STOPS = "recentStops";
    private static final String TABLE_RECENT_JOURNEYS = "recentJourneys";
    private static final String TABLE_RECENT_PLACES = "recentPlaces";
    private static final String TABLE_FAVOURITE_JOURNEYS = "favouriteJourneys";
    private static final String TABLE_MATCHED_PLACES = "matchedPlaces";
    private static final String TABLE_MATCHED_PLACES_DETAILS = "matchedPlaceDetails";
    private static final String TABLE_MESSAGES = "messages";
    private final Context mContext;
    public static final double MULTIPLIER = 100000000.0;
    private static final int MAX_RECENTS = 5;

    private enum STOP_COLUMN_NAME{
        ID, STOP_NAME, LOCALITY, LONGITUDE, LATITUDE, ATCO_CODE, ENABLED
    }

    private enum FAVOURITE_STOPS_COLUMN_NAME{
        ID, STOP_ID, OVERRIDE_NAME, ORDER_NUM, SENT
    }

    private enum FAVOURITE_JOURNEYS_COLUMN_NAME{
        ID, TO_NAME, TO_OVERRIDE_NAME, TO_LAT, TO_LNG, FROM_NAME, FROM_OVERRIDE_NAME, FROM_LAT, FROM_LNG, SENT
    }

    private enum FAVOURITE_PLACES_COLUMN_NAME{
        ID, PLACE_NAME,OVERRIDE_NAME,LOCALITY,LATITUDE,LONGITUDE, SENT
    }

    private enum RECENT_PLACES_COLUMN{
        ID, PLACE_NAME,LOCALITY,LATITUDE,LONGITUDE,ORDER_NUM
    }

    private enum RECENT_STOPS_COLUMN{
        ID, STOP_ID, ORDER_NUM
    }

    private enum MATCHED_PLACES_COLUMN{
        ID, STRING_REF, FROM_LAT, FROM_LNG, CREATION_DATE
    }

    private enum MATCHED_PLACES_DETAILS_COLUMN{
        ID, REF_ID, NAME, LOCALITY, LATITUDE, LONGITUDE
    }

    private enum MESSAGES_COLUMN{
        ID, TITLE, BODY, WEB_URL, DATE_TEXT, TOPIC, SEEN
    }

    private static final String CREATE_STOPS_TABLE = "CREATE TABLE " + TABLE_STOPS + "("
            + STOP_COLUMN_NAME.ID.name() + " INTEGER PRIMARY KEY,"
            + STOP_COLUMN_NAME.STOP_NAME.name() + " TEXT,"
            + STOP_COLUMN_NAME.LOCALITY.name() + " TEXT,"
            + STOP_COLUMN_NAME.LONGITUDE.name() + " INTEGER,"
            + STOP_COLUMN_NAME.LATITUDE.name() + " INTEGER,"
            + STOP_COLUMN_NAME.ATCO_CODE.name() + " TEXT,"
            + STOP_COLUMN_NAME.ENABLED.name() + " INTEGER,"
            + "UNIQUE (" + STOP_COLUMN_NAME.ATCO_CODE.name() + ") ON CONFLICT REPLACE)";

    private static final String CREATE_FAVOURITE_PLACES_TABLE = "CREATE TABLE " + TABLE_FAVOURITE_PLACES + "("
            + FAVOURITE_PLACES_COLUMN_NAME.ID.name() + " INTEGER PRIMARY KEY,"
            + FAVOURITE_PLACES_COLUMN_NAME.PLACE_NAME.name() + " TEXT,"
            + FAVOURITE_PLACES_COLUMN_NAME.LOCALITY.name() + " TEXT,"
            + FAVOURITE_PLACES_COLUMN_NAME.LATITUDE.name() + " INTEGER,"
            + FAVOURITE_PLACES_COLUMN_NAME.LONGITUDE.name() + " INTEGER,"
            + FAVOURITE_PLACES_COLUMN_NAME.OVERRIDE_NAME.name() + " TEXT, "
            + FAVOURITE_PLACES_COLUMN_NAME.SENT.name() + " INTEGER, "
            + "UNIQUE (" + FAVOURITE_PLACES_COLUMN_NAME.OVERRIDE_NAME.name() + ") ON CONFLICT REPLACE)";

    private static final String CREATE_FAVOURITE_STOPS_TABLE = "CREATE TABLE " + TABLE_FAVOURITE_STOPS + "("
            + FAVOURITE_STOPS_COLUMN_NAME.ID.name() + " INTEGER PRIMARY KEY,"
            + FAVOURITE_STOPS_COLUMN_NAME.STOP_ID.name() + " INTEGER,"
            + FAVOURITE_STOPS_COLUMN_NAME.OVERRIDE_NAME.name() + " TEXT,"
            + FAVOURITE_STOPS_COLUMN_NAME.ORDER_NUM.name() + " INTEGER,"
            + FAVOURITE_STOPS_COLUMN_NAME.SENT.name() + " INTEGER, "
            + " FOREIGN KEY (" + FAVOURITE_STOPS_COLUMN_NAME.STOP_ID.name() + ") REFERENCES "
            + TABLE_STOPS + "(" + STOP_COLUMN_NAME.ID.name() + ") "
            + "UNIQUE(" + FAVOURITE_STOPS_COLUMN_NAME.OVERRIDE_NAME.name() + ") ON CONFLICT REPLACE);";

    private static final String CREATE_RECENT_PLACES_TABLE = "CREATE TABLE " + TABLE_RECENT_PLACES + "("
            + RECENT_PLACES_COLUMN.ID.name() + " INTEGER PRIMARY KEY,"
            + RECENT_PLACES_COLUMN.PLACE_NAME.name() + " TEXT,"
            + RECENT_PLACES_COLUMN.LOCALITY.name() + " TEXT,"
            + RECENT_PLACES_COLUMN.LATITUDE.name() + " INTEGER,"
            + RECENT_PLACES_COLUMN.LONGITUDE.name() + " INTEGER,"
            + RECENT_PLACES_COLUMN.ORDER_NUM.name() + " INTEGER)";

    private static final String CREATE_MATCHED_PLACES_TABLE = "CREATE TABLE " + TABLE_MATCHED_PLACES + "("
            + MATCHED_PLACES_COLUMN.ID.name() + " INTEGER PRIMARY KEY,"
            + MATCHED_PLACES_COLUMN.FROM_LAT.name() + " INTEGER,"
            + MATCHED_PLACES_COLUMN.FROM_LNG.name() + " INTEGER,"
            + MATCHED_PLACES_COLUMN.STRING_REF.name() + " TEXT,"
            + MATCHED_PLACES_COLUMN.CREATION_DATE.name() + " INTEGER);";

    private static final String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
            + MESSAGES_COLUMN.ID.name() + " INTEGER PRIMARY KEY,"
            + MESSAGES_COLUMN.TITLE.name() + " TEXT,"
            + MESSAGES_COLUMN.BODY.name() + " TEXT,"
            + MESSAGES_COLUMN.WEB_URL.name() + " TEXT,"
            + MESSAGES_COLUMN.DATE_TEXT.name() + " TEXT,"
            + MESSAGES_COLUMN.TOPIC.name() + " TEXT,"
            + MESSAGES_COLUMN.SEEN.name() + " INTEGER);";

    /*private static final String CREATE_MATCHED_PLACES_DETAILS_TABLE = "CREATE TABLE " + TABLE_MATCHED_PLACES_DETAILS + "("
            + MATCHED_PLACES_DETAILS_COLUMN.ID.name() + " INTEGER PRIMARY KEY,"
            + MATCHED_PLACES_DETAILS_COLUMN.REF_ID.name() + " INTEGER,"
            + MATCHED_PLACES_DETAILS_COLUMN.NAME.name() + " TEXT,"
            + MATCHED_PLACES_DETAILS_COLUMN.LOCALITY.name() + " TEXT,"
            + MATCHED_PLACES_DETAILS_COLUMN.GOOGLEID.name() + " TEXT,"
            + " FOREIGN KEY (" + MATCHED_PLACES_DETAILS_COLUMN.REF_ID.name() + ") REFERENCES "
            + TABLE_MATCHED_PLACES + "(" + MATCHED_PLACES_COLUMN.ID.name() + "));";*/

    private static final String CREATE_MATCHED_PLACES_DETAILS_TABLE = "CREATE TABLE " + TABLE_MATCHED_PLACES_DETAILS + "("
            + MATCHED_PLACES_DETAILS_COLUMN.ID.name() + " INTEGER PRIMARY KEY,"
            + MATCHED_PLACES_DETAILS_COLUMN.REF_ID.name() + " INTEGER,"
            + MATCHED_PLACES_DETAILS_COLUMN.NAME.name() + " TEXT,"
            + MATCHED_PLACES_DETAILS_COLUMN.LOCALITY.name() + " TEXT,"
            + MATCHED_PLACES_DETAILS_COLUMN.LATITUDE.name() + " INTEGER,"
            + MATCHED_PLACES_DETAILS_COLUMN.LONGITUDE.name() + " INTEGER,"
            + " FOREIGN KEY (" + MATCHED_PLACES_DETAILS_COLUMN.REF_ID.name() + ") REFERENCES "
            + TABLE_MATCHED_PLACES + "(" + MATCHED_PLACES_COLUMN.ID.name() + "));";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_STOPS_TABLE);
        db.execSQL(CREATE_FAVOURITE_PLACES_TABLE);
        db.execSQL(CREATE_FAVOURITE_STOPS_TABLE);
        db.execSQL(CREATE_RECENT_PLACES_TABLE);
        db.execSQL(CREATE_MATCHED_PLACES_TABLE);
        db.execSQL(CREATE_MATCHED_PLACES_DETAILS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
        writeStopsToDatabase(FileHandler.readInStopList(mContext), db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATCHED_PLACES_DETAILS);
        db.execSQL(CREATE_MATCHED_PLACES_DETAILS_TABLE);

    }


    /*

    Initialization methods to setup data from CSV Files

     */

    private boolean writeStopsToDatabase(ArrayList<Stop> stops, SQLiteDatabase db){

        //ContentValues values;
        String sql = String.format(Locale.ENGLISH,"INSERT INTO %s (%s,%s,%s,%s,%s,%s) values (?,?,?,?,?,?)",
                TABLE_STOPS,
                STOP_COLUMN_NAME.ATCO_CODE.name(),
                STOP_COLUMN_NAME.ENABLED.name(),
                STOP_COLUMN_NAME.LOCALITY.name(),
                STOP_COLUMN_NAME.LATITUDE.name(),
                STOP_COLUMN_NAME.LONGITUDE.name(),
                STOP_COLUMN_NAME.STOP_NAME.name());
        db.beginTransaction();
        SQLiteStatement stmt = db.compileStatement(sql);

        for(Stop stop : stops){

            stmt.bindString(1,stop.getAtcoCode());
            stmt.bindLong(2,(stop.isEnabled() ? 1 : 0));
            stmt.bindString(3, stop.getLocality());
            stmt.bindLong(4,(long)(stop.getPosition().latitude * MULTIPLIER));
            stmt.bindLong(5,(long)(stop.getPosition().longitude * MULTIPLIER));
            stmt.bindString(6, stop.getStopName());
            long entryId = stmt.executeInsert();
            stmt.clearBindings();

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        return true;

    }


    /*
     *
     *STOPS - Methods relating to stop CRUD
     *
     */

    public boolean isStopFavourite(String atcoCode) {

        SQLiteDatabase db = getReadableDatabase();
        int id = getStopId(atcoCode, db);

        String selectQuery = "SELECT * FROM " + TABLE_FAVOURITE_STOPS + " WHERE " + FAVOURITE_STOPS_COLUMN_NAME.STOP_ID + "=" + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int results = cursor.getCount();
        cursor.close();
        return results != 0;

    }

    private int getStopId(String atcoCode, SQLiteDatabase db) {

        int id = -1;

        String query = String.format(Locale.ENGLISH,"SELECT %s FROM %s WHERE %s='%s'",
                STOP_COLUMN_NAME.ID.name(),
                TABLE_STOPS,
                STOP_COLUMN_NAME.ATCO_CODE.name(),
                atcoCode);

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return id;
    }

    public void updateFavouriteStops(Map<String, Stop> stopsMap){

        //Clear the stops database
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FAVOURITE_STOPS,null,null);

        ContentValues values;

        for(String key : stopsMap.keySet()){

            Stop stop = stopsMap.get(key);

            int stopId = getStopId(stop.getAtcoCode(), db);
            values = new ContentValues();
            values.put(FAVOURITE_STOPS_COLUMN_NAME.STOP_ID.name(),stopId);
            values.put(FAVOURITE_STOPS_COLUMN_NAME.OVERRIDE_NAME.name(),stop.getOverrideName());
            db.insertWithOnConflict(TABLE_FAVOURITE_STOPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }

        db.close();
    }

    public void updateStops(ArrayList<Stop> stopsList){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values;

        for(Stop stop : stopsList){

            values = new ContentValues();
            values.put(STOP_COLUMN_NAME.ATCO_CODE.name(),stop.getAtcoCode());
            values.put(STOP_COLUMN_NAME.ENABLED.name(),(stop.isEnabled() ? 1 : 0));
            values.put(STOP_COLUMN_NAME.LOCALITY.name(), stop.getLocality());
            values.put(STOP_COLUMN_NAME.LATITUDE.name(),(stop.getPosition().latitude * MULTIPLIER));
            values.put(STOP_COLUMN_NAME.LONGITUDE.name(),(stop.getPosition().longitude * MULTIPLIER));
            values.put(STOP_COLUMN_NAME.STOP_NAME.name(),stop.getStopName());
            db.insertWithOnConflict(TABLE_STOPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }

        db.close();
    }

    public boolean deleteFavouriteStop(Stop stop){

        SQLiteDatabase db = getWritableDatabase();
        boolean value =  db.delete(TABLE_FAVOURITE_STOPS,FAVOURITE_STOPS_COLUMN_NAME.STOP_ID.name() + "=?", new String[]{String.valueOf(stop.getStopId())}) > 0;
        db.close();
        return value;
    }

    public void addRecentStop(Stop stop){

        SQLiteDatabase db = getWritableDatabase();
        String query = "Select * from " + TABLE_RECENT_STOPS;
        ArrayList<Long[]> entries = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                if(stop.getStopId() == cursor.getInt(1)){
                    //This means the stop is already stored as recent so exit this function
                    return;
                }

                Long[] entry = new Long[2];
                entry[0] = cursor.getLong(1);
                entry[1] = 1 + (cursor.getLong(2));

                if(entry[1] <= MAX_RECENTS){
                    entries.add(entry);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        Long[] entry = new Long[2];
        entry[0] = stop.getStopId();
        entry[1] = 1L;
        entries.add(entry);
        db.delete(TABLE_RECENT_STOPS,null, null);

        ContentValues values;

        for(Long[] stopEntry : entries){

            values = new ContentValues();
            values.put(RECENT_STOPS_COLUMN.STOP_ID.name(),stopEntry[0]);
            values.put(RECENT_STOPS_COLUMN.ORDER_NUM.name(),stopEntry[1]);
            db.insertWithOnConflict(TABLE_RECENT_STOPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
    }

    public boolean deleteRecentStop(int stopID){

        SQLiteDatabase db = getWritableDatabase();
        boolean value =  db.delete(TABLE_RECENT_STOPS,RECENT_STOPS_COLUMN.STOP_ID.name() + "=?", new String[]{String.valueOf(stopID)}) > 0;

        String query = String.format(Locale.ENGLISH,"Select * from %s ORDER BY %s ASC",
                TABLE_RECENT_STOPS,
                RECENT_STOPS_COLUMN.ORDER_NUM.name());
        ArrayList<Integer[]> entries = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);
        int i = 1;

        if (cursor.moveToFirst()) {
            do {
                Integer[] entry = new Integer[2];
                entry[0] = cursor.getInt(1);
                entry[1] = i;
                i++;

                if(entry[1] <= MAX_RECENTS){
                    entries.add(entry);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        db.delete(TABLE_RECENT_STOPS,null, null);
        ContentValues values;

        for(Integer[] stopEntry : entries){

            values = new ContentValues();
            values.put(RECENT_STOPS_COLUMN.STOP_ID.name(),stopEntry[0]);
            values.put(RECENT_STOPS_COLUMN.ORDER_NUM.name(),stopEntry[1]);
            db.insertWithOnConflict(TABLE_RECENT_STOPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
        return value;

    }

    public ArrayList<Stop> getAllStops(){

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Stop> stopList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STOPS + " WHERE " + STOP_COLUMN_NAME.ENABLED.name() + ">0";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Stop stop = new Stop();
                stop.setStopId(cursor.getInt(0));
                stop.setStopName(cursor.getString(1));
                stop.setLocality(cursor.getString(2));
                stop.setPosition(new LatLng((cursor.getDouble(4) / (double) MULTIPLIER), (cursor.getDouble(3) / (double) MULTIPLIER)));
                stop.setAtcoCode(cursor.getString(5));
                stop.setEnabled(true);
                stopList.add(stop);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return stopList;
    }

    public boolean saveFavouriteStop(Stop stop, boolean sendToServer){

        SQLiteDatabase db = getWritableDatabase();

        if(stop.getStopId() == 0){
            stop.setStopId(getStopId(stop.getAtcoCode(), db));
        }

        try{
            ContentValues values = new ContentValues();
            values.put(FAVOURITE_STOPS_COLUMN_NAME.STOP_ID.name(),stop.getStopId());
            values.put(FAVOURITE_STOPS_COLUMN_NAME.ORDER_NUM.name(),500);
            values.put(FAVOURITE_STOPS_COLUMN_NAME.OVERRIDE_NAME.name(), stop.getOverrideName());
            db.insertWithOnConflict(TABLE_FAVOURITE_STOPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            //db.close();
            return true;
        }catch(Exception e){
            Toast.makeText(mContext, "FAILED",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public ArrayList<Stop> getFavouriteStops(){

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Stop> stopList = new ArrayList<>();
        String selectQuery = String.format("SELECT fs.%s, fs.%s, s.%s, s.%s, s.%s, s.%s, s.%s, s.%s, s.%s FROM %s fs JOIN %s s ON s.%s = fs.%s",
                FAVOURITE_STOPS_COLUMN_NAME.OVERRIDE_NAME.name(),
                FAVOURITE_STOPS_COLUMN_NAME.ORDER_NUM.name(),
                STOP_COLUMN_NAME.ID.name(),
                STOP_COLUMN_NAME.STOP_NAME.name(),
                STOP_COLUMN_NAME.LOCALITY.name(),
                STOP_COLUMN_NAME.LATITUDE.name(),
                STOP_COLUMN_NAME.LONGITUDE.name(),
                STOP_COLUMN_NAME.ATCO_CODE.name(),
                STOP_COLUMN_NAME.ENABLED.name(),
                TABLE_FAVOURITE_STOPS,
                TABLE_STOPS,
                STOP_COLUMN_NAME.ID.name(),
                FAVOURITE_STOPS_COLUMN_NAME.STOP_ID.name());
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Stop stop = new Stop();
                stop.setOverrideName(cursor.getString(0));
                stop.setOrderNumber(cursor.getInt(1));
                stop.setStopId(cursor.getInt(2));
                stop.setStopName(cursor.getString(3));
                stop.setLocality(cursor.getString(4));
                stop.setPosition(new LatLng((cursor.getDouble(5)/(double) MULTIPLIER), (cursor.getDouble(6)/(double) MULTIPLIER)));
                stop.setAtcoCode(cursor.getString(7));
                stop.setEnabled(cursor.getInt(8)!= 0);
                stopList.add(stop);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return stopList;
    }

    public ArrayList<Stop> getRecentStops(){

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Stop> stopList = new ArrayList<>();
        String selectQuery = String.format("SELECT fs.%s, s.%s, s.%s, s.%s, s.%s, s.%s, s.%s, s.%s FROM %s fs JOIN %s s ON s.%s = fs.%s",
                RECENT_STOPS_COLUMN.STOP_ID.name(),
                STOP_COLUMN_NAME.ID.name(),
                STOP_COLUMN_NAME.STOP_NAME.name(),
                STOP_COLUMN_NAME.LOCALITY.name(),
                STOP_COLUMN_NAME.LATITUDE.name(),
                STOP_COLUMN_NAME.LONGITUDE.name(),
                STOP_COLUMN_NAME.ATCO_CODE.name(),
                STOP_COLUMN_NAME.ENABLED.name(),
                TABLE_RECENT_STOPS,
                TABLE_STOPS,
                STOP_COLUMN_NAME.ID.name(),
                RECENT_STOPS_COLUMN.STOP_ID.name());
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Stop stop = new Stop();
                stop.setOrderNumber(0);
                stop.setStopId(cursor.getInt(1));
                stop.setStopName(cursor.getString(2));
                stop.setLocality(cursor.getString(3));
                stop.setPosition(new LatLng(cursor.getDouble(4) / MULTIPLIER, cursor.getDouble(5) / MULTIPLIER));
                stop.setAtcoCode(cursor.getString(6));
                stop.setEnabled(cursor.getInt(7)!= 0);
                stopList.add(stop);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return stopList;
    }

    public ArrayList<Stop> getAllStopsInArea(LatLng position){

        int BOUNDARY_SIZE = 700000;

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Stop> stopList = new ArrayList<>();
        String selectQuery = String.format(Locale.ENGLISH,"SELECT * FROM %s  WHERE %s > 0 AND %s > %d AND %s < %d AND %s > %d AND %s < %d",
                TABLE_STOPS,
                STOP_COLUMN_NAME.ENABLED.name(),
                STOP_COLUMN_NAME.LATITUDE,
                (long)((position.latitude * MULTIPLIER) - BOUNDARY_SIZE),
                STOP_COLUMN_NAME.LATITUDE,
                (long)((position.latitude * MULTIPLIER) + BOUNDARY_SIZE),
                STOP_COLUMN_NAME.LONGITUDE,
                (long)((position.longitude * MULTIPLIER) - BOUNDARY_SIZE),
                STOP_COLUMN_NAME.LONGITUDE,
                (long)((position.longitude * MULTIPLIER) + BOUNDARY_SIZE));
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Stop stop = new Stop();
                stop.setStopId(cursor.getInt(0));
                stop.setStopName(cursor.getString(1));
                stop.setLocality(cursor.getString(2));
                stop.setPosition(new LatLng(cursor.getDouble(4) / MULTIPLIER, cursor.getDouble(3) / MULTIPLIER));
                stop.setAtcoCode(cursor.getString(5));
                stop.setEnabled(true);
                stopList.add(stop);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stopList;
    }

    public Stop getStopFromStopCode(String atcoCode){

        SQLiteDatabase db;

        if(atcoCode == null || atcoCode.equalsIgnoreCase("")){
            return null;
        }

        try{
            db = getReadableDatabase();
        }catch(Exception e){
            return null;
        }

        String selectQuery = "SELECT * FROM " + TABLE_STOPS + " WHERE " + STOP_COLUMN_NAME.ATCO_CODE.name() + "=?";
        Cursor cursor = db.rawQuery(selectQuery, new String[] {atcoCode});
        Stop stop = null;

        if(cursor.getCount() == 0){
            return null;
        }

        if (cursor.moveToFirst()) {
            do {
                stop = new Stop();
                stop.setStopId(cursor.getInt(0));
                stop.setStopName(cursor.getString(1));
                stop.setLocality(cursor.getString(2));
                stop.setPosition(new LatLng((cursor.getDouble(4) / (double) MULTIPLIER), (cursor.getDouble(3) / (double) MULTIPLIER)));
                stop.setAtcoCode(cursor.getString(5));
                stop.setEnabled(true);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return stop;
    }

    public boolean isStopFavourite(Stop stop){

        if(stop.getStopId() == 0){
            return isStopFavourite(stop.getAtcoCode());
        }

        boolean containsStop = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVOURITE_STOPS + " WHERE " + FAVOURITE_STOPS_COLUMN_NAME.STOP_ID + " =?", new String[]{String.valueOf(stop.getStopId())});

        if(cursor.getCount() > 0){
            containsStop = true;
        }

        cursor.close();
        db.close();
        return containsStop;

    }

    /*
     *
     *
     *PLACES - Methods relating to place CRUD
     *
     *
     */

    public boolean deleteFavouritePlace(TomTomPlace place){

        try{
            SQLiteDatabase db = getWritableDatabase();
            boolean value =  db.delete(TABLE_FAVOURITE_PLACES,FAVOURITE_PLACES_COLUMN_NAME.OVERRIDE_NAME.name() + "=?", new String[]{String.valueOf(place.getName())}) > 0;
            //setJourneyOverrideToNullFromPlace(place.getName(), db);
            db.close();
            return value;
        }catch(Exception e){
            return false;
        }

    }

    public void deleteFavouriteNulls(){

        try{
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_FAVOURITE_PLACES,FAVOURITE_PLACES_COLUMN_NAME.OVERRIDE_NAME.name() + "='null' OR " + FAVOURITE_PLACES_COLUMN_NAME.PLACE_NAME + "='null'",null);
            db.close();

        }catch(Exception ignore){}

    }


    //changed from

    private void setJourneyOverrideToNullFromPlace(String placeName, SQLiteDatabase db) {

        String selection = FAVOURITE_JOURNEYS_COLUMN_NAME.TO_OVERRIDE_NAME.name()  + "=?";
        ContentValues values = new ContentValues();
        values.put(FAVOURITE_JOURNEYS_COLUMN_NAME.TO_OVERRIDE_NAME.name(),"");
        db.update(TABLE_FAVOURITE_JOURNEYS, values, selection, null);

        selection = FAVOURITE_JOURNEYS_COLUMN_NAME.FROM_OVERRIDE_NAME.name()  + "=?";
        values = new ContentValues();
        values.put(FAVOURITE_JOURNEYS_COLUMN_NAME.FROM_OVERRIDE_NAME.name(),"");
        db.update(TABLE_FAVOURITE_JOURNEYS, values, selection, new String[]{placeName});

    }

    private int getPlaceId(TomTomPlace place, SQLiteDatabase db) {

        int id = -1;

        String fromName = place.getName().replaceAll("'","''");

        String query = String.format(Locale.ENGLISH,"SELECT %s FROM %s WHERE %s = '%s'",
                FAVOURITE_PLACES_COLUMN_NAME.ID.name(),
                TABLE_FAVOURITE_PLACES,
                FAVOURITE_PLACES_COLUMN_NAME.OVERRIDE_NAME.name(),
                fromName);

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return id;

    }

    public ArrayList<TomTomPlace> getFavouritePlaces(){

        ArrayList<TomTomPlace> placeList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FAVOURITE_PLACES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                String name = cursor.getString(5);
                String locality = cursor.getString(2);
                double lat = cursor.getDouble(3) / MULTIPLIER;
                double lng = cursor.getDouble(4) / MULTIPLIER;
                TomTomPlace place = new TomTomPlace(lat, lng, name, locality);
                place.setFavourite(true);
                placeList.add(place);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return placeList;

    }

    public boolean saveFavouritePlace(TomTomPlace place, boolean sendToServer){

        SQLiteDatabase db = getWritableDatabase();
        boolean result = saveFavouritePlace(place, db);
        db.close();
        return result;
    }

    private boolean saveFavouritePlace(TomTomPlace place, SQLiteDatabase db){

        if(place.getName().equalsIgnoreCase("work") || place.getName().equalsIgnoreCase("home")){
            place.setName(place.getName().toLowerCase(Locale.ENGLISH));
        }

        int id = getPlaceId(place, db);

        try{
            ContentValues values = new ContentValues();

            if (id != -1) {
                values.put(FAVOURITE_PLACES_COLUMN_NAME.ID.name(),id);
            }

            values.put(FAVOURITE_PLACES_COLUMN_NAME.PLACE_NAME.name(),place.getName());
            values.put(FAVOURITE_PLACES_COLUMN_NAME.OVERRIDE_NAME.name(),place.getName());
            values.put(FAVOURITE_PLACES_COLUMN_NAME.LATITUDE.name(),(place.getLat() * MULTIPLIER));
            values.put(FAVOURITE_PLACES_COLUMN_NAME.LONGITUDE.name(),(place.getLng() * MULTIPLIER));
            values.put(FAVOURITE_PLACES_COLUMN_NAME.LOCALITY.name(),place.getLocality());
            db.insertWithOnConflict(TABLE_FAVOURITE_PLACES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return true;

        }catch(Exception e){
            return false;
        }
    }

    /*
     *
     *
     *JOURNEYS - Methods relating to journey CRUD
     *
     *
     */



    /*


    String place references for google places API



     */

    private int getKeyID(String key, SQLiteDatabase db, LatLng position){

        int lat, lng;


        if(position == null){
            lat = 0;
            lng = 0;
        }else{
            lat = (int) (position.latitude * 100.0);
            lng = (int) (position.longitude * 100.0);
        }

        key = key.replaceAll("'","''");

        String query = String.format(Locale.ENGLISH,"Select ID from %s WHERE %s = '%s'",
                TABLE_MATCHED_PLACES,
                MATCHED_PLACES_COLUMN.STRING_REF.name(),
                key
        );
        Cursor cursor = db.rawQuery(query,null);
        int id = -1;

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return id;

    }

    public void addPlaceReferences(String key, ArrayList<TomTomPlace> places, LatLng position){

        SQLiteDatabase db = getWritableDatabase();
        int keyID = getKeyID(key, db, position);

        if(keyID == -1){
            //This means the key does not currently exist so add it to the database and then add the places.
            ContentValues values = new ContentValues();
            values.put(MATCHED_PLACES_COLUMN.STRING_REF.name(),key);
            values.put(MATCHED_PLACES_COLUMN.CREATION_DATE.name(), Calendar.getInstance().getTime().getTime());
            db.insertWithOnConflict(TABLE_MATCHED_PLACES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            keyID = getKeyID(key, db, position);

        }

        for(TomTomPlace place : places){

            ContentValues values = new ContentValues();
            values.put(MATCHED_PLACES_DETAILS_COLUMN.REF_ID.name(),keyID);
            values.put(MATCHED_PLACES_DETAILS_COLUMN.NAME.name(), place.getName());
            values.put(MATCHED_PLACES_DETAILS_COLUMN.LOCALITY.name(), place.getLocality());
            values.put(MATCHED_PLACES_DETAILS_COLUMN.LATITUDE.name(), place.getDatabaseLat());
            values.put(MATCHED_PLACES_DETAILS_COLUMN.LONGITUDE.name(), place.getDatabaseLng());
            db.insertWithOnConflict(TABLE_MATCHED_PLACES_DETAILS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        }

        db.close();

    }

    public ArrayList<TomTomPlace> getPlaceReferences(String key, LatLng position){

        SQLiteDatabase db = getWritableDatabase();
        int keyID = getKeyID(key, db, position);
        ArrayList<TomTomPlace> places = new ArrayList<>();

        if(keyID == -1){
            //This means the key does not currently exist so send back an empty array
            db.close();
            return places;

        }

        String query = "SELECT * FROM " + TABLE_MATCHED_PLACES_DETAILS + " WHERE " + MATCHED_PLACES_DETAILS_COLUMN.REF_ID.name() + " = " + keyID;
        Cursor cursor = db.rawQuery(query,null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(2);
                String locality = cursor.getString(3);
                double lat = cursor.getDouble(4) / MULTIPLIER;
                double lng = cursor.getDouble(5) / MULTIPLIER;

                TomTomPlace place = new TomTomPlace(lat, lng, name, locality);
                places.add(place);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return places;

    }


    public void deletePlaceReferences(){

        long fortNight = Calendar.getInstance().getTime().getTime();
        fortNight = fortNight - (14 * 24 * 60 * 60 * 1000); // 14 days * 24 hours * 60 minutes * 60 seconds * 1000 milliseconds

        try{
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_MATCHED_PLACES,MATCHED_PLACES_COLUMN.CREATION_DATE.name() + "<?", new String[]{String.valueOf(fortNight)});
            db.close();
        }catch(Exception ignore){
            Log.e("DELETE REFS", ignore.getMessage());
        }

    }
}
