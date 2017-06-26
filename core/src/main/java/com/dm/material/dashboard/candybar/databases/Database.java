package com.dm.material.dashboard.candybar.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.material.dashboard.candybar.helpers.JsonHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "candybar_database";
    private static final int DATABASE_VERSION = 7;

    private static final String TABLE_REQUEST = "icon_request";
    private static final String TABLE_PREMIUM_REQUEST = "premium_request";
    private static final String TABLE_WALLPAPERS = "wallpapers";

    private static final String KEY_ID = "id";

    private static final String KEY_ORDER_ID = "order_id";
    private static final String KEY_PRODUCT_ID = "product_id";

    private static final String KEY_NAME = "name";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_REQUESTED_ON = "requested_on";

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_URL = "url";
    private static final String KEY_ADDED_ON = "added_on";

    private Context mContext;

    @NonNull
    public static Database get(@NonNull Context context) {
        return new Database(context);
    }

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_REQUEST = "CREATE TABLE IF NOT EXISTS " +TABLE_REQUEST+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" +KEY_ACTIVITY+ ") ON CONFLICT REPLACE)";
        String CREATE_TABLE_PREMIUM_REQUEST = "CREATE TABLE IF NOT EXISTS " +TABLE_PREMIUM_REQUEST+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_ORDER_ID + " TEXT NOT NULL, " +
                KEY_PRODUCT_ID + " TEXT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" +KEY_ACTIVITY+ ") ON CONFLICT REPLACE)";
        String CREATE_TABLE_WALLPAPER = "CREATE TABLE IF NOT EXISTS " +TABLE_WALLPAPERS+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME+ " TEXT NOT NULL, " +
                KEY_AUTHOR + " TEXT NOT NULL, " +
                KEY_URL + " TEXT NOT NULL, " +
                KEY_THUMB_URL + " TEXT NOT NULL, " +
                KEY_ADDED_ON + " TEXT NOT NULL, " +
                "UNIQUE (" +KEY_URL+ ") ON CONFLICT REPLACE)";
        db.execSQL(CREATE_TABLE_REQUEST);
        db.execSQL(CREATE_TABLE_PREMIUM_REQUEST);
        db.execSQL(CREATE_TABLE_WALLPAPER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db, oldVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db, oldVersion);
    }

    public void close() {
        if (this.getWritableDatabase().isOpen()) {
            this.getWritableDatabase().close();
            LogUtil.e("database forced to close");
        }
    }

    private void resetDatabase(SQLiteDatabase db, int oldVersion) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=\'table\'", null);
        List<String> tables = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                tables.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<Request> requests = getRequestedApps(db);
        List<Request> premiumRequest = getPremiumRequest(db);

        for (int i = 0; i < tables.size(); i++) {
            try {
                String dropQuery = "DROP TABLE IF EXISTS " + tables.get(i);
                if (!tables.get(i).equalsIgnoreCase("SQLITE_SEQUENCE"))
                    db.execSQL(dropQuery);
            } catch (Exception ignored) {}
        }
        onCreate(db);

        for (int i = 0; i < requests.size(); i++) {
            addRequest(db,
                    requests.get(i).getName(),
                    requests.get(i).getPackageName(),
                    requests.get(i).getActivity());
        }

        if (oldVersion <= 3) return;

        for (int i = 0; i < premiumRequest.size(); i++) {
            addPremiumRequest(db,
                    premiumRequest.get(i).getOrderId(),
                    premiumRequest.get(i).getProductId(),
                    premiumRequest.get(i).getName(),
                    premiumRequest.get(i).getActivity(),
                    premiumRequest.get(i).getRequestedOn());
        }
    }

    public void addRequest(String name, String activity, String requestedOn) {
        SQLiteDatabase db = this.getWritableDatabase();
        addRequest(db, name, activity, requestedOn);
        db.close();
    }

    private void addRequest(SQLiteDatabase db, String name, String activity, String requestedOn) {
        if (db == null) return;
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            values.put(KEY_ACTIVITY, activity);
            if (requestedOn != null) values.put(KEY_REQUESTED_ON, requestedOn);

            db.insert(TABLE_REQUEST, null, values);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public boolean isRequested(String activity) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REQUEST, null, KEY_ACTIVITY + " = ?",
                new String[]{activity}, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        db.close();
        return rowCount > 0;
    }

    private List<Request> getRequestedApps(@Nullable SQLiteDatabase db) {
        List<Request> requests = new ArrayList<>();
        if (db == null) db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(TABLE_REQUEST, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Request request = new Request(
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            true);
                    requests.add(request);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return requests;
    }

    private void addPremiumRequest(@Nullable SQLiteDatabase db, String orderId, String productId, String name,
                                   String activity, String requestedOn) {
        if (db == null) db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ORDER_ID, orderId);
            values.put(KEY_PRODUCT_ID, productId);
            values.put(KEY_NAME, name);
            values.put(KEY_ACTIVITY, activity);

            if (requestedOn != null)
                values.put(KEY_REQUESTED_ON, requestedOn);

            db.insert(TABLE_PREMIUM_REQUEST, null, values);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public void addPremiumRequest(String orderId, String productId, String name,
                                  String activity, String requestedOn) {
        SQLiteDatabase db = this.getWritableDatabase();
        addPremiumRequest(db, orderId, productId, name, activity, requestedOn);
        db.close();
    }

    public List<Request> getPremiumRequest(@Nullable SQLiteDatabase db) {
        List<Request> requests = new ArrayList<>();
        if (db == null) db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(TABLE_PREMIUM_REQUEST,
                    null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Request request = new Request(
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4));
                    requests.add(request);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return requests;
    }

    public void addWallpapers(List<?> list) {
        String query = "INSERT INTO " +TABLE_WALLPAPERS+ " (" +KEY_NAME+ "," +KEY_AUTHOR+ "," +KEY_URL+ ","
                +KEY_THUMB_URL+ "," +KEY_ADDED_ON+ ") VALUES (?,?,?,?,?);";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(query);
        db.beginTransaction();

        for (int i = 0; i < list.size(); i++) {
            statement.clearBindings();

            Wallpaper wallpaper;
            if (list.get(i) instanceof Wallpaper) {
                wallpaper = (Wallpaper) list.get(i);
            } else {
                wallpaper = JsonHelper.getWallpaper(mContext, list.get(i));
            }

            if (wallpaper != null) {
                statement.bindString(1, wallpaper.getName());
                statement.bindString(2, wallpaper.getAuthor());
                statement.bindString(3, wallpaper.getURL());
                statement.bindString(4, WallpaperHelper.getThumbnailUrl(
                        wallpaper.getURL(),
                        wallpaper.getThumbUrl()));
                statement.bindString(5, TimeHelper.getLongDateTime());
                statement.execute();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public int getWallpapersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, null, null, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        db.close();
        return rowCount;
    }

    public List<Wallpaper> getWallpapers() {
        List<Wallpaper> wallpapers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS,
                null, null, null, null, null, KEY_ADDED_ON + " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = new Wallpaper(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpapers;
    }

    public Wallpaper getRandomWallpaper() {
        Wallpaper wallpaper = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +TABLE_WALLPAPERS+ " ORDER BY RANDOM() LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                wallpaper = new Wallpaper(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpaper;
    }

    public void deleteIconRequestData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_REQUEST});
        db.delete(TABLE_REQUEST, null, null);
        db.close();
    }

    public void deleteWallpapers(List<Wallpaper> wallpapers) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < wallpapers.size(); i++) {
            db.delete(TABLE_WALLPAPERS, KEY_URL +" = ?",
                    new String[]{wallpapers.get(i).getURL()});
        }
        db.close();
    }

    public void deleteWallpapers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
        db.delete(TABLE_WALLPAPERS, null, null);
        db.close();
    }
}
