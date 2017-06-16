package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.HomeListener;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

public class RequestHelper {

    @NonNull
    private static String loadAppFilter(@NonNull Context context) {
        try {
            StringBuilder sb = new StringBuilder();
            XmlPullParser xpp = context.getResources().getXml(R.xml.appfilter);

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        sb.append(xpp.getAttributeValue(null, "component"));
                    }
                }
                xpp.next();
            }
            return sb.toString();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return "";
    }

    @NonNull
    public static List<Request> loadMissingApps(@NonNull Context context) {
        List<Request> requests = new ArrayList<>();
        String activities = RequestHelper.loadAppFilter(context);
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                intent, PackageManager.GET_RESOLVED_FILTER);
        CandyBarMainActivity.sInstalledAppsCount = installedApps.size();

        try {
            Collections.sort(installedApps,
                    new ResolveInfo.DisplayNameComparator(packageManager));
        } catch (Exception ignored) {}

        for (ResolveInfo app : installedApps) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName +"/"+ app.activityInfo.name;

            if (!activities.contains(activity)) {
                String name = LocaleHelper.getOtherAppLocaleName(
                        context, new Locale("en-US"), packageName);
                if (name == null)
                    name = app.activityInfo.loadLabel(packageManager).toString();

                boolean requested = Database.get(context).isRequested(activity);
                requests.add(new Request(
                        name,
                        app.activityInfo.packageName,
                        activity,
                        requested));
            }
        }
        return requests;
    }

    public static void prepareIconRequest(@NonNull Context context) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (context.getResources().getBoolean(R.bool.enable_icon_request) ||
                                context.getResources().getBoolean(R.bool.enable_premium_request)) {
                            CandyBarMainActivity.sMissedApps = RequestHelper
                                    .loadMissingApps(context);
                        }
                        return true;
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    if (context == null) return;

                    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                    if (fm == null) return;

                    Fragment fragment = fm.findFragmentByTag("home");
                    if (fragment == null) return;

                    HomeListener listener = (HomeListener) fragment;
                    listener.onHomeDataUpdated(null);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static String writeRequest(@NonNull Request request) {
        return "\n\n" +
                request.getName() +
                "\n" +
                request.getActivity() +
                "\n" +
                "https://play.google.com/store/apps/details?id=" + request.getPackageName();
    }

    public static String writeAppFilter(@NonNull Request request) {
        return  "\t<!-- " + request.getName() + " -->" +
                "\n" +
                "\t<item component=\"ComponentInfo{" +request.getActivity()+
                "}\" drawable=\"" +
                request.getName().toLowerCase().replace(" ", "_") +
                "\" />" +
                "\n\n";
    }

    public static String writeAppMap(@NonNull Request request) {
        return  "\t<!-- " + request.getName() + " -->" +
                "\n" +
                "\t<item class=\"" + request.getPackageName() + "\" name=\"" +
                request.getName().toLowerCase().replace(" ", "_") +
                "\" />" +
                "\n\n";
    }

    public static String writeThemeResources(@NonNull Request request) {
        return  "\t<!-- " + request.getName() + " -->" +
                "\n" +
                "\t<AppIcon name=\"" +request.getActivity()+ "\" image=\"" +
                request.getName().toLowerCase().replace(" ", "_") +
                "\" />" +
                "\n\n";
    }

    public static void showAlreadyRequestedDialog(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(R.string.request_requested)
                .positiveText(R.string.close)
                .show();
    }

    public static void showIconRequestLimitDialog(@NonNull Context context) {
        boolean reset = context.getResources().getBoolean(R.bool.reset_icon_request_limit);
        int limit = context.getResources().getInteger(R.integer.icon_request_limit);
        String message = String.format(context.getResources().getString(R.string.request_limit), limit);
        message += " "+ String.format(context.getResources().getString(R.string.request_used),
                Preferences.get(context).getRegularRequestUsed());

        if (Preferences.get(context).isPremiumRequestEnabled())
            message += " "+ context.getResources().getString(R.string.request_limit_buy);

        if (reset) message += "\n\n"+ context.getResources().getString(R.string.request_limit_reset);
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestRequired(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(R.string.premium_request_required)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestLimitDialog(@NonNull Context context, int selected) {
        String message = String.format(context.getResources().getString(R.string.premium_request_limit),
                Preferences.get(context).getPremiumRequestCount());
        message += " "+ String.format(context.getResources().getString(R.string.premium_request_limit1),
                selected);
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestStillAvailable(@NonNull Context context) {
        String message = String.format(context.getResources().getString(
                R.string.premium_request_already_purchased),
                Preferences.get(context).getPremiumRequestCount());
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static boolean isReadyToSendPremiumRequest(@NonNull Context context) {
        boolean isReady = Preferences.get(context).isConnectedToNetwork();
        if (!isReady) {
            new MaterialDialog.Builder(context)
                    .typeface(
                            TypefaceHelper.getMedium(context),
                            TypefaceHelper.getRegular(context))
                    .title(R.string.premium_request)
                    .content(R.string.premium_request_no_internet)
                    .positiveText(R.string.close)
                    .show();
        }
        return isReady;
    }

    public static void showPremiumRequestConsumeFailed(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(R.string.premium_request_consume_failed)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestExist(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(R.string.premium_request_exist)
                .positiveText(R.string.close)
                .show();
    }

    public static void checkPiracyApp(@NonNull Context context) {
        boolean premiumRequest = context.getResources().getBoolean(R.bool.enable_premium_request);
        //Dashboard don't need to check piracy app if premium request is disabled
        if (!premiumRequest) {
            Preferences.get(context).setPremiumRequestEnabled(false);
            RequestListener listener = (RequestListener) context;
            listener.onPiracyAppChecked(true);
            return;
        }

        //Lucky Patcher and Freedom package name
        String[] strings = new String[] {
                "com.chelpus.lackypatch",
                "com.dimonvideo.luckypatcher",
                "com.forpda.lp",
                //"com.android.protips", This is not lucky patcher or freedom
                "com.android.vending.billing.InAppBillingService.LUCK",
                "com.android.vending.billing.InAppBillingService.LOCK",
                "cc.madkite.freedom",
                "com.android.vending.billing.InAppBillingService.LACK"
        };

        boolean isPiracyAppInstalled = false;
        for (String string : strings) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        string, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    isPiracyAppInstalled = true;
                    break;
                }
            } catch (Exception ignored) {}
        }

        Preferences.get(context).setPremiumRequestEnabled(!isPiracyAppInstalled);

        RequestListener listener = (RequestListener) context;
        listener.onPiracyAppChecked(isPiracyAppInstalled);
    }
}
