/* Copyright (C) 2009, 2010 Android Nerds Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidnerds.reader.util.api;

/**
 * This class holds one Google Reader label, it contains the human readable title
 * and the id to access the label on the server; See {@link org.androidnerds.reader.util.api.Tags}
 * for the parsing of the group of labels.
 *
 * @author mike.novak
 */
public class Label {
    private static final String TAG = "Reader.Api";

    private String mTitle;
    private String mId;
    private String mSortId;

    public Label(String title, String id) {
        mTitle = title;
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String t) {
        mTitle = t;
    }

    public String getId() {
        return mId;
    }

    public String getSortId() {
        return mSortId;
    }

    public void setSortId(String s) {
        mSortId = s;
    }
}

