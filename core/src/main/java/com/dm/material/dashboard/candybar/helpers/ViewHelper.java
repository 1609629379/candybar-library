package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

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

public class ViewHelper {

    public static void resetNavigationBarTranslucent(@NonNull Context context, int orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);

            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AppCompatActivity) context).getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((AppCompatActivity) context).getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                ColorHelper.setNavigationBarColor(context, Color.BLACK);
            }
        }
    }

    public static int getStatusBarHeight(@NonNull Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static Point getRealScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
        }
        return size;
    }

    public static void changeSearchViewTextColor(@Nullable View view, int text, int hint) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(text);
                ((TextView) view).setHintTextColor(hint);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i), text, hint);
                }
            }
        }
    }

    public static void removeSearchViewSearchIcon(@Nullable View view) {
        if (view != null) {
            ImageView searchIcon = (ImageView) view;
            ViewGroup linearLayoutSearchView = (ViewGroup) view.getParent();
            if (linearLayoutSearchView != null) {
                linearLayoutSearchView.removeView(searchIcon);
                linearLayoutSearchView.addView(searchIcon);

                searchIcon.setAdjustViewBounds(true);
                searchIcon.setMaxWidth(0);
                searchIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                searchIcon.setImageDrawable(null);
            }
        }
    }

    public static void resetSpanCount(@NonNull Context context, @NonNull RecyclerView recyclerView, @IntegerRes int id) {
        try {
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                manager.setSpanCount(context.getResources().getInteger(id));
                manager.requestLayout();
            } else if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                manager.setSpanCount(context.getResources().getInteger(id));
                manager.requestLayout();
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public static void setFastScrollColor(@Nullable RecyclerFastScroller fastScroll) {
        if (fastScroll == null) return;

        Context context = fastScroll.getContext();
        if (context instanceof ContextThemeWrapper) {
            context = ((ContextThemeWrapper) context).getBaseContext();
        }

        int color = ContextCompat.getColor(context,
                Preferences.getPreferences(context).isDarkTheme() ?
                        R.color.fastScrollHandleDark : R.color.fastScrollHandle);
        int accent = ColorHelper.getAttributeColor(context, R.attr.colorAccent);

        fastScroll.setBarColor(color);
        fastScroll.setHandleNormalColor(color);
        fastScroll.setHandlePressedColor(accent);
    }
}
