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
package org.androidnerds.reader.parser;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.Reader;
import org.androidnerds.reader.util.DateUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PostListParser extends DefaultHandler {
	
	private static final String TAG = "PostListParser";
	
	private Handler mHandler;
	private long mId;
	private String mUrl;
	private ContentResolver mContent;
	private ChannelPost mPostBuf;
	
	private int mState;
	private static final int STATE_IN_ITEM = (1 << 2);
	private static final int STATE_IN_ITEM_TITLE = (1 << 3);
	private static final int STATE_IN_ITEM_LINK = (1 << 4);
	private static final int STATE_IN_ITEM_DESC = (1 << 5);
	private static final int STATE_IN_ITEM_DATE = (1 << 6);
	private static final int STATE_IN_ITEM_AUTHOR = (1 << 7);
	private static final int STATE_IN_TITLE = (1 << 8);
	
	private static final HashMap<String, Integer> mStateMap;
	
	static {
		mStateMap = new HashMap<String, Integer>();
		mStateMap.put("item", new Integer(STATE_IN_ITEM));
		mStateMap.put("entry", new Integer(STATE_IN_ITEM));
		mStateMap.put("title", new Integer(STATE_IN_ITEM_TITLE));
		mStateMap.put("link", new Integer(STATE_IN_ITEM_LINK));
		mStateMap.put("description", new Integer(STATE_IN_ITEM_DESC));
		mStateMap.put("content", new Integer(STATE_IN_ITEM_DESC));
		mStateMap.put("content:encoded", new Integer(STATE_IN_ITEM_DESC));
		mStateMap.put("dc:date", new Integer(STATE_IN_ITEM_DATE));
		mStateMap.put("updated", new Integer(STATE_IN_ITEM_DATE));
		mStateMap.put("pubDate", new Integer(STATE_IN_ITEM_DATE));
		mStateMap.put("dc:author", new Integer(STATE_IN_ITEM_AUTHOR));
		mStateMap.put("author", new Integer(STATE_IN_ITEM_AUTHOR));
	}
	
	public PostListParser(ContentResolver resolver) {
		super();
		mContent = resolver;
	}
	
	public long syncDb(Handler h, long id, String feedUrl) throws Exception {
		mHandler = h;
		mId = id;
		mUrl = feedUrl;
		
		SAXParserFactory fact = SAXParserFactory.newInstance();
		SAXParser sp = fact.newSAXParser();
		XMLReader reader = sp.getXMLReader();
		
		reader.setContentHandler(this);
		reader.setErrorHandler(this);
		
		URL url = new URL(mUrl);
		URLConnection c = url.openConnection();
		c.setRequestProperty("User-Agent", "Android/1.5");
		
		reader.parse(new InputSource(c.getInputStream()));
		
		return mId;
	}
	
	public boolean updateFavicon(long id, String iconUrl) throws MalformedURLException {
		return updateFavicon(id, new URL(iconUrl));
	}
	
	public boolean updateFavicon(long id, URL url) {
		InputStream stream = null;
		OutputStream ico = null;
		boolean r = false;
		
		Uri iconUri = Reader.Channels.CONTENT_URI.buildUpon().appendPath(String.valueOf(id))
				.appendPath("icon").build();
				
		try {
			stream = url.openStream();
			ico = mContent.openOutputStream(iconUri);
			
			byte[] b = new byte[1024];
			int n;
			
			while ((n = stream.read(b)) != -1) {
				ico.write(b, 0, n);
			}
			
			r = true;
		} catch (Exception e) {
			Log.d(TAG, "Exception caught:: " + e.toString());
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
				
				if (ico != null) {
					ico.close();
				}
			} catch (IOException io) {
				Log.d(TAG, "Exception caught:: " + io.toString());
			}
		}
		
		return r;
	}
	
	public void startElement(String uri, String name, String qName, Attributes attrs) { 
		
		Log.d(TAG, "the tag name is: " + name);
		
		if (mId == -1 && name.equals("title") && (mState & STATE_IN_ITEM) == 0) {
			mState |= STATE_IN_TITLE;
		}
		
		Integer state = mStateMap.get(name);
		
		if (state != null) {
			mState |= state.intValue();
			
			if (state.intValue() == STATE_IN_ITEM) {
				mPostBuf = new ChannelPost();
			} else if ((mState & STATE_IN_ITEM) != 0 && state.intValue() == STATE_IN_ITEM_LINK) {
				String href = attrs.getValue("href");
				
				if (href != null) {
					mPostBuf.link = href;
				}
			}
		}
	}
	
	public void endElement(String uri, String name, String qName, Attributes attrs) {
		Integer state = mStateMap.get(name);
		
		if (state != null) {
			mState &= ~(state.intValue());
			
			if (state.intValue() == STATE_IN_ITEM) {
				
				if (mId == -1) {
					Log.d(TAG, "found an item before the end of the title tag, fix parser.");
					return;
				}
			
				String[] dupProj = new String[] { Reader.Posts._ID };
				Uri listUri = ContentUris.withAppendedId(Reader.Posts.CONTENT_URI_LIST, mId);
			
				Cursor dup = mContent.query(listUri, dupProj, "title=? AND url=?",
						new String[] { mPostBuf.title, mPostBuf.link }, null);
					
				if (dup.getCount() == 0) {
					ContentValues values = new ContentValues();
				
					values.put(Reader.Posts.CHANNEL_ID, mId);
					values.put(Reader.Posts.TITLE, mPostBuf.title);
					values.put(Reader.Posts.URL, mPostBuf.link);
					values.put(Reader.Posts.AUTHOR, mPostBuf.author);
					values.put(Reader.Posts.DATE, mPostBuf.getDate());
					values.put(Reader.Posts.BODY, mPostBuf.desc);
				
					Uri added = mContent.insert(Reader.Posts.CONTENT_URI, values);
				}
			
				dup.close();
			}
		}
	}
	
	public void characters(char ch[], int start, int length) {
		if (mId == -1 && (mState & STATE_IN_TITLE) != 0) {
			ContentValues values = new ContentValues();
			
			values.put(Reader.Channels.TITLE, new String(ch, start, length));
			values.put(Reader.Channels.URL, mUrl);
			
			Uri added = mContent.insert(Reader.Channels.CONTENT_URI, values);
			
			mId = Long.parseLong(added.getPathSegments().get(1));
			
			mState &= ~STATE_IN_TITLE;
			
			return;
		}
		
		if ((mState & STATE_IN_ITEM) == 0) {
			return;
		}
		
		switch (mState) {
		case STATE_IN_ITEM | STATE_IN_ITEM_TITLE:
			mPostBuf.title = new String(ch, start, length);
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_DESC:
			mPostBuf.desc += new String(ch, start, length);
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_LINK:
			mPostBuf.link = new String(ch, start, length);
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_DATE:
			mPostBuf.setDate(new String(ch, start, length));
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_AUTHOR:
			mPostBuf.author = new String(ch, start, length);
		default:
			Log.d(TAG, "default hit parsing characters: " + new String(ch, start, length));
		}
	}
	
	private class ChannelPost {
		public String title;
		public Date date;
		public String desc = new String();
		public String link;
		public String author;
		
		public ChannelPost() {
			desc = new String();
		}
		
		public void setDate(String d) {
			date = DateUtils.parseDate(d);
			
			if (date == null) {
				date = new Date();
			}
		}
		
		public String getDate() {
			return DateUtils.formatDate(date);
		}
	}
}