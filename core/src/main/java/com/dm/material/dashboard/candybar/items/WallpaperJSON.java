package com.dm.material.dashboard.candybar.items;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

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

@JsonObject
public class WallpaperJSON {

    @JsonField(name = "name")
    public String name;

    @JsonField(name = "author")
    public String author;

    @JsonField(name = "url")
    public String url;

    @JsonField(name = "thumbUrl")
    public String thumbUrl;

    @JsonField(name = "Wallpapers")
    public List<WallpaperJSON> getWalls;

}
