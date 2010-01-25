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

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Subscriptions class manages adding, editing, removing and listing 
 * all the subscriptions for the given Google Reader account.
 *
 * @author mike.novak
 */
public class Subscriptions {
    private static final String SUB_URL = "https://www.google.com/reader/api/0/subscription";
    private static final String TAG = "Reader.Api";

    /**
     * This method queries Google Reader for the list of subscribed feeds.
     * 
     * @param sid authentication code to pass along in a cookie.
     * @return arr returns a JSONArray of JSONObjects for each feed.
     *
     * The JSONObject returned by the service looks like this:
     *    id: this is the feed url.
     *    title: this is the title of the feed.
     *    sortid: this has not been figured out yet.
     *    firstitemsec: this has not been figured out yet.
     */    
    public static JSONArray getSubscriptionList(String sid) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(SUB_URL + "/list?output=json");
        BasicClientCookie cookie = Authentication.buildCookie(sid);

        try {
            client.getCookieStore().addCookie(cookie);
            
            HttpResponse response = client.execute(get);
            HttpEntity respEntity = response.getEntity();

            Log.d(TAG, "Response from server: " + response.getStatusLine());

            InputStream in = respEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line = "";
            String arr = "";
            while ((line = reader.readLine()) != null) {
                arr += line;
            }

            JSONObject obj = new JSONObject(arr);
            JSONArray array = obj.getJSONArray("subscriptions");

            reader.close();
            client.getConnectionManager().shutdown();

            return array;
        } catch (Exception e) {
            Log.d(TAG, "Exception caught:: " + e.toString());
            return null;
        }
    }
}
