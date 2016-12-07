package com.dm.material.dashboard.candybar.fragments;

import android.animation.AnimatorListenerAdapter;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.SearchListener;

import org.xmlpull.v1.XmlPullParser;

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

public class IconsBaseFragment extends Fragment {

    private ViewPager mPager;
    private ProgressBar mProgress;
    private TabLayout mTabLayout;

    private AsyncTask<Void, Void, Boolean> mGetIcons;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_base, container, false);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        Animator.startSlideDownAnimation(getActivity(),
                mTabLayout, view.findViewById(R.id.shadow));
        getIcons();
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        search.setIcon(DrawableHelper.getTintedDrawable(getActivity(),
                R.drawable.ic_toolbar_search, color));

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm == null) return false;

                setHasOptionsMenu(false);
                View view = getActivity().findViewById(R.id.shadow);
                if (view != null) view.animate().translationY(-mTabLayout.getHeight())
                        .setDuration(200).start();
                mTabLayout.animate().translationY(-mTabLayout.getHeight()).setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(android.animation.Animator animation) {
                                super.onAnimationEnd(animation);
                                Fragment prev = fm.findFragmentByTag("home");
                                if (prev != null) return;

                                try {
                                    SearchListener listener = (SearchListener) getActivity();
                                    listener.OnSearchCollapse(true);
                                } catch (Exception ignored) {}

                                fm.beginTransaction()
                                        .replace(R.id.container, new IconsSearchFragment(), IconsSearchFragment.TAG)
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }).start();

                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mGetIcons != null) mGetIcons.cancel(true);
        super.onDestroy();
    }

    private void getIcons() {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

            SparseArrayCompat<Icon> sections;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sections = new SparseArrayCompat<>();
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        XmlResourceParser parser = getActivity().getResources().getXml(R.xml.drawable);
                        int eventType = parser.getEventType();
                        String category = "";
                        int count = 0;

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (parser.getName().equals("category")) {
                                    String title = parser.getAttributeValue(null, "title");
                                    if (!category.equals(title)) {
                                        if (category.length() > 0)
                                            sections.append(sections.size(), new Icon(category, count));
                                        category = title;
                                        count = 0;
                                    }
                                } else if (parser.getName().equals("item")) {
                                    String name = parser.getAttributeValue(null, "drawable");
                                    int id = DrawableHelper.getResourceId(getActivity(), name);
                                    if (id > 0) count += 1;
                                }
                            }

                            eventType = parser.next();
                        }

                        sections.append(sections.size(), new Icon(category, count));
                        parser.close();
                        return true;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                mProgress.setVisibility(View.GONE);
                if (aBoolean) {
                    setHasOptionsMenu(true);
                    mPager.setAdapter(new PagerIconsAdapter(getChildFragmentManager(), sections));
                } else {
                    Toast.makeText(getActivity(), R.string.icons_load_failed,
                            Toast.LENGTH_LONG).show();
                }

                mGetIcons = null;
            }
        }.execute();
    }

    private class PagerIconsAdapter extends FragmentStatePagerAdapter {

        private final SparseArrayCompat<Icon> mIcons;

        PagerIconsAdapter(FragmentManager fm, SparseArrayCompat<Icon> icons) {
            super(fm);
            mIcons = icons;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mIcons.get(position).getTitle() +" ("+ mIcons.get(position).getRes() +")";
        }

        @Override
        public Fragment getItem(int position) {
            return IconsFragment.newInstance(mIcons.get(position).getTitle());
        }

        @Override
        public int getCount() {
            return mIcons.size();
        }

    }

}
