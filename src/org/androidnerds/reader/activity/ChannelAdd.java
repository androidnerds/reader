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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.androidnerds.reader.R;
import org.androidnerds.reader.parser.PostListParser;
import org.androidnerds.reader.provider.Reader;

import java.net.MalformedURLException;
import java.net.URL;

public class ChannelAdd extends Activity {
	
	private static final String TAG = "ChannelAdd";
	
	private EditText mUrlText;
	
	protected ProgressDialog mBusy;
	final Handler mHandler = new Handler();
	
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
		
		mBusy = ProgressDialog.show(ChannelAdd.this, "Downloading",
			"Accessing XML Feed...", true, false);
			
		Thread t = new Thread() {
			public void run() {
				try {
					PostListParser parser = new PostListParser(getContentResolver());
					
					final long id = parser.syncDb(null, -1, url);
					
					if (id >= 0) {
						URL iconUrl = getDefaultFavicon(url);
						parser.updateFavicon(id, iconUrl);
					}
					
					mHandler.post(new Runnable() {
						public void run() {
							mBusy.dismiss();
							
							Uri uri = ContentUris.withAppendedId(Reader.Channels.CONTENT_URI, id);
							getIntent().setData(uri);
							
							setResult(RESULT_OK, getIntent());
							finish();
						}
					});
				} catch (Exception e) {
					Log.d("::Exception::", e.toString());
					final String errmsg = e.getMessage();
					final String errmsgFull = e.toString();

		    		mHandler.post(new Runnable() {
		    			public void run()
		    			{
		    				mBusy.dismiss();

		    				String errstr = ((errmsgFull != null) ? errmsgFull : errmsg);

		    				new AlertDialog.Builder(ChannelAdd.this)
		    					.setTitle("Feed Error")
		    					.setMessage("An error was encountered while accessing the feed: " + errstr)
		    					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    						//whatever.
		    						public void onClick(DialogInterface dialog, int whichButton) {
		    							
		    						}
		    					}).create();
		    			}
		    		});
				}
			}
		};
		
		t.start();
	}
}