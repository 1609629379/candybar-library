package com.dm.material.dashboard.candybar.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.fragments.dialog.IconPreviewFragment;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

public class IconsHelper {

    @NonNull
    public static List<Icon> getIconsList(@NonNull Context context) throws Exception {
        XmlResourceParser parser = context.getResources().getXml(R.xml.drawable);
        int eventType = parser.getEventType();
        String section = "";
        List<Icon> icons = new ArrayList<>();
        List<Icon> sections = new ArrayList<>();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("category")) {
                    String title = parser.getAttributeValue(null, "title");
                    if (!section.equals(title)) {
                        if (section.length() > 0)
                            sections.add(
                                    new Icon(section, icons));
                    }
                    section = title;
                    icons = new ArrayList<>();
                } else if (parser.getName().equals("item")) {
                    String name = parser.getAttributeValue(null, "drawable");
                    int id = DrawableHelper.getResourceId(context, name);
                    if (id > 0) {
                        icons.add(new Icon(name, id));
                    }
                }
            }

            eventType = parser.next();
        }
        sections.add(new Icon(section, icons));
        parser.close();
        return sections;
    }

    public static void prepareIconsList(@NonNull Context context) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (CandyBarMainActivity.sSections == null) return false;

                        for (int i = 0; i < CandyBarMainActivity.sSections.size(); i++) {
                            List<Icon> icons = CandyBarMainActivity.sSections.get(i).getIcons();

                            if (context.getResources().getBoolean(R.bool.show_icon_name)) {
                                for (Icon icon : icons) {
                                    boolean replacer = context.getResources().getBoolean(
                                            R.bool.enable_icon_name_replacer);
                                    String name = replaceName(context, replacer, icon.getTitle());
                                    icon.setTitle(name);
                                }
                            }

                            if (context.getResources().getBoolean(R.bool.enable_icons_sort)) {
                                Collections.sort(icons, new AlphanumComparator() {
                                    @Override
                                    public int compare(Object o1, Object o2) {
                                        String s1 = ((Icon) o1).getTitle();
                                        String s2 = ((Icon) o2).getTitle();
                                        return super.compare(s1, s2);
                                    }
                                });

                                CandyBarMainActivity.sSections.get(i).setIcons(icons);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }
        }.execute();
    }

    public static String replaceName(@NonNull Context context, boolean iconReplacer, String name) {
        if (iconReplacer) {
            String[] replacer = context.getResources().getStringArray(R.array.icon_name_replacer);
            for (String replace : replacer) {
                String[] strings = replace.split(",");
                if (strings.length > 0)
                    name = name.replace(strings[0], strings.length > 1 ? strings[1] : "");
            }
        }
        name = name.replaceAll("_", " ");
        name = name.trim().replaceAll("\\s+", " ");
        char character = Character.toUpperCase(name.charAt(0));
        return character + name.substring(1);
    }

    public static void selectIcon(@NonNull Context context, int action, Icon icon) {
        if (action == IntentHelper.ICON_PICKER) {
            Intent intent = new Intent();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                    "drawable://" + icon.getRes(), ImageConfig.getRawImageOptions().build());

            intent.putExtra("icon", bitmap);
            ((AppCompatActivity) context).setResult(bitmap != null ?
                    Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
            ((AppCompatActivity) context).finish();
        } else if (action == IntentHelper.IMAGE_PICKER) {
            Intent intent = new Intent();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                    "drawable://" + icon.getRes(), ImageConfig.getRawImageOptions().build());
            if (bitmap != null) {
                File file = new File(context.getCacheDir(), icon.getTitle() + ".png");
                FileOutputStream outStream;
                try {
                    outStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    Uri uri = FileHelper.getUriFromFile(context, context.getPackageName(), file);
                    if (uri == null) uri = Uri.fromFile(file);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception | OutOfMemoryError e) {
                    Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                }
                intent.putExtra("return-data", false);
            }
            ((AppCompatActivity) context).setResult(bitmap != null ?
                    Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
            ((AppCompatActivity) context).finish();
        } else {
            IconPreviewFragment.showIconPreview(((AppCompatActivity) context)
                            .getSupportFragmentManager(),
                    icon.getTitle(), icon.getRes());
        }
    }
}
