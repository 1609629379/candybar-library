package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import com.dm.material.dashboard.candybar.R;

import java.io.File;
import java.util.Calendar;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class PreferencesHelper {

    private Context mContext;

    private static final String PREFERENCES_NAME = "candybar_preferences";

    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_CACHE_ALLOWED = "cache_allowed";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_APPLY_TIPS = "apply_tips";
    private static final String KEY_WALLPAPER_TIPS = "wallpaper_tips";
    private static final String KEY_ROTATE_TIME = "rotate_time";
    private static final String KEY_ROTATE_MINUTE = "rotate_minute";
    private static final String KEY_WIFI_ONLY = "wifi_only";
    private static final String KEY_DOWNLOADED_ONLY = "downloaded_only";
    private static final String KEY_WALLS_DIRECTORY = "wallpaper_directory";
    private static final String KEY_WALLS_LAST_UPDATE = "wallpaper_lats_update";
    private static final String KEY_PREMIUM_REQUEST = "premium_request";
    private static final String KEY_PREMIUM_REQUEST_PRODUCT = "premium_request_product";
    private static final String KEY_PREMIUM_REQUEST_COUNT = "premium_request_count";
    private static final String KEY_REGULAR_REQUEST_USED= "regular_request_used";
    private static final String KEY_INAPP_BILLING_TYPE = "inapp_billing_type";
    private static final String KEY_LICENSED = "licensed";

    public PreferencesHelper(@NonNull Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        return getSharedPreferences().getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRun(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_FIRST_RUN, bool).apply();
    }

    public boolean isCacheAllowed() {
        boolean cache = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        return getSharedPreferences().getBoolean(KEY_CACHE_ALLOWED, cache);
    }

    public void setCacheAllowed(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_CACHE_ALLOWED, bool).apply();
    }

    public boolean isDarkTheme() {
        return getSharedPreferences().getBoolean(KEY_DARK_THEME,
                mContext.getResources().getBoolean(R.bool.use_dark_theme));
    }

    public void setDarkTheme(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_DARK_THEME, bool).apply();
    }

    public boolean isShowApplyTips() {
        return getSharedPreferences().getBoolean(KEY_APPLY_TIPS, true);
    }

    public void showApplyTips(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_APPLY_TIPS, bool).apply();
    }

    public boolean isShowWallpaperTips() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPER_TIPS, true);
    }

    public void showWallpaperTips(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPER_TIPS, bool).apply();
    }

    public void setRotateTime (int time) {
        getSharedPreferences().edit().putInt(KEY_ROTATE_TIME, time).apply();
    }

    public int getRotateTime() {
        return getSharedPreferences().getInt(KEY_ROTATE_TIME, 3600000);
    }

    public void setRotateMinute (boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_ROTATE_MINUTE, bool).apply();
    }

    public boolean isRotateMinute() {
        return getSharedPreferences().getBoolean(KEY_ROTATE_MINUTE, false);
    }

    public boolean isWifiOnly() {
        return getSharedPreferences().getBoolean(KEY_WIFI_ONLY, false);
    }

    public void setWifiOnly (boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WIFI_ONLY, bool).apply();
    }

    public void setDownloadedOnly (boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_DOWNLOADED_ONLY, bool).apply();
    }

    public boolean isDownloadedOnly() {
        return getSharedPreferences().getBoolean(KEY_DOWNLOADED_ONLY, false);
    }

    public void setWallsDirectory (String directory) {
        getSharedPreferences().edit().putString(KEY_WALLS_DIRECTORY, directory).apply();
    }

    public String getWallsDirectory() {
        return getSharedPreferences().getString(KEY_WALLS_DIRECTORY, "");
    }

    public boolean isWallpaperSaved (String filename) {
        return new File(getWallsDirectory() + "/" + filename).exists();
    }

    public boolean isPremiumRequest() {
        return getSharedPreferences().getBoolean(KEY_PREMIUM_REQUEST, false);
    }

    public void setPremiumRequest(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_PREMIUM_REQUEST, bool).apply();
    }

    public String getPremiumRequestProductId() {
        return getSharedPreferences().getString(KEY_PREMIUM_REQUEST_PRODUCT, "");
    }

    public void setPremiumRequestProductId(String productId) {
        getSharedPreferences().edit().putString(KEY_PREMIUM_REQUEST_PRODUCT, productId).apply();
    }

    public int getPremiumRequestCount() {
        return getSharedPreferences().getInt(KEY_PREMIUM_REQUEST_COUNT, 0);
    }

    public void setPremiumRequestCount(int count) {
        getSharedPreferences().edit().putInt(KEY_PREMIUM_REQUEST_COUNT, count).apply();
    }

    public int getRegularRequestUsed() {
        return getSharedPreferences().getInt(KEY_REGULAR_REQUEST_USED, 0);
    }

    public void setRegularRequestUsed(int used) {
        getSharedPreferences().edit().putInt(KEY_REGULAR_REQUEST_USED, used).apply();
    }

    public int getInAppBillingType() {
        return getSharedPreferences().getInt(KEY_INAPP_BILLING_TYPE, -1);
    }

    public void setInAppBillingType(int type) {
        getSharedPreferences().edit().putInt(KEY_INAPP_BILLING_TYPE, type).apply();
    }

    public boolean isLicensed() {
        return getSharedPreferences().getBoolean(KEY_LICENSED, false);
    }

    public void setLicensed(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_LICENSED, bool).apply();
    }

    public int getVersion() {
        return getSharedPreferences().getInt(KEY_APP_VERSION, 0);
    }

    public void setVersion(int version) {
        getSharedPreferences().edit().putInt(KEY_APP_VERSION, version).apply();
    }

    public boolean isNewVersion() {
        int version = 0;
        try {
            version = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {}
        if (version > getVersion()) {
            boolean resetLimit = mContext.getResources().getBoolean(R.bool.reset_icon_request_limit);
            if (resetLimit) setRegularRequestUsed(0);
            setVersion(version);
            return true;
        } else {
            return false;
        }
    }

    public void setWallpaperLastUpdate() {
        getSharedPreferences().edit().putInt(KEY_WALLS_LAST_UPDATE, getCurrentDay()).apply();
    }

    private int getWallpaperLastUpdate () {
        return getSharedPreferences().getInt(KEY_WALLS_LAST_UPDATE, 0);
    }

    private int getCurrentDay() {
        return Calendar.getInstance().get(Calendar.DATE);
    }

    public boolean isTimeToUpdateWallpaper() {
        int saved = getWallpaperLastUpdate();
        int updateTime = mContext.getResources().getInteger(R.integer.wallpaper_update_every_days);
        int current = getCurrentDay();
        if ((current-saved) >= updateTime || (saved-current) >= updateTime) {
            setWallpaperLastUpdate();
            return true;
        } else {
            return false;
        }
    }

    public boolean isConnectedToNetwork() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnectedAsPreferred() {
        try {
            if (isWifiOnly()) {
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        activeNetworkInfo.isConnected();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
