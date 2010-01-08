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
import android.content.ContentValues;
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
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
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
				Reader.Posts.DATE, Reader.Posts.AUTHOR, Reader.Posts.STARRED };
	
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

	static {
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
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(Reader.Posts.CONTENT_URI, id);
		
    	startActivity(new Intent(Intent.ACTION_VIEW, uri));
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

	private void onSetMessageFavorite(long postId, boolean newFavorite) {
		Uri uri = ContentUris.withAppendedId(Reader.Posts.CONTENT_URI, postId);
        ContentValues values = new ContentValues();
		
		if (newFavorite) {
			values.put(Reader.Posts.STARRED, 1);
		} else {
			values.put(Reader.Posts.STARRED, 0);
		}
		
		Log.d(TAG, "Updating view..." + postId);
		
		getContentResolver().update(uri, values, null, null);
    }

	final static class PostListItemCache {
		public View chipView;
		public TextView titleView;
		public TextView authorView;
		public TextView dateView;
		public ImageView selectedView;
		public ImageView favoriteView;
		public boolean selected;
		public boolean favorite;
		public long postId;
	}
	
	public class PostListAdapter extends ResourceCursorAdapter implements Filterable {
		
		private Drawable mAttachmentIcon;
        private Drawable mFavoriteIconOn;
        private Drawable mFavoriteIconOff;
        private Drawable mSelectedIconOn;
        private Drawable mSelectedIconOff;

		private HashSet<Long> mChecked = new HashSet<Long>();
		private Context mContext;
		
		public PostListAdapter(Context context, Cursor cur) {
			super(context, R.layout.post_list_item, cur, false);

			mContext = context;
			
			Resources resources = context.getResources();
			mFavoriteIconOn = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_on);
	        mFavoriteIconOff = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_off);
	        mSelectedIconOn = resources.getDrawable(R.drawable.btn_check_buttonless_dark_on);
	        mSelectedIconOff = resources.getDrawable(R.drawable.btn_check_buttonless_dark_off);
		}

		public Set<Long> getSelectedSet() {
            return mChecked;
        }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			Cursor cursor = mCursor;
			
			if (!cursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

			if (convertView == null) {
				v = newView(mContext, cursor, parent);
			} else {
				v = convertView;
				PostListItem item = (PostListItem) v;
				long id = cursor.getLong(cursor.getColumnIndex(Reader.Posts._ID));
				item.mPostId = id;
				item.mSelected = mChecked.contains(Long.valueOf(id));
			}
			
			bindView(v, mContext, cursor);
			
			return v;	
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final PostListItemCache cache = (PostListItemCache) view.getTag();
			PostListItem item = (PostListItem) view;
					
			long postId = cursor.getLong(cursor.getColumnIndex(Reader.Posts._ID));			
			boolean selected = mChecked.contains(Long.valueOf(postId));
			
			int starred = cursor.getInt(cursor.getColumnIndex(Reader.Posts.STARRED));
			
			if (starred == 0) {
				cache.favorite = false;
			} else {
				cache.favorite = true;
			}
			
			int chipResId = mColorChipResIds[0];
			cache.chipView.setBackgroundResource(chipResId);
			
			String text = cursor.getString(cursor.getColumnIndex(Reader.Posts.TITLE));
			cache.titleView.setText(text);
			
			text = cursor.getString(cursor.getColumnIndex(Reader.Posts.AUTHOR));
			cache.authorView.setText(text);
			
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

				cache.dateView.setText(fmt.format(date));
			} catch (ParseException e) {
				Log.d(TAG, "Exception caught:: " + e.toString());
			}
			
			int unread = cursor.getInt(cursor.getColumnIndex(Reader.Posts.READ));
						
			if (unread == 1) {
				cache.titleView.setTypeface(Typeface.DEFAULT);
				cache.authorView.setTypeface(Typeface.DEFAULT);
				cache.dateView.setTypeface(Typeface.DEFAULT);
				view.setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.list_item_background_read));
				chipResId = mColorChipResIds[3];
				cache.chipView.setBackgroundResource(chipResId);
			} else {
				cache.titleView.setTypeface(Typeface.DEFAULT_BOLD);
				cache.authorView.setTypeface(Typeface.DEFAULT_BOLD);
				cache.dateView.setTypeface(Typeface.DEFAULT_BOLD);
				view.setBackgroundDrawable(context.getResources().getDrawable(
						R.drawable.list_item_background_unread));
			}
			
            cache.selectedView.setImageDrawable(selected ? mSelectedIconOn : mSelectedIconOff);

            cache.favoriteView.setImageDrawable(cache.favorite ? mFavoriteIconOn : mFavoriteIconOff);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final PostListItem view = (PostListItem) super.newView(context, cursor, parent);
			view.bindViewInit(this, true);
			
			final PostListItemCache cache = new PostListItemCache();
			
			cache.postId = cursor.getLong(cursor.getColumnIndex(Reader.Posts._ID));
			view.mPostId = cache.postId;
						
			cache.chipView = view.findViewById(R.id.chip);
			cache.titleView = (TextView) view.findViewById(R.id.post_title);
			cache.authorView = (TextView) view.findViewById(R.id.post_author);
			cache.dateView = (TextView) view.findViewById(R.id.post_date);
			cache.selectedView = (ImageView) view.findViewById(R.id.selected_post);
			cache.favoriteView = (ImageView) view.findViewById(R.id.favorite_post);
			int starred = cursor.getInt(cursor.getColumnIndex(Reader.Posts.STARRED));
			cache.selected = mChecked.contains(Long.valueOf(view.mPostId));
			
			if (starred == 0) {
				cache.favorite = false;
			} else {
				cache.favorite = true;
			}
			
			view.setTag(cache);
			return view;
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
            onSetMessageFavorite(item.mPostId, newFavorite);
		}
	}
}