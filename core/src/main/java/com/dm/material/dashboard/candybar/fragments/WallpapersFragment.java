package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ListHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.WallpapersAdapter;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.TapIntroHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.WallpapersListener;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;
import com.rafakob.drawme.DrawMeButton;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.dm.material.dashboard.candybar.helpers.ViewHelper.setFastScrollColor;

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

public class WallpapersFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipe;
    private ProgressBar mProgress;
    private RecyclerFastScroller mFastScroll;
    private DrawMeButton mPopupBubble;

    private HttpURLConnection mConnection;
    private AsyncTask<Void, Void, Boolean> mGetWallpapers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.wallpapers_grid);
        mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mPopupBubble = (DrawMeButton) view.findViewById(R.id.popup_bubble);

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);

        initPopupBubble();
        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);
        mSwipe.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.swipeRefresh));

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count)));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        mSwipe.setOnRefreshListener(() -> {
            if (mProgress.getVisibility() == View.GONE)
                getWallpapers(true);
            else mSwipe.setRefreshing(false);
        });

        getWallpapers(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView,
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count));
    }

    @Override
    public void onDestroy() {
        if (mGetWallpapers != null) {
            try {
                if (mConnection != null) mConnection.disconnect();
            } catch (Exception ignored){}
            mGetWallpapers.cancel(true);
        }
        super.onDestroy();
    }

    private void initPopupBubble() {
        int color = ContextCompat.getColor(getActivity(), R.color.popupBubbleText);
        mPopupBubble.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_arrow_up, color), null, null, null);
        mPopupBubble.setOnClickListener(view -> {
            WallpapersListener listener = (WallpapersListener) getActivity();
            listener.onWallpapersChecked(null);

            AnimationHelper.hide(getActivity().findViewById(R.id.popup_bubble))
                    .duration(400)
                    .start();

            getWallpapers(true);
        });
    }

    private void showPopupBubble() {
        int wallpapersCount = Database.getInstance(getActivity()).getWallpapersCount();
        if (wallpapersCount == 0) return;

        if (Preferences.get(getActivity()).getAvailableWallpapersCount() > wallpapersCount) {
            AnimationHelper.show(mPopupBubble)
                    .interpolator(new LinearOutSlowInInterpolator())
                    .duration(400)
                    .start();
        }
    }

    private void getWallpapers(boolean refreshing) {
        final String wallpaperUrl = getActivity().getResources().getString(R.string.wallpaper_json);
        mGetWallpapers = new AsyncTask<Void, Void, Boolean>() {

            WallpaperJSON wallpapersJSON;
            List<Wallpaper> wallpapers;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (!refreshing) mProgress.setVisibility(View.VISIBLE);
                else mSwipe.setRefreshing(true);

                DrawMeButton popupBubble = (DrawMeButton) getActivity().findViewById(R.id.popup_bubble);
                if (popupBubble.getVisibility() == View.VISIBLE) popupBubble.setVisibility(View.GONE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Database database = Database.getInstance(getActivity());
                        if (!refreshing && (database.getWallpapersCount() > 0)) {
                            wallpapers = database.getWallpapers();
                            return true;
                        }

                        URL url = new URL(wallpaperUrl);
                        mConnection = (HttpURLConnection) url.openConnection();
                        mConnection.setConnectTimeout(15000);

                        if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream stream = mConnection.getInputStream();
                            wallpapersJSON = LoganSquare.parse(stream, WallpaperJSON.class);

                            if (wallpapersJSON == null) return false;
                            if (refreshing) {
                                wallpapers = database.getWallpapers();
                                List<Wallpaper> newWallpapers = new ArrayList<>();
                                for (WallpaperJSON wallpaper : wallpapersJSON.getWalls) {
                                    newWallpapers.add(new Wallpaper(
                                            wallpaper.name,
                                            wallpaper.author,
                                            wallpaper.url,
                                            wallpaper.thumbUrl));
                                }

                                List<Wallpaper> intersection = (List<Wallpaper>)
                                        ListHelper.intersect(newWallpapers, wallpapers);
                                List<Wallpaper> deleted = (List<Wallpaper>)
                                        ListHelper.difference(intersection, wallpapers);
                                List<Wallpaper> newlyAdded = (List<Wallpaper>)
                                        ListHelper.difference(intersection, newWallpapers);

                                database.deleteWallpapers(deleted);
                                database.addWallpapers(newlyAdded);

                                Preferences.get(getActivity()).setAvailableWallpapersCount(
                                        database.getWallpapersCount());
                            } else {
                                if (database.getWallpapersCount() > 0) database.deleteWallpapers();
                                database.addWallpapers(wallpapersJSON);
                            }

                            wallpapers = database.getWallpapers();
                            return true;
                        }
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
                if (!refreshing) mProgress.setVisibility(View.GONE);
                else mSwipe.setRefreshing(false);
                if (aBoolean) {
                    mRecyclerView.setAdapter(new WallpapersAdapter(getActivity(), wallpapers));

                    WallpapersListener listener = (WallpapersListener) getActivity();
                    listener.onWallpapersChecked(new Intent()
                            .putExtra("size", Preferences.get(getActivity()).getAvailableWallpapersCount())
                            .putExtra("packageName", getActivity().getPackageName()));

                    try {
                        TapIntroHelper.showWallpapersIntro(getActivity(), mRecyclerView);
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.connection_failed,
                            Toast.LENGTH_LONG).show();
                }
                mConnection = null;
                mGetWallpapers = null;
                showPopupBubble();
            }

        }.execute();
    }
}
