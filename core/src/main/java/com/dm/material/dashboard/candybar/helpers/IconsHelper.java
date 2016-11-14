package com.dm.material.dashboard.candybar.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dm.material.dashboard.candybar.fragments.dialog.IconPreviewFragment;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;

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

public class IconsHelper {

    public static String replaceIconName(String name) {
        char character = Character.toUpperCase(name.charAt(0));
        String finalString = character + name.substring(1);
        return finalString.replace("_", " ");
    }

    public static void selectIcon(Context context, int action, Icon icon) {
        if (action == IntentHelper.ICON_PICKER) {
            Intent intent = new Intent();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                    "drawable://" + icon.getRes());

            intent.putExtra("icon", bitmap);
            ((AppCompatActivity) context).setResult(bitmap != null ?
                    Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
            ((AppCompatActivity) context).finish();
        } else if (action == IntentHelper.IMAGE_PICKER) {
            Intent intent = new Intent();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                    "drawable://" + icon.getRes());
            if (bitmap != null) {
                File folder = FileHelper.getCacheDirectory(context);

                boolean createFolder = true;
                if (!folder.exists())
                    createFolder = folder.mkdirs();
                if (createFolder) {
                    File file = new File(folder, icon.getTitle() + ".png");
                    FileOutputStream outStream;
                    try {
                        outStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        intent.setData(Uri.fromFile(file));
                    } catch (Exception | OutOfMemoryError e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                    }
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
