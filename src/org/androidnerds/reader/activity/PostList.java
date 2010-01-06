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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.Reader;
import org.androidnerds.reader.view.PostListItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PostList extends ListActivity {
	
	private static final String TAG = "PostList";
	private static final String PREFS = "readerprefs";
	
	private static final String[] PROJECTION = new String[] {
		Reader.Posts._ID, Reader.Posts.CHANNEL_ID, Reader.Posts.TITLE, Reader.Posts.READ,
				Reader.Posts.DATE, Reader.Posts.AUTHOR };
	
	private Cursor mCursor;
	private long mId;
	private View mMultiSelectPanel;
	
	private static final int[] mColorChipResIds = new int[] {
        R.drawable.appointment_indicator_leftside_1,
        R.drawable.appointment_indicator_leftside_5,
		R.drawable.appointment_indicator_leftside_16,
		R.drawable.appointment_indicator_leftside_19,
    };

	private static final SimpleDateFormat mDateFmtDB;
	private static final SimpleDateFormat mDateFmtToday;
	private static final SimpleDateFormat mDateFmt;

	static
	{
		mDateFmtDB = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		mDateFmtToday = new SimpleDateFormat("h:mma");

		/* TODO: Format date according to the current locale preference. */
		mDateFmt = new SimpleDateFormat("MM/dd/yyyy");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.post_list);
		
		mMultiSelectPanel = findViewById(R.id.post_footer_organize);
		
		Uri uri = getIntent().getData();
		mCursor = managedQuery(uri, PROJECTION, null, null, null);
		mId = Long.parseLong(uri.getPathSegments().get(1));
		
		ListAdapter adapter = new PostListAdapter(this, mCursor);
		setListAdapter(adapter);
	}
	
	private void showMultiPanel(boolean show) {
        if (show && mMultiSelectPanel.getVisibility() != View.VISIBLE) {
            mMultiSelectPanel.setVisibility(View.VISIBLE);
            mMultiSelectPanel.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.footer_appear));
        } else if (!show && mMultiSelectPanel.getVisibility() != View.GONE) {
            mMultiSelectPanel.setVisibility(View.GONE);
            mMultiSelectPanel.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.footer_disappear));
        }

        if (show) {
            //updateFooterButtonNames();
        }
    }

	public class PostListAdapter extends CursorAdapter implements Filterable {
		
		private HashMap<Long, PostListItem> itemMap;
		private LayoutInflater mInflater;
		private Drawable mAttachmentIcon;
        private Drawable mFavoriteIconOn;
        private Drawable mFavoriteIconOff;
        private Drawable mSelectedIconOn;
        private Drawable mSelectedIconOff;

		private HashSet<Long> mChecked = new HashSet<Long>();
		
		public PostListAdapter(Context context, Cursor cur) {
			super(context, cur);
			itemMap = new HashMap<Long, PostListItem>();
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Resources resources = context.getResources();
			mFavoriteIconOn = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_on);
	        mFavoriteIconOff = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_off);
	        mSelectedIconOn = resources.getDrawable(R.drawable.btn_check_buttonless_dark_on);
	        mSelectedIconOff = resources.getDrawable(R.drawable.btn_check_buttonless_dark_off);
		}
		
		protected void updateItemMap(Cursor cursor, PostListItem item) {
			long postId = cursor.getLong(cursor.getColumnIndex(Reader.Posts._ID));
			
			itemMap.put(new Long(postId), item);
		}
		
		public Set<Long> getSelectedSet() {
            return mChecked;
        }
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			PostListItem item = (PostListItem) view;
			item.bindViewInit(this, true);
			
			long postId = cursor.getLong(cursor.getColumnIndex(Reader.Posts._ID));
			item.mPostId = postId;

			item.mSelected = mChecked.contains(Long.valueOf(item.mPostId));
						
			View chipView = view.findViewById(R.id.chip);
			int chipResId = mColorChipResIds[0];
			chipView.setBackgroundResource(chipResId);
			
			TextView titleView = (TextView) view.findViewById(R.id.post_title);
			String text = cursor.getString(cursor.getColumnIndex(Reader.Posts.TITLE));
			titleView.setText(text);
			
			TextView authorView = (TextView) view.findViewById(R.id.post_author);
			text = cursor.getString(cursor.getColumnIndex(Reader.Posts.AUTHOR));
			authorView.setText(text);
			
			TextView dateView = (TextView) view.findViewById(R.id.post_date);
			String datestr = cursor.getString(cursor.getColumnIndex(Reader.Posts.DATE));
			
			try {
				Date date = mDateFmtDB.parse(datestr);

				Calendar then = new GregorianCalendar();
				then.setTime(date);

				Calendar now = new GregorianCalendar();

				SimpleDateFormat fmt;

				if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR))
					fmt = mDateFmtToday;
				else
					fmt = mDateFmt;

				dateView.setText(fmt.format(date));
			} catch (ParseException e) {
				Log.d(TAG, "Exception caught:: " + e.toString());
			}
			
			int unread = cursor.getInt(cursor.getColumnIndex(Reader.Posts.READ));
						
			if (unread == 1) {
				titleView.setTypeface(Typeface.DEFAULT);
				authorView.setTypeface(Typeface.DEFAULT);
				dateView.setTypeface(Typeface.DEFAULT);
				view.setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.list_item_background_read));
				chipResId = mColorChipResIds[3];
				chipView.setBackgroundResource(chipResId);
			} else {
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
				authorView.setTypeface(Typeface.DEFAULT_BOLD);
				dateView.setTypeface(Typeface.DEFAULT_BOLD);
				view.setBackgroundDrawable(context.getResources().getDrawable(
						R.drawable.list_item_background_unread));
			}
			
			ImageView selectedView = (ImageView) view.findViewById(R.id.selected_post);
            selectedView.setImageDrawable(item.mSelected ? mSelectedIconOn : mSelectedIconOff);

            ImageView favoriteView = (ImageView) view.findViewById(R.id.favorite_post);
            favoriteView.setImageDrawable(item.mFavorite ? mFavoriteIconOn : mFavoriteIconOff);

			updateItemMap(cursor, item);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.post_list_item, parent, false);
		}
		
		public PostListItem getViewByItemId(long id) {
			return itemMap.get(new Long(id));
		}
		
		public void updateSelected(PostListItem item, boolean newSelected) {
			ImageView selectedView = (ImageView) item.findViewById(R.id.selected_post);
            selectedView.setImageDrawable(newSelected ? mSelectedIconOn : mSelectedIconOff);

            // Set checkbox state in list, and show/hide panel if necessary
            Long id = Long.valueOf(item.mPostId);
            if (newSelected) {
                mChecked.add(id);
            } else {
                mChecked.remove(id);
            }

            PostList.this.showMultiPanel(mChecked.size() > 0);
		}
		
		public void updateFavorite(PostListItem item, boolean newFavorite) {
			ImageView favoriteView = (ImageView) item.findViewById(R.id.favorite_post);
            favoriteView.setImageDrawable(newFavorite ? mFavoriteIconOn : mFavoriteIconOff);
            //onSetMessageFavorite(item.mId, newFavorite);
		}
	}
}