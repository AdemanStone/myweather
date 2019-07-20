package com.qst.myweather;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CityDB {
    public static final String CITY_DB_NAME = "city2.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;


    //打开数据库操作函数
    public CityDB(Context context, String path){
        db = context.openOrCreateDatabase(CITY_DB_NAME, Context.MODE_PRIVATE, null);
    }

    public List<City> getAllCity(){
        List<City> list = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM " + CITY_TABLE_NAME, null);


        while (c.moveToNext()){
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, allPY, allFirstPY, firstPY);

            list.add(item);
        }
        return list;
    }
}
