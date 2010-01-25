/* Copyright (C) 2009, 2010 Android Nerds Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.androidnerds.reader.Constants;
import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.Reader;
import org.androidnerds.reader.view.ChannelListItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChannelList extends ListActivity {
	
	public static final int INSERT_ID = Menu.FIRST;
	public static final int ACCOUNT_ID = Menu.FIRST + 1;
	
	private static final String TAG = "ChannelList";
	private static final String PREFS = "readerprefs";
	
	private static final String[] PROJECTION = new String[] {
		Reader.Channels._ID, Reader.Channels.ICON, Reader.Channels.TITLE, Reader.Channels.URL
	};
	
	private Cursor mCursor;
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_list);

		mMultiSelectPanel = findViewById(R.id.footer_organize);
		
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
                        if (Constants.PRE_ECLAIR) {
			    startActivity(new Intent(this, AccountActivity.class));
                        } else {
                            startActivity(new Intent(this, EclairAccountActivity.class));
                        }
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		String action = getIntent().getAction();

    	if (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)) {
    		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
    		
			Log.d(TAG, "List Item Id: " + id);
			
    		Intent intent = getIntent();
    		intent.setData(uri);
    		setResult(RESULT_OK, intent);
    	} else {
    		Uri uri = ContentUris.withAppendedId(Reader.Posts.CONTENT_URI_LIST, id);
    		
    		startActivity(new Intent(Intent.ACTION_VIEW, uri));
    	}
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

	final static class ChannelListItemCache {
		public View chipView;
		public TextView titleView;
		public TextView postCount;
		public TextView lastPostView;
		public ImageView selectedView;
	}
	
	public class ChannelListAdapter extends ResourceCursorAdapter implements Filterable {
		
        private Drawable mSelectedIconOn;
        private Drawable mSelectedIconOff;
		private Context mContext;
		
		private HashSet<Long> mChecked = new HashSet<Long>();
		
		public ChannelListAdapter(Context context, Cursor cur) {
			super(context, R.layout.channel_list_item, cur, false);
			
			mContext = context;

			Resources resources = context.getResources();
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
				throw new IllegalStateException("could not move to cursor position: " + position);
			}
			
			if (convertView == null) {
				v = newView(mContext, cursor, parent);
			} else {
				v = convertView;
				ChannelListItem item = (ChannelListItem) v;
				item.mChannelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));
				item.mSelected = mChecked.contains(Long.valueOf(item.mChannelId));
			}
			
			bindView(v, mContext, cursor);
			return v;
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final ChannelListItemCache cache = (ChannelListItemCache) view.getTag();
			ChannelListItem item = (ChannelListItem) view;
			
			long channelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));
			item.mChannelId = channelId;
			
			item.mSelected = mChecked.contains(Long.valueOf(item.mChannelId));
			
			ContentResolver resolver = context.getContentResolver();
			Cursor unread = resolver.query(ContentUris.withAppendedId(Reader.Posts.CONTENT_URI_LIST, 
					channelId), new String[] { Reader.Posts._ID }, "read=0", null, null);
			
			int unreadCount = unread.getCount();
			unread.close();
			
			Cursor lastPost = resolver.query(ContentUris.withAppendedId(Reader.Posts.CONTENT_URI_LIST,
					channelId), new String[] { Reader.Posts.DATE }, null, null, "posted_on DESC LIMIT 1");
					
			String strdate = "";
			String formattedDate = "";
			
			if (lastPost.moveToNext()) {
				strdate = lastPost.getString(lastPost.getColumnIndex(Reader.Posts.DATE));
			}	
			
			lastPost.close();
			
			if (!strdate.equals("")) {
				try {
					Date date = mDateFmtDB.parse(strdate);

					Calendar then = new GregorianCalendar();
					then.setTime(date);

					Calendar now = new GregorianCalendar();

					SimpleDateFormat fmt;

					if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR))
						fmt = mDateFmtToday;
					else
						fmt = mDateFmt;

					formattedDate = fmt.format(date);
				} catch (ParseException e) {
					Log.d(TAG, "Exception caught:: " + e.toString());
				}
			}
			
			int chipResId = mColorChipResIds[0];
			cache.chipView.setBackgroundResource(chipResId);
			
			String text = cursor.getString(cursor.getColumnIndex(Reader.Channels.TITLE));
			cache.titleView.setText(text);
			
			text = "Last post: " + formattedDate;
			cache.lastPostView.setText(text);
			
			cache.postCount.setText(new Integer(unreadCount).toString());
			
			if (unreadCount == 0) {
				cache.titleView.setTypeface(Typeface.DEFAULT);
				cache.lastPostView.setTypeface(Typeface.DEFAULT);
				cache.postCount.setTypeface(Typeface.DEFAULT);
				view.setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.list_item_background_read));
				chipResId = mColorChipResIds[3];
				cache.chipView.setBackgroundResource(chipResId);
			} else {
				cache.titleView.setTypeface(Typeface.DEFAULT_BOLD);
				cache.lastPostView.setTypeface(Typeface.DEFAULT_BOLD);
				cache.postCount.setTypeface(Typeface.DEFAULT_BOLD);
				view.setBackgroundDrawable(context.getResources().getDrawable(
						R.drawable.list_item_background_unread));
			}
			
            cache.selectedView.setImageDrawable(item.mSelected ? mSelectedIconOn : mSelectedIconOff);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final ChannelListItem view = (ChannelListItem) super.newView(context, cursor, parent);
			view.bindViewInit(this, true);
			
			final ChannelListItemCache cache = new ChannelListItemCache();
			
			view.mChannelId = cursor.getLong(cursor.getColumnIndex(Reader.Channels._ID));
			view.mSelected = mChecked.contains(Long.valueOf(view.mChannelId));
			
			cache.chipView = view.findViewById(R.id.chip);
			cache.titleView = (TextView) view.findViewById(R.id.channel_name);
			cache.postCount = (TextView) view.findViewById(R.id.channel_post_count);
			cache.lastPostView = (TextView) view.findViewById(R.id.channel_last_post);
			cache.selectedView = (ImageView) view.findViewById(R.id.selected);
			
			view.setTag(cache);
			return view;
		}
		
		public void updateSelected(ChannelListItem item, boolean newSelected) {
			ImageView selectedView = (ImageView) item.findViewById(R.id.selected);
            selectedView.setImageDrawable(newSelected ? mSelectedIconOn : mSelectedIconOff);

            // Set checkbox state in list, and show/hide panel if necessary
            Long id = Long.valueOf(item.mChannelId);
            if (newSelected) {
                mChecked.add(id);
            } else {
                mChecked.remove(id);
            }

            ChannelList.this.showMultiPanel(mChecked.size() > 0);
		}
	}
}
