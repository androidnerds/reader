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
package org.androidnerds.reader.view;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.Reader;

public class ChannelListRow extends LinearLayout {
	
	private ImageView mIcon;
	private TextView mName;
	private TextView mCount;
	private ProgressBar mRefresh;
	
	public ChannelListRow(Context context, ViewGroup parent) {
		super(context);
		
		LayoutInflater inflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.channel_list_item, parent, false);
		
		mIcon = (ImageView) view.findViewById(R.id.channel_icon);
		mName = (TextView) view.findViewById(R.id.channel_name);
		mCount = (TextView) view.findViewById(R.id.channel_post_count);
		mRefresh = (ProgressBar) view.findViewById(R.id.channel_refresh);
		mRefresh.setVisibility(View.GONE);
		
		addView(view);
	}
	
	public void bindView(Cursor cursor) {
		ContentResolver resolver = getContext().getContentResolver();
		
		long channelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));
		
		Cursor unread = resolver.query(ContentUris.withAppendedId(Reader.Posts.CONTENT_URI_LIST, 
				channelId), new String[] { Reader.Posts._ID }, "read=0", null, null);
		
		Typeface tf;
		
		int unreadCount = unread.getCount();
		unread.close();
		
		if (unreadCount > 0) {
			tf = Typeface.DEFAULT_BOLD;
		} else {
			tf = Typeface.DEFAULT;
		}
		
		String icon = cursor.getString(cursor.getColumnIndex(Reader.Channels.ICON));
		
		mIcon.setImageURI(Uri.parse(icon));
		mName.setTypeface(Typeface.DEFAULT);
		
		String title = cursor.getString(cursor.getColumnIndex(Reader.Channels.TITLE));
		mName.setText(title);
		
		mCount.setTypeface(tf);
		mCount.setText(new Integer(unreadCount).toString());
	}
	
	public void startRefresh() {
		mCount.setVisibility(GONE);
		mRefresh.setVisibility(VISIBLE);
	}
	
	public void updateRefresh(int progress) {
		//possibly list the item progress here.
	}
	
	public void finishRefresh(Cursor cursor) {
		mRefresh.setVisibility(GONE);
		bindView(cursor);
		mCount.setVisibility(VISIBLE);
	}
	
	public void finishRefresh(long channelId) {
		Cursor cursor = getContext().getContentResolver().query
				(ContentUris.withAppendedId(Reader.Channels.CONTENT_URI, channelId),
				new String[] { Reader.Channels._ID, Reader.Channels.TITLE, Reader.Channels.ICON },
				null, null, null);
				
		if (cursor.getCount() < 1) {
			return;
		}
		
		cursor.isFirst();
		cursor.moveToNext();
		finishRefresh(cursor);
		cursor.close();
	}
}