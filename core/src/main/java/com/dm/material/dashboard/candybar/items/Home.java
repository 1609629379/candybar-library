package com.dm.material.dashboard.candybar.items;

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

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

public class Home {

    private int mIcon;
    private String mTitle;
    private String mSubtitle;
    private Home.Type mType;

    public Home(@DrawableRes int icon, String title, String subtitle, @NonNull Home.Type type) {
        mIcon = icon;
        mTitle = title;
        mSubtitle = subtitle;
        mType = type;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public Home.Type getType() {
        return mType;
    }

    public enum Type {
        APPLY(0),
        DONATE(1),
        ICONS(2),
        DIMENSION(3);

        private int mType;

        Type(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }
    }
}
