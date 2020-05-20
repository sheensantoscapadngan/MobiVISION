package com.example.android.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.Toast;

public class DatabaseConnect extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Configurations.db";
    private static final String COL1 = "location";
    private static final String COL2 = "symbol";
    private static final String COL3 = "front";
    private static final String COL4 = "front_dir";
    private static final String COL5 = "lleft";
    private static final String COL6 = "left_dir";
    private static final String COL7 = "right";
    private static final String COL8 = "right_dir";
    private static final String DOWNLOADED_LIST = "downloadedList";

    private static final String DOWNLOADED_TABLE = "DownloadedTable";
    private static Context context;


    public DatabaseConnect(Context context) {
        super(context,DATABASE_NAME, null, 2);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + DOWNLOADED_TABLE + "(DOWNLOADEDLIST TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean saveInfo(Bundle info, String table_name){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1,info.getString("location"));
        contentValues.put(COL2,info.getString("symbol"));
        contentValues.put(COL3,info.getString("front"));
        contentValues.put(COL4,info.getString("front_dir"));
        contentValues.put(COL5,info.getString("left"));
        contentValues.put(COL6,info.getString("left_dir"));
        contentValues.put(COL7,info.getString("right"));
        contentValues.put(COL8,info.getString("right_dir"));

        long res = db.insert(table_name,null,contentValues);
        if(res != -1)
            return true;
        else
            return false;

    }

    public void insertTable(String table_name){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("create table if not exists " + table_name + "(LOCATION TEXT, SYMBOL TEXT, FRONT TEXT, FRONT_DIR TEXT, LLEFT TEXT,LEFT_DIR TEXT, RIGHT TEXT, RIGHT_DIR TEXT)");
    }

    public Boolean saveToDownloadedList(String table_names){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        String tableCopy = table_names.replace("5"," ");
        contentValues.put(DOWNLOADED_LIST,tableCopy);

        long res = db.insert(DOWNLOADED_TABLE,null,contentValues);
        if(res != -1)
            return true;
        else
            return false;

    }

    public Cursor getTableList(){

        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + DOWNLOADED_TABLE,null);
        return res;

    }

    public Cursor getTable(String table){
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + table,null);
        return res;

    }

}

