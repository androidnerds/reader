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

import android.net.Uri;
import android.provider.BaseColumns;

public final class Reader {
	
	public static final String AUTHORITY = "org.androidnerds.reader.provider.Reader";
	
	public interface Channels extends BaseColumns {
		
		/**
		 * The Uri for accessing a specific channel.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/channels");
		
		public static final String DEFAULT_SORT_ORDER = "title ASC";
		
		/**
		 * Channel title, can be modified by the user but is pulled from the feed itself by default.
		 */
		public static final String TITLE = "title";
		
		/**
		 * The feed url to pull, this is the actual url and not the Google Reader version.
		 */
		public static final String URL = "url";
		
		/**
		 * The feed website's favicon, if this isn't present the default app icon will be used.
		 */
		public static final String ICON = "icon";
		public static final String ICON_URL = "icon_url";
		
		/**
		 * The logo attribute from the xml feed, if this exists we'll use it.
		 */
		public static final String LOGO = "logo";
		
		public static final class SQL {
			
			public static final String TABLE = "channels";
			
			public static final String CREATE = "CREATE TABLE " + TABLE 
				+ " (_id INTEGER PRIMARY KEY, " + TITLE + " TEXT UNIQUE, " + URL + " TEXT UNIQUE, "
				+ ICON + " TEXT, " + ICON_URL + " TEXT, " + LOGO + " TEXT);";
				
			public static final String DROP = "DROP TABLE IF EXISTS " + TABLE;
		}
	}
	
	public interface Posts extends BaseColumns {
		
		/**
		 * Uri for accessing a specific post.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/posts");
		
		/**
		 * Uri for accessing a list of posts for a specific channel.
		 */
		public static final Uri CONTENT_URI_LIST = 
			Uri.parse("content://" + AUTHORITY + "/postlist");
		
		public static final String DEFAULT_SORT_ORDER = "posted_on DESC";
		
		/**
		 * Reference to the channel _id that a post belongs to.
		 */
		public static final String CHANNEL_ID = "channel_id";
		
		/**
		 * Boolean value to determine whether the post has been read or not.
		 */
		public static final String READ = "read";
		
		/**
		 * The post subject.
		 */
		public static final String TITLE = "title";
		
		/**
		 * The post author.
		 */
		public static final String AUTHOR = "author";
		
		/**
		 * The url to allow the user to see the full post.
		 */
		public static final String URL = "url";
		
		/**
		 * The body of the post.
		 */
		public static final String BODY = "body";
		
		/**
		 * Date of the post.
		 */
		public static final String DATE = "posted_on";
		
		public static final class SQL {
			
			public static final String TABLE = "posts";
			
			public static final String CREATE = "CREATE TABLE " + TABLE
				+ " (_id INTEGER PRIMARY KEY, " + CHANNEL_ID + " INTEGER, " + TITLE
				+ " TEXT, " + URL + " TEXT, " + DATE + " DATETIME, " + BODY + " TEXT, " 
				+ AUTHOR + " TEXT, " + READ + " INTEGER(1) DEFAULT '0');";
			
			public static final String[] INDEX  = new String[] {
				"CREATE UNIQUE INDEX unq_post ON " + TABLE + " (" + TITLE + ", " + URL + ");",
				"CREATE INDEX idx_channel ON " + TABLE + " (" + CHANNEL_ID + ");"
			};
			
			public static final String DROP = "DROP TABLE IF EXISTS " + TABLE;
		}
		
	}
}