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

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.Reader;
import org.androidnerds.reader.view.ChannelListItem;

import java.util.HashMap;

public class ChannelList extends ListActivity {
	
	public static final int INSERT_ID = Menu.FIRST;
	public static final int ACCOUNT_ID = Menu.FIRST + 1;
	
	private static final String TAG = "ReaderList";
	private static final String PREFS = "readerprefs";
	
	private static final String[] PROJECTION = new String[] {
		Reader.Channels._ID, Reader.Channels.ICON, Reader.Channels.TITLE, Reader.Channels.URL
	};
	
	private Cursor mCursor;
	
	private static final int[] mColorChipResIds = new int[] {
        R.drawable.appointment_indicator_leftside_1,
        R.drawable.appointment_indicator_leftside_5,
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_list);

		Intent intent = getIntent();
		
		if (intent.getData() == null) {
			intent.setData(Reader.Channels.CONTENT_URI);
		}
		
		if (intent.getAction() == null) {
			intent.setAction(Intent.ACTION_VIEW);
		}
		
		mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
		
		ListAdapter adapter = new ChannelListAdapter(this, mCursor);
		setListAdapter(adapter);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, INSERT_ID, 0, "New Feed").setIcon(android.R.drawable.ic_menu_add)
				.setShortcut('3', 'a');
		
		menu.add(0, ACCOUNT_ID, 0, "Account").setIcon(R.drawable.ic_menu_account_list);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
			return true;
		case ACCOUNT_ID:
			startActivity(new Intent(this, AccountActivity.class));
		}
		
		return super.onOptionsItemSelected(item);
	}

	public static class ChannelListAdapter extends CursorAdapter implements Filterable {
		
		private HashMap<Long, ChannelListItem> itemMap;
		private LayoutInflater mInflater;
		private Drawable mAttachmentIcon;
        private Drawable mFavoriteIconOn;
        private Drawable mFavoriteIconOff;
        private Drawable mSelectedIconOn;
        private Drawable mSelectedIconOff;

		public ChannelListAdapter(Context context, Cursor cur) {
			super(context, cur);
			itemMap = new HashMap<Long, ChannelListItem>();
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Resources resources = context.getResources();
			mFavoriteIconOn = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_on);
	        mFavoriteIconOff = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_off);
	        mSelectedIconOn = resources.getDrawable(R.drawable.btn_check_buttonless_dark_on);
	        mSelectedIconOff = resources.getDrawable(R.drawable.btn_check_buttonless_dark_off);

		}
		
		protected void updateItemMap(Cursor cursor, ChannelListItem item) {
			long channelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));
			
			itemMap.put(new Long(channelId), item);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ChannelListItem item = (ChannelListItem) view;
			item.bindViewInit(this, true);
			
			long channelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));

			ContentResolver resolver = context.getContentResolver();
			Cursor unread = resolver.query(ContentUris.withAppendedId(Reader.Posts.CONTENT_URI_LIST, 
					channelId), new String[] { Reader.Posts._ID }, "read=0", null, null);
			
			int unreadCount = unread.getCount();
			unread.close();
						
			View chipView = view.findViewById(R.id.chip);
			int chipResId = mColorChipResIds[0];
			chipView.setBackgroundResource(chipResId);
			
			TextView titleView = (TextView) view.findViewById(R.id.channel_name);
			String text = cursor.getString(cursor.getColumnIndex(Reader.Channels.TITLE));
			titleView.setText(text);
			
			TextView lastPostView = (TextView) view.findViewById(R.id.channel_last_post);
			text = "Last post on: ";
			lastPostView.setText(text);
			
			TextView postCount = (TextView) view.findViewById(R.id.channel_post_count);
			postCount.setText(new Integer(unreadCount).toString());
			
			if (unreadCount == 0) {
				titleView.setTypeface(Typeface.DEFAULT);
				lastPostView.setTypeface(Typeface.DEFAULT);
				postCount.setTypeface(Typeface.DEFAULT);
				view.setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.list_item_background_read));
			} else {
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
				lastPostView.setTypeface(Typeface.DEFAULT_BOLD);
				postCount.setTypeface(Typeface.DEFAULT_BOLD);
				view.setBackgroundDrawable(context.getResources().getDrawable(
						R.drawable.list_item_background_unread));
			}
			
			ImageView selectedView = (ImageView) view.findViewById(R.id.selected);
            selectedView.setImageDrawable(item.mSelected ? mSelectedIconOn : mSelectedIconOff);

            ImageView favoriteView = (ImageView) view.findViewById(R.id.favorite);
            favoriteView.setImageDrawable(item.mFavorite ? mFavoriteIconOn : mFavoriteIconOff);

			updateItemMap(cursor, item);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.channel_list_item, parent, false);
		}
		
		public ChannelListItem getViewByItemId(long id) {
			return itemMap.get(new Long(id));
		}
		
		public void updateSelected(ChannelListItem item, boolean newSelected) {
			
		}
		
		public void updateFavorite(ChannelListItem item, boolean newFavorite) {
			
		}
	}
}
