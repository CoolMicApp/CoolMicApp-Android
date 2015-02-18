package com.vorbisdemo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "COLDATA";
    // Contacts table name
    private static final String TABLE_CONTACTS = "COOLMIC";
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_GENERAL_USERNAME = "generalUsername";
    private static final String KEY_SERVERNAME = "servername";
    private static final String KEY_MOUNTPOINT = "mountpoint";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_SAMPLERATE ="samplerate";
    private static final String KEY_CHANNELS = "channel";
    private static final String KEY_QUALITY = "quality";
    private static final String TERM_CONDITION ="false";
  
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + "(" 
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," 
                + KEY_GENERAL_USERNAME + " TEXT," + KEY_SERVERNAME + " TEXT," 
                + KEY_MOUNTPOINT + " TEXT," +KEY_USERNAME + " TEXT,"  
                + KEY_PASSWORD + " TEXT," +KEY_SAMPLERATE + " TEXT," 
                 + KEY_CHANNELS + " TEXT," +KEY_QUALITY + " TEXT,"           
                + TERM_CONDITION + " TEXT"+ ")"; 
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
        // Drop older table if existed 
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS); 
        // Create tables again
        onCreate(db);
    }
    
    /**
     * All CRUD(Create, Read, Update, Delete) Operations 
     */
    
    // Adding new contact
    void addCoolMicSetting(CoolMic coolmic) {
        SQLiteDatabase db = this.getWritableDatabase();  
        ContentValues values = new ContentValues(); 
        values.put(KEY_TITLE, coolmic.getTitle());                        //
        values.put(KEY_GENERAL_USERNAME, coolmic.getGeneralUsername());  // 
        values.put(KEY_SERVERNAME, coolmic.getServerName());             // 
        values.put(KEY_MOUNTPOINT, coolmic.getMountpoint());             //
        values.put(KEY_USERNAME, coolmic.getUsername());                 // 
        values.put(KEY_PASSWORD, coolmic.getPassword());                  // 
        values.put(KEY_SAMPLERATE, coolmic.getSampleRate());              // 
        values.put(KEY_CHANNELS, coolmic.getChannels());                 // 
        values.put(KEY_QUALITY, coolmic.getQuality());                  // 
        values.put(TERM_CONDITION, coolmic.getTermCondition());           // 
        db.insert(TABLE_CONTACTS, null, values);                        // 
        db.close(); 													 //  
    }

    // Getting single contact
    CoolMic getCoolMicDetails(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
        		KEY_TITLE, KEY_GENERAL_USERNAME,KEY_SERVERNAME,KEY_MOUNTPOINT,KEY_USERNAME,
        		KEY_PASSWORD,KEY_SAMPLERATE,KEY_CHANNELS,KEY_QUALITY,TERM_CONDITION }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        CoolMic coolmic = new CoolMic(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), 
                cursor.getString(8), cursor.getString(9), cursor.getString(10));
        return coolmic;
    }
     
  
    // Updating single contact
    public int updateCoolMicDetails(CoolMic coolmic) {
        SQLiteDatabase db = this.getWritableDatabase(); 
        ContentValues values = new ContentValues();       
        values.put(KEY_TITLE, coolmic.getTitle());                       // 
        values.put(KEY_GENERAL_USERNAME, coolmic.getGeneralUsername());  // 
        values.put(KEY_SERVERNAME, coolmic.getServerName());             // 
        values.put(KEY_MOUNTPOINT, coolmic.getMountpoint());             // 
        values.put(KEY_USERNAME, coolmic.getUsername());                 // 
        values.put(KEY_PASSWORD, coolmic.getPassword());                 // 
        values.put(KEY_SAMPLERATE, coolmic.getSampleRate());             // 
        values.put(KEY_CHANNELS, coolmic.getChannels());                 //  
        values.put(KEY_QUALITY, coolmic.getQuality());                   //  
        values.put(TERM_CONDITION, coolmic.getTermCondition());
        // updating row 
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?", 
                new String[] { String.valueOf(coolmic.getID()) });
    } 
  
    // Deleting single contact 
    public void deleteContact(CoolMic coolmic) {
        SQLiteDatabase db = this.getWritableDatabase(); 
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?", 
                new String[] { String.valueOf(coolmic.getID()) }); 
        db.close(); 
    }
     
    // Getting contacts Count 
    public int getCoolMicSettingCount() { 
    	int count = 0;
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS; 
        SQLiteDatabase db = this.getReadableDatabase(); 
        Cursor cursor = db.rawQuery(countQuery, null); 
        if(cursor != null && !cursor.isClosed()){
            count = cursor.getCount();
            cursor.close();
        }   
        return count;
    }
 
}
