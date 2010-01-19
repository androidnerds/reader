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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

/**
 * This class does not get instantiated and all of the member methods are static. 
 * It provides authentication parameters for the other classes in the application.
 *
 * @author mike novak
 */
public class Authentication {
	
	private static final String TAG = "Authentication";
	private static final String TOKEN_URL = "http://www.google.com/reader/api/0/token";
	private static final String AUTH_URL = "https://www.google.com/accounts/ClientLogin";
	
	private Authentication() { }
	
	/**
	 * This method returns back to the caller a proper authentication token to use with
	 * the other API calls to Google Reader.
	 *
	 * @param user - the Google username
	 * @param pass - the Google password
	 * @return sid - the returned authentication token for use with the API.
	 *
	 */
	public static String getAuthToken(String user, String pass) {
		NameValuePair username = new BasicNameValuePair("Email", user);
		NameValuePair password = new BasicNameValuePair("Passwd", pass);
		NameValuePair service = new BasicNameValuePair("service", "reader");
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(username);
		pairs.add(password);
		pairs.add(service);
		
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(AUTH_URL);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs);
			
			post.setEntity(entity);
			
			HttpResponse response = client.execute(post);
			HttpEntity respEntity = response.getEntity();
			
			Log.d(TAG, "Server Response: " + response.getStatusLine());
			
			InputStream in = respEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			String result = null;
			
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("SID")) {
					result = line.substring(line.indexOf("=") + 1);
				}
			}
			
			reader.close();
			client.getConnectionManager().shutdown();
			
			return result;
		} catch (Exception e) {
			Log.d(TAG, "Exception caught:: " + e.toString());
			return null;
		}
	}
	
	/**
	 * This method generates a quick token to send with API requests that require
	 * editing content. This method is called as the API request is being built so
	 * that it doesn't expire prior to the actual execution.
	 *
	 * @param sid - the user's authentication token from ClientLogin
	 * @return token - the edit token generated by the server.
	 *
	 */
	public static String generateFastToken(String sid) {
		try {
			BasicClientCookie cookie = Authentication.buildCookie(sid);
			DefaultHttpClient client = new DefaultHttpClient();
			
			client.getCookieStore().addCookie(cookie);
			
			HttpGet get = new HttpGet(TOKEN_URL);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			Log.d(TAG, "Server Response: " + response.getStatusLine());
			
			InputStream in = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				Log.d(TAG, "Response Content: " + line);
			}
			
			reader.close();
			client.getConnectionManager().shutdown();
			
			return line;
		} catch (Exception e) {
			Log.d(TAG, "Exception caught:: " + e.toString());
			return null;
		}
	}
	
	/**
	 * This method sets up the sid cookie for the httpclient. Each client
	 * calls this method to build the cookie so the request is properly
	 * authenticated.
	 *
	 * @param sid - the user's authentication token from ClientLogin.
	 * @return cookie - the cookie object to add to the httpclient store.
	 *
	 */
	public static BasicClientCookie buildCookie(String sid) {
		BasicClientCookie cookie = new BasicClientCookie("SID", sid);
		cookie.setDomain(".google.com");
		cookie.setPath("/");
		
		return cookie;
	}
}