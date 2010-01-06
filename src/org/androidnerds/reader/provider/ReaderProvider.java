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
package org.androidnerds.reader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import org.androidnerds.reader.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ReaderProvider extends ContentProvider {
	
	private static final String TAG = "ReaderProvider";
	private static final String DATABASE_NAME = "reader.db";
	private static final int DATABASE_VERSION = 2;
	
	private DatabaseHelper mHelper;
	
	private static final HashMap<String, String> CHANNEL_LIST_PROJECTION_MAP;
	private static final HashMap<String, String> POST_LIST_PROJECTION_MAP;
	
	private static final int CHANNELS = 1;
	private static final int CHANNEL_ID = 2;
	private static final int POSTS = 3;
	private static final int POST_ID = 4;
	private static final int CHANNEL_POSTS = 5;
	private static final int CHANNELICON_ID = 6;
	
	private static final UriMatcher URI_MATCHER;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context c) {
			super(c, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Reader.Channels.SQL.CREATE);
			db.execSQL(Reader.Posts.SQL.CREATE);
		}
		
		private void execIndex(SQLiteDatabase db, String[] index) {
			for (int i = 0; i < index.length; i++) {
				db.execSQL(index[i]);
			}
		}
		
		private void onDrop(SQLiteDatabase db) {
			db.execSQL(Reader.Channels.SQL.DROP);
			db.execSQL(Reader.Posts.SQL.DROP);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE " + Reader.Channels.SQL.TABLE + " ADD COLUMN "
						+ Reader.Channels.SYNC + " INTEGER(1) DEFAULT '0';");
				db.execSQL("ALTER TABLE " + Reader.Posts.SQL.TABLE + " ADD COLUMN "
						+ Reader.Posts.SYNC + " INTEGER(1) DEFAULT '0';");
				db.execSQL("ALTER TABLE " + Reader.Posts.SQL.TABLE + " ADD COLUMN "
						+ Reader.Posts.STARRED + " INTEGER(1) DEFAULT '0';");
				break;
			default:
				onDrop(db);
				onCreate(db);
				break;
			}
		}
	}
	
	@Override
	public boolean onCreate() {
		mHelper = new DatabaseHelper(getContext());
		
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sort) {
				
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String defaultSort = null;
		
		switch (URI_MATCHER.match(uri)) {
		case CHANNELS:
			qb.setTables(Reader.Channels.SQL.TABLE);
			qb.setProjectionMap(CHANNEL_LIST_PROJECTION_MAP);
			defaultSort = Reader.Channels.DEFAULT_SORT_ORDER;
			break;
		case CHANNEL_ID:
			qb.setTables(Reader.Channels.SQL.TABLE);
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		case CHANNEL_POSTS:
			qb.setTables(Reader.Posts.SQL.TABLE);
			qb.appendWhere("channel_id=" +  uri.getPathSegments().get(1));
			defaultSort = Reader.Posts.DEFAULT_SORT_ORDER;
			break;
		case POST_ID:
			qb.setTables(Reader.Posts.SQL.TABLE);
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		String orderBy;
		
		if (TextUtils.isEmpty(sort)) {
			orderBy = defaultSort;
		} else {
			orderBy = sort;
		}
		
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}
	
	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "Uri is " + uri);
		Log.d(TAG, "Actual Uri is " + Reader.AUTHORITY + "channels");
		
		switch (URI_MATCHER.match(uri)) {
		case CHANNELS:
			return "vnd.android.cursor.dir/vnd.reader.channel";
		case CHANNEL_ID:
			return "vnd.android.cursor.item/vnd.reader.channel";
		case CHANNELICON_ID:
			return "image/x-icon";
		case POSTS:
		case CHANNEL_POSTS:
			return "vnd.android.cursor.dir/vnd.reader.post";
		case POST_ID:
			return "vnd.android.cursor.item/vnd.reader.post";
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private String getIconFilename(long channelId) {
		return "channel" + channelId + ".ico";
	}
	
	private String getIconPath(long channelId) {
		return getContext().getFileStreamPath(getIconFilename(channelId)).getAbsolutePath();
	}
	
	private void copyDefaultIcon(String path) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(path);
		
		InputStream ico = getContext().getResources().openRawResource(R.drawable.feedicon);
		
		byte[] buf = new byte[1024];
		int n;
		
		while ((n = ico.read(buf)) != -1) {
			out.write(buf, 0, n);
		}
		
		ico.close();
		out.close();
	}
	
	public ParcelFileDescriptor openFile(Uri uri, String mode) 
			throws FileNotFoundException {
		
		switch (URI_MATCHER.match(uri)) {
		case CHANNELICON_ID:
			long id = Long.valueOf(uri.getPathSegments().get(1));
			String path = getIconPath(id);
			
			if (mode.equals("rw") == true)
			{
				FileOutputStream foo = getContext().openFileOutput(getIconFilename(id), 0);
				
				try { 
					foo.write(new byte[] { 't' }); 
					foo.close(); 
				} catch (Exception e) { 
					Log.d(TAG, "Exception caught: " + e.toString());
				}
			}
			
			File file = new File(path);
			int modeint;
			
			if (mode.equals("rw")) {
				modeint = ParcelFileDescriptor.MODE_READ_WRITE | 
					ParcelFileDescriptor.MODE_TRUNCATE;
			} else {
				modeint = ParcelFileDescriptor.MODE_READ_ONLY;
				
				if (!file.exists()) {
					try {
						copyDefaultIcon(path);
					} catch (IOException e) {
						Log.d(TAG, "Unable to create the icon file ", e);
						return null;
					}
				}
			}
			
			return ParcelFileDescriptor.open(file, modeint);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private long insertChannels(SQLiteDatabase db, ContentValues values) {
		Resources r = Resources.getSystem();
		
		if (!values.containsKey(Reader.Channels.TITLE)) {
			values.put(Reader.Channels.TITLE, r.getString(android.R.string.untitled));
		}
		
		long id = db.insert(Reader.Channels.SQL.TABLE, "title", values);
		
		if (!values.containsKey(Reader.Channels.ICON)) {
			Uri iconUri;
			
			iconUri = Reader.Channels.CONTENT_URI.buildUpon()
			  .appendPath(String.valueOf(id))
			  .appendPath("icon")
			  .build();
			
			ContentValues update = new ContentValues();
			update.put(Reader.Channels.ICON, iconUri.toString());
			db.update(Reader.Channels.SQL.TABLE, update, "_id=" + id, null);
		}
		
		return id;
	}
	
	private long insertPosts(SQLiteDatabase db, ContentValues values) {
		return db.insert(Reader.Posts.SQL.TABLE, Reader.Posts.TITLE, values);
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		long rowId;
		
		if (values == null) {
			values = new ContentValues();
		}
		
		Uri result;
		
		if (URI_MATCHER.match(uri) == CHANNELS) {
			rowId = insertChannels(db, values);
			uri = ContentUris.withAppendedId(Reader.Channels.CONTENT_URI, rowId);
		} else if (URI_MATCHER.match(uri) == POSTS) {
			rowId = insertPosts(db, values);
			uri = ContentUris.withAppendedId(Reader.Posts.CONTENT_URI, rowId);
		} else {
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		if (rowId > 0) {
			assert(uri != null);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		throw new SQLException("Failed to insert row into: " + uri);
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;
		
		switch (URI_MATCHER.match(uri)) {
		case CHANNELS:
			count = db.delete(Reader.Channels.SQL.TABLE, where, whereArgs);
			break;
		case CHANNEL_ID:
			where = "_id=" + uri.getPathSegments().get(1) + 
				(!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(Reader.Channels.SQL.TABLE, where, whereArgs);
			break;
		case POSTS:
			count = db.delete(Reader.Posts.SQL.TABLE, where, whereArgs);
			break;
		case POST_ID:
			where = "_id=" + uri.getPathSegments().get(1) +
				(!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(Reader.Posts.SQL.TABLE, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;
		
		switch (URI_MATCHER.match(uri)) {
		case CHANNELS:
			values.put(Reader.Channels.SYNC, 1);
			count = db.update(Reader.Channels.SQL.TABLE, values, where, whereArgs);
			break;
		case CHANNEL_ID:
			values.put(Reader.Channels.SYNC, 1);
			where = "_id=" + uri.getPathSegments().get(1) +
				(!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(Reader.Channels.SQL.TABLE, values, where, whereArgs);
			break;
		case POSTS:
			values.put(Reader.Posts.SYNC, 1);
			count = db.update(Reader.Posts.SQL.TABLE, values, where, whereArgs);
			break;
		case POST_ID:
			values.put(Reader.Posts.SYNC, 1);
			where = "_id=" + uri.getPathSegments().get(1) +
				(!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(Reader.Posts.SQL.TABLE, values, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Uknown Uri: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Reader.AUTHORITY, "channels", CHANNELS);
		URI_MATCHER.addURI(Reader.AUTHORITY, "channels/#", CHANNEL_ID);
		URI_MATCHER.addURI(Reader.AUTHORITY, "channels/#/icon", CHANNELICON_ID);
		URI_MATCHER.addURI(Reader.AUTHORITY, "posts", POSTS);
		URI_MATCHER.addURI(Reader.AUTHORITY, "posts/#", POST_ID);
		URI_MATCHER.addURI(Reader.AUTHORITY, "postlist/#", CHANNEL_POSTS);
		
		CHANNEL_LIST_PROJECTION_MAP = new HashMap<String, String>();
		CHANNEL_LIST_PROJECTION_MAP.put(Reader.Channels._ID, "_id");
		CHANNEL_LIST_PROJECTION_MAP.put(Reader.Channels.TITLE, "title");
		CHANNEL_LIST_PROJECTION_MAP.put(Reader.Channels.URL, "url");
		CHANNEL_LIST_PROJECTION_MAP.put(Reader.Channels.ICON, "icon");
		CHANNEL_LIST_PROJECTION_MAP.put(Reader.Channels.LOGO, "logo");
		CHANNEL_LIST_PROJECTION_MAP.put(Reader.Channels.SYNC, "sync");
		
		POST_LIST_PROJECTION_MAP = new HashMap<String, String>();
		POST_LIST_PROJECTION_MAP.put(Reader.Posts._ID, "_id");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.CHANNEL_ID, "channel_id");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.READ, "read");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.TITLE, "title");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.URL, "url");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.AUTHOR, "author");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.DATE, "posted_on");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.BODY, "body");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.STARRED, "starred");
		POST_LIST_PROJECTION_MAP.put(Reader.Posts.SYNC, "sync");
	}
}