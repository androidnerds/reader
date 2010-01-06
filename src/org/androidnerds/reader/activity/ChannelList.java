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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ListAdapter;

import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.Reader;
import org.androidnerds.reader.view.ChannelListRow;

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

	private static class ChannelListAdapter extends CursorAdapter implements Filterable {
		
		private HashMap<Long, ChannelListRow> rowMap;
		
		public ChannelListAdapter(Context c, Cursor cur) {
			super(c, cur);
			rowMap = new HashMap<Long, ChannelListRow>();
		}
		
		protected void updateRowMap(Cursor cursor, ChannelListRow row) {
			long channelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));
			
			rowMap.put(new Long(channelId), row);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ChannelListRow row = (ChannelListRow) view;
			row.bindView(cursor);
			updateRowMap(cursor, row);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			ChannelListRow row = new ChannelListRow(context, parent);
			row.bindView(cursor);
			updateRowMap(cursor, row);
			
			return row;
		}
		
		public ChannelListRow getViewByRowId(long id) {
			return rowMap.get(new Long(id));
		}
	}
}
