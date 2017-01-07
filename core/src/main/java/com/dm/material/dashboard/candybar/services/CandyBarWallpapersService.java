package com.dm.material.dashboard.candybar.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.webkit.URLUtil;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;
import com.dm.material.dashboard.candybar.receivers.CandyBarBroadcastReceiver;
import com.dm.material.dashboard.candybar.utils.Tag;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

public class CandyBarWallpapersService extends IntentService {

    private static final String SERVICE = "candybar.wallpapers.service";

    public CandyBarWallpapersService() {
        super(SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(1);
            String wallpaperUrl = getResources().getString(R.string.wallpaper_json);
            if (!URLUtil.isValidUrl(wallpaperUrl)) return;

            URL url = new URL(wallpaperUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(CandyBarBroadcastReceiver.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                WallpaperJSON wallpapersJSON = LoganSquare.parse(stream, WallpaperJSON.class);
                if (wallpapersJSON == null) return;

                int size = wallpapersJSON.getWalls.size();
                broadcastIntent.putExtra("size", size);
                sendBroadcast(broadcastIntent);
            }
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }
}
