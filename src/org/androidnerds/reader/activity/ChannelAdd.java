/*
 * Copyright (C) 2007-2010 Michael Novak <mike@androidnerds.org>, Josh Guilfoyle <jasta@devtcg.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.androidnerds.reader.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.androidnerds.reader.R;

import java.net.MalformedURLException;
import java.net.URL;

public class ChannelAdd extends Activity {
	
	private static final String TAG = "ChannelAdd";
	
	private EditText mUrlText;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.channel_add);
		
		mUrlText = (EditText) findViewById(R.id.url_edit);
		
		Button add = (Button) findViewById(R.id.feed_add);
		add.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				addChannel();
			}
		});
	}
	
	private static URL getDefaultFavicon(String url) {
		try {
			URL orig = new URL(url);
			URL iconUrl = new URL(orig.getProtocol(), orig.getHost(),
					orig.getPort(), "/favicon.ico");
					
			return iconUrl;
		} catch (MalformedURLException e) {
			Log.d(TAG, "Exception caught: " + e.toString());
			return null;
		}
	}
	
	private void addChannel() {
		final String url = mUrlText.getText().toString();
	}
}